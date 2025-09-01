package dev.thoq.zenith.controller;

import dev.thoq.zenith.collector.MetricsCollector;
import dev.thoq.zenith.collector.NetworkStatsCollector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.Map;

@Path("/api")
public class DashboardRestController {

    private final MetricsCollector metricsCollector;
    private final NetworkStatsCollector networkStatsCollector;

    public DashboardRestController() {
        this.metricsCollector = new MetricsCollector();
        this.networkStatsCollector = new NetworkStatsCollector();
    }

    @GET
    @Path("/metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getMetrics() {
        Map<String, Object> response = new HashMap<>();

        var metrics = metricsCollector.collect();
        var networkStats = networkStatsCollector.collect();

        for(var metric : metrics)
            response.putAll(metric);

        for(var stat : networkStats)
            response.putAll(stat);

        return response;
    }
}
