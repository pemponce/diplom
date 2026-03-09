package com.example.common;

import java.time.Instant;

public record MetricRaw(String service, Instant ts, double rps, double p95ms, double cpu, double queueDepth) {}
