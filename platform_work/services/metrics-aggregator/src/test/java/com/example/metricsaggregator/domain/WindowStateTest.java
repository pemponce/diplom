package com.example.metricsaggregator.domain;

import com.example.metricsaggregator.domain.window.WindowState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class WindowStateTest {

    @Test
    void extractsClosedWindowsOnly() throws InterruptedException {
        WindowState state = new WindowState();
        // Добавляем метрику в прошлую минуту
        Instant pastMinute = Instant.now().minus(2, ChronoUnit.MINUTES);
        state.add("svc", pastMinute, 100.0, 150.0, 0.5, 3.0);
        state.add("svc", pastMinute, 120.0, 160.0, 0.6, 4.0);

        var closed = state.extractClosed();
        assertEquals(1, closed.size(), "Должно быть одно закрытое окно");
        var agg = closed.get(0);
        assertEquals("svc", agg.service());
        assertEquals(110.0, agg.rps(), 0.01, "RPS должен быть средним (100+120)/2");
    }

    @Test
    void doesNotExtractCurrentMinute() {
        WindowState state = new WindowState();
        state.add("svc", Instant.now(), 100.0, 150.0, 0.5, 3.0);
        var closed = state.extractClosed();
        assertTrue(closed.isEmpty(), "Текущая минута не должна быть извлечена");
    }

    @Test
    void multipleServicesHandledIndependently() {
        WindowState state = new WindowState();
        Instant past = Instant.now().minus(2, ChronoUnit.MINUTES);
        state.add("svc-a", past, 100.0, 100.0, 0.4, 1.0);
        state.add("svc-b", past, 200.0, 200.0, 0.8, 5.0);

        var closed = state.extractClosed();
        assertEquals(2, closed.size());
        assertTrue(closed.stream().anyMatch(a -> a.service().equals("svc-a")));
        assertTrue(closed.stream().anyMatch(a -> a.service().equals("svc-b")));
    }

    @Test
    void extractRemovesFromState() {
        WindowState state = new WindowState();
        Instant past = Instant.now().minus(2, ChronoUnit.MINUTES);
        state.add("svc", past, 100.0, 150.0, 0.5, 3.0);

        state.extractClosed(); // первый вызов
        var second = state.extractClosed(); // второй вызов
        assertTrue(second.isEmpty(), "После extractClosed() состояние должно быть очищено");
    }
}
