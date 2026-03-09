package com.example.metricsaggregator.domain.window;

import java.time.Instant;

public record MinuteAggregate(
    String service, Instant ts,
    double rps, double p95, double cpu, double queue) {}
