# Predictive Autoscaling Platform

Система прогнозирования нагрузки и автоматического масштабирования микросервисов в контейнеризированной среде.

## Архитектура

```
metrics-agent
     │  (metrics.raw)
     ▼
   Kafka
     │  (metrics.raw)
     ▼
metrics-aggregator ──► PostgreSQL (metrics_minute)
     │  (metrics.agg)
     ▼
forecast-service  ──► REST API /api/v1/forecast/{service}
     │  (forecasts)
     ▼
  Kubernetes HPA / KEDA ScaledObject
```

## Сервисы

| Сервис              | Порт | Назначение                                       |
|---------------------|------|--------------------------------------------------|
| metrics-agent       | 8080 | Генератор метрик (RPS, CPU, p95, queueDepth)     |
| metrics-aggregator  | 8081 | Kafka Consumer, агрегация по минутам, PostgreSQL  |
| forecast-service    | 8082 | EMA прогноз, scaling decision, REST API           |
| Kafka UI            | 8090 | Мониторинг топиков                               |
| Prometheus          | 9090 | Сбор метрик приложений                           |
| Grafana             | 3000 | Дашборды (admin/admin)                           |

## Топики Kafka

| Топик         | Продюсер            | Консьюмер           |
|---------------|---------------------|---------------------|
| metrics.raw   | metrics-agent       | metrics-aggregator  |
| metrics.agg   | metrics-aggregator  | forecast-service    |
| forecasts     | forecast-service    | (внешние системы)   |

## Алгоритм прогнозирования (EMA)

EMA (Exponential Moving Average) с параметром α=0.3:

```
EMA_t = α × RPS_t + (1 − α) × EMA_{t−1}
```

Решение о масштабировании:
- `EMA ≥ 150 RPS` → **SCALE_UP**
- `EMA ≤ 50 RPS`  → **SCALE_DOWN**
- иначе          → **KEEP**

## Запуск

```bash
# 1. Собрать проект
mvn clean package -DskipTests

# 2. Поднять инфраструктуру
cd deploy/compose
docker-compose up -d

# 3. Проверить прогноз
curl http://localhost:8082/api/v1/forecast/api-gateway
curl http://localhost:8082/api/v1/forecast/api-gateway/decision
curl http://localhost:8082/api/v1/forecast/api-gateway/replicas?rpsPerReplica=100
```

## REST API forecast-service

```
GET /api/v1/forecast                          # все прогнозы
GET /api/v1/forecast/{service}                # прогноз для сервиса
GET /api/v1/forecast/{service}/decision       # scaling-решение
GET /api/v1/forecast/{service}/replicas       # желаемое число реплик
GET /actuator/health                          # health check
GET /actuator/prometheus                      # метрики для Prometheus
```
