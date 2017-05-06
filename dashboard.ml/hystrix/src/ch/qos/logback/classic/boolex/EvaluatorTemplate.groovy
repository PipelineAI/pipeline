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
package ch.qos.logback.classic.boolex

import ch.qos.logback.classic.spi.ILoggingEvent

import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static ch.qos.logback.classic.Level.ERROR;

// WARNING
// If this file is renamed, this should be reflected in
// logback-classic/pom.xml  resources section.

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class EvaluatorTemplate implements IEvaluator {

  boolean doEvaluate(ILoggingEvent event) {
    ILoggingEvent e = event;
    //EXPRESSION
  }


}
