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

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Class contains support functionality for {@link io.jmix.core.security.impl.CurrentAuthenticationImpl} and
 * {@link CurrentUserSubstitutionImpl}
 */
@Component("core_CurrentAuthenticationSupport")
public class CurrentAuthenticationSupport {

    protected Metadata metadata;

    protected MetadataTools metadataTools;

    protected DataManager dataManager;

    @Autowired
    public CurrentAuthenticationSupport(Metadata metadata, DataManager dataManager, MetadataTools metadataTools) {
        this.metadata = metadata;
        this.dataManager = dataManager;
        this.metadataTools = metadataTools;
    }

    /**
     * In some cases security context may contain a JPA entity instance where lazy loading is broken. To fix such cases
     * we reload user instance before returning it to the client.
     */
    public UserDetails reloadUser(UserDetails user) {
        if (shouldReloadUser(user)) {
            return dataManager.load(Id.of(user)).optional().orElse(user);
        }
        return user;
    }

    protected boolean shouldReloadUser(UserDetails user) {
        MetaClass metaClass = metadata.findClass(user.getClass());
        return metaClass != null && metadataTools.isJpaEntity(metaClass);
    }
}
