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

import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.NoConnectException;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * A bootstrap connector which establishes a connection to an OOo server.
 * <p>
 * Most of the source code in this class has been taken from the Java class
 * "Bootstrap.java" (Revision: 1.15) from the UDK projekt (Uno Software Develop-
 * ment Kit) from OpenOffice.org (http://udk.openoffice.org/). The source code
 * is available for example through a browser based online version control
 * access at http://udk.openoffice.org/source/browse/udk/. The Java class
 * "Bootstrap.java" is there available at
 * http://udk.openoffice.org/source/browse/udk/javaunohelper/com/sun/star/comp/helper/Bootstrap.java?view=markup
 * <p>
 * The idea to develop this BootstrapConnector comes from the blog "Getting
 * started with the OpenOffice.org API part III : starting OpenOffice.org with
 * jars not in the OOo install dir by Wouter van Reeven"
 * (http://technology.amis.nl/blog/?p=1284) and from various posts in the
 * "(Unofficial) OpenOffice.org Forum" at http://www.oooforum.org/ and the
 * "OpenOffice.org Community Forum" at http://user.services.openoffice.org/
 * complaining about "no office executable found!".
 */
public class BootstrapConnector {

    private static final Logger log = LoggerFactory.getLogger(BootstrapConnector.class);

    protected static final int CONNECTION_RETRY_INTERVAL = 500;
    protected Supplier<Integer> connectionTimeoutSupplier;
    /**
     * The OOo server.
     */
    private OOServer oooServer;
    /**
     * The connection string which has ben used to establish the connection.
     */
    private String oooConnectionString;

    /**
     * Constructs a bootstrap connector which connects to the specified
     * OOo server.
     *
     * @param oooServer The OOo server
     */
    public BootstrapConnector(OOServer oooServer, Supplier<Integer> connectionTimeoutSupplier) {
        this.oooServer = oooServer;
        this.oooConnectionString = null;
        this.connectionTimeoutSupplier = connectionTimeoutSupplier;
    }

    /**
     * Connects to an OOo server using the specified accept option and
     * connection string and returns a component context for using the
     * connection to the OOo server.
     * <p>
     * The accept option and the connection string should match to get a
     * connection. OOo provides to different types of connections:
     * 1) The socket connection
     * 2) The named pipe connection
     * <p>
     * To create a socket connection a host and port must be provided.
     * For example using the host "localhost" and the port "8100" the
     * accept option and connection string looks like this:
     * - accept option    : -accept=socket,host=localhost,port=8100;urp;
     * - connection string: uno:socket,host=localhost,port=8100;urp;StarOffice.ComponentContext
     * <p>
     * To create a named pipe a pipe name must be provided. For example using
     * the pipe name "oooPipe" the accept option and connection string looks
     * like this:
     * - accept option    : -accept=pipe,name=oooPipe;urp;
     * - connection string: uno:pipe,name=oooPipe;urp;StarOffice.ComponentContext
     *
     * @param oooConnectionString The connection string
     * @return The component context
     */
    public XComponentContext connect(String oooConnectionString) throws BootstrapException {

        this.oooConnectionString = oooConnectionString;

        XComponentContext xContext;
        try {
            // get local context
            XComponentContext xLocalContext = getLocalContext();

            oooServer.start();

            // initial service manager
            XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
            if (xLocalServiceManager == null)
                throw new BootstrapException("no initial service manager!");

            // create a URL resolver
            XUnoUrlResolver xUrlResolver = UnoUrlResolver.create(xLocalContext);

            long connectionTimeoutMillis = connectionTimeoutSupplier.get() * 1000;
            // wait until office is started but no longer than connectionTimeoutMillis
            long start = System.currentTimeMillis();
            for (; ; ) {
                try {
                    xContext = getRemoteContext(xUrlResolver);
                    break;
                } catch (NoConnectException ex) {
                    if (System.currentTimeMillis() - start < connectionTimeoutMillis) {
                        // Retry to connect after a short interval
                        Thread.sleep(CONNECTION_RETRY_INTERVAL);
                    } else {
                        throw new BootstrapException("Unable to connect to the OO process", ex);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BootstrapException(e);
        }
        return xContext;
    }

    /**
     * Disconnects from an OOo server using the connection string from the
     * previous connect.
     * <p>
     * If there has been no previous connect, the disconnects does nothing.
     * <p>
     * If there has been a previous connect, disconnect tries to terminate
     * the OOo server and kills the OOo server process the connect started.
     */
    public void disconnect() {

        if (oooConnectionString == null)
            return;

        // call office to terminate itself
        try {
            // get local context
            XComponentContext xLocalContext = getLocalContext();

            // create a URL resolver
            XUnoUrlResolver xUrlResolver = UnoUrlResolver.create(xLocalContext);

            // get remote context
            XComponentContext xRemoteContext = getRemoteContext(xUrlResolver);

            // get desktop to terminate office
            Object desktop = xRemoteContext.getServiceManager().createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xRemoteContext);
            XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
            xDesktop.terminate();
        } catch (Exception e) {
            log.error("Unable to terminate office");
        }

        oooServer.kill();
        oooConnectionString = null;
    }

    /**
     * Create default local component context.
     *
     * @return The default local component context
     */
    protected XComponentContext getLocalContext() throws Exception {

        XComponentContext xLocalContext = Bootstrap.createInitialComponentContext(null);
        if (xLocalContext == null) {
            throw new BootstrapException("no local component context!");
        }
        return xLocalContext;
    }

    /**
     * Try to connect to office.
     *
     * @return The remote component context
     */
    protected XComponentContext getRemoteContext(XUnoUrlResolver xUrlResolver) throws BootstrapException,
            ConnectionSetupException, IllegalArgumentException, NoConnectException {

        Object context = xUrlResolver.resolve(oooConnectionString);
        XComponentContext xContext = UnoRuntime.queryInterface(XComponentContext.class, context);
        if (xContext == null) {
            throw new BootstrapException("no component context!");
        }
        return xContext;
    }
}