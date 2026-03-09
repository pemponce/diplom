package com.example.metricsaggregator.repository;

import com.example.metricsaggregator.model.MetricMinute;
import com.example.metricsaggregator.model.MetricMinuteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricMinuteRepository extends JpaRepository<MetricMinute, MetricMinuteId> {

}
