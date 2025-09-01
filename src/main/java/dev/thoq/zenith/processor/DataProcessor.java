package dev.thoq.zenith.processor;

import dev.thoq.zenith.service.analytics.AnomalyDetectionService;
import dev.thoq.zenith.service.analytics.ReportGenerationService;
import dev.thoq.zenith.service.analytics.TrendAnalysisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataProcessor {
    private float lastTimestamp;
    private final List<Float> timestamps;
    private final AnomalyDetectionService anomalyDetectionService;
    private final ReportGenerationService reportGenerationService;
    private final TrendAnalysisService trendAnalysisService;

    public DataProcessor() {
        this.anomalyDetectionService = new AnomalyDetectionService();
        this.reportGenerationService = new ReportGenerationService();
        this.trendAnalysisService = new TrendAnalysisService();
        this.timestamps = new ArrayList<>();
    }

    public void process(Map<String, Map<String, Double>> data) {
        this.lastTimestamp = System.currentTimeMillis() / 1000.0f;
        this.timestamps.add(getLastTimestamp());

        List<Map<String, String>> anomalies = anomalyDetectionService.detectAnomalies(timestamps, data);
        reportGenerationService.generateReport(anomalies, data);
        trendAnalysisService.analyzeTrends(data);
    }

    public float getLastTimestamp() {
        return lastTimestamp;
    }
}
