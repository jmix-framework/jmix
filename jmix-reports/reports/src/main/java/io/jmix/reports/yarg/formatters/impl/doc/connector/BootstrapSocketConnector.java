/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.doc.connector;

import io.jmix.reports.yarg.formatters.impl.doc.connector.BootstrapConnector;
import io.jmix.reports.yarg.formatters.impl.doc.connector.BootstrapException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OOServer;
import com.sun.star.uno.XComponentContext;

import java.util.function.Supplier;

/**
 * A Bootstrap Connector which uses a socket to connect to an OOo server.
 */
public class BootstrapSocketConnector extends BootstrapConnector {

    /**
     * Constructs a bootstrap socket connector which connects to the specified
     * OOo server.
     *
     * @param oooServer The OOo server
     */
    public BootstrapSocketConnector(OOServer oooServer, Supplier<Integer> connectionTimeoutSupplier) {
        super(oooServer, connectionTimeoutSupplier);
    }

    /**
     * Connects to an OOo server using the specified host and port for the
     * socket and returns a component context for using the connection to the
     * OOo server.
     *
     * @param host The host
     * @param port The port
     * @return The component context
     */
    public XComponentContext connect(String host, int port) throws BootstrapException {
        String unoConnectString = "uno:socket,host=" + host + ",port=" + port + ";urp;StarOffice.ComponentContext";
        return connect(unoConnectString);
    }
}