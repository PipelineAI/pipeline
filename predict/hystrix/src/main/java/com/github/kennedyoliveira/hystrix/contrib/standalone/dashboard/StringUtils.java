package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

/**
 * Utility methods for {@link String}.
 *
 * @author Kennedy Oliveira
 * @since 1.5.6
 */
public class StringUtils {

  /**
   * Checks whether the {@link String} {@code s} is {@code null} or not.
   *
   * @param s {@link String} to test.
   * @return {@code true} if is {@code null} or empty, {@code false} otherwise.
   */
  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
