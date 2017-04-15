/*
 * Copyright (C) 2016 Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.kubeflix;

import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.init.TurbineInit;
import com.netflix.turbine.plugins.PluginsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicBoolean;

public class TurbineLifecycle implements SmartLifecycle {

    private static final Logger LOGGER  = LoggerFactory.getLogger(TurbineLifecycle.class);

    private final AtomicBoolean running = new AtomicBoolean();
    private final InstanceDiscovery instanceDiscovery;

    public TurbineLifecycle(InstanceDiscovery instanceDiscovery) {
        this.instanceDiscovery = instanceDiscovery;
    }


    @Override
    public void start() {
        LOGGER.info("Initializing Turbine");
        PluginsFactory.setInstanceDiscovery(instanceDiscovery);
        TurbineInit.init();
        running.set(true);
    }

    @Override
    public void stop() {
        LOGGER.info("Destroying Turbine");
        TurbineInit.stop();
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        callback.run();
    }
}
