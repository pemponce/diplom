package com.example.forecastservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AggregatesConsumer {

    // Читает батч записей из metrics.agg, подтверждает вручную
    @KafkaListener(
            topics = "metrics.agg",
            groupId = "forecast",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records == null || records.isEmpty()) {
            // Нечего подтверждать, просто выходим
            return;
        }

        try {
            // Пример простой обработки
            for (ConsumerRecord<String, String> r : records) {
                String key = r.key();
                String value = r.value(); // JSON из агрегатора
                // TODO: распарсить value, посчитать прогноз и отправить в "forecasts"
                // e.g. produceForecast(key, value);
            }

            // Подтверждаем только после успешной обработки
            ack.acknowledge();
        } catch (Exception ex) {
            // Ошибки полетят в DefaultErrorHandler (ретраи), ack тут НЕ вызываем
            throw ex;
        }
    }
}
