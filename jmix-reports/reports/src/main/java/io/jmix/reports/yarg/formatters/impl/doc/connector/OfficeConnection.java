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

import io.jmix.reports.yarg.exception.OpenOfficeException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;

import java.util.concurrent.atomic.AtomicLong;

import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;

public class OfficeConnection {
    protected static AtomicLong bridgeIndex = new AtomicLong();

    protected String openOfficePath;
    protected io.jmix.reports.yarg.formatters.impl.doc.connector.OOServer oooServer;
    protected Integer port;
    protected io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegration officeIntegration;
    protected io.jmix.reports.yarg.formatters.impl.doc.connector.BootstrapSocketConnector bsc;

    protected volatile XComponentContext xComponentContext;
    protected volatile io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider officeResourceProvider;
    protected volatile boolean closed = true;


    public OfficeConnection(String openOfficePath, Integer port, ProcessManager processManager, OfficeIntegration officeIntegration) {
        this.port = port;
        this.officeIntegration = officeIntegration;
        this.oooServer = new io.jmix.reports.yarg.formatters.impl.doc.connector.OOServer(openOfficePath, OOServer.getDefaultOOoOptions(),
                "localhost", port, officeIntegration::getTemporaryDirPath, processManager);
        this.bsc = new BootstrapSocketConnector(oooServer, officeIntegration::getConnectionTimeoutSec);
        this.openOfficePath = openOfficePath;
    }

    public io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider getOOResourceProvider() {
        return officeResourceProvider;
    }

    public void open() throws OpenOfficeException {
        if (this.closed) {
            try {
                XComponentContext localContext = bsc.connect("127.0.0.1", port);
                String connectionString = "socket,host=127.0.0.1,port=" + port;
                XMultiComponentFactory localServiceManager = localContext.getServiceManager();
                XConnector connector = as(XConnector.class,
                        localServiceManager.createInstanceWithContext("com.sun.star.connection.Connector", localContext));
                XConnection connection = connector.connect(connectionString);
                XBridgeFactory bridgeFactory = as(XBridgeFactory.class,
                        localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext));
                String bridgeName = "yarg_" + bridgeIndex.incrementAndGet();
                XBridge bridge = bridgeFactory.createBridge(bridgeName, "urp", connection, null);
                XMultiComponentFactory serviceManager = as(XMultiComponentFactory.class, bridge.getInstance("StarOffice.ServiceManager"));
                XPropertySet properties = as(XPropertySet.class, serviceManager);
                xComponentContext = as(XComponentContext.class, properties.getPropertyValue("DefaultContext"));

                officeResourceProvider = new OfficeResourceProvider(xComponentContext, officeIntegration);
                closed = false;
            } catch (Exception e) {
                close();
                throw new OpenOfficeException("Unable to create Open office components.", e);
            }
        }
    }

    public void close() {
        bsc.disconnect();
        closed = true;
    }
}