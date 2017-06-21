/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.gaffer

/**
 * @author Ceki G&uuml;c&uuml;
 */
public interface ConfigurationContributor {

  /**
   * The list of method mapping from the contributor into the configuration mechanism,
   * e.g. the ConfiguratorDelegate
   *
   * <p>The key in the map is the method being contributed and the value is the name of
   * the method in the target class.
   * @return
   */
  public Map<String, String> getMappings()

}
