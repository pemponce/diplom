package com.example.forecastservice.kafka;

import com.example.forecastservice.model.ForecastRps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикует результаты прогнозирования в топик "forecasts".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForecastProducer {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void send(ForecastRps f) {
        try {
            String json = om.writeValueAsString(f);
            kafka.send(new ProducerRecord<>("forecasts", f.service(), json));
            log.debug("Published forecast for service={}", f.service());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ForecastRps for service={}", f.service(), e);
            throw new RuntimeException(e);
        }
    }
}
