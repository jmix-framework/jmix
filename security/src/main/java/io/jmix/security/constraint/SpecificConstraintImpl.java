/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.constraint;

import io.jmix.core.constraint.SpecificConstraint;
import io.jmix.core.context.SpecificOperationAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(SpecificConstraintImpl.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SpecificConstraintImpl implements SpecificConstraint<SpecificOperationAccessContext> {
    public static final String NAME = "sec_SpecificConstraintImpl";

    protected final Class<SpecificOperationAccessContext> contextClass;
    protected final String resourceName;

    protected SecureOperations secureOperations;
    protected ResourcePolicyStore policyStore;

    public SpecificConstraintImpl(Class<SpecificOperationAccessContext> contextClass, String resourceName) {
        this.contextClass = contextClass;
        this.resourceName = resourceName;
    }

    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    public void setPolicyStore(ResourcePolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<SpecificOperationAccessContext> getContextType() {
        return contextClass;
    }

    @Override
    public void applyTo(SpecificOperationAccessContext context) {
        if (secureOperations.isSpecificPermitted(resourceName, policyStore)) {
            context.setDenied();
        }
    }
}
