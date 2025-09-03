package dev.thoq.zenith.processor;

import dev.thoq.zenith.model.types.MonitorData;
import dev.thoq.zenith.util.LoggingUtils;

public class RealTimeProcessor {
    private final LoggingUtils logger;
    private volatile boolean running = false;
    private Thread realtimeProcessorThread;
    private final MetricsAggregator metricsAggregator;
    private final DataProcessor dataProcessor;

    public RealTimeProcessor() {
        this.logger = LoggingUtils.getLogger(RealTimeProcessor.class);
        this.metricsAggregator = new MetricsAggregator();
        this.dataProcessor = new DataProcessor();
    }

    public void runUpdateProcessStats() {
        logger.info("Starting realtime processor thread...");

        if(running) {
            logger.warn("Process stats thread is already running, ignoring request to start it...");
            return;
        }

        running = true;
        realtimeProcessorThread = new Thread(() -> {
            Thread.currentThread().setName("Zenith-RealtimeProcessorr");

            while(running) {
                metricsAggregator.aggregate();
                MonitorData data = metricsAggregator.getAggregatedData();

                dataProcessor.process(data);
            }
        });

        realtimeProcessorThread.start();
    }

    public void stopUpdateProcessStats() {
        logger.info("Stopping realtime processor thread...");
        running = false;

        if(realtimeProcessorThread != null) {
            realtimeProcessorThread.interrupt();

            try {
                realtimeProcessorThread.join(1000);
            } catch(InterruptedException ex) {
                logger.trace("Interrupted while waiting for process stats thread to stop", ex);
                Thread.currentThread().interrupt();
            }

            realtimeProcessorThread = null;
        }

        logger.info("Stopped process stats thread!");
    }
}

