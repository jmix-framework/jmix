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
import io.jmix.core.accesscontext.ExportImportEntityContext;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_ImportEntityConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExportImportEntityConstraint implements EntityOperationConstraint<ExportImportEntityContext> {

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
    public Class<ExportImportEntityContext> getContextType() {
        return ExportImportEntityContext.class;
    }

    @Override
    public void applyTo(ExportImportEntityContext context) {
        for (MetaProperty metaProperty : context.getEntityClass().getProperties()) {
            if (!secureOperations.isEntityAttrUpdatePermitted(new MetaPropertyPath(context.getEntityClass(), metaProperty), policyStore)) {
                context.notImportedAttribute(metaProperty.getName());
            }
            if (!secureOperations.isEntityAttrReadPermitted(new MetaPropertyPath(context.getEntityClass(), metaProperty), policyStore)) {
                context.notExportedAttribute(metaProperty.getName());
            }
        }
    }
}
