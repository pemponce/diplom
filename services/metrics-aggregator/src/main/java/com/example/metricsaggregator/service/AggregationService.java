package com.example.metricsaggregator.service;

import com.example.common.MetricRaw;
import com.example.metricsaggregator.domain.window.WindowState;
import com.example.metricsaggregator.kafka.AggregatesProducer;
import com.example.metricsaggregator.repository.MetricMinuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregationService {
  private final WindowState state = new WindowState();
  private final MetricMinuteRepository repo;
  private final AggregatesProducer producer;

  public void accept(MetricRaw m){
    state.add(m.service(), m.ts(), m.rps(), m.p95ms(), m.cpu(), m.queueDepth());
  }

  public void flushClosedWindows(){
    var list = state.extractClosed();
    list.forEach(repo::upsert);
    list.forEach(producer::send);
  }
}
