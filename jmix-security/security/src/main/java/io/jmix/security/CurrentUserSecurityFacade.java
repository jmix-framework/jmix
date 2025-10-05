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
 * TODO
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
     * TODO
     * @param metaPropertyPath
     * @return
     */
    public boolean canAttributeBeRead(MetaPropertyPath metaPropertyPath){
        return secureOperations.isEntityAttrReadPermitted(metaPropertyPath, policyStore);
    }

    /**
     * TODO
     * @param metaClass
     * @return
     */
    public boolean canEntityBeRead(MetaClass metaClass){
        return secureOperations.isEntityReadPermitted(metaClass, policyStore);
    }
}