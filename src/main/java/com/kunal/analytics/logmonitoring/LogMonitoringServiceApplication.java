package com.kunal.analytics.logmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LogMonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogMonitoringServiceApplication.class, args);
	}

}
