package dev.thoq.zenith.collector;

import dev.thoq.zenith.service.monitoring.NetworkMonitorService;
import dev.thoq.zenith.service.monitoring.ResourceMonitorService;
import dev.thoq.zenith.util.LoggingUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("BusyWait")
public class MetricsCollector {
    private Thread metricsThread;
    private volatile boolean running = false;
    private final ResourceMonitorService resourceMonitorService;
    private final NetworkMonitorService networkMonitorService;
    private final LoggingUtils logger;

    public MetricsCollector() {
        this.resourceMonitorService = new ResourceMonitorService();
        this.networkMonitorService = new NetworkMonitorService();
        this.logger = LoggingUtils.getLogger(MetricsCollector.class);
    }

    public void runUpdateMetrics() {
        logger.info("Starting metrics thread...");
        if(running) {
            logger.warn("Metrics thread is already running, ignoring request to start it...");
            return;
        }

        running = true;
        metricsThread = new Thread(() -> {
            Thread.currentThread().setName("Zenith-ResourceMonitor");

            while(running) {
                resourceMonitorService.updateAllMetrics();
                try {
                    Thread.sleep(100);
                } catch(InterruptedException ex) {
                    logger.trace("Interrupted while waiting for metrics thread to update", ex);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        metricsThread.start();
    }

    public void stopUpdateMetrics() {
        logger.info("Stopping metrics thread...");
        running = false;

        if(metricsThread != null) {
            metricsThread.interrupt();

            try {
                metricsThread.join(1000);
            } catch(InterruptedException ex) {
                logger.trace("Interrupted while waiting for metrics thread to stop", ex);
                Thread.currentThread().interrupt();
            }

            metricsThread = null;
        }

        logger.info("Stopped metrics thread!");
    }

    public Collection<Map<String, Double>> collect() {
        Map<String, Double> cpu = Map.of("cpu", resourceMonitorService.getCpuUsage());
        Map<String, Double> memory = Map.of("memory", resourceMonitorService.getMemoryUsagePercentage());
        Map<String, Double> disk = Map.of("disk", resourceMonitorService.getDiskUsageMb());
        Map<String, Long> upload = Map.of("up", networkMonitorService.getDataUp());
        Map<String, Long> download = Map.of("down", networkMonitorService.getDataDown());

        return List.of(cpu, memory, disk);
    }
}
