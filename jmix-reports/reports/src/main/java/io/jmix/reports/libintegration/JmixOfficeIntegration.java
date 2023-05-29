/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.reports.libintegration;

import io.jmix.reports.yarg.exception.OpenOfficeException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeConnection;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegration;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.comp.helper.BootstrapException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

public class JmixOfficeIntegration extends OfficeIntegration {

    public JmixOfficeIntegration(String openOfficePath, Integer... ports) {
        super(openOfficePath, ports);
    }

    public JmixOfficeIntegration(String openOfficePath, List<Integer> ports) {
        super(openOfficePath, ports.toArray(new Integer[0]));
    }

    @Override
    public void runTaskWithTimeout(final OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException {
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final OfficeConnection connection = acquireConnection();
        Future future = null;
        try {
            Callable<Void> task = () -> {
                SecurityContextHolder.setContext(securityContext);
                connection.open();
                officeTask.processTaskInOpenOffice(connection.getOOResourceProvider());
                SecurityContextHolder.clearContext();
                return null;
            };
            future = executor.submit(task);
            future.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new ReportingInterruptedException("LibreOffice task interrupted");
        } catch (ExecutionException ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, ex);
            }

            if (ex.getCause() instanceof OpenOfficeException) {
                throw (OpenOfficeException) ex.getCause();
            }

            throw new RuntimeException(ex.getCause());
        } catch (OpenOfficeException ex) {
            connection.close();
            throw ex;
        } catch (TimeoutException tex) {
            try {
                if (Thread.interrupted()) {
                    throw new ReportingInterruptedException("LibreOffice task interrupted");
                }
            } finally {
                connection.close();
            }
            if (tex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, tex);
            }
            throw new OpenOfficeException(tex);
        } catch (Throwable ex) {
            connection.close();
            if (ex.getCause() instanceof BootstrapException) {
                throw new OpenOfficeException("Failed to connect to LibreOffice. Please check LibreOffice path " + openOfficePath, ex);
            }
            throw new OpenOfficeException(ex);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            releaseConnection(connection);
        }
    }

    @PreDestroy
    protected void destroyOfficeIntegration() {
        connectionsQueue.clear();
        for (OfficeConnection connection : connections) {
            try {
                connection.close();
            } catch (Exception e) {
                //Do nothing
            }
        }
        executor.shutdown();
    }
}