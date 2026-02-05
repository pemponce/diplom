package com.example.forecastservice.model;

import java.time.Instant;

public record ForecastRps(
        String service,
        Instant ts,       // момент, для которого даём прогноз (начало следующей минуты)
        double rpsForecast,
        String method     // "EMA(alpha=0.3)" например
) {}
