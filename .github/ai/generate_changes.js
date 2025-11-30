// .github/ai/generate_changes.js - tuned for Unified Log Analytics Platform
import fs from "fs";
import path from "path";
import fetch from "node-fetch";

const perplexityKey = process.env.PERPLEXITY_API_KEY;
const task = process.argv.slice(2).join(" ");

if (!perplexityKey) {
  console.error("Missing PERPLEXITY_API_KEY");
  process.exit(1);
}

if (!task) {
  console.error("Missing task argument");
  process.exit(1);
}

// Collect limited context from the repo so the model understands structure
function collectContext() {
  const roots = [
    "src/main/java",
    "src/main/resources",
    "docker-compose.yml",
    "pom.xml"
  ];

  const chunks = [];

  const pushFile = (file) => {
    if (!fs.existsSync(file)) return;
    const stat = fs.statSync(file);

    if (stat.isDirectory()) {
      const entries = fs.readdirSync(file);
      for (const e of entries) {
        const full = path.join(file, e);
        const s = fs.statSync(full);
        if (s.isDirectory()) {
          pushFile(full);
        } else if (
          full.endsWith(".java") ||
          full.endsWith(".yml") ||
          full.endsWith(".yaml") ||
          full.endsWith(".properties")
        ) {
          const content = fs.readFileSync(full, "utf8");
          chunks.push(`FILE: ${full}\n${content}\n\n`);
          if (chunks.join("").length > 25000) return;
        }
      }
    } else {
      const content = fs.readFileSync(file, "utf8");
      chunks.push(`FILE: ${file}\n${content}\n\n`);
    }
  };

  roots.forEach(pushFile);

  return chunks.join("");
}

async function callPerplexityForCode(context) {
  const prompt =
    `You are working in a Java Spring Boot project called "Unified Log Analytics & Monitoring Platform". ` +
    `It uses Spring Boot (REST, Security, MongoDB, Redis), Elasticsearch, Kibana, MongoDB, Redis, and Docker.\n\n` +
    `Current project files (truncated):\n${context}\n\n` +
    `TASK: ${task}\n\n` +
    `Generate a JSON array of file changes. Each item MUST be:\n` +
    `{"path": "relative/path/File.java", "content": "FULL FILE CONTENT"}\n\n` +
    `Rules:\n` +
    `- Use correct package names based on existing structure (e.g. com.logplatform.controller, service, config, etc.).\n` +
    `- For new code, include full compilable files (package, imports, class, etc.).\n` +
    `- You MAY update existing files (e.g. docker-compose.yml, application.properties) by outputting the full updated content.\n` +
    `- Prefer adding:\n` +
    `  * New controllers/services for the task\n` +
    `  * Elasticsearch index mappings and queries\n` +
    `  * Redis caching logic where appropriate\n` +
    `  * MongoDB entities/repositories if persistence is needed\n` +
    `  * Docker / config updates needed for new components\n` +
    `- Respond with ONLY a valid JSON array (no markdown, no comments, no explanations).`;

  const body = {
    model: "sonar-reasoning-pro",
    messages: [
      {
        role: "system",
        content: "You are a precise code generation agent that outputs only valid JSON."
      },
      { role: "user", content: prompt }
    ],
    max_tokens: 2400
  };

  const res = await fetch("https://api.perplexity.ai/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${perplexityKey}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    console.error("Perplexity API error:", await res.text());
    process.exit(1);
  }

  const data = await res.json();
  let content = data.choices?.[0]?.message?.content || "[]";

  // Try to extract the JSON array even if the model wraps it
  const firstBracket = content.indexOf("[");
  const lastBracket = content.lastIndexOf("]");
  if (firstBracket >= 0 && lastBracket > firstBracket) {
    content = content.slice(firstBracket, lastBracket + 1);
  }

  let files;
  try {
    files = JSON.parse(content);
  } catch (e) {
    console.error("Failed to parse AI response as JSON:", content);
    process.exit(1);
  }

  if (!Array.isArray(files)) {
    console.error("AI response is not an array:", files);
    process.exit(1);
  }

  return files;
}

(async () => {
  try {
    const context = collectContext();
    const files = await callPerplexityForCode(context);

    for (const f of files) {
      const filePath = f.path;
      const fileContent = f.content;

      if (!filePath || !fileContent) {
        console.warn("Skipping invalid file entry:", f);
        continue;
      }

      const dir = path.dirname(filePath);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }

      fs.writeFileSync(filePath, fileContent, "utf8");
      console.log("Wrote file:", filePath);
    }

    console.log("AI code generation finished.");
  } catch (e) {
    console.error("AI code generation failed:", e);
    process.exit(1);
  }
})();
