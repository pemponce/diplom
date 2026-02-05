CREATE TABLE IF NOT EXISTS metrics_minute (
                                              ts           timestamptz NOT NULL,
                                              service      text        NOT NULL,
                                              rps          double precision,
                                              p95_ms       double precision,
                                              cpu          double precision,
                                              queue_depth  double precision,
                                              CONSTRAINT pk_metrics_minute PRIMARY KEY (ts, service)
    );

CREATE INDEX IF NOT EXISTS idx_metrics_minute_service_ts
    ON metrics_minute(service, ts DESC);
