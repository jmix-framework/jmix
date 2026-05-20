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

package io.jmix.aitools.introspection;

import io.jmix.aitools.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.aitools.introspection.model.EntityDescriptor;
import io.jmix.aitools.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.introspection.model.EntitySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("aitols_AvailableEntityService")
public class AvailableEntityService {

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;

    protected List<EntitySummary> entitySummaries;

    public List<EntitySummary> getEntitySummaries() {
        if (entitySummaries == null) {
            entitySummaries = modelIntrospector.getEntityDescriptors().stream()
                    .sorted(Comparator.comparing(EntityDescriptor::getName))
                    .map(this::toEntitySummary)
                    .toList();
        }
        return entitySummaries;
    }

    public List<EntityDescriptor> findEntityDescriptorsByNames(Collection<String> entityNames) {
        if (entityNames == null || entityNames.isEmpty()) {
            return List.of();
        }

        List<EntityDescriptor> entityDescriptors = new ArrayList<>();
        for (String entityName : entityNames) {
            EntityDescriptor entityDescriptor = modelIntrospector.getEntityDescriptor(entityName);
            if (entityDescriptor != null) {
                entityDescriptors.add(entityDescriptor);
            }
        }

        return List.copyOf(entityDescriptors);
    }

    protected EntitySummary toEntitySummary(EntityDescriptor entityDescriptor) {
        List<String> propertyNames = new ArrayList<>();
        Set<String> propertyLocalizedNames = new LinkedHashSet<>();

        for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
            propertyNames.add(propertyDescriptor.getName());
            propertyLocalizedNames.addAll(propertyDescriptor.getLocalizedNames());
        }

        return new EntitySummary(
                entityDescriptor.getName(),
                entityDescriptor.getLocalizedNames(),
                propertyNames,
                propertyLocalizedNames.stream().toList()
        );
    }
}
