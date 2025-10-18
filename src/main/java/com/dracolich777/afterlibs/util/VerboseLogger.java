package com.dracolich777.afterlibs.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced logging system with verbose mode support and per-class log level control.
 * 
 * Features:
 * - Global verbose mode toggle
 * - Per-class log level configuration
 * - Automatic caller class detection
 * - Thread-safe operation
 * - Performance metrics logging
 * - Stack trace utilities
 */
public class VerboseLogger {
    
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();
    private static boolean globalVerboseMode = false;
    private static final Map<String, Level> classLogLevels = new ConcurrentHashMap<>();
    
    /**
     * Enable or disable verbose logging globally
     * @param enabled true to enable verbose logging
     */
    public static void setVerboseMode(boolean enabled) {
        globalVerboseMode = enabled;
        if (enabled) {
            getLogger(VerboseLogger.class).info("=== VERBOSE LOGGING ENABLED GLOBALLY ===");
        } else {
            getLogger(VerboseLogger.class).info("=== VERBOSE LOGGING DISABLED GLOBALLY ===");
        }
    }
    
    /**
     * Check if verbose mode is enabled globally
     * @return true if verbose mode is enabled
     */
    public static boolean isVerboseMode() {
        return globalVerboseMode;
    }
    
    /**
     * Set log level for a specific class
     * @param clazz The class to configure
     * @param level The log level to set
     */
    public static void setLogLevel(Class<?> clazz, Level level) {
        classLogLevels.put(clazz.getName(), level);
        getLogger(clazz).info("Log level set to {} for class {}", level, clazz.getSimpleName());
    }
    
    /**
     * Get a logger for a specific class
     * @param clazz The class requesting the logger
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LOGGERS.computeIfAbsent(clazz.getName(), k -> LogManager.getLogger(clazz));
    }
    
    /**
     * Get a logger for the calling class (auto-detected)
     * @return Logger instance
     */
    public static Logger getLogger() {
        String callerClassName = getCallerClassName();
        return LOGGERS.computeIfAbsent(callerClassName, k -> LogManager.getLogger(callerClassName));
    }
    
    /**
     * Log a verbose message (only if verbose mode is enabled)
     * @param clazz The class logging the message
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void verbose(Class<?> clazz, String message, Object... args) {
        if (globalVerboseMode || shouldLog(clazz, Level.DEBUG)) {
            getLogger(clazz).debug("[VERBOSE] " + message, args);
        }
    }
    
    /**
     * Log a verbose message with auto-detected caller class
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void verbose(String message, Object... args) {
        if (globalVerboseMode) {
            getLogger().debug("[VERBOSE] " + message, args);
        }
    }
    
    /**
     * Log an info message
     * @param clazz The class logging the message
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void info(Class<?> clazz, String message, Object... args) {
        if (shouldLog(clazz, Level.INFO)) {
            getLogger(clazz).info(message, args);
        }
    }
    
    /**
     * Log an info message with auto-detected caller class
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void info(String message, Object... args) {
        getLogger().info(message, args);
    }
    
    /**
     * Log a warning message
     * @param clazz The class logging the message
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void warn(Class<?> clazz, String message, Object... args) {
        getLogger(clazz).warn(message, args);
    }
    
    /**
     * Log a warning message with auto-detected caller class
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void warn(String message, Object... args) {
        getLogger().warn(message, args);
    }
    
    /**
     * Log an error message
     * @param clazz The class logging the message
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void error(Class<?> clazz, String message, Object... args) {
        getLogger(clazz).error(message, args);
    }
    
    /**
     * Log an error message with auto-detected caller class
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void error(String message, Object... args) {
        getLogger().error(message, args);
    }
    
    /**
     * Log an error message with exception
     * @param clazz The class logging the message
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }
    
    /**
     * Log an error message with exception and auto-detected caller class
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void error(String message, Throwable throwable) {
        getLogger().error(message, throwable);
    }
    
    /**
     * Log a debug message
     * @param clazz The class logging the message
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void debug(Class<?> clazz, String message, Object... args) {
        if (shouldLog(clazz, Level.DEBUG)) {
            getLogger(clazz).debug(message, args);
        }
    }
    
    /**
     * Log a debug message with auto-detected caller class
     * @param message The message to log
     * @param args Optional arguments for message formatting
     */
    public static void debug(String message, Object... args) {
        getLogger().debug(message, args);
    }
    
    /**
     * Log execution time of a code block
     * @param clazz The class logging the measurement
     * @param taskName Name of the task being measured
     * @param startTime Start time from System.nanoTime()
     */
    public static void logExecutionTime(Class<?> clazz, String taskName, long startTime) {
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        
        if (globalVerboseMode || shouldLog(clazz, Level.DEBUG)) {
            getLogger(clazz).debug("[PERFORMANCE] {} completed in {:.3f}ms", taskName, durationMs);
        }
    }
    
    /**
     * Log a separator line for better log readability
     * @param clazz The class logging the separator
     * @param title Optional title for the separator
     */
    public static void logSeparator(Class<?> clazz, String title) {
        if (globalVerboseMode || shouldLog(clazz, Level.INFO)) {
            if (title != null && !title.isEmpty()) {
                getLogger(clazz).info("=".repeat(20) + " {} " + "=".repeat(20), title);
            } else {
                getLogger(clazz).info("=".repeat(50));
            }
        }
    }
    
    /**
     * Log a separator line with auto-detected caller class
     * @param title Optional title for the separator
     */
    public static void logSeparator(String title) {
        if (globalVerboseMode) {
            if (title != null && !title.isEmpty()) {
                getLogger().info("=".repeat(20) + " {} " + "=".repeat(20), title);
            } else {
                getLogger().info("=".repeat(50));
            }
        }
    }
    
    /**
     * Check if logging should occur for a specific class and level
     * @param clazz The class to check
     * @param level The log level to check
     * @return true if logging should occur
     */
    private static boolean shouldLog(Class<?> clazz, Level level) {
        Level classLevel = classLogLevels.get(clazz.getName());
        if (classLevel != null) {
            return level.isMoreSpecificThan(classLevel);
        }
        return true; // Default to allowing all logs
    }
    
    /**
     * Get the class name of the caller (2 levels up the stack)
     * @return The fully qualified class name of the caller
     */
    private static String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] is getStackTrace()
        // stackTrace[1] is getCallerClassName()
        // stackTrace[2] is the VerboseLogger method
        // stackTrace[3] is the actual caller
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName();
        }
        return "Unknown";
    }
    
    /**
     * Print current stack trace for debugging
     * @param clazz The class requesting the stack trace
     */
    public static void printStackTrace(Class<?> clazz) {
        if (globalVerboseMode) {
            Logger logger = getLogger(clazz);
            logger.debug("=== STACK TRACE ===");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 2; i < stackTrace.length; i++) { // Skip getStackTrace() and printStackTrace()
                logger.debug("  at {}", stackTrace[i]);
            }
        }
    }
}
