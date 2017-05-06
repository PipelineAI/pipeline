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
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.Appender
import ch.qos.logback.core.spi.AppenderAttachable;

/**
 * @author Ceki G&uuml;c&uuml;
 */
class AppenderDelegate extends ComponentDelegate {

  Map<String, Appender<?>> appendersByName = [:]

  AppenderDelegate(Appender appender) {
    super(appender)
  }

  AppenderDelegate(Appender appender, List<Appender<?>> appenders) {
    super(appender)
    appendersByName = appenders.collectEntries { [(it.name) : it]}
  }

  String getLabel() {
    "appender"
  }

  void appenderRef(String name){
    if (!AppenderAttachable.class.isAssignableFrom(component.class)) {
        def errorMessage= component.class.name + ' does not implement ' + AppenderAttachable.class.name + '.'
        throw new IllegalArgumentException(errorMessage)
    }
    component.addAppender(appendersByName[name])
  }
}
