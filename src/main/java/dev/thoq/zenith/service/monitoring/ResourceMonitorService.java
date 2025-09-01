package dev.thoq.zenith.service.monitoring;

import dev.thoq.zenith.util.LoggingUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;

@SuppressWarnings("unused")
public class ResourceMonitorService {
    private static final LoggingUtils logger = LoggingUtils.getLogger(ResourceMonitorService.class);
    private double memoryUsageMb;
    private double cpuUsage;
    private double diskUsageMb;
    private final boolean isWindows;
    private final boolean isMac;
    private final boolean isLinux;

    public ResourceMonitorService() {
        this.memoryUsageMb = 0;
        this.cpuUsage = 0;
        this.diskUsageMb = 0;

        String os = System.getProperty("os.name").toLowerCase();
        this.isWindows = os.contains("windows");
        this.isMac = os.contains("mac") || os.contains("darwin");
        this.isLinux = os.contains("linux") || os.contains("unix");
    }

    public void updateAllMetrics() {
        updateMemoryUsage();
        updateCpuUsage();
        updateDiskUsage();
    }

    private void updateMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() +
                    memoryBean.getNonHeapMemoryUsage().getUsed();
            this.memoryUsageMb = usedMemory / (1024.0 * 1024.0);
        } catch(Exception e) {
            logger.error("Failed to update memory usage", e);
        }
    }

    private void updateCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            try {
                Method method = osBean.getClass().getMethod("getProcessCpuLoad");
                Double cpuLoad = (Double) method.invoke(osBean);
                if(cpuLoad != null && cpuLoad >= 0) {
                    this.cpuUsage = cpuLoad * 100.0;
                } else {
                    fallbackCpuUsage(osBean);
                }
            } catch(Exception e) {
                fallbackCpuUsage(osBean);
            }
        } catch(Exception e) {
            logger.error("Failed to update CPU usage", e);
        }
    }

    private void fallbackCpuUsage(OperatingSystemMXBean osBean) {
        try {
            double loadAverage = osBean.getSystemLoadAverage();
            if(loadAverage >= 0) {
                int availableProcessors = osBean.getAvailableProcessors();
                this.cpuUsage = Math.min((loadAverage / availableProcessors) * 100.0, 100.0);
            } else {
                this.cpuUsage = 0.0;
            }
        } catch(Exception e) {
            this.cpuUsage = 0.0;
        }
    }

    private void updateDiskUsage() {
        try {
            long totalUsed = 0;
            for(FileStore store : FileSystems.getDefault().getFileStores()) {
                try {
                    if(store.isReadOnly()) continue;

                    String storeType = store.type().toLowerCase();
                    String storeName = store.name().toLowerCase();

                    if(shouldSkipFileStore(storeType, storeName)) continue;

                    long totalSpace = store.getTotalSpace();
                    long usableSpace = store.getUsableSpace();

                    if(totalSpace > 0 && totalSpace != Long.MAX_VALUE)
                        totalUsed += (totalSpace - usableSpace);
                } catch(IOException | UnsupportedOperationException ignored) {
                }
            }
            this.diskUsageMb = totalUsed / (1024.0 * 1024.0);
        } catch(Exception e) {
            logger.error("Failed to update disk usage", e);
        }
    }

    private boolean shouldSkipFileStore(String storeType, String storeName) {
        if(isLinux) {
            return storeType.contains("tmpfs") ||
                    storeType.contains("devtmpfs") ||
                    storeType.contains("proc") ||
                    storeType.contains("sys") ||
                    storeType.contains("cgroup") ||
                    storeType.contains("devpts") ||
                    storeType.contains("securityfs") ||
                    storeType.contains("squashfs") ||
                    storeType.contains("overlay") ||
                    storeType.contains("fuse") ||
                    storeName.startsWith("/dev") ||
                    storeName.startsWith("/proc") ||
                    storeName.startsWith("/sys") ||
                    storeName.startsWith("/run") ||
                    storeName.contains("snap");
        }

        if(isMac) {
            return storeType.contains("devfs") ||
                    storeType.contains("map") ||
                    storeType.contains("autofs") ||
                    storeType.contains("nullfs") ||
                    storeName.startsWith("/dev") ||
                    storeName.contains("/Volumes/com.apple") ||
                    storeName.contains(".vol");
        }

        if(isWindows) {
            return storeType.contains("cdfs") ||
                    storeType.contains("udf") ||
                    (storeName.length() == 2 && storeName.endsWith(":") &&
                            (storeName.startsWith("a") || storeName.startsWith("b")));
        }

        return false;
    }

    public double getMemoryUsageMb() {
        return memoryUsageMb;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getDiskUsageMb() {
        return diskUsageMb;
    }

    public double getMemoryUsagePercentage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();

            if(maxMemory <= 0 || maxMemory == Long.MAX_VALUE) {
                Runtime runtime = Runtime.getRuntime();
                maxMemory = runtime.maxMemory();
                usedMemory = runtime.totalMemory() - runtime.freeMemory();
            }

            return maxMemory > 0 ? (usedMemory * 100.0) / maxMemory : 0.0;
        } catch(Exception e) {
            logger.error("Failed to calculate memory usage percentage", e);
            return 0.0;
        }
    }

    public String getOperatingSystem() {
        if(isWindows) return "Windows";
        if(isMac) return "macOS";
        if(isLinux) return "Linux";
        return "Unknown";
    }

    public double getSystemMemoryUsagePercentage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

            try {
                Method totalMemoryMethod = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
                Method freeMemoryMethod = osBean.getClass().getMethod("getFreePhysicalMemorySize");

                Long totalMemory = (Long) totalMemoryMethod.invoke(osBean);
                Long freeMemory = (Long) freeMemoryMethod.invoke(osBean);

                if(totalMemory != null && freeMemory != null && totalMemory > 0) {
                    long usedMemory = totalMemory - freeMemory;
                    return (usedMemory * 100.0) / totalMemory;
                }
            } catch(Exception ignored) {
            }

            return getMemoryUsagePercentage();
        } catch(Exception e) {
            logger.error("Failed to get system memory usage", e);
            return 0.0;
        }
    }

    public long getAvailableProcessors() {
        try {
            return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        } catch(Exception e) {
            return Runtime.getRuntime().availableProcessors();
        }
    }
}
