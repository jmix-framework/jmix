/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core.security.impl;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthenticationUserLoader;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("core_CurrentAuthenticationUserLoader")
public class CurrentAuthenticationUserLoaderImpl implements CurrentAuthenticationUserLoader {

    protected DataManager dataManager;

    protected Metadata metadata;

    protected MetadataTools metadataTools;

    protected CoreProperties coreProperties;

    protected EntityStates entityStates;

    public CurrentAuthenticationUserLoaderImpl(DataManager dataManager, Metadata metadata, MetadataTools metadataTools,
                                               CoreProperties coreProperties, EntityStates entityStates) {
        this.dataManager = dataManager;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.coreProperties = coreProperties;
        this.entityStates = entityStates;
    }

    public UserDetails reloadUser(UserDetails user) {
        if (shouldReloadUser(user)) {
            return dataManager.load(Id.of(user)).optional().orElse(user);
        }
        return user;
    }

    protected boolean shouldReloadUser(UserDetails user) {
        if (!coreProperties.isCurrentAuthenticationUserReloadEnabled()) return false;
        MetaClass metaClass = metadata.findClass(user.getClass());
        return metaClass != null && metadataTools.isJpaEntity(metaClass) && !entityStates.isNew(user);
    }

}
