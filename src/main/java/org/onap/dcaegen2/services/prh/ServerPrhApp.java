/*-
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.prh;

import java.util.logging.Level;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Logger;

public class ServerPrhApp {

    private static int port = 8080;
    private static Logger logger = Logger.getLogger(ServerPrhApp.class.getName());

    public static void main(String... args) {
        logger.info("Starting PRH Application Service...");
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        Server jettyServer = new Server(port);
        jettyServer.setHandler(contextHandler);

        ServletHolder jerseyServlet = contextHandler
            .addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", "org.onap.dcaegen2.services.prh");

        try {

            jettyServer.start();
            logger.info("Server jetty running on port: " + port);
            jettyServer.join();
        } catch (Exception ex) {
            logger.log(Level.ALL, "Error occurred while starting Jetty", ex);
            System.exit(1);
        } finally {
            jettyServer.destroy();
        }

    }

    private static void parseSettingsFileFromArgs() {
        //TODO: Definition of for riding file configuration
    }
}