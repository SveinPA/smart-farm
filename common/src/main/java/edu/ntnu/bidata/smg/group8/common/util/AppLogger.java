package edu.ntnu.bidata.smg.group8.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for obtaining SLF4J loggers with standardized configuration.
 * 
 * Example usage:
 * <pre>
 * private static final Logger logger = AppLogger.get(MyClass.class);
 * logger.info("This is a log message.");
 * </pre>
 */
public class AppLogger {
 
  private AppLogger() {
    // Utility class, no instances allowed
  }

  /**
   * Returns an SLF4J logger for the given class.
   * 
   * @param clazz The class for which to get the logger.
   * @return A configured SLF4J logger.
   */
  public static Logger get(Class<?> clazz) {
    return LoggerFactory.getLogger(clazz);
  }

  /**
   * Returns an SLF4J logger with a custom name.
   *
   * @param name The name of the logger.
   * @return A configured SLF4J logger.
   */
  public static Logger get(String name) {
    return LoggerFactory.getLogger(name);
  }
}
