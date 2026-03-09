package com.example.forecastservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MinuteAggregate(
        String service,
        Instant ts,
        double rps,
        double p95,
        double cpu,
        double queue
) {}