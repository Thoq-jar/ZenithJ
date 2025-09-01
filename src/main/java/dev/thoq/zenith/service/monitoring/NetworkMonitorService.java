package dev.thoq.zenith.service.monitoring;

import dev.thoq.zenith.util.LoggingUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkMonitorService {
    private long dataUp;
    private long dataDown;
    private long previousUpBytes = 0;
    private long previousDownBytes = 0;
    private boolean isRunning = false;
    private final ScheduledExecutorService scheduler;
    private final LoggingUtils logger = LoggingUtils.getLogger(NetworkMonitorService.class);
    private final String osName = System.getProperty("os.name").toLowerCase();

    public NetworkMonitorService() {
        this.dataUp = 0;
        this.dataDown = 0;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startMonitoring();
    }

    private void startMonitoring() {
        if(isRunning) {
            logger.warn("Network stats thread is already running, ignoring request to start it...");
            return;
        }

        isRunning = true;
        scheduler.scheduleAtFixedRate(this::updateData, 0, 1, TimeUnit.SECONDS);
    }

    public void updateData() {
        try {
            long[] currentStats = getNetworkStats();
            assert currentStats != null;

            long currentUpBytes = currentStats[0];
            long currentDownBytes = currentStats[1];

            if(previousUpBytes > 0 && previousDownBytes > 0) {
                dataUp = currentUpBytes - previousUpBytes;
                dataDown = currentDownBytes - previousDownBytes;
            }

            previousUpBytes = currentUpBytes;
            previousDownBytes = currentDownBytes;
        } catch(Exception e) {
            logger.warn("Failed to update network data: " + e.getMessage());
            dataUp = 0;
            dataDown = 0;
        }
    }

    private long[] getNetworkStats() {
        if(osName.contains("linux")) {
            return getLinuxNetworkStats();
        } else if(osName.contains("mac") || osName.contains("darwin")) {
            return getMacNetworkStats();
        } else if(osName.contains("win")) {
            return getWindowsNetworkStats();
        } else {
            logger.warn("Unsupported operating system: " + osName);
            return null;
        }
    }

    private long[] getLinuxNetworkStats() {
        long totalTxBytes = 0;
        long totalRxBytes = 0;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while(interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                if(!netInterface.isLoopback() && netInterface.isUp()) {
                    String interfaceName = netInterface.getName();

                    Path txPath = Paths.get("/sys/class/net/" + interfaceName + "/statistics/tx_bytes");
                    Path rxPath = Paths.get("/sys/class/net/" + interfaceName + "/statistics/rx_bytes");

                    if(Files.exists(txPath) && Files.exists(rxPath)) {
                        totalTxBytes += Long.parseLong(Files.readString(txPath).trim());
                        totalRxBytes += Long.parseLong(Files.readString(rxPath).trim());
                    }
                }
            }
        } catch(Exception e) {
            logger.warn("Error reading Linux network stats: " + e.getMessage());
            return null;
        }

        return new long[]{totalTxBytes, totalRxBytes};
    }

    private long[] getMacNetworkStats() {
        long totalTxBytes = 0;
        long totalRxBytes = 0;

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"netstat", "-ibn"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");

                if(parts.length >= 10 && !line.contains("Name") && !line.contains("Link")) {
                    if(!parts[0].startsWith("lo")) {
                        try {
                            totalRxBytes += Long.parseLong(parts[6]);
                            totalTxBytes += Long.parseLong(parts[9]);
                        } catch(NumberFormatException ignored) {
                        }
                    }
                }
            }

            process.waitFor();
        } catch(Exception e) {
            logger.warn("Error reading macOS network stats: " + e.getMessage());
            return null;
        }

        return new long[]{totalTxBytes, totalRxBytes};
    }

    private long[] getWindowsNetworkStats() {
        long totalTxBytes = 0;
        long totalRxBytes = 0;

        try {
            String command = "powershell.exe -Command \"Get-NetAdapterStatistics | Where-Object {$_.Name -notlike '*Loopback*'} | Measure-Object -Property BytesSent,BytesReceived -Sum | Select-Object -ExpandProperty Sum\"";
            Process process = Runtime.getRuntime().exec(new String[]{command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String sentLine = reader.readLine();
            String receivedLine = reader.readLine();

            if(sentLine != null && !sentLine.trim().isEmpty()) {
                totalTxBytes = Long.parseLong(sentLine.trim());
            }
            if(receivedLine != null && !receivedLine.trim().isEmpty()) {
                totalRxBytes = Long.parseLong(receivedLine.trim());
            }

            process.waitFor();
        } catch(Exception e) {
            logger.warn("Error reading Windows network stats: " + e.getMessage());

            try {
                String wmiCommand = "wmic path Win32_NetworkAdapter where NetConnectionStatus=2 get BytesSentPerSec,BytesReceivedPerSec /format:csv";
                Process wmiProcess = Runtime.getRuntime().exec(new String[]{wmiCommand});
                BufferedReader wmiReader = new BufferedReader(new InputStreamReader(wmiProcess.getInputStream()));
                String line;

                while((line = wmiReader.readLine()) != null) {
                    if(line.contains(",") && !line.contains("Node")) {
                        String[] parts = line.split(",");
                        if(parts.length >= 3) {
                            try {
                                if(!parts[1].trim().isEmpty())
                                    totalRxBytes += Long.parseLong(parts[1].trim());

                                if(!parts[2].trim().isEmpty())
                                    totalTxBytes += Long.parseLong(parts[2].trim());
                            } catch(NumberFormatException ignored) {
                            }
                        }
                    }
                }
                wmiProcess.waitFor();
            } catch(Exception wmiEx) {
                logger.warn("WMI fallback failed: " + wmiEx.getMessage());
                return null;
            }
        }

        return new long[]{totalTxBytes, totalRxBytes};
    }

    public long getDataUp() {
        return dataUp;
    }

    public long getDataDown() {
        return dataDown;
    }

    public void stopMonitoring() {
        if(scheduler != null && !scheduler.isShutdown()) {
            isRunning = false;
            scheduler.shutdown();
            try {
                if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch(InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
