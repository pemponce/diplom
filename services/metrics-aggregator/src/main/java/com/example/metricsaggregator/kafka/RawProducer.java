package com.example.metricsaggregator.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class RawProducer {
  private final KafkaTemplate<String,String> kafka;
  private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());
  private final Random rnd = new Random();

  @Scheduled(fixedRate = 1000)
  public void tick() {
    var m = new com.example.common.MetricRaw(
        "api-gateway", Instant.now(),
        80 + rnd.nextInt(40), 150 + rnd.nextInt(80),
        0.4 + rnd.nextDouble()*0.4, rnd.nextInt(20));
    try { kafka.send("metrics.raw", m.service(), om.writeValueAsString(m)); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
