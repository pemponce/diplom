package com.example.common;

import java.time.Instant;

public record MetricAgg(String service, Instant ts, String win,
                        double rps, double p95, double cpu, double queue) {}
