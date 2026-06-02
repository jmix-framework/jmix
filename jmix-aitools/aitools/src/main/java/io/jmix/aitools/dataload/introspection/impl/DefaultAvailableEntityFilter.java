/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.introspection.impl;

import io.jmix.aitools.dataload.introspection.AvailableEntityFilter;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.core.AccessManager;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("aitols_DefaultAvailableEntityFilter")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class DefaultAvailableEntityFilter implements AvailableEntityFilter {

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;

    @Override
    public List<EntityDescriptor> filter(List<EntityDescriptor> entityDescriptors) {
        if (entityDescriptors.isEmpty()) {
            return List.of();
        }

        List<EntityDescriptor> filtered = new ArrayList<>(entityDescriptors.size());
        for (EntityDescriptor entityDescriptor : entityDescriptors) {
            if (isPermitted(entityDescriptor.getName())) {
                filtered.add(entityDescriptor);
            }
        }

        return List.copyOf(filtered);
    }

    protected boolean isPermitted(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        if (metaClass == null) {
            return false;
        }

        CrudEntityContext context = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(context);
        return context.isReadPermitted();
    }
}
