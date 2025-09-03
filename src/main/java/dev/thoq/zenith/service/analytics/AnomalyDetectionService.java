package dev.thoq.zenith.service.analytics;

import dev.thoq.zenith.model.types.Anomalies;
import dev.thoq.zenith.model.types.MonitorData;
import dev.thoq.zenith.model.types.Timestamps;
import dev.thoq.zenith.model.types.impl.AnomaliesImpl;

import java.util.Map;

@SuppressWarnings("unused")
public class AnomalyDetectionService {
    public AnomalyDetectionService() {
    }

    public Anomalies detectAnomalies(Timestamps timestamps, MonitorData data) {
        // todo: implement anomaly detection service
        AnomaliesImpl anomalies = new AnomaliesImpl();

        anomalies.add(Map.of("1", "1"));

        return anomalies;
    }
}
