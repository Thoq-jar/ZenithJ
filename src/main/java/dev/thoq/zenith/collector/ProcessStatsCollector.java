package dev.thoq.zenith.collector;

import dev.thoq.zenith.service.monitoring.ProcessMonitorService;
import dev.thoq.zenith.util.LoggingUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("BusyWait")
public class ProcessStatsCollector {
    private final ProcessMonitorService processMonitorService;
    private final LoggingUtils logger;
    private volatile boolean running = false;
    private Thread processStatsThread;

    public ProcessStatsCollector() {
        this.logger = LoggingUtils.getLogger(ProcessStatsCollector.class);
        this.processMonitorService = new ProcessMonitorService();
    }

    public void runUpdateProcessStats() {
        logger.info("Starting process stats thread...");

        if(running) {
            logger.warn("Process stats thread is already running, ignoring request to start it...");
            return;
        }

        running = true;
        processStatsThread = new Thread(() -> {
            Thread.currentThread().setName("Zenith-ProcessStatsCollector");

            while(running) {
                processMonitorService.updateData();

                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    logger.trace("Interrupted while waiting for process stats thread to update", ex);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        processStatsThread.start();
    }

    public void stopUpdateProcessStats() {
        logger.info("Stopping process stats thread...");
        running = false;

        if(processStatsThread != null) {
            processStatsThread.interrupt();

            try {
                processStatsThread.join(1000);
            } catch(InterruptedException ex) {
                logger.trace("Interrupted while waiting for process stats thread to stop", ex);
                Thread.currentThread().interrupt();
            }

            processStatsThread = null;
        }

        logger.info("Stopped process stats thread!");
    }

    public Collection<Map<String, Object>> collect() {
        Map<String, Object> processData = Map.of(
                "processes", processMonitorService.getTopProcessNames(),
                "count", processMonitorService.getProcessCount(),
                "monitoring", processMonitorService.isMonitoring()
        );

        return List.of(processData);
    }
}
