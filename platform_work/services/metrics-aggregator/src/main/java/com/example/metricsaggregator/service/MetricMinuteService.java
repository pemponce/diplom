package com.example.metricsaggregator.service;

import com.example.metricsaggregator.domain.window.MinuteAggregate;
import com.example.metricsaggregator.model.MetricMinute;
import com.example.metricsaggregator.model.MetricMinuteId;
import com.example.metricsaggregator.repository.MetricMinuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MetricMinuteService {
    private final MetricMinuteRepository repo;

    @Transactional
    public void saveOrUpdateFromAggregate(MinuteAggregate a) {
        var id = new MetricMinuteId(a.ts(), a.service());
        var entity = MetricMinute.builder()
                .id(id)
                .rps(a.rps())
                .p95Ms(a.p95())
                .cpu(a.cpu())
                .queueDepth(a.queue())
                .build();
        repo.save(entity); // JPA сам решит INSERT/UPDATE
    }

    @Transactional
    public void saveOrUpdateAll(Collection<MinuteAggregate> list) {
        if (list == null || list.isEmpty()) return;
        var entities = list.stream()
                .map(a -> MetricMinute.builder()
                        .id(new MetricMinuteId(a.ts(), a.service()))
                        .rps(a.rps())
                        .p95Ms(a.p95())
                        .cpu(a.cpu())
                        .queueDepth(a.queue())
                        .build())
                .toList();
        repo.saveAll(entities); // батчом (если включить batching)
    }
}
