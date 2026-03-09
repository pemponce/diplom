package com.example.forecastservice.service;

import com.example.forecastservice.model.MinuteAggregate;
import com.example.forecastservice.model.ScalingDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ForecastServiceTest {

    private ForecastService service;
    private ForecastStateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ForecastStateRegistry();
        // alpha=0.3, scaleUp=150, scaleDown=50
        service = new ForecastService(0.3, 150.0, 50.0, registry);
    }

    private MinuteAggregate agg(String svc, double rps) {
        return new MinuteAggregate(svc, Instant.now(), rps, 100.0, 0.5, 2.0);
    }

    @Test
    void highRpsProducesScaleUp() {
        // Прогоняем несколько итераций с высоким RPS
        for (int i = 0; i < 10; i++) {
            service.process(agg("api-gateway", 200.0));
        }
        var forecast = registry.getLatest("api-gateway").orElseThrow();
        assertEquals(ScalingDecision.SCALE_UP, forecast.scalingDecision());
    }

    @Test
    void lowRpsProducesScaleDown() {
        for (int i = 0; i < 10; i++) {
            service.process(agg("api-gateway", 10.0));
        }
        var forecast = registry.getLatest("api-gateway").orElseThrow();
        assertEquals(ScalingDecision.SCALE_DOWN, forecast.scalingDecision());
    }

    @Test
    void mediumRpsProducesKeep() {
        for (int i = 0; i < 10; i++) {
            service.process(agg("api-gateway", 100.0));
        }
        var forecast = registry.getLatest("api-gateway").orElseThrow();
        assertEquals(ScalingDecision.KEEP, forecast.scalingDecision());
    }

    @Test
    void forecastStoredInRegistry() {
        service.process(agg("order-service", 80.0));
        assertTrue(registry.getLatest("order-service").isPresent());
    }

    @Test
    void separateStatePerService() {
        for (int i = 0; i < 10; i++) {
            service.process(agg("svc-a", 200.0));
            service.process(agg("svc-b", 10.0));
        }
        assertEquals(ScalingDecision.SCALE_UP, registry.getLatest("svc-a").orElseThrow().scalingDecision());
        assertEquals(ScalingDecision.SCALE_DOWN, registry.getLatest("svc-b").orElseThrow().scalingDecision());
    }
}
