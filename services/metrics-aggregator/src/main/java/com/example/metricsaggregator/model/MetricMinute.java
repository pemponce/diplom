package com.example.metricsaggregator.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metrics_minute")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricMinute {

    @EmbeddedId
    private MetricMinuteId id;

    @Column(name = "rps")
    private Double rps;

    @Column(name = "p95_ms")
    private Double p95Ms;

    @Column(name = "cpu")
    private Double cpu;

    @Column(name = "queue_depth")
    private Double queueDepth;
}
