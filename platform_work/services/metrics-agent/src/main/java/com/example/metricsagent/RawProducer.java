package com.example.metricsagent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;
import java.util.Random;

/**
 * Генератор синтетических метрик.
 *
 * <p>Моделирует суточный профиль нагрузки:
 * <ul>
 *   <li>Базовый уровень: 80–120 RPS
 *   <li>Утренний пик (09:00–11:00): 150–250 RPS
 *   <li>Дневной пик (13:00–15:00): 180–280 RPS
 *   <li>Спад ночью: 10–40 RPS
 * </ul>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RawProducer {

    private static final String TOPIC = "metrics.raw";
    private static final String SERVICE = "api-gateway";

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Random rnd = new Random();

    @Scheduled(fixedRate = 1000)
    public void tick() {
        try {
            double rps = simulateRps();
            double cpu = Math.min(1.0, rps / 300.0 + rnd.nextDouble() * 0.1);
            double p95ms = 50 + (rps / 3.0) + rnd.nextDouble() * 30;

            var payload = Map.of(
                    "service",    SERVICE,
                    "ts",         Instant.now().toString(),
                    "rps",        rps,
                    "p95ms",      p95ms,
                    "cpu",        cpu,
                    "queueDepth", (int)(rps / 50.0)
            );
            String json = om.writeValueAsString(payload);
            kafka.send(TOPIC, SERVICE, json);
            log.debug("Sent metric: rps={:.1f} cpu={:.2f}", rps, cpu);
        } catch (Exception e) {
            log.error("Failed to send metric", e);
        }
    }

    /** Симулирует суточный профиль нагрузки */
    private double simulateRps() {
        int hour = LocalTime.now().getHour();
        double base;
        if (hour >= 9 && hour < 11) {
            base = 180 + rnd.nextInt(70);   // утренний пик
        } else if (hour >= 13 && hour < 15) {
            base = 200 + rnd.nextInt(80);   // дневной пик
        } else if (hour >= 22 || hour < 6) {
            base = 10 + rnd.nextInt(30);    // ночной спад
        } else {
            base = 80 + rnd.nextInt(60);    // базовый уровень
        }
        // добавляем небольшой шум ±10%
        return base * (0.9 + rnd.nextDouble() * 0.2);
    }
}
