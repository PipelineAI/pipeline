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
package ch.qos.logback.classic.gaffer;


import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.jmx.JMXConfigurator
import ch.qos.logback.classic.jmx.MBeanUtil
import ch.qos.logback.classic.joran.ReconfigureOnChangeTask;
import ch.qos.logback.classic.net.ReceiverBase
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.Appender
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.status.StatusListener
import ch.qos.logback.core.util.CachingDateFormatter
import ch.qos.logback.core.util.Duration
import ch.qos.logback.core.spi.LifeCycle
import ch.qos.logback.core.spi.ContextAware

import javax.management.MalformedObjectNameException
import javax.management.ObjectName

import java.lang.management.ManagementFactory
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Ceki G&uuml;c&uuml;
 */

public class ConfigurationDelegate extends ContextAwareBase {

    List<Appender> appenderList = [];

    Object getDeclaredOrigin() {
        return this;
    }

    void scan(String scanPeriodStr = null) {
        if (scanPeriodStr) {
            ReconfigureOnChangeTask rocTask = new ReconfigureOnChangeTask();
            rocTask.setContext(context);
            context.putObject(CoreConstants.RECONFIGURE_ON_CHANGE_TASK, rocTask);
            try {
                Duration duration = Duration.valueOf(scanPeriodStr);
                ScheduledExecutorService scheduledExecutorService = context.getScheduledExecutorService();

                ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(rocTask, duration.getMilliseconds(), duration.getMilliseconds(), TimeUnit.MILLISECONDS);
                context.addScheduledFuture(scheduledFuture);
                addInfo("Setting ReconfigureOnChangeTask scanning period to " + duration);
            } catch (NumberFormatException nfe) {
                addError("Error while converting [" + scanPeriodStr + "] to long", nfe);
            }
        }
    }

    void statusListener(Class listenerClass) {
        StatusListener statusListener = listenerClass.newInstance()
        context.statusManager.add(statusListener)
        if(statusListener instanceof ContextAware) {
            ((ContextAware) statusListener).setContext(context);
        }
        if(statusListener instanceof LifeCycle) {
            ((LifeCycle) statusListener).start();
        }
        addInfo("Added status listener of type [${listenerClass.canonicalName}]");
    }

    void conversionRule(String conversionWord, Class converterClass) {
        String converterClassName = converterClass.getName();

        Map<String, String> ruleRegistry = (Map) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
        if (ruleRegistry == null) {
            ruleRegistry = new HashMap<String, String>();
            context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
        }
        // put the new rule into the rule registry
        addInfo("registering conversion word " + conversionWord + " with class [" + converterClassName + "]");
        ruleRegistry.put(conversionWord, converterClassName);
    }

    void root(Level level, List<String> appenderNames = []) {
        if (level == null) {
            addError("Root logger cannot be set to level null");
        } else {
            logger(org.slf4j.Logger.ROOT_LOGGER_NAME, level, appenderNames);
        }
    }

    void logger(String name, Level level, List<String> appenderNames = [], Boolean additivity = null) {
        if (name) {
            Logger logger = ((LoggerContext) context).getLogger(name);
            addInfo("Setting level of logger [${name}] to " + level);
            logger.level = level;

            for (aName in appenderNames) {
                Appender appender = appenderList.find { it -> it.name == aName };
                if (appender != null) {
                    addInfo("Attaching appender named [${aName}] to " + logger);
                    logger.addAppender(appender);
                } else {
                    addError("Failed to find appender named [${aName}]");
                }
            }

            if (additivity != null) {
                logger.additive = additivity;
            }
        } else {
            addInfo("No name attribute for logger");
        }
    }

    void appender(String name, Class clazz, Closure closure = null) {
        addInfo("About to instantiate appender of type [" + clazz.name + "]");
        Appender appender = clazz.newInstance();
        addInfo("Naming appender as [" + name + "]");
        appender.name = name
        appender.context = context
        appenderList.add(appender)
        if (closure != null) {
            AppenderDelegate ad = new AppenderDelegate(appender, appenderList)
            copyContributions(ad, appender)
            ad.context = context;
            closure.delegate = ad;
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure();
        }
        try {
            appender.start()
        } catch (RuntimeException e) {
            addError("Failed to start apppender named [" + name + "]", e)
        }
    }

    void receiver(String name, Class aClass, Closure closure = null) {
        addInfo("About to instantiate receiver of type [" + clazz.name + "]");
        ReceiverBase receiver = aClass.newInstance();
        receiver.context = context;
        if(closure != null) {
            ComponentDelegate componentDelegate = new ComponentDelegate(receiver);
            componentDelegate.context = context;
            closure.delegate = componentDelegate;
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure();
        }
        try {
            receiver.start()
        } catch (RuntimeException e) {
            addError("Failed to start receiver of type [" + aClass.getName() + "]", e)
        }
    }

    private void copyContributions(AppenderDelegate appenderDelegate, Appender appender) {
        if (appender instanceof ConfigurationContributor) {
            ConfigurationContributor cc = (ConfigurationContributor) appender;
            cc.getMappings().each() { oldName, newName ->
                appenderDelegate.metaClass."${newName}" = appender.&"$oldName"
            }
        }
    }

    void turboFilter(Class clazz, Closure closure = null) {
        addInfo("About to instantiate turboFilter of type [" + clazz.name + "]");
        TurboFilter turboFilter = clazz.newInstance();
        turboFilter.context = context

        if (closure != null) {
            ComponentDelegate componentDelegate = new ComponentDelegate(turboFilter);
            componentDelegate.context = context;
            closure.delegate = componentDelegate;
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure();
        }
        turboFilter.start();
        addInfo("Adding aforementioned turbo filter to context");
        context.addTurboFilter(turboFilter)
    }

    String timestamp(String datePattern, long timeReference = -1) {
        long now = -1;

        if (timeReference == -1) {
            addInfo("Using current interpretation time, i.e. now, as time reference.");
            now = System.currentTimeMillis()
        } else {
            now = timeReference
            addInfo("Using " + now + " as time reference.");
        }
        CachingDateFormatter sdf = new CachingDateFormatter(datePattern);
        sdf.format(now)
    }

    /**
     * Creates and registers a {@link JMXConfigurator} with the platform MBean Server.
     * Allows specifying a custom context name to derive the used ObjectName, or a complete
     * ObjectName string representation to determine your own (the syntax automatically determines the intent).
     *
     * @param name custom context name or full ObjectName string representation (defaults to null)
     */
    void jmxConfigurator(String name = null) {
        def objectName = null
        def contextName = context.name
        if (name != null) {
            // check if this is a valid ObjectName
            try {
                objectName = new ObjectName(name)
            } catch (MalformedObjectNameException e) {
                contextName = name
            }
        }
        if (objectName == null) {
            def objectNameAsStr = MBeanUtil.getObjectNameFor(contextName, JMXConfigurator.class)
            objectName = MBeanUtil.string2ObjectName(context, this, objectNameAsStr)
            if (objectName == null) {
                addError("Failed to construct ObjectName for [${objectNameAsStr}]")
                return
            }
        }

        def platformMBeanServer = ManagementFactory.platformMBeanServer
        if (!MBeanUtil.isRegistered(platformMBeanServer, objectName)) {
            JMXConfigurator jmxConfigurator = new JMXConfigurator((LoggerContext) context, platformMBeanServer, objectName)
            try {
                platformMBeanServer.registerMBean(jmxConfigurator, objectName)
            } catch (all) {
                addError("Failed to create mbean", all)
            }
        }
    }

}
