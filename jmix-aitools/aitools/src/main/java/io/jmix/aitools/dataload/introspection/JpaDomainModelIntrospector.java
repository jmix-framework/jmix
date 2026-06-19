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
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.aitools.dataload.introspection.introspector.MetaPropertyIntrospector;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.dataload.introspection.model.RelationPropertyDescriptor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds and provides in-memory descriptors of JPA entities and their properties.
 */
public class JpaDomainModelIntrospector {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected AiToolsDataLoadProperties dataLoadProperties;
    @Autowired
    protected List<MetaPropertyIntrospector> propertyIntrospectors;

    protected volatile Map<String, EntityDescriptor> entitiesByName = Map.of();
    protected volatile Map<String, Map<String, EntityPropertyDescriptor>> propertiesByEntityName = Map.of();

    protected boolean initialized = false;

    @EventListener
    protected void onApplicationStarted(ContextRefreshedEvent event) {
        if (!initialized) {
            introspect();
        }
    }

    /**
     * Rebuilds entity and property descriptor indexes from current metadata, applying the inclusion and
     * exclusion rules from the application properties.
     * <p>
     * Each entity is checked against the rules below, in order; the first matching rule wins:
     * <ol>
     *     <li>listed in {@code excludeEntities} — excluded (takes precedence over everything else);</li>
     *     <li>listed in {@code includeEntities} or matching {@code includePackages} — included
     *     (overrides the system-level, DTO and {@code excludePackages} rules below);</li>
     *     <li>system-level entity while {@code excludeSystemLevelEntities} is enabled — excluded;</li>
     *     <li>not a JPA entity or embeddable (a DTO) — excluded;</li>
     *     <li>matching {@code excludePackages} — excluded;</li>
     *     <li>if {@code includeEntities} or {@code includePackages} is configured (allow-list mode) —
     *     included only when it matches one of them, otherwise excluded;</li>
     *     <li>otherwise — included by default.</li>
     * </ol>
     *
     * @see AiToolsDataLoadProperties#getExcludeEntities()
     * @see AiToolsDataLoadProperties#getExcludePackages()
     * @see AiToolsDataLoadProperties#getIncludeEntities()
     * @see AiToolsDataLoadProperties#getIncludePackages()
     * @see AiToolsDataLoadProperties#getExcludeSystemLevelEntities()
     */
    public void introspect() {
        Collection<MetaClass> classes = metadata.getClasses();
        Map<String, EntityDescriptor> entitiesMap = new HashMap<>(classes.size());
        Map<String, Map<String, EntityPropertyDescriptor>> propertiesMap = new HashMap<>();

        for (MetaClass metaClass : classes) {
            if (shouldInclude(metaClass)) {
                EntityDescriptor entityDescriptor = introspect(metaClass);
                entitiesMap.put(metaClass.getName(), entityDescriptor);
                propertiesMap.put(entityDescriptor.getName(), indexProperties(entityDescriptor));
            }
        }

        entitiesByName = Collections.unmodifiableMap(entitiesMap);
        propertiesByEntityName = Collections.unmodifiableMap(propertiesMap);

        initialized = true;
    }

    /**
     * Returns a descriptor for the given metaclass.
     *
     * @param metaClass entity metaclass
     * @return entity descriptor, or {@code null} if the entity is not indexed
     */
    @Nullable
    public EntityDescriptor getEntityDescriptor(MetaClass metaClass) {
        return getEntityDescriptor(metaClass.getName());
    }

    /**
     * Returns descriptor by entity name.
     *
     * @param entityName Jmix entity name
     * @return entity descriptor, or {@code null} if the entity is not indexed
     */
    @Nullable
    public EntityDescriptor getEntityDescriptor(String entityName) {
        return entitiesByName.get(entityName);
    }

    /**
     * Returns all indexed entity descriptors.
     *
     * @return collection of all indexed entity descriptors
     */
    public Collection<EntityDescriptor> getEntityDescriptors() {
        return entitiesByName.values();
    }

    /**
     * Checks whether the entity is indexed.
     *
     * @param entityName entity name
     * @return {@code true} if entity is indexed, {@code false} otherwise
     */
    public boolean containsEntity(String entityName) {
        return entitiesByName.containsKey(entityName);
    }

    /**
     * Checks whether the entity contains the given property.
     *
     * @param entityName   Jmix entity name
     * @param propertyName property name
     * @return {@code true} if entity contains given property, {@code false} otherwise
     */
    public boolean containsProperty(String entityName, String propertyName) {
        return getPropertyDescriptor(entityName, propertyName) != null;
    }

    /**
     * Returns property descriptor for an entity property.
     *
     * @param entityName   Jmix entity name
     * @param propertyName property name
     * @return property descriptor, or {@code null} if entity or property is not indexed
     */
    @Nullable
    public EntityPropertyDescriptor getPropertyDescriptor(String entityName, String propertyName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return null;
        }
        return properties.get(propertyName);
    }

    /**
     * Returns all property descriptors for the entity.
     *
     * @param entityName entity name
     * @return property descriptors, or empty collection if entity is not indexed
     */
    public Collection<EntityPropertyDescriptor> getPropertyDescriptors(String entityName) {
        Map<String, EntityPropertyDescriptor> properties = propertiesByEntityName.get(entityName);
        if (properties == null) {
            return List.of();
        }
        return properties.values();
    }

    /**
     * Returns relation descriptor for the given property if it is a relation.
     *
     * @param entityName   entity name
     * @param propertyName property name
     * @return relation descriptor, or {@code null} if property is missing or not a relation
     */
    @Nullable
    public RelationPropertyDescriptor getRelationPropertyDescriptor(String entityName, String propertyName) {
        EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(entityName, propertyName);
        return propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor
                ? relationPropertyDescriptor
                : null;
    }

    /**
     * Checks whether a dot-separated property path can be resolved for the entity.
     *
     * @param entityName   entity name
     * @param propertyPath dot-separated property path
     * @return {@code true} if the path can be resolved, {@code false} otherwise
     */
    public boolean containsPropertyPath(String entityName, String propertyPath) {
        return resolvePropertyPath(entityName, propertyPath) != null;
    }

    /**
     * Resolves a dot-separated property path to property descriptors.
     *
     * @param entityName   root entity name
     * @param propertyPath dot-separated property path
     * @return resolved descriptors in traversal order, or {@code null} if a path is invalid
     */
    @Nullable
    public List<EntityPropertyDescriptor> resolvePropertyPath(String entityName, String propertyPath) {
        if (propertyPath.isBlank()) {
            return null;
        }

        String currentEntityName = entityName;
        List<EntityPropertyDescriptor> resolvedPath = new ArrayList<>();

        String[] segments = propertyPath.split("\\.");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            EntityPropertyDescriptor propertyDescriptor = getPropertyDescriptor(currentEntityName, segment);
            if (propertyDescriptor == null) {
                return null;
            }

            resolvedPath.add(propertyDescriptor);

            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                currentEntityName = relationPropertyDescriptor.getTargetEntityName();
            } else if (i < segments.length - 1) {
                // A non-relation property (datatype/enum/embedded) may only appear as the last segment.
                return null;
            }
        }

        return Collections.unmodifiableList(resolvedPath);
    }

    /**
     * Creates a descriptor for a single entity.
     *
     * @param metaClass entity meta-class
     * @return entity descriptor with localized names and properties
     */
    public EntityDescriptor introspect(MetaClass metaClass) {
        String name = metaClass.getName();
        List<String> localizedNames = getEntityLocalizedNames(metaClass);

        List<EntityPropertyDescriptor> properties = introspectProperties(metaClass);

        String comment = metadataTools.getMetaAnnotationValue(metaClass, Comment.class);

        return new EntityDescriptor(name, localizedNames, properties, comment);
    }

    protected List<EntityPropertyDescriptor> introspectProperties(MetaClass metaClass) {
        List<EntityPropertyDescriptor> propertyDescriptors = new ArrayList<>();
        for (MetaProperty property : metaClass.getProperties()) {
            EntityPropertyDescriptor propertyDescriptor = introspectProperty(property);
            if (propertyDescriptor != null) {
                propertyDescriptors.add(propertyDescriptor);
            }
        }
        return propertyDescriptors;
    }

    @Nullable
    protected EntityPropertyDescriptor introspectProperty(MetaProperty property) {
        for (MetaPropertyIntrospector propertyIntrospector : propertyIntrospectors) {
            if (propertyIntrospector.supports(property)) {
                return propertyIntrospector.introspect(property);
            }
        }
        return null;
    }

    protected List<String> getEntityLocalizedNames(MetaClass metaClass) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messageTools.getEntityCaption(metaClass, locale);
            if (!isEntityCaptionFallback(metaClass, localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    protected boolean isEntityCaptionFallback(MetaClass metaClass, String localizedName) {
        return metaClass.getName().equals(localizedName)
                || metaClass.getJavaClass().getSimpleName().equals(localizedName);
    }

    protected boolean shouldInclude(MetaClass metaClass) {
        if (isExplicitlyExcluded(metaClass)) {
            return false;
        }

        if (isExplicitlyIncluded(metaClass)) {
            return true;
        }

        if (Boolean.TRUE.equals(dataLoadProperties.getExcludeSystemLevelEntities())
                && metadataTools.isSystemLevel(metaClass)) {
            return false;
        }

        if (isDtoEntity(metaClass)) {
            return false;
        }

        if (matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), dataLoadProperties.getExcludePackages())) {
            return false;
        }

        if (!dataLoadProperties.getIncludeEntities().isEmpty()
                || !dataLoadProperties.getIncludePackages().isEmpty()) {
            return matchesEntityName(metaClass, dataLoadProperties.getIncludeEntities())
                    || matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), dataLoadProperties.getIncludePackages());
        }

        return true;
    }

    protected boolean isExplicitlyIncluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, dataLoadProperties.getIncludeEntities())
                || matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(), dataLoadProperties.getIncludePackages());
    }

    protected boolean isExplicitlyExcluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, dataLoadProperties.getExcludeEntities());
    }

    protected boolean matchesEntityName(MetaClass metaClass, List<String> entityNames) {
        return entityNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .anyMatch(metaClass.getName()::equals);
    }

    protected boolean matchesAnyPackagePrefix(String packageName, List<String> packagePrefixes) {
        return packagePrefixes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(prefix -> !prefix.isEmpty())
                .anyMatch(prefix -> packageName.equals(prefix) || packageName.startsWith(prefix + "."));
    }

    protected boolean isDtoEntity(MetaClass metaClass) {
        return !metadataTools.isJpaEntity(metaClass) && !metadataTools.isJpaEmbeddable(metaClass);
    }

    protected Map<String, EntityPropertyDescriptor> indexProperties(EntityDescriptor entityDescriptor) {
        return entityDescriptor.getProperties().stream()
                .collect(Collectors.toUnmodifiableMap(EntityPropertyDescriptor::getName, property -> property));
    }
}
