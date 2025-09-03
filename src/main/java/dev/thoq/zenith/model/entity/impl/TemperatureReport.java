package dev.thoq.zenith.model.entity.impl;

import dev.thoq.zenith.model.entity.Report;
import dev.thoq.zenith.model.enums.AlertSeverity;
import dev.thoq.zenith.model.types.Timestamps;

@SuppressWarnings("unused")
public class TemperatureReport extends Report {
    private double temperature;

    public TemperatureReport(Timestamps timestamps, double temperature) {
        super(timestamps);
        this.temperature = temperature;
    }

    public TemperatureReport(Timestamps timestamps, double temperature,
                             AlertSeverity severity, String message, String reason) {
        super(timestamps, severity, message, reason);
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public TemperatureReport copy() {
        return new TemperatureReport(
                getTimestamps(),
                temperature,
                getSeverity(),
                getMessage(),
                getReason()
        );
    }

    @Override
    public String toString() {
        return String.format("TemperatureReport{temperature=%.2f°C, severity=%s, message='%s', reason='%s', timestamps=%s}",
                temperature, getSeverity(), getMessage(), getReason(), getTimestamps());
    }
}
