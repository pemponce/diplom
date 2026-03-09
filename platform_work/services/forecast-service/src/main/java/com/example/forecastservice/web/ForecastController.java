package com.example.forecastservice.web;

import com.example.forecastservice.model.ForecastRps;
import com.example.forecastservice.model.ScalingDecision;
import com.example.forecastservice.service.ForecastStateRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API для получения текущих прогнозов и scaling-решений.
 * Может использоваться внешними системами (KEDA, Kubernetes HPA External Metrics).
 */
@RestController
@RequestMapping("/api/v1/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastStateRegistry registry;

    /** Получить последний прогноз для конкретного сервиса */
    @GetMapping("/{service}")
    public ResponseEntity<ForecastRps> getForecast(@PathVariable String service) {
        return registry.getLatest(service)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Получить все последние прогнозы */
    @GetMapping
    public Map<String, ForecastRps> getAllForecasts() {
        return registry.getAll();
    }

    /** Получить текущее scaling-решение для сервиса (для интеграции с K8s) */
    @GetMapping("/{service}/decision")
    public ResponseEntity<Map<String, String>> getDecision(@PathVariable String service) {
        return registry.getLatest(service)
                .map(f -> ResponseEntity.ok(Map.of(
                        "service", service,
                        "decision", f.scalingDecision().name(),
                        "rpsForecast", String.format("%.2f", f.rpsForecast()),
                        "method", f.method()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Health-check / метрика для KEDA ExternalScaler */
    @GetMapping("/{service}/replicas")
    public ResponseEntity<Map<String, Object>> getDesiredReplicas(
            @PathVariable String service,
            @RequestParam(defaultValue = "100") double rpsPerReplica
    ) {
        return registry.getLatest(service)
                .map(f -> {
                    int replicas = (int) Math.ceil(f.rpsForecast() / rpsPerReplica);
                    replicas = Math.max(1, replicas); // минимум 1 реплика
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "service", service,
                            "desiredReplicas", replicas,
                            "rpsForecast", f.rpsForecast(),
                            "decision", f.scalingDecision()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
