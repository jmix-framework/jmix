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

package io.jmix.aitools.dataload.repair;

import io.jmix.aitools.dataload.introspection.AvailableEntityService;
import io.jmix.aitools.dataload.introspection.model.EmbeddedPropertyDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntityDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntityPropertyDescriptor;
import io.jmix.aitools.dataload.introspection.model.EntitySummary;
import io.jmix.aitools.dataload.introspection.model.EnumPropertyDescriptor;
import io.jmix.aitools.dataload.introspection.model.RelationPropertyDescriptor;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jmix.aitools.dataload.validation.validator.RootEntityValidator.ROOT_ENTITY_UNKNOWN_CODE;
import static io.jmix.aitools.dataload.validation.validator.SelectedValuesValidator.SELECTED_ENTITY_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedEntitiesValidator.USED_ENTITY_UNKNOWN_CODE;
import static io.jmix.aitools.dataload.validation.validator.UsedPropertyPathsValidator.PROPERTY_PATH_INVALID_CODE;

/**
 * Describes the part of the data model that a repair needs to correct entity and attribute names in an invalid
 * query: the entities the query uses and the entities reached by the valid part of its paths, plus the names of
 * all available entities when the query names an unknown entity.
 * <p>
 * The schema is composed only for validation issues that are about names, and it goes through
 * {@link AvailableEntityService}, so it never describes an entity or attribute that domain-model discovery hides
 * from the current user.
 */
@Component("aitls_JpqlRepairSchemaComposer")
public class JpqlRepairSchemaComposer {

    @Autowired
    protected AvailableEntityService availableEntityService;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    /**
     * Composes a compact schema for the query draft of the repair request.
     *
     * @param request repair request with the query draft and its validation issues
     * @return schema text, or {@code null} if no issue of the request needs the data model or nothing can be
     * described
     */
    @Nullable
    public String compose(JpqlRepairRequest request) {
        Set<String> issueCodes = request.getValidationResult().getIssues().stream()
                .map(JpqlValidationIssue::getCode)
                .collect(Collectors.toSet());
        if (issueCodes.stream().noneMatch(getSchemaIssueCodes()::contains)) {
            return null;
        }

        StringBuilder schema = new StringBuilder();

        List<EntityDescriptor> entities = collectEntities(request.getGeneratedJpqlResult().getJpql());
        if (!entities.isEmpty()) {
            schema.append("Entities the query uses, with their attributes:");
            for (EntityDescriptor entity : entities) {
                schema.append('\n').append(formatEntity(entity));
            }
        }

        if (issueCodes.stream().anyMatch(getUnknownEntityIssueCodes()::contains)) {
            List<String> entityNames = availableEntityService.getEntitySummaries().stream()
                    .map(EntitySummary::getEntityName)
                    .toList();
            if (!entityNames.isEmpty()) {
                if (!schema.isEmpty()) {
                    schema.append('\n');
                }
                schema.append("All available entities: ").append(String.join(", ", entityNames));
            }
        }

        return schema.isEmpty() ? null : schema.toString();
    }

    /**
     * Returns the codes of the validation issues that need the schema to be repaired.
     *
     * @return issue codes
     */
    protected Set<String> getSchemaIssueCodes() {
        return Set.of(ROOT_ENTITY_UNKNOWN_CODE, USED_ENTITY_UNKNOWN_CODE, PROPERTY_PATH_INVALID_CODE,
                SELECTED_ENTITY_CODE);
    }

    /**
     * Returns the codes of the validation issues about an unknown entity, for which the schema also lists the
     * names of all available entities.
     *
     * @return issue codes
     */
    protected Set<String> getUnknownEntityIssueCodes() {
        return Set.of(ROOT_ENTITY_UNKNOWN_CODE, USED_ENTITY_UNKNOWN_CODE);
    }

    /**
     * Returns the available entities that the query declares or reaches through the valid part of its paths, in
     * the order they are met.
     *
     * @param jpql query draft
     * @return available entity descriptors, or an empty list if the query cannot be parsed
     */
    protected List<EntityDescriptor> collectEntities(String jpql) {
        QueryParser queryParser = JpqlValidatorSupport.getQueryParser(queryTransformerFactory, jpql);
        if (queryParser == null) {
            return List.of();
        }

        // Keyed by entity name, null for an entity that is unknown or not available to the current user.
        Map<String, @Nullable EntityDescriptor> entities = new LinkedHashMap<>();
        try {
            for (String entityName : queryParser.getAllEntityNames()) {
                findAvailableEntity(entityName, entities);
            }
        } catch (RuntimeException e) {
            // Not resolvable by the parser, for example because of an unknown entity: the paths are still read.
        }
        try {
            for (QueryParser.QueryPath queryPath : queryParser.getQueryPaths()) {
                if (!queryPath.getFullPath().equals(queryPath.getVariableName())) {
                    addEntitiesOnPath(queryPath.getEntityName(), queryPath.getPropertyPath(), entities);
                }
            }
        } catch (RuntimeException e) {
            // The paths cannot be read: the declared entities are described alone.
        }

        return entities.values().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Adds the entities reached by the valid part of a property path. The path is walked through the available
     * descriptors, so it stops at an attribute that is invalid or hidden from the current user.
     *
     * @param entityName   entity the path starts from
     * @param propertyPath dot-separated property path
     * @param entities     entities collected so far, updated in place
     */
    protected void addEntitiesOnPath(String entityName, String propertyPath,
                                     Map<String, @Nullable EntityDescriptor> entities) {
        EntityDescriptor current = findAvailableEntity(entityName, entities);
        for (String segment : propertyPath.split("\\.")) {
            if (current == null) {
                return;
            }
            String targetEntityName = findTargetEntityName(current, segment);
            if (targetEntityName == null) {
                return;
            }
            current = findAvailableEntity(targetEntityName, entities);
        }
    }

    @Nullable
    protected String findTargetEntityName(EntityDescriptor entity, String propertyName) {
        for (EntityPropertyDescriptor property : entity.getProperties()) {
            if (property.getName().equals(propertyName)) {
                if (property instanceof RelationPropertyDescriptor relation) {
                    return relation.getTargetEntityName();
                }
                // The Java type of an embedded property holds the name of the embeddable entity.
                return property instanceof EmbeddedPropertyDescriptor ? property.getJavaType() : null;
            }
        }
        return null;
    }

    @Nullable
    protected EntityDescriptor findAvailableEntity(String entityName,
                                                   Map<String, @Nullable EntityDescriptor> entities) {
        if (!entities.containsKey(entityName)) {
            List<EntityDescriptor> found = availableEntityService.findEntityDescriptorsByNames(List.of(entityName));
            entities.put(entityName, found.isEmpty() ? null : found.get(0));
        }
        return entities.get(entityName);
    }

    /**
     * Formats an entity as one line: its name followed by its attributes and their types.
     *
     * @param entity entity descriptor
     * @return formatted entity
     */
    protected String formatEntity(EntityDescriptor entity) {
        return entity.getName() + ": " + entity.getProperties().stream()
                .map(this::formatProperty)
                .collect(Collectors.joining("; "));
    }

    /**
     * Formats an attribute with its type: a reference names its target entity and cardinality, an embedded
     * attribute its embeddable entity, an enum attribute its enum.
     *
     * @param property attribute descriptor
     * @return formatted attribute
     */
    protected String formatProperty(EntityPropertyDescriptor property) {
        if (property instanceof RelationPropertyDescriptor relation) {
            return relation.getName() + " -> " + relation.getTargetEntityName()
                    + " (" + relation.getCardinality() + ")";
        }
        if (property instanceof EmbeddedPropertyDescriptor) {
            return property.getName() + " embedded " + property.getJavaType();
        }
        if (property instanceof EnumPropertyDescriptor enumProperty) {
            return property.getName() + " enum " + enumProperty.getEnumType().getName();
        }
        return property.getName() + " " + property.getJavaType();
    }
}
