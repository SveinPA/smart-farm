package edu.ntnu.bidata.smg.group8.control.logic.history;

/**
 * Statistical summary of sensor readings over a time period.
 * Contains minimum, maximum, and average values.
 * 
 * @author Svein Antonsen
 * @since 1.0
 * 
 * @param min Minimum value in the time period
 * @param max Maximum value in the time period
 * @param average Average (mean) value in the time period
 */
public record Statistics(double min, double max, double average) {
  
  /**
   * Returns a Statistics instance representing "No data available".
   * All values are NaN (Not a Number).
   */
  public static Statistics empty() {
    return new Statistics(Double.NaN, Double.NaN, Double.NaN);
  }

  /**
   * Checks if this statistics contains valid data.
   * 
   * @return true if statistics are valid, false if empty (NaN)
   */
  public boolean isValid() {
    return !Double.isNaN(min) && !Double.isNaN(max) && !Double.isNaN(average);
  }
}
