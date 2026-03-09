package com.example.forecastservice.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmaForecasterTest {

    @Test
    void firstCallReturnsSameValue() {
        EmaForecaster forecaster = new EmaForecaster(0.3);
        double result = forecaster.updateAndForecast("api-gateway", 100.0);
        assertEquals(100.0, result, 0.001, "Первый вызов должен вернуть само значение");
    }

    @Test
    void smoothesSpikeCorrectly() {
        EmaForecaster forecaster = new EmaForecaster(0.3);
        forecaster.updateAndForecast("svc", 100.0); // инициализация: EMA = 100
        double after = forecaster.updateAndForecast("svc", 200.0);
        // EMA = 0.3*200 + 0.7*100 = 60 + 70 = 130
        assertEquals(130.0, after, 0.001, "EMA должна сгладить скачок");
    }

    @Test
    void convergesOnConstantInput() {
        EmaForecaster forecaster = new EmaForecaster(0.3);
        double val = 0;
        for (int i = 0; i < 50; i++) {
            val = forecaster.updateAndForecast("svc", 100.0);
        }
        assertEquals(100.0, val, 0.01, "EMA должна сойтись к константе");
    }

    @Test
    void independentStatePerService() {
        EmaForecaster forecaster = new EmaForecaster(0.3);
        forecaster.updateAndForecast("svc-a", 100.0);
        forecaster.updateAndForecast("svc-b", 200.0);
        double a = forecaster.updateAndForecast("svc-a", 100.0);
        double b = forecaster.updateAndForecast("svc-b", 200.0);
        assertNotEquals(a, b, "Разные сервисы должны иметь независимое EMA-состояние");
    }

    @Test
    void invalidAlphaThrows() {
        assertThrows(IllegalArgumentException.class, () -> new EmaForecaster(0.0));
        assertThrows(IllegalArgumentException.class, () -> new EmaForecaster(1.0));
        assertThrows(IllegalArgumentException.class, () -> new EmaForecaster(-0.1));
    }

    @Test
    void methodTagContainsAlpha() {
        EmaForecaster forecaster = new EmaForecaster(0.3);
        assertTrue(forecaster.methodTag().contains("0.3"), "methodTag должен содержать alpha");
    }
}
