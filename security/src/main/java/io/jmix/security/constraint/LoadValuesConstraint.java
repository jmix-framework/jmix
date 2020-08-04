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

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.PermissionType;
import io.jmix.data.impl.context.LoadValuesAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(LoadValuesConstraint.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoadValuesConstraint implements EntityOperationConstraint<LoadValuesAccessContext> {
    public static final String NAME = "sec_LoadValuesConstraint";

    protected SecureOperations secureOperations;
    protected ResourcePolicyStore policyStore;

    @Autowired
    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    @Autowired
    public void setPolicyStore(ResourcePolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<LoadValuesAccessContext> getContextType() {
        return LoadValuesAccessContext.class;
    }

    @Override
    public void applyTo(LoadValuesAccessContext context) {
        for (MetaClass entityClass : context.getEntityClasses()) {
            if (!secureOperations.isEntityReadPermitted(entityClass, policyStore)) {
                context.setDenied();
                return;
            }
        }

        for (MetaPropertyPath propertyPath : context.getAllPropertyPaths()) {
            if (!secureOperations.isEntityAttrReadPermitted(propertyPath, policyStore)) {
                throw new AccessDeniedException(PermissionType.ENTITY_ATTR, propertyPath.getMetaClass() + "." + propertyPath.toPathString());
            }
        }

        for (MetaPropertyPath propertyPath : context.getSelectedPropertyPaths()) {
            if (!secureOperations.isEntityAttrReadPermitted(propertyPath, policyStore)) {
                for (Integer index : context.getSelectedIndexes(propertyPath)) {
                    context.addDeniedSelectedIndex(index);
                }
            }
        }
    }
}
