package dev.thoq.zenith.model.entity.impl;

import dev.thoq.zenith.model.entity.Report;
import dev.thoq.zenith.model.enums.AlertSeverity;
import dev.thoq.zenith.model.types.Timestamps;

@SuppressWarnings("unused")
public class NetworkReport extends Report {
    private double up;
    private double down;

    public NetworkReport(Timestamps timestamps, double up, double down) {
        super(timestamps);
        this.up = up;
        this.down = down;
    }

    public NetworkReport(Timestamps timestamps, double up, double down,
                         AlertSeverity severity, String message, String reason) {
        super(timestamps, severity, message, reason);
        this.up = up;
        this.down = down;
    }

    public double getUp() {
        return up;
    }

    public double getDown() {
        return down;
    }

    public void setUp(double up) {
        this.up = up;
    }

    public void setDown(double down) {
        this.down = down;
    }

    @Override
    public NetworkReport copy() {
        return new NetworkReport(
                getTimestamps(),
                getUp(),
                getDown(),
                getSeverity(),
                getMessage(),
                getReason()
        );
    }

    @Override
    public String toString() {
        return String.format("NetworkReport{up=%s, down=%s, severity=%s, message='%s', reason='%s', timestamps=%s}",
                up, down, getSeverity(), getMessage(), getReason(), getTimestamps());
    }
}
