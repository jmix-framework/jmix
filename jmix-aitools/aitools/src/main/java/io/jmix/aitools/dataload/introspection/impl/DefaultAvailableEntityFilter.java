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
import io.jmix.aitools.dataload.introspection.model.EntityPropertyDescriptor;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link AvailableEntityFilter} that keeps only entities the current user is allowed to read,
 * based on Jmix CRUD access constraints. Within each permitted entity, keeps only attributes the user
 * is allowed to view, based on entity attribute access constraints.
 */
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
            MetaClass metaClass = metadata.findClass(entityDescriptor.getName());
            if (metaClass != null && isReadPermitted(metaClass)) {
                filtered.add(filterProperties(entityDescriptor, metaClass));
            }
        }

        return List.copyOf(filtered);
    }

    protected boolean isReadPermitted(MetaClass metaClass) {
        CrudEntityContext context = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(context);
        return context.isReadPermitted();
    }

    protected EntityDescriptor filterProperties(EntityDescriptor entityDescriptor, MetaClass metaClass) {
        List<EntityPropertyDescriptor> properties = entityDescriptor.getProperties();
        List<EntityPropertyDescriptor> permitted = new ArrayList<>(properties.size());
        for (EntityPropertyDescriptor property : properties) {
            if (isViewPermitted(metaClass, property.getName())) {
                permitted.add(property);
            }
        }

        // Descriptors are shared cached instances, so a narrowed copy is created instead of mutation.
        if (permitted.size() == properties.size()) {
            return entityDescriptor;
        }
        return new EntityDescriptor(entityDescriptor.getName(), entityDescriptor.getLocalizedNames(),
                List.copyOf(permitted), entityDescriptor.getComment());
    }

    protected boolean isViewPermitted(MetaClass metaClass, String propertyName) {
        EntityAttributeContext context = new EntityAttributeContext(metaClass, propertyName);
        accessManager.applyRegisteredConstraints(context);
        return context.canView();
    }
}
