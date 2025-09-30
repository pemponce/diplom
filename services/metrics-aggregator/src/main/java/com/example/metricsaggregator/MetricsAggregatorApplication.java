package com.example.metricsaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetricsAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsAggregatorApplication.class, args);
	}

}
