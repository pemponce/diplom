package com.example.forecastservice.model;

import java.time.Instant;

public record MinuteAggregate(
        String service,
        Instant ts,
        double rps,
        double p95,
        double cpu,
        double queue
) {}
