package com.example.forecastservice.core;

import java.util.concurrent.ConcurrentHashMap;

/** Простой per-service EMA по rps */
public class EmaForecaster {
    private final double alpha; // 0..1
    private final ConcurrentHashMap<String, Double> state = new ConcurrentHashMap<>();

    public EmaForecaster(double alpha) {
        if (alpha <= 0 || alpha >= 1) throw new IllegalArgumentException("alpha must be (0,1)");
        this.alpha = alpha;
    }

    public double updateAndForecast(String service, double lastRps) {
        return state.merge(service, lastRps, (prev, x) -> alpha * x + (1 - alpha) * prev);
    }

    public String methodTag() {
        return "EMA(alpha=" + alpha + ")";
    }
}
