package com.example.forecastservice.model;

import java.time.Instant;

/**
 * Результат прогнозирования: предсказанный RPS на следующую минуту
 * и рекомендация по масштабированию.
 */
public record ForecastRps(
        String service,
        Instant ts,             // момент прогноза
        double rpsCurrent,      // текущий RPS (последняя агрегированная минута)
        double rpsForecast,     // прогнозный RPS на следующую минуту
        double cpuCurrent,      // текущий CPU
        String method,          // "EMA(alpha=0.3)"
        ScalingDecision scalingDecision  // SCALE_UP / SCALE_DOWN / KEEP
) {}
