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
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_CrudEntityConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CrudEntityConstraint implements EntityOperationConstraint<CrudEntityContext> {

    protected PolicyStore policyStore;
    protected SecureOperations secureOperations;

    @Autowired
    public void setPolicyStore(PolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Autowired
    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    @Override
    public Class<CrudEntityContext> getContextType() {
        return CrudEntityContext.class;
    }

    @Override
    public void applyTo(CrudEntityContext context) {
        if (!secureOperations.isEntityCreatePermitted(context.getEntityClass(), policyStore)) {
            context.setCreateDenied();
        }
        if (!secureOperations.isEntityReadPermitted(context.getEntityClass(), policyStore)) {
            context.setReadDenied();
        }
        if (!secureOperations.isEntityUpdatePermitted(context.getEntityClass(), policyStore)) {
            context.setUpdateDenied();
        }
        if (!secureOperations.isEntityDeletePermitted(context.getEntityClass(), policyStore)) {
            context.setDeleteDenied();
        }
    }
}
