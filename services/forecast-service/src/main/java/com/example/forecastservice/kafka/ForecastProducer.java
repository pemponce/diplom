package com.example.forecastservice.kafka;

import com.example.forecastservice.model.ForecastRps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ForecastProducer {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    public void send(ForecastRps f) {
        try {
            var json = om.writeValueAsString(f);
            kafka.send(new ProducerRecord<>("forecasts", f.service(), json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
