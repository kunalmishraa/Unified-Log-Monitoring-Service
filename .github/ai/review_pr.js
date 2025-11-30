// .github/ai/review_pr.js
import fetch from "node-fetch";

const githubToken = process.env.GITHUB_TOKEN;
const perplexityKey = process.env.PERPLEXITY_API_KEY;
const repoFull = process.env.REPO_FULL;
const prNumber = process.env.PR_NUMBER;

if (!githubToken || !perplexityKey || !repoFull || !prNumber) {
  console.error("Missing required environment variables (GITHUB_TOKEN, PERPLEXITY_API_KEY, REPO_FULL, PR_NUMBER).");
  process.exit(1);
}

const [owner, repo] = repoFull.split("/");

async function getPRAndDiff() {
  const prUrl = `https://api.github.com/repos/${owner}/${repo}/pulls/${prNumber}`;

  const prRes = await fetch(prUrl, {
    headers: {
      "Authorization": `Bearer ${githubToken}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28"
    }
  });

  if (!prRes.ok) {
    console.error("Failed to fetch PR:", prRes.status, await prRes.text());
    process.exit(1);
  }

  const pr = await prRes.json();

  const diffRes = await fetch(pr.diff_url, {
    headers: {
      "Authorization": `Bearer ${githubToken}`,
      "Accept": "application/vnd.github.v3.diff"
    }
  });

  if (!diffRes.ok) {
    console.error("Failed to fetch diff:", diffRes.status, await diffRes.text());
    process.exit(1);
  }

  const diff = await diffRes.text();
  return { pr, diff };
}

async function callPerplexityReview(pr, diff) {
  const trimmedDiff = diff.slice(0, 25000);

  const body = {
    model: "sonar-reasoning-pro",
    messages: [
      {
        role: "system",
        content:
          "You are a senior backend engineer reviewing a pull request in a Java Spring Boot + Elasticsearch + MongoDB + Redis + Docker project. " +
          "Focus on code quality, architecture, logging, error handling, security, test coverage, and configuration. " +
          "Respond in GitHub-flavored markdown with clear sections: Summary, Strengths, Issues, Suggestions, Tests to Add."
      },
      {
        role: "user",
        content:
          `Repository: ${owner}/${repo}\n` +
          `PR #${pr.number}: ${pr.title}\n\n` +
          `PR description:\n${pr.body || "No description."}\n\n` +
          `Diff:\n${trimmedDiff}`
      }
    ],
    max_tokens: 1200
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
    console.error("Perplexity API error:", res.status, await res.text());
    process.exit(1);
  }

  const data = await res.json();
  const content = data.choices?.[0]?.message?.content || "No review generated.";
  return content;
}

async function postPRComment(commentBody) {
  const url = `https://api.github.com/repos/${owner}/${repo}/issues/${prNumber}/comments`;

  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${githubToken}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28"
    },
    body: JSON.stringify({ body: commentBody })
  });

  if (!res.ok) {
    console.error("Failed to post PR comment:", res.status, await res.text());
    process.exit(1);
  }
}

(async () => {
  try {
    console.log(`Running AI PR review for ${owner}/${repo}#${prNumber}...`);

    const { pr, diff } = await getPRAndDiff();
    const review = await callPerplexityReview(pr, diff);

    const commentBody = `### 🤖 AI PR Review (Perplexity)\n\n${review}`;
    await postPRComment(commentBody);

    console.log("AI PR review comment posted successfully.");
  } catch (err) {
    console.error("AI PR review failed:", err);
    process.exit(1);
  }
})();
