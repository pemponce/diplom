package com.example.metricsaggregator.repository;

import com.example.metricsaggregator.domain.window.MinuteAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class MetricMinuteRepository {
  private final JdbcTemplate jdbc;

  public void upsert(MinuteAggregate a){
    jdbc.update("""
      CREATE TABLE IF NOT EXISTS metrics_minute (
        ts timestamptz NOT NULL,
        service text NOT NULL,
        rps double precision,
        p95_ms double precision,
        cpu double precision,
        queue_depth double precision,
        PRIMARY KEY (ts, service)
      );
      
      CREATE INDEX IF NOT EXISTS idx_metrics_minute_service_ts
        ON metrics_minute(service, ts);
      
      """, Timestamp.from(a.ts()), a.service(), a.rps(), a.p95(), a.cpu(), a.queue());
  }
}
