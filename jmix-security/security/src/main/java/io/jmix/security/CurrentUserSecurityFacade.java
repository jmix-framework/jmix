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

package io.jmix.security;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.stereotype.Component;

/**
 * A facade that helps to get rights of the current user.
 */
@Component("sec_CurrentUserSecurityFacade")
public class CurrentUserSecurityFacade {

    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;

    public CurrentUserSecurityFacade(SecureOperations secureOperations, PolicyStore policyStore) {
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
    }

    /**
     * Checks if the current user has permission to read the entity attribute specified by the given {@link MetaPropertyPath}.
     *
     * @param metaPropertyPath - the property path for the rights checking
     * @return - true if the current user has the permission and false if not.
     */
    public boolean canAttributeBeRead(MetaPropertyPath metaPropertyPath) {
        return secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore);
    }

    /**
     * Checks if the current user has permission to read the entity that is represented with the {@link MetaClass}.
     *
     * @param metaClass - a MetaClass of the entity for the rights checking
     * @return - true if the current user has the permission and false if not.
     */
    public boolean canEntityBeRead(MetaClass metaClass) {
        return secureOperations.isEntityReadPermitted(metaClass, policyStore);
    }
}