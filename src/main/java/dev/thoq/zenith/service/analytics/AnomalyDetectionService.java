package dev.thoq.zenith.service.analytics;

import java.util.List;
import java.util.Map;

public class AnomalyDetectionService {
    public AnomalyDetectionService() {
    }

    public List<Map<String, String>> detectAnomalies(List<Float> timestamps, Map<String, Map<String, Double>> data) {
        // todo: implement anomaly detection service

        return List.of(Map.of("1", "1"));
    }
}
