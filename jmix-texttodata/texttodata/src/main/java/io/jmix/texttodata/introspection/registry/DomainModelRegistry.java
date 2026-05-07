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

package io.jmix.texttodata.introspection.registry;

import io.jmix.texttodata.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("textdt_DomainModelRegistry")
public class DomainModelRegistry {

    @Autowired
    protected JpaDomainModelIntrospector domainModelIntrospector;

    protected Map<String, EntityDescriptor> entitiesByName = Map.of();
    protected Map<String, Map<String, EntityPropertyDescriptor>> propertiesByEntityName = Map.of();

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        Collection<EntityDescriptor> entityDescriptors = domainModelIntrospector.getEntityDescriptors();

        Map<String, EntityDescriptor> entitiesByName = new LinkedHashMap<>();
        Map<String, Map<String, EntityPropertyDescriptor>> propertiesByEntityName = new LinkedHashMap<>();

        for (EntityDescriptor entityDescriptor : entityDescriptors) {
            entitiesByName.put(entityDescriptor.getName(), entityDescriptor);
            propertiesByEntityName.put(entityDescriptor.getName(), indexProperties(entityDescriptor));
        }

        this.entitiesByName = Collections.unmodifiableMap(entitiesByName);
        this.propertiesByEntityName = Collections.unmodifiableMap(propertiesByEntityName);
    }

    public Collection<EntityDescriptor> getEntityDescriptors() {
        return entitiesByName.values();
    }

    public boolean containsEntity(String entityName) {
        return entitiesByName.containsKey(entityName);
    }

    @Nullable
    public EntityDescriptor getEntityDescriptor(String entityName) {
        return entitiesByName.get(entityName);
    }

    public boolean containsProperty(String entityName, String propertyName) {
        return getPropertyDescriptor(entityName, propertyName) != null;
    }

    @Nullable
    public EntityPropertyDescriptor getPropertyDescriptor(String entityName, String propertyName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return null;
        }
        return properties.get(propertyName);
    }

    public Collection<EntityPropertyDescriptor> getPropertyDescriptors(String entityName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return List.of();
        }
        return properties.values();
    }

    @Nullable
    public RelationPropertyDescriptor getRelationPropertyDescriptor(String entityName, String propertyName) {
        EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(entityName, propertyName);
        return propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor
                ? relationPropertyDescriptor
                : null;
    }

    public boolean containsPropertyPath(String entityName, String propertyPath) {
        return resolvePropertyPath(entityName, propertyPath) != null;
    }

    @Nullable
    public List<EntityPropertyDescriptor> resolvePropertyPath(String entityName, String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }

        String currentEntityName = entityName;
        List<EntityPropertyDescriptor> resolvedPath = new ArrayList<>();

        for (String segment : propertyPath.split("\\.")) {
            EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(currentEntityName, segment);
            if (propertyDescriptor == null) {
                return null;
            }

            resolvedPath.add(propertyDescriptor);

            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                currentEntityName = relationPropertyDescriptor.getTargetEntityName();
            } else if (!segment.equals(lastSegment(propertyPath))) {
                return null;
            }
        }

        return Collections.unmodifiableList(resolvedPath);
    }

    protected Map<String, EntityPropertyDescriptor> indexProperties(EntityDescriptor entityDescriptor) {
        return entityDescriptor.getProperties().stream()
                .collect(Collectors.toUnmodifiableMap(EntityPropertyDescriptor::getName, property -> property));
    }

    protected String lastSegment(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
