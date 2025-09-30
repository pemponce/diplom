package com.example.metricsaggregator.kafka;

import com.example.metricsaggregator.domain.window.MinuteAggregate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AggregatesProducer {
  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

  public void send(MinuteAggregate a){
    try {
      var json = om.writeValueAsString(new Out(a));
      kafka.send("metrics.agg", a.service(), json);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private record Out(String service, java.time.Instant ts, String win,
                     double rps, double p95, double cpu, double queue) {
    Out(MinuteAggregate a){ this(a.service(), a.ts(), "1m", a.rps(), a.p95(), a.cpu(), a.queue()); }
  }
}
