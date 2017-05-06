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

import java.lang.reflect.Method
import ch.qos.logback.core.joran.util.beans.BeanUtil

/**
 * @author Ceki G&uuml;c&uuml;
 */
class PropertyUtil {

  static boolean hasAdderMethod(Object obj, String name) {
    String addMethod = "add${upperCaseFirstLetter(name)}";
    return obj.metaClass.respondsTo(obj, addMethod);
  }

  static NestingType nestingType(Object obj, String name) {
    def decapitalizedName = BeanUtil.INSTANCE.toLowerCamelCase(name);
    if (obj.hasProperty(decapitalizedName)) {
      return NestingType.SINGLE;
    }
    if (hasAdderMethod(obj, name)) {
      return NestingType.AS_COLLECTION;
    }
    return NestingType.NA;
  }

  static void attach(NestingType nestingType, Object component, Object subComponent, String name) {
    switch (nestingType) {
      case NestingType.SINGLE:
        name = BeanUtil.INSTANCE.toLowerCamelCase(name);
        component."${name}" = subComponent;
        break;
      case NestingType.AS_COLLECTION:
        String firstUpperName = PropertyUtil.upperCaseFirstLetter(name)
        component."add${firstUpperName}"(subComponent);
        break;
    }
  }

  static String transformFirstLetter(String s, Closure closure) {
    if (s == null || s.length() == 0)
      return s;

    String firstLetter = new String(s.getAt(0));

    String modifiedFistLetter = closure(firstLetter);

    if (s.length() == 1)
      return modifiedFistLetter
    else
      return modifiedFistLetter + s.substring(1);

  }

  static String upperCaseFirstLetter(String s) {
    return transformFirstLetter(s, {String it -> it.toUpperCase()})
  }

}
