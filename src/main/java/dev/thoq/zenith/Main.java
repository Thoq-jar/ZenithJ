package dev.thoq.zenith;

import dev.thoq.zenith.collector.MetricsCollector;
import dev.thoq.zenith.collector.NetworkStatsCollector;
import dev.thoq.zenith.collector.ProcessStatsCollector;
import dev.thoq.zenith.processor.RealTimeProcessor;
import dev.thoq.zenith.util.LoggingUtils;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

public class Main implements QuarkusApplication {
    private static final LoggingUtils logger = LoggingUtils.getLogger(Main.class);
    private static final MetricsCollector metricsCollector = new MetricsCollector();
    private static final NetworkStatsCollector networkStatsCollector = new NetworkStatsCollector();
    private static final ProcessStatsCollector processStatsCollector = new ProcessStatsCollector();
    private static final RealTimeProcessor realtimeProcessor = new RealTimeProcessor();

    public static void initialize() {
        metricsCollector.runUpdateMetrics();
        networkStatsCollector.runUpdateNetworkStats();
        processStatsCollector.runUpdateProcessStats();
        realtimeProcessor.runUpdateProcessStats();
    }

    public static void shutdown() {
        metricsCollector.stopUpdateMetrics();
        networkStatsCollector.stopUpdateNetworkStats();
        processStatsCollector.stopUpdateProcessStats();
        realtimeProcessor.stopUpdateProcessStats();
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("Zenith-Bootstrap");
        logger.info("Starting Zenith with Quarkus...");
        Quarkus.run(Main.class, args);
    }

    @Override
    public int run(String... args) {
        Thread.currentThread().setName("Zenith-Bootstrap");

        logger.info("Booting Zenith...");
        initialize();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("Zenith-ShutdownHook");
            logger.info("Shutting down Zenith...");

            shutdown();
        }));

        logger.info("Zenith booted successfully!");
        logger.info("Dashboard available at: http://localhost:9595");

        Quarkus.waitForExit();
        return 0;
    }
}
