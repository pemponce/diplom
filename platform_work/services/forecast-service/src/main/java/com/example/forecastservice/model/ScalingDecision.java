package com.example.forecastservice.model;

/**
 * Рекомендация системы масштабирования.
 */
public enum ScalingDecision {
    SCALE_UP,    // увеличить число реплик
    SCALE_DOWN,  // уменьшить число реплик
    KEEP         // оставить без изменений
}
