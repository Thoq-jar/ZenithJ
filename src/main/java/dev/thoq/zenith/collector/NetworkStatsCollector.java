package dev.thoq.zenith.collector;

import dev.thoq.zenith.service.monitoring.NetworkMonitorService;
import dev.thoq.zenith.util.LoggingUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("BusyWait")
public class NetworkStatsCollector {
    private final NetworkMonitorService networkMonitorService;
    private final LoggingUtils logger;
    private volatile boolean running = false;
    private Thread networkStatsThread;

    public NetworkStatsCollector() {
        this.logger = LoggingUtils.getLogger(NetworkStatsCollector.class);
        this.networkMonitorService = new NetworkMonitorService();
    }

    public void runUpdateNetworkStats() {
        logger.info("Starting network stats thread...");

        if(running) {
            logger.warn("Network stats thread is already running, ignoring request to start it...");
            return;
        }

        running = true;
        networkStatsThread = new Thread(() -> {
            Thread.currentThread().setName("Zenith-NetworkStatsCollector");

            while(running) {
                networkMonitorService.updateData();
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    logger.trace("Interrupted while waiting for network stats thread to update", ex);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        networkStatsThread.start();
    }

    public void stopUpdateNetworkStats() {
        logger.info("Stopping network stats thread...");
        running = false;
        networkMonitorService.stopMonitoring();

        if(networkStatsThread != null) {
            networkStatsThread.interrupt();

            try {
                networkStatsThread.join(1000);
            } catch(InterruptedException ex) {
                logger.trace("Interrupted while waiting for network stats thread to stop", ex);
                Thread.currentThread().interrupt();
            }

            networkStatsThread = null;
        }

        logger.info("Stopped network stats thread!");
    }

    public Collection<Map<String, Long>> collect() {
        Map<String, Long> upload = Map.of("up", networkMonitorService.getDataUp());
        Map<String, Long> download = Map.of("down", networkMonitorService.getDataDown());

        return List.of(upload, download);
    }
}
