package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

/**
 * Simple server configurations.
 *
 * @author Kennedy Oliveira
 */
public final class Configuration {

  /**
   * Server port to listen to, the default is {@code 7979}.
   */
  public static final String SERVER_PORT = "serverPort";

  /**
   * Server address to bind, the default is {@code 0.0.0.0}
   */
  public static final String BIND_ADDRESS = "bindAddress";

  /**
   * Flag for disabling compression on the server, the default is {@link Boolean#FALSE}.
   */
  public static final String DISABLE_COMPRESSION = "disableCompression";

  /**
   * Utility class, no instances for you!
   */
  private Configuration() {}
}
