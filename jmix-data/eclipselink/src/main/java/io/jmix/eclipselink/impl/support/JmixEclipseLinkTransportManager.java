/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink.impl.support;

import org.eclipse.persistence.exceptions.RemoteCommandManagerException;
import org.eclipse.persistence.internal.sessions.coordination.RemoteConnection;
import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
import org.eclipse.persistence.sessions.coordination.broadcast.BroadcastTransportManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("eclipselink_JmixEclipseLinkTransportManager")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixEclipseLinkTransportManager extends BroadcastTransportManager {

    @Autowired
    protected ObjectProvider<JmixEclipseLinkRemoteConnection> connectionProvider;

    public JmixEclipseLinkTransportManager(RemoteCommandManager rcm) {
        super(rcm);
    }

    protected JmixEclipseLinkRemoteConnection createConnection() throws RemoteCommandManagerException {
        try {
            return connectionProvider.getObject(this.rcm);
        } catch (Exception ex) {
            throw new RemoteCommandManagerException(ex.getMessage());
        }
    }

    @Override
    public void createConnections() {
        createLocalConnection();
        createExternalConnection();
    }

    @SuppressWarnings("unchecked")
    public void createExternalConnection() {
        synchronized (this.connectionsToExternalServices) {
            if (this.connectionsToExternalServices.isEmpty()) {
                try {
                    this.connectionsToExternalServices.put(this.rcm.getServiceId().getId(), this.localConnection);
                } catch (RemoteCommandManagerException rcmException) {
                    rcm.handleException(rcmException);
                }
            }
        }
    }

    @Override
    public synchronized void createLocalConnection() {
        if (this.localConnection == null) {
            try {
                this.localConnection = createConnection();
            } catch (RemoteCommandManagerException rcmException) {
                rcm.handleException(rcmException);
            }
        }
    }

    @Override
    public Map<String, RemoteConnection> getConnectionsToExternalServicesForCommandPropagation() {
        if (getConnectionsToExternalServices().isEmpty() && !this.rcm.isStopped()) {
            createExternalConnection();
        }
        return super.getConnectionsToExternalServicesForCommandPropagation();
    }

    @Override
    public void removeLocalConnection() {
    }

    @Override
    public void setConfig(String config) {
    }

    @Override
    public void setRemoteCommandManager(RemoteCommandManager rcm) {
        super.setRemoteCommandManager(rcm);
        rcm.setSerializer(null);
    }
}