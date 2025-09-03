package dev.thoq.zenith.processor;

import dev.thoq.zenith.model.types.Anomalies;
import dev.thoq.zenith.model.types.MonitorData;
import dev.thoq.zenith.model.types.Timestamps;
import dev.thoq.zenith.model.types.impl.TimestampsImpl;
import dev.thoq.zenith.service.analytics.AnomalyDetectionService;
import dev.thoq.zenith.service.analytics.ReportGenerationService;
import dev.thoq.zenith.service.analytics.TrendAnalysisService;

public class DataProcessor {
    private float lastTimestamp;
    private final Timestamps timestamps = new TimestampsImpl();
    private final AnomalyDetectionService anomalyDetectionService;
    private final ReportGenerationService reportGenerationService;
    private final TrendAnalysisService trendAnalysisService;

    public DataProcessor() {
        this.anomalyDetectionService = new AnomalyDetectionService();
        this.reportGenerationService = new ReportGenerationService();
        this.trendAnalysisService = new TrendAnalysisService();
    }

    public void process(MonitorData data) {
        this.lastTimestamp = System.currentTimeMillis() / 1000.0f;
        this.timestamps.add(getLastTimestamp());

        Anomalies anomalies = anomalyDetectionService.detectAnomalies(timestamps, data);
        reportGenerationService.generateReport(anomalies, data);
        trendAnalysisService.analyzeTrends(data);
    }

    public float getLastTimestamp() {
        return lastTimestamp;
    }
}
