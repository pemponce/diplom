package com.example.metricsaggregator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricMinuteId implements Serializable {

    @Column(name = "ts", nullable = false)
    private Instant ts;

    @Column(name = "service", nullable = false)
    private String service;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricMinuteId that)) return false;
        return Objects.equals(ts, that.ts) &&
               Objects.equals(service, that.service);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ts, service);
    }
}
