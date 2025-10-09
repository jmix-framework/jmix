/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.searching;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Decorates CurrentUserSecurityFacade and adds functionality that is necessary for the Search Engine.
 */
@Component("search_SearchSecurityDecorator")
public class SearchSecurityDecorator {

    protected final Metadata metadata;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;

    public SearchSecurityDecorator(Metadata metadata, SecureOperations secureOperations, PolicyStore policyStore) {
        this.metadata = metadata;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
    }

    /**
     * Gets a filtered list of the names of the entities that could be read with the current user.
     *
     * @param requestedEntities - a list of the entity names for the rights checking
     * @return a filtered list of the names of the entities that could be read with the current user
     */
    public List<String> resolveEntitiesAllowedToSearch(Collection<String> requestedEntities) {
        return requestedEntities.stream()
                .filter(entity -> {
                    MetaClass metaClass = metadata.getClass(entity);
                    return secureOperations.isEntityReadPermitted(metaClass, policyStore);
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if the current user has permission to read the entity attribute specified by the given meta-property path.
     *
     * @param metaPropertyPath - the property path for the rights checking
     * @return - true if the current user has the permission and false if not.
     */
    public boolean isEntityAttrReadPermitted(MetaPropertyPath metaPropertyPath) {
        return secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore);
    }

    /**
     * Checks if the current user has permission to read the entity that is represented with the MetaClass.
     *
     * @param metaClass - a MetaClass of the entity for the rights checking
     * @return - true if the current user has the permission and false if not.
     */
    public boolean isEntityReadPermitted(MetaClass metaClass) {
        return secureOperations.isEntityReadPermitted(metaClass, policyStore);
    }
}
