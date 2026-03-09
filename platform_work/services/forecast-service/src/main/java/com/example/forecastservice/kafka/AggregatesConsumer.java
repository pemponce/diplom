package com.example.forecastservice.kafka;

import com.example.forecastservice.model.MinuteAggregate;
import com.example.forecastservice.service.ForecastService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatesConsumer {

    private final ForecastService forecastService;
    private final ForecastProducer forecastProducer;
    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(
            topics = "metrics.agg",
            groupId = "forecast",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records == null || records.isEmpty()) {
            return;
        }
        try {
            for (ConsumerRecord<String, String> r : records) {
                MinuteAggregate agg = om.readValue(r.value(), MinuteAggregate.class);
                log.debug("Received aggregate: service={} rps={} cpu={}", agg.service(), agg.rps(), agg.cpu());
                var forecast = forecastService.process(agg);
                forecastProducer.send(forecast);
                log.info("Forecast sent: service={} rpsForecast={:.2f} decision={}",
                        forecast.service(), forecast.rpsForecast(), forecast.scalingDecision());
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing aggregates batch", ex);
            throw new RuntimeException(ex);
        }
    }
}
