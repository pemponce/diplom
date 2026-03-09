package com.example.forecastservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    // Если у тебя auto.create.topics выключен — создадим топики кодом
    @Bean
    @ConditionalOnProperty(name = "app.create-topics", havingValue = "true", matchIfMissing = false)
    public NewTopic metricsAggTopic() { return new NewTopic("metrics.agg", 1, (short)1); }

    @Bean
    @ConditionalOnProperty(name = "app.create-topics", havingValue = "true", matchIfMissing = false)
    public NewTopic forecastsTopic() { return new NewTopic("forecasts", 1, (short)1); }
}
