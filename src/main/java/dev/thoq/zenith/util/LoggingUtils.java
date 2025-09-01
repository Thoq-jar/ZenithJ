package dev.thoq.zenith.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"unused", "StringBufferReplaceableByString", "SameParameterValue"})
public record LoggingUtils(String className) {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    public enum Level {
        TRACE(BRIGHT_BLACK, "TRACE"),
        DEBUG(CYAN, "DEBUG"),
        INFO(GREEN, "INFO "),
        FIXME(BRIGHT_YELLOW, "FIXME"),
        WARN(YELLOW, "WARN "),
        ERROR(RED, "ERROR"),
        FATAL(BRIGHT_RED + BOLD, "FATAL");

        private final String color;
        private final String label;

        Level(String color, String label) {
            this.color = color;
            this.label = label;
        }

        public String getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final ConcurrentMap<String, LoggingUtils> loggerCache = new ConcurrentHashMap<>();

    /**
     * Get a logger instance for the given class
     */
    public static LoggingUtils getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    /**
     * Get a logger instance for the given class name
     */
    public static LoggingUtils getLogger(String className) {
        return loggerCache.computeIfAbsent(className, LoggingUtils::new);
    }

    /**
     * Log a trace message
     */
    public void trace(String message, Object... args) {
        log(Level.TRACE, message, args);
    }

    /**
     * Log a debug message
     */
    public void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    /**
     * Log an info message
     */
    public void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    /**
     * Log a fix me message
     */
    public void fixme(String message, Object... args) {
        log(Level.FIXME, message, args);
    }

    /**
     * Log a warning message
     */
    public void warn(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    /**
     * Log an error message
     */
    public void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }

    /**
     * Log an error message with throwable
     */
    public void error(String message, Throwable throwable, Object... args) {
        log(Level.ERROR, message, args);
        if(throwable != null) {
            logStackTrace(throwable);
        }
    }

    /**
     * Log a fatal message
     */
    public void fatal(String message, Object... args) {
        log(Level.FATAL, message, args);
    }

    /**
     * Log a fatal message with throwable
     */
    public void fatal(String message, Throwable throwable, Object... args) {
        log(Level.FATAL, message, args);

        if(throwable != null)
            logStackTrace(throwable);
    }

    /**
     * Main logging method
     */
    private void log(Level level, String message, Object... args) {
        LocalDateTime now = LocalDateTime.now();
        String threadName = Thread.currentThread().getName();

        String formattedMessage = formatMessage(message, args);
        StringBuilder logEntry = new StringBuilder();

        logEntry.append(BRIGHT_BLACK)
                .append(now.format(DATE_FORMATTER))
                .append(RESET)
                .append(" ");

        logEntry.append(BRIGHT_WHITE)
                .append(now.format(TIME_FORMATTER))
                .append(RESET)
                .append(" ");

        logEntry.append("[")
                .append(level.getColor())
                .append(level.getLabel())
                .append(RESET)
                .append("] ");

        logEntry.append("[")
                .append(CYAN)
                .append(String.format("%-20s", truncateOrPad(threadName, 20)))
                .append(RESET)
                .append("] ");

        logEntry.append(BLUE)
                .append(String.format("%-20s", truncateOrPad(className, 20)))
                .append(RESET)
                .append(" - ");

        logEntry.append(formattedMessage);

        System.out.println(logEntry);
    }

    /**
     * Format message with arguments
     */
    private String formatMessage(String message, Object... args) {
        if(args == null || args.length == 0) {
            return message;
        }

        try {
            return String.format(message, args);
        } catch(Exception e) {
            return message + " " + Arrays.toString(args);
        }
    }

    /**
     * Truncate or pad string to specified length
     */
    private String truncateOrPad(String str, int length) {
        if(str.length() > length) {
            return str.substring(0, length - 3) + "...";
        } else {
            return str;
        }
    }

    /**
     * Log stack trace for throwable
     */
    private void logStackTrace(Throwable throwable) {
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(RED).append("Exception: ").append(throwable.getClass().getSimpleName())
                .append(": ").append(throwable.getMessage()).append(RESET);
        System.out.println(stackTrace);

        StackTraceElement[] elements = throwable.getStackTrace();
        for(StackTraceElement element : elements)
            System.out.println(BRIGHT_BLACK + "    at " + element.toString() + RESET);

        if(throwable.getCause() != null) {
            System.out.println(YELLOW + "Caused by:" + RESET);
            logStackTrace(throwable.getCause());
        }
    }

    /**
     * Enable or disable colors (for environments that don't support ANSI)
     */
    private static boolean colorsEnabled = true;

    public static void setColorsEnabled(boolean enabled) {
        colorsEnabled = enabled;
    }

    public static boolean areColorsEnabled() {
        return colorsEnabled;
    }
}
