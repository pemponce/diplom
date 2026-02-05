package com.example.metricsagent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class RawProducer {

  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private final Random rnd = new Random();

  // каждую секунду шлём одну метрику
  @Scheduled(fixedRate = 1000)
  public void tick() {
    try {
      var payload = Map.of(
          "service", "api-gateway",
          "ts", Instant.now().toString(),
          "rps", 80 + rnd.nextInt(40),
          "p95ms", 150 + rnd.nextInt(80),
          "cpu", 0.40 + rnd.nextDouble() * 0.40,
          "queueDepth", rnd.nextInt(10)
      );
      String json = om.writeValueAsString(payload);
      kafka.send("metrics.raw", "api-gateway", json);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
