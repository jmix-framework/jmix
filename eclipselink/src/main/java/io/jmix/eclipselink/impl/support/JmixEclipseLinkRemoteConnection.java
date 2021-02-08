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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.eclipselink.impl.entitycache.QueryCacheManager;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;
import org.eclipse.persistence.internal.sessions.coordination.broadcast.BroadcastRemoteConnection;
import org.eclipse.persistence.sessions.coordination.MergeChangeSetCommand;
import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component("eclipselink_JmixEclipseLinkRemoteConnection")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixEclipseLinkRemoteConnection extends BroadcastRemoteConnection {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected QueryCacheManager queryCacheManager;
    @Autowired(required = false)
    protected EclipseLinkChannelSupplier channelSupplier;
    @Autowired
    protected StandardSerialization serialization;

    public JmixEclipseLinkRemoteConnection(RemoteCommandManager rcm) {
        super(rcm);
    }

    @PostConstruct
    protected void init() {
        if (channelSupplier != null) {
            rcm.logDebug("creating_broadcast_connection", getInfo());
            try {
                channelSupplier.get().subscribe(this::onMessage);
                rcm.logDebug("broadcast_connection_created", getInfo());
            } catch (RuntimeException ex) {
                rcm.logDebug("failed_to_create_broadcast_connection", getInfo());
                close();
                throw ex;
            }
        }
    }

    public boolean isLocal() {
        return true;
    }

    @Override
    protected Object executeCommandInternal(Object command) {
        Object[] debugInfo = null;
        if (this.rcm.shouldLogDebugMessage()) {
            debugInfo = logDebugBeforePublish(null);
        }

        if (queryCacheManager.isEnabled()) {
            invalidateQueryCache(command);
        }

        if (channelSupplier != null) {
            Message<?> message = MessageBuilder.withPayload(serialization.serialize(command)).build();
            channelSupplier.get().send(message);
        }

        if (debugInfo != null) {
            logDebugAfterPublish(debugInfo, null);
        }

        return null;
    }

    public void onMessage(Message<?> message) {
        if (rcm.shouldLogDebugMessage()) {
            logDebugOnReceiveMessage(null);
        }

        Object command = serialization.deserialize((byte[]) message.getPayload());
        if (queryCacheManager.isEnabled()) {
            invalidateQueryCache(command);
        }
        processReceivedObject(command, "");
    }

    @Override
    protected boolean areAllResourcesFreedOnClose() {
        return !isLocal();
    }

    @Override
    protected void closeInternal() {
    }

    @Override
    protected void createDisplayString() {
        this.displayString = Helper.getShortClassName(this) + "[" + serviceId.toString() + "]";
    }

    @Override
    protected boolean shouldCheckServiceId() {
        return false;
    }

    protected void invalidateQueryCache(Object command) {
        if (command instanceof MergeChangeSetCommand) {
            MergeChangeSetCommand changeSetCommand = (MergeChangeSetCommand) command;
            UnitOfWorkChangeSet changeSet = changeSetCommand.getChangeSet(null);
            if (changeSet != null && changeSet.getAllChangeSets() != null) {
                Set<String> typeNames = new HashSet<>();
                changeSet.getAllChangeSets().values().stream().filter(obj -> obj.getClassName() != null).forEach(obj -> {
                    MetaClass metaClass = metadata.findClass(ReflectionHelper.getClass(obj.getClassName()));
                    if (metaClass != null) {
                        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                        typeNames.add(metaClass.getName());
                    }
                });
                queryCacheManager.invalidate(typeNames);
            }
        }
    }
}
