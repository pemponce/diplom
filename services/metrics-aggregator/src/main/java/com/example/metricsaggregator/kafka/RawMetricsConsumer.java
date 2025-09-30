package com.example.metricsaggregator.kafka;

import com.example.common.MetricRaw;
import com.example.metricsaggregator.service.AggregationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RawMetricsConsumer {
  private final AggregationService service;
  private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

  @KafkaListener(topics = "metrics.raw", groupId = "agg", containerFactory = "kafkaListenerContainerFactory")
  public void onBatch(List<ConsumerRecord<String,String>> batch, Acknowledgment ack) throws JsonProcessingException {
    System.out.println("RAW batch size=" + batch.size() +
            " first=" + (batch.isEmpty()? "[]" : batch.get(0).value()));
    for (var rec : batch) {
      var raw = om.readValue(rec.value(), MetricRaw.class);
      service.accept(raw);
    }
    service.flushClosedWindows(); // выдать готовые минутные агрегаты
    ack.acknowledge();
  }
}
