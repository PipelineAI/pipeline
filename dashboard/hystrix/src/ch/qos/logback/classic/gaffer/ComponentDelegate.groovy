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

import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.spi.LifeCycle
import ch.qos.logback.core.spi.ContextAware
import ch.qos.logback.core.joran.spi.NoAutoStartUtil

/**
 * @author Ceki G&uuml;c&uuml;
 */
class ComponentDelegate extends ContextAwareBase {

  final Object component;

  final List fieldsToCascade = [];

  ComponentDelegate(Object component) {
    this.component = component;
  }

  String getLabel() { "component" }

  String getLabelFistLetterInUpperCase() { getLabel()[0].toUpperCase() + getLabel().substring(1) }

  void methodMissing(String name, def args) {
    NestingType nestingType = PropertyUtil.nestingType(component, name);
    if (nestingType == NestingType.NA) {
      addError("${getLabelFistLetterInUpperCase()} ${getComponentName()} of type [${component.getClass().canonicalName}] has no appplicable [${name}] property ")
      return;
    }

    String subComponentName
    Class clazz
    Closure closure
    (subComponentName, clazz, closure) = analyzeArgs(args)
    if (clazz != null) {
      Object subComponent = clazz.newInstance()
      if (subComponentName && subComponent.hasProperty(name)) {
        subComponent.name = subComponentName;
      }
      if (subComponent instanceof ContextAware) {
        subComponent.context = context;
      }
      if (closure) {
        ComponentDelegate subDelegate = new ComponentDelegate(subComponent)

        cascadeFields(subDelegate)
        subDelegate.context = context
        injectParent(subComponent)
        closure.delegate = subDelegate
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
      }
      if (subComponent instanceof LifeCycle && NoAutoStartUtil.notMarkedWithNoAutoStart(subComponent)) {
        subComponent.start();
      }
      PropertyUtil.attach(nestingType, component, subComponent, name)
    } else {
      addError("No 'class' argument specified for [${name}] in ${getLabel()} ${getComponentName()} of type [${component.getClass().canonicalName}]");
    }
  }

  void cascadeFields(ComponentDelegate subDelegate) {
    for (String k: fieldsToCascade) {
      subDelegate.metaClass."${k}" = this."${k}"
    }
  }

  void injectParent(Object subComponent) {
    if(subComponent.hasProperty("parent")) {
      subComponent.parent = component;
    }
  }

  void propertyMissing(String name, def value) {
    NestingType nestingType = PropertyUtil.nestingType(component, name);
    if (nestingType == NestingType.NA) {
      addError("${getLabelFistLetterInUpperCase()} ${getComponentName()} of type [${component.getClass().canonicalName}] has no appplicable [${name}] property ")
      return;
    }
    PropertyUtil.attach(nestingType, component, value, name)
  }


  def analyzeArgs(Object[] args) {
    String name;
    Class clazz;
    Closure closure;

    if (args.size() > 3) {
      addError("At most 3 arguments allowed but you passed $args")
      return [name, clazz, closure]
    }

    if (args[-1] instanceof Closure) {
      closure = args[-1]
      args -= args[-1]
    }

    if (args.size() == 1) {
      clazz = parseClassArgument(args[0])
    }

    if (args.size() == 2) {
      name = parseNameArgument(args[0])
      clazz = parseClassArgument(args[1])
    }

    return [name, clazz, closure]
  }

  Class parseClassArgument(arg) {
    if (arg instanceof Class) {
      return arg
    } else if (arg instanceof String) {
      return Class.forName(arg)
    } else {
      addError("Unexpected argument type ${arg.getClass().canonicalName}")
      return null;
    }
  }

  String parseNameArgument(arg) {
    if (arg instanceof String) {
      return arg
    } else {
      addError("With 2 or 3 arguments, the first argument must be the component name, i.e of type string")
      return null;
    }
  }

  String getComponentName() {
    if (component.hasProperty("name"))
      return "[${component.name}]"
    else
      return ""

  }
}