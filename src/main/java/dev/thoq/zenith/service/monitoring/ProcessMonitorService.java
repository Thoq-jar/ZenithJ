package dev.thoq.zenith.service.monitoring;

import dev.thoq.zenith.util.LoggingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessMonitorService {
    private final LoggingUtils logger = LoggingUtils.getLogger(ProcessMonitorService.class);
    private final Collection<String> topProcessNames;
    private final String os = System.getProperty("os.name").toLowerCase();

    public ProcessMonitorService() {
        this.topProcessNames = new ArrayList<>();
    }

    public void updateData() {
        List<String> processes = getTopProcesses();
        processes.forEach(name -> {
            if(!topProcessNames.contains(name)) {
                topProcessNames.add(name);
            }
        });
    }

    private List<String> getTopProcesses() {
        if(os.contains("win")) return getWindowsProcesses();
        if(os.contains("mac")) return getMacProcesses();
        if(os.contains("nix") || os.contains("nux") || os.contains("aix")) return getLinuxProcesses();
        return Collections.emptyList();
    }

    private List<String> getWindowsProcesses() {
        List<String> processes = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("wmic", "process", "get", "name,processid,percentprocessortime", "/format:csv").start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null) {
                    String processName = extractWindowsProcessName(line);
                    if(processName != null) {
                        processes.add(processName);
                    }
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
        } catch(IOException | InterruptedException e) {
            logger.error("Error getting Windows processes: " + e.getMessage());
            return getWindowsProcessesFallback();
        }

        return processes.subList(0, Math.min(processes.size(), 10));
    }

    private String extractWindowsProcessName(String line) {
        if(line.trim().isEmpty() || line.startsWith("Node")) return null;

        String[] parts = line.split(",");
        if(parts.length < 2 || parts[1].trim().isEmpty()) return null;

        return parts[1].trim();
    }

    private List<String> getWindowsProcessesFallback() {
        List<String> processes = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("tasklist", "/fo", "csv").start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.readLine();
                String line;
                while((line = reader.readLine()) != null) {
                    String processName = extractTasklistProcessName(line);
                    if(processName != null) {
                        processes.add(processName);
                    }
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
        } catch(IOException | InterruptedException e) {
            logger.error("Error getting Windows processes fallback: " + e.getMessage());
        }

        return processes.subList(0, Math.min(processes.size(), 10));
    }

    private String extractTasklistProcessName(String line) {
        String[] parts = line.split(",");
        if(parts.length == 0) return null;
        return parts[0].replace("\"", "");
    }

    private List<String> getMacProcesses() {
        List<String> processes = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("ps", "-eo", "comm,%cpu", "--sort=-%cpu").start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.readLine();
                String line;
                int count = 0;
                while((line = reader.readLine()) != null && count < 10) {
                    String processName = extractMacProcessName(line);
                    if(processName != null) {
                        processes.add(processName);
                        count++;
                    }
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
        } catch(IOException | InterruptedException ex) {
            logger.trace("Error getting Mac processes: ", ex);
        }

        return processes;
    }

    private String extractMacProcessName(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        if(parts.length < 1) return null;

        String processName = parts[0];
        if(processName.contains("/")) {
            processName = processName.substring(processName.lastIndexOf("/") + 1);
        }
        return processName;
    }

    private List<String> getLinuxProcesses() {
        List<String> processes = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("ps", "-eo", "comm,pcpu", "--sort=-pcpu", "--no-headers").start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                int count = 0;
                while((line = reader.readLine()) != null && count < 10) {
                    String processName = extractLinuxProcessName(line);
                    if(processName != null) {
                        processes.add(processName);
                        count++;
                    }
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
        } catch(IOException | InterruptedException ex) {
            logger.trace("Error getting Linux processes: ", ex);
        }

        return processes;
    }

    private String extractLinuxProcessName(String line) {
        String[] parts = line.trim().split("\\s+", 2);
        if(parts.length < 1) return null;
        return parts[0];
    }

    public Collection<String> getTopProcessNames() {
        return new ArrayList<>(topProcessNames);
    }

    public int getProcessCount() {
        return topProcessNames.size();
    }

    public boolean isMonitoring() {
        return !topProcessNames.isEmpty();
    }
}
