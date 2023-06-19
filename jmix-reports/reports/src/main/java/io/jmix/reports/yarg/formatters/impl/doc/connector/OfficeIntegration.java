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

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.jmix.reports.yarg.exception.OpenOfficeException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class OfficeIntegration implements OfficeIntegrationAPI {
    protected volatile boolean platformDependProcessManagement = true;
    protected final ExecutorService executor;
    protected final BlockingQueue<OfficeConnection> connectionsQueue = new LinkedBlockingDeque<>();
    protected final Set<OfficeConnection> connections = new CopyOnWriteArraySet<>();

    protected String openOfficePath;
    protected String temporaryDirPath;
    protected Integer[] openOfficePorts;
    protected Integer timeoutInSeconds = DEFAULT_TIMEOUT;
    protected int connectionTimeoutSec = DEFAULT_CONNECTION_TIMEOUT;
    protected int countOfRetry = DEFAULT_RETRY_COUNT;
    protected int retryIntervalMs = DEFAULT_RETRY_INTERVAL;
    protected Boolean displayDeviceAvailable = false;

    public OfficeIntegration(String openOfficePath, Integer... ports) {
        this.openOfficePath = openOfficePath;
        this.openOfficePorts = ports;
        initConnections(ports);
        executor = createExecutor();
    }

    public void setTemporaryDirPath(String temporaryDirPath) {
        this.temporaryDirPath = temporaryDirPath;
    }

    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable) {
        this.displayDeviceAvailable = displayDeviceAvailable;
    }

    public void setCountOfRetry(int countOfRetry) {
        this.countOfRetry = countOfRetry;
    }

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(int retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public String getTemporaryDirPath() {
        return temporaryDirPath;
    }

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public int getConnectionTimeoutSec() {
        return connectionTimeoutSec;
    }

    public void setConnectionTimeoutSec(int connectionTimeoutSec) {
        this.connectionTimeoutSec = connectionTimeoutSec;
    }

    public Boolean isDisplayDeviceAvailable() {
        return displayDeviceAvailable;
    }

    @Override
    public void runTaskWithTimeout(final OfficeTask officeTask, int timeoutInSeconds) throws io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException {
        final OfficeConnection connection = acquireConnection();
        Future future = null;
        try {
            Callable<Void> task = () -> {
                connection.open();
                officeTask.processTaskInOpenOffice(connection.getOOResourceProvider());
                return null;
            };
            future = executor.submit(task);
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new ReportingInterruptedException("Open office task interrupted");
        } catch (ExecutionException ex) {
            connection.close();
            if (ex.getCause() instanceof io.jmix.reports.yarg.formatters.impl.doc.connector.BootstrapException
                    || ex.getCause() instanceof com.sun.star.comp.helper.BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new RuntimeException(ex.getCause());
        } catch (TimeoutException tex) {
            try {
                if (Thread.interrupted()) {
                    throw new ReportingInterruptedException("Open office task interrupted");
                }
            } finally {
                connection.close();
            }
            if (tex.getCause() instanceof io.jmix.reports.yarg.formatters.impl.doc.connector.BootstrapException
                    || tex.getCause() instanceof com.sun.star.comp.helper.BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, tex);
            }
            throw new OpenOfficeException(tex);
        } catch (Throwable ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException
                    || ex.getCause() instanceof com.sun.star.comp.helper.BootstrapException) {
                throw new OpenOfficeException("Failed to connect to open office. Please check open office path " + openOfficePath, ex);
            }
            throw new OpenOfficeException(ex);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            releaseConnection(connection);
        }
    }

    public int getCountOfRetry() {
        return countOfRetry;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public String getAvailablePorts() {
        List<Integer> ports = new ArrayList<>(connections.size());
        for (OfficeConnection officeConnection : connectionsQueue) {
            if (officeConnection.port != null) {
                ports.add(officeConnection.port);
            }
        }
        if (!ports.isEmpty()) {
            return Joiner.on(" ").join(ports);
        } else {
            return "No available ports";
        }
    }

    public void hardReloadAccessPorts() {
        for (OfficeConnection connection : connections) {
            connection.close();
        }

        connectionsQueue.clear();
        connectionsQueue.addAll(connections);
    }

    public boolean getPlatformDependProcessManagement() {
        return platformDependProcessManagement;
    }

    public void setPlatformDependProcessManagement(boolean platformDependProcessManagement) {
        this.platformDependProcessManagement = platformDependProcessManagement;
    }

    protected ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(connections.size(),
                new ThreadFactoryBuilder()
                        .setNameFormat("OfficeIntegration-%d")
                        .build());
    }

    protected OfficeConnection acquireConnection() throws io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException {
        final OfficeConnection connection = connectionsQueue.poll();
        if (connection != null) {
            return connection;
        } else {
            throw new NoFreePortsException("Couldn't get free port from pool");
        }
    }

    protected void releaseConnection(OfficeConnection officeConnection) {
        connectionsQueue.add(officeConnection);
    }

    protected void initConnections(Integer[] ports) {
        for (Integer port : ports) {
            connections.add(createConnection(port));
        }

        connectionsQueue.addAll(connections);
    }

    protected OfficeConnection createConnection(Integer port) {
        return new OfficeConnection(openOfficePath, port, resolveProcessManager(), this);
    }

    protected ProcessManager resolveProcessManager() {
        if (platformDependProcessManagement) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.startsWith("windows"))
                return new WinProcessManager();
            if (os.startsWith("linux"))
                return new LinuxProcessManager();
        }
        return new JavaProcessManager();
    }
}
