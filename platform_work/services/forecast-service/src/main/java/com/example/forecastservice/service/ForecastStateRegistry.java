package com.example.forecastservice.service;

import com.example.forecastservice.model.ForecastRps;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory реестр последних прогнозов по каждому сервису.
 * Используется REST API для отдачи текущего состояния.
 */
@Component
public class ForecastStateRegistry {

    private final ConcurrentHashMap<String, ForecastRps> latest = new ConcurrentHashMap<>();

    public void update(ForecastRps forecast) {
        latest.put(forecast.service(), forecast);
    }

    public Optional<ForecastRps> getLatest(String service) {
        return Optional.ofNullable(latest.get(service));
    }

    public Map<String, ForecastRps> getAll() {
        return Map.copyOf(latest);
    }
}
