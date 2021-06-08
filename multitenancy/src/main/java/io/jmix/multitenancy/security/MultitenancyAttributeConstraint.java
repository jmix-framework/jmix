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

package io.jmix.multitenancy.security;

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.multitenancy.core.TenantEntityOperation;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("mten_MultitenancyAttributeConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultitenancyAttributeConstraint implements EntityOperationConstraint<UiEntityAttributeContext> {

    private final TenantProvider tenantProvider;
    private final TenantEntityOperation tenantEntityOperation;

    public MultitenancyAttributeConstraint(TenantProvider tenantProvider,
                                           TenantEntityOperation tenantEntityOperation) {
        this.tenantProvider = tenantProvider;
        this.tenantEntityOperation = tenantEntityOperation;
    }

    @Override
    public Class<UiEntityAttributeContext> getContextType() {
        return UiEntityAttributeContext.class;
    }

    @Override
    public void applyTo(UiEntityAttributeContext context) {
        if (TenantProvider.NO_TENANT.equals(tenantProvider.getCurrentUserTenantId())) {
            return;
        }
        addHideTenantIdPermission(context);
    }

    private void addHideTenantIdPermission(UiEntityAttributeContext context) {
        MetaProperty tenantMetaProperty = tenantEntityOperation.findTenantProperty(context.getPropertyPath().getMetaClass().getJavaClass());
        if (tenantMetaProperty != null && context.getPropertyPath().getMetaProperty().getName().equals(tenantMetaProperty.getName())) {
            context.setViewDenied();
            context.setModifyDenied();
        }
    }
}
