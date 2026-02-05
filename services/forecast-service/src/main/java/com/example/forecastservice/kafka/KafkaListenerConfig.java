package com.example.forecastservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // ВАЖНО: адрес брокера выбирай по месту запуска сервиса.
        // Если сервис на хосте Windows — localhost:9092
        // Если в Docker/WSL-сети — kafka:29092
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "forecast");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Чтобы вручную коммитить
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Когда группы ещё нет — читать с конца (можно поставить earliest для тестов)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Нормальные размеры батча
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // Batch-режим
        factory.setBatchListener(true);

        // MANUAL ack, иначе Acknowledgment не будет доступен
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Мягкий обработчик ошибок с ретраями
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3L)));

        // Если вдруг топика нет — не падать на старте
        factory.getContainerProperties().setMissingTopicsFatal(false);

        return factory;
    }
}
