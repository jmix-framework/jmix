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

import io.jmix.core.MetadataTools;
import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.multitenancy.data.TenantRepository;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("mten_MultiTenancyAttributeConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultiTenancyAttributeConstraint implements EntityOperationConstraint<UiEntityAttributeContext> {

    private static final Logger log = LoggerFactory.getLogger(MultiTenancyAttributeConstraint.class);

    private final BeanFactory beanFactory;
    private final TenantRepository tenantRepository;
    private final MetadataTools metadataTools;

    public MultiTenancyAttributeConstraint(BeanFactory beanFactory,
                                           TenantRepository tenantRepository,
                                           MetadataTools metadataTools) {
        this.beanFactory = beanFactory;
        this.tenantRepository = tenantRepository;
        this.metadataTools = metadataTools;
    }

    @Override
    public Class<UiEntityAttributeContext> getContextType() {
        return UiEntityAttributeContext.class;
    }

    @Override
    public void applyTo(UiEntityAttributeContext context) {
        try {
            CurrentAuthentication authentication = beanFactory.getBean(CurrentAuthentication.class);
            if (authentication.getAuthentication() == null || authentication.getAuthentication().getDetails() == null) {
                return;
            }
            //TODO: compile
            //String tenantId = ((ClientDetails) authentication.getAuthentication().getDetails()).getTenantId();
            String tenantId = null;
            Tenant tenant = tenantRepository.findTenantById(tenantId);
            if (tenant == null) {
                return;
            }
            addHideTenantIdPermission(context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void addHideTenantIdPermission(UiEntityAttributeContext context) {
        String tenantIdPropertyName = null;
        //TODO: compile
        //String tenantIdPropertyName = metadataTools.findTenantIdProperty(context.getPropertyPath().getMetaClass().getJavaClass());
        if (context.getPropertyPath().getMetaProperty().getName().equals(tenantIdPropertyName)) {
            context.setViewDenied();
            context.setModifyDenied();
        }
    }
}
