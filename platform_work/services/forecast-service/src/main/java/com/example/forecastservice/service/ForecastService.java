package com.example.forecastservice.service;

import com.example.forecastservice.core.EmaForecaster;
import com.example.forecastservice.model.ForecastRps;
import com.example.forecastservice.model.MinuteAggregate;
import com.example.forecastservice.model.ScalingDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Сервис прогнозирования нагрузки на основе EMA (Exponential Moving Average).
 *
 * <p>Принимает минутные агрегаты метрик, строит краткосрочный прогноз RPS
 * и принимает упреждающее решение о масштабировании.
 */
@Slf4j
@Service
public class ForecastService {

    private final EmaForecaster forecaster;
    private final ForecastStateRegistry registry;
    private final double scaleUpThreshold;
    private final double scaleDownThreshold;

    public ForecastService(
            @Value("${forecast.ema.alpha:0.3}") double alpha,
            @Value("${forecast.scale-up-threshold:150.0}") double scaleUpThreshold,
            @Value("${forecast.scale-down-threshold:50.0}") double scaleDownThreshold,
            ForecastStateRegistry registry
    ) {
        this.forecaster = new EmaForecaster(alpha);
        this.scaleUpThreshold = scaleUpThreshold;
        this.scaleDownThreshold = scaleDownThreshold;
        this.registry = registry;
    }

    /**
     * Обрабатывает агрегат: строит EMA-прогноз, формирует scaling-решение,
     * сохраняет в реестр и возвращает результат.
     */
    public ForecastRps process(MinuteAggregate agg) {
        double forecastedRps = forecaster.updateAndForecast(agg.service(), agg.rps());
        ScalingDecision decision = decide(forecastedRps);

        log.info("[FORECAST] service={} currentRps={:.1f} forecastRps={:.1f} cpu={:.2f} decision={}",
                agg.service(), agg.rps(), forecastedRps, agg.cpu(), decision);

        ForecastRps result = new ForecastRps(
                agg.service(),
                Instant.now(),
                agg.rps(),
                forecastedRps,
                agg.cpu(),
                forecaster.methodTag(),
                decision
        );

        registry.update(result);
        return result;
    }

    private ScalingDecision decide(double forecastedRps) {
        if (forecastedRps >= scaleUpThreshold) return ScalingDecision.SCALE_UP;
        if (forecastedRps <= scaleDownThreshold) return ScalingDecision.SCALE_DOWN;
        return ScalingDecision.KEEP;
    }
}
