package dev.thoq.zenith.model.dto;

import dev.thoq.zenith.model.enums.AlertSeverity;
import dev.thoq.zenith.model.types.MonitorData;

@SuppressWarnings("unused")
public record AlertDto(String message, String reason, AlertSeverity severity, MonitorData monitorData) {
    public String buildAlert() {
        return String.format("%s: %s %s", severity, message, reason);
    }
}
