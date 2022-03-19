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

package io.jmix.securitydata.constraint;

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.data.accesscontext.LoadValuesAccessContext;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_LoadValuesConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoadValuesConstraint implements EntityOperationConstraint<LoadValuesAccessContext> {

    protected SecureOperations secureOperations;
    protected PolicyStore policyStore;

    @Autowired
    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    @Autowired
    public void setPolicyStore(PolicyStore policyStore) {
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
                throw new AccessDeniedException("attribute", propertyPath.getMetaClass() + "." + propertyPath.toPathString());
            }
        }

        for (MetaPropertyPath propertyPath : context.getSelectedPropertyPaths()) {
            if (propertyPath == null) {
                return;
            }

            if (!secureOperations.isEntityAttrReadPermitted(propertyPath, policyStore)) {
                for (Integer index : context.getSelectedIndexes(propertyPath)) {
                    context.addDeniedSelectedIndex(index);
                }
            }
        }
    }
}
