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

package io.jmix.aitools.dataload.introspection;

import io.jmix.aitools.AiToolsDataLoadProperties;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntitySummary;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides the subset of introspected domain-model entities that is currently available to the user.
 * <p>
 * Availability is resolved through an {@link AvailableEntityFilter}. By default, the filter checks
 * read access for entities and view access for their attributes using Jmix security constraints.
 * Applications may replace that behavior by registering a custom {@code AvailableEntityFilter} bean.
 * <p>
 * This service is intended for LLM-facing metadata discovery. It hides entities and attributes that
 * are not available to the current user before they are exposed through tool calls.
 */
@Component("aitls_AvailableEntityService")
public class AvailableEntityService {

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;
    @Autowired
    protected AvailableEntityFilter availableEntityFilter;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AiToolsDataLoadProperties dataLoadProperties;

    /**
     * Returns compact summaries for all entities available to the current user.
     * <p>
     * The result is filtered through the active {@link AvailableEntityFilter} and sorted by entity name.
     *
     * @return immutable list of available entity summaries, or an empty list if no entities are available
     */
    public List<EntitySummary> getEntitySummaries() {
        return getAvailableEntityDescriptors().stream()
                .sorted(Comparator.comparing(EntityDescriptor::getName))
                .map(this::toEntitySummary)
                .toList();
    }

    /**
     * Returns detailed descriptors for the requested entity names that are both known to the introspector
     * and available to the current user.
     * <p>
     * Unknown entity names are ignored. If all requested entities are unknown or filtered out by the
     * active {@link AvailableEntityFilter}, the method returns an empty list.
     *
     * @param entityNames entity names to resolve
     * @return immutable list of available entity descriptors for the requested names
     */
    public List<EntityDescriptor> findEntityDescriptorsByNames(@Nullable Collection<String> entityNames) {
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

        return hideSystemLevelAttributes(availableEntityFilter.filter(List.copyOf(entityDescriptors)));
    }

    protected List<EntityDescriptor> getAvailableEntityDescriptors() {
        return hideSystemLevelAttributes(
                availableEntityFilter.filter(List.copyOf(modelIntrospector.getEntityDescriptors())));
    }

    /**
     * Drops {@link io.jmix.core.entity.annotation.SystemLevel}-annotated attributes from the exposed
     * descriptors when {@code jmix.aitools.dataload.exclude-system-level-attributes} is enabled. The
     * attributes are only hidden from discovery: they remain in the introspected index, so a generated
     * query may still reference them.
     *
     * @param entityDescriptors descriptors to narrow
     * @return descriptors without their system-level attributes, or the same list when hiding is disabled
     */
    protected List<EntityDescriptor> hideSystemLevelAttributes(List<EntityDescriptor> entityDescriptors) {
        if (!Boolean.TRUE.equals(dataLoadProperties.getExcludeSystemLevelAttributes())) {
            return entityDescriptors;
        }
        return entityDescriptors.stream()
                .map(this::hideSystemLevelAttributes)
                .toList();
    }

    protected EntityDescriptor hideSystemLevelAttributes(EntityDescriptor entityDescriptor) {
        MetaClass metaClass = metadata.findClass(entityDescriptor.getName());
        if (metaClass == null) {
            return entityDescriptor;
        }

        List<EntityPropertyDescriptor> properties = entityDescriptor.getProperties();
        List<EntityPropertyDescriptor> exposed = new ArrayList<>(properties.size());
        for (EntityPropertyDescriptor property : properties) {
            if (!isSystemLevel(metaClass, property.getName())) {
                exposed.add(property);
            }
        }

        if (exposed.size() == properties.size()) {
            return entityDescriptor;
        }
        return new EntityDescriptor(entityDescriptor.getName(), entityDescriptor.getLocalizedNames(),
                List.copyOf(exposed), entityDescriptor.getComment());
    }

    protected boolean isSystemLevel(MetaClass metaClass, String propertyName) {
        MetaProperty metaProperty = metaClass.findProperty(propertyName);
        return metaProperty != null && metadataTools.isSystemLevel(metaProperty);
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
