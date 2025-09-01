package dev.thoq.zenith.processor;

import dev.thoq.zenith.service.monitoring.NetworkMonitorService;
import dev.thoq.zenith.service.monitoring.ResourceMonitorService;

import java.util.Map;

public class MetricsAggregator {
    private Map<String, Map<String, Double>> data;
    private final ResourceMonitorService resourceMonitorService;
    private final NetworkMonitorService networkMonitorService;

    public MetricsAggregator() {
        this.resourceMonitorService = new ResourceMonitorService();
        this.networkMonitorService = new NetworkMonitorService();
    }

    public Map<String, Map<String, Double>> getAggregatedData() {
        return data;
    }

    public void aggregate() {
        Map<String, Double> cpu = Map.of("usage", resourceMonitorService.getCpuUsage());
        Map<String, Double> memory = Map.of("usage", resourceMonitorService.getMemoryUsagePercentage());
        Map<String, Double> disk = Map.of("usage", resourceMonitorService.getDiskUsageMb());
        Map<String, Double> network = Map.of(
                "upload", (double) networkMonitorService.getDataUp(),
                "download", (double) networkMonitorService.getDataDown()
        );

        this.data = Map.of(
                "cpu", cpu,
                "memory", memory,
                "disk", disk,
                "network", network
        );
    }
}
