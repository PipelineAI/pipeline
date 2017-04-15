/*
 * Copyright (C) 2015 Red Hat, Inc
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
 */

package io.fabric8.kubeflix;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DiscoveryFeedbackServlet extends HttpServlet {

    private final InstanceDiscovery instanceDiscovery;

    public DiscoveryFeedbackServlet(InstanceDiscovery instanceDiscovery) {
        this.instanceDiscovery = instanceDiscovery;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set response content type
        resp.setContentType("text/html");

        String suffix = DynamicPropertyFactory.getInstance().getStringProperty("turbine.instanceUrlSuffix", "").getValue();

        // Actual logic goes here.
        PrintWriter out = resp.getWriter();
        out.println("<h1>Hystrix Endpoints:</h1>");
        try {
            for (Instance instance : instanceDiscovery.getInstanceList()) {
                out.println("<h3>http://" + instance.getHostname() + suffix + " " + instance.getCluster() + ":" + instance.isUp() + "</h3>");
            }
        } catch (Throwable t) {
            t.printStackTrace(out);
        }
        out.flush();
    }

}
