package com.example.metricsaggregator.service;

import com.example.common.MetricRaw;
import com.example.metricsaggregator.domain.window.WindowState;
import com.example.metricsaggregator.kafka.AggregatesProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregationService {

    // Оконное состояние (минутные окна)
    private final WindowState state = new WindowState();

    // Переехали на сервис с JPA внутри
    private final MetricMinuteService minuteService;

    // Паблиш минутных агрегатов дальше по шине (опционально)
    private final AggregatesProducer producer;

    /** Принимаем сырое событие и складываем в окно */
    public void accept(MetricRaw m) {
        // Важно: здесь мы добавляем в окно значения как есть
        state.add(m.service(), m.ts(), m.rps(), m.p95ms(), m.cpu(), m.queueDepth());
    }

    /** Выгружаем и закрытые окна сохраняем в БД + публикуем в Kafka */
    public void flushClosedWindows() {
        var list = state.extractClosed();
        list.forEach(a -> System.out.println("AGG> " + a));

        // Сохраняем батчем через JPA (INSERT/UPDATE решит сам по первичному ключу)
        minuteService.saveOrUpdateAll(list);

        // Если нужно — публикуем агрегаты в другой топик
        list.forEach(producer::send);
    }
}
