package dev.thoq.zenith.model.entity;

import dev.thoq.zenith.model.enums.AlertSeverity;
import dev.thoq.zenith.model.types.Timestamps;

@SuppressWarnings("unused")
public abstract class Report {
    private final Timestamps timestamps;
    private AlertSeverity severity;
    private String message;
    private String reason;

    protected Report(Timestamps timestamps) {
        this.timestamps = timestamps;
    }

    protected Report(Timestamps timestamps, AlertSeverity severity, String message, String reason) {
        this.timestamps = timestamps;
        this.severity = severity;
        this.message = message;
        this.reason = reason;
    }

    public abstract Report copy();

    public Timestamps getTimestamps() {
        return timestamps;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("%s{severity=%s, message='%s', reason='%s', timestamps=%s}",
                getClass().getSimpleName(), severity, message, reason, timestamps);
    }
}
