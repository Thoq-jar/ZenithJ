package dev.thoq.zenith.controller;

import dev.thoq.zenith.collector.MetricsCollector;
import dev.thoq.zenith.collector.NetworkStatsCollector;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class DashboardController {

    @Inject
    Template dashboard;

    public DashboardController() {
        MetricsCollector metricsCollector = new MetricsCollector();
        NetworkStatsCollector networkStatsCollector = new NetworkStatsCollector();

        metricsCollector.runUpdateMetrics();
        networkStatsCollector.runUpdateNetworkStats();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String dashboard() {
        return dashboard.render();
    }
}
