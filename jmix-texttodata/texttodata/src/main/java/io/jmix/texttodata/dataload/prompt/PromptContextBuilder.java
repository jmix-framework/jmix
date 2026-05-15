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

package io.jmix.texttodata.dataload.prompt;

import io.jmix.texttodata.TextToDataProperties;
import io.jmix.texttodata.introspection.model.DatatypePropertyDescriptor;
import io.jmix.texttodata.introspection.model.EmbeddedPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumValueDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import io.jmix.texttodata.introspection.search.DomainModelSearchCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component("textdt_PromptContextBuilder")
public class PromptContextBuilder {

    private static final Logger log = LoggerFactory.getLogger(PromptContextBuilder.class);

    @Autowired
    protected TextToDataProperties textToDataProperties;

    @Autowired
    protected DomainModelRegistry domainModelRegistry;

    public String build(Collection<DomainModelSearchCandidate> candidates) {
        return build(candidates, textToDataProperties.getRelationExpansionDepth());
    }

    public String build(Collection<DomainModelSearchCandidate> candidates, int relationDepth) {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Map<String, EntityDescriptor> selectedEntities = new LinkedHashMap<>();
        for (DomainModelSearchCandidate candidate : candidates) {
            addEntityWithRelations(selectedEntities, candidate.getEntity(), relationDepth);
        }

        log.debug("Selected entities: {}", selectedEntities.keySet());

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (EntityDescriptor entityDescriptor : selectedEntities.values()) {
            if (!first) {
                builder.append(System.lineSeparator()).append(System.lineSeparator());
            }
            builder.append(formatEntity(entityDescriptor));
            first = false;
        }
        return builder.toString();
    }

    protected void addEntityWithRelations(Map<String, EntityDescriptor> selectedEntities,
                                          EntityDescriptor entityDescriptor,
                                          int relationDepth) {
        if (selectedEntities.putIfAbsent(entityDescriptor.getName(), entityDescriptor) != null) {
            return;
        }

        if (relationDepth <= 0) {
            return;
        }

        for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                EntityDescriptor targetEntity = domainModelRegistry.getEntityDescriptor(
                        relationPropertyDescriptor.getTargetEntityName());
                if (targetEntity != null) {
                    addEntityWithRelations(selectedEntities, targetEntity, relationDepth - 1);
                }
            }
        }
    }

    protected String formatEntity(EntityDescriptor entityDescriptor) {
        StringBuilder builder = new StringBuilder();
        builder.append("Entity ").append(entityDescriptor.getName());
        if (!entityDescriptor.getLocalizedNames().isEmpty()) {
            builder.append(" captions=").append(entityDescriptor.getLocalizedNames());
        }

        List<String> datatypes = new ArrayList<>();
        List<String> enums = new ArrayList<>();
        List<String> embeddeds = new ArrayList<>();
        List<String> relations = new ArrayList<>();

        for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
            if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                relations.add(formatRelationProperty(relationPropertyDescriptor));
            } else if (propertyDescriptor instanceof EnumPropertyDescriptor enumPropertyDescriptor) {
                enums.add(formatEnumProperty(enumPropertyDescriptor));
            } else if (propertyDescriptor instanceof EmbeddedPropertyDescriptor embeddedPropertyDescriptor) {
                embeddeds.add(formatEmbeddedProperty(embeddedPropertyDescriptor));
            } else if (propertyDescriptor instanceof DatatypePropertyDescriptor datatypePropertyDescriptor) {
                datatypes.add(formatDatatypeProperty(datatypePropertyDescriptor));
            }
        }

        appendSection(builder, "datatypes", datatypes);
        appendSection(builder, "enums", enums);
        appendSection(builder, "embedded", embeddeds);
        appendSection(builder, "relations", relations);

        return builder.toString();
    }

    protected void appendSection(StringBuilder builder, String title, List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        values.sort(Comparator.naturalOrder());
        builder.append(System.lineSeparator())
                .append(title)
                .append(": ")
                .append(String.join("; ", values));
    }

    protected String formatDatatypeProperty(DatatypePropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getName() + " " + propertyDescriptor.getJavaType() + " "
                + formatFlags(propertyDescriptor);
    }

    protected String formatEmbeddedProperty(EmbeddedPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getName() + " " + propertyDescriptor.getJavaType() + " "
                + formatFlags(propertyDescriptor);
    }

    protected String formatEnumProperty(EnumPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getName() + " enum[" + formatEnumValues(propertyDescriptor) + "] "
                + formatFlags(propertyDescriptor)
                + formatEnumStorageMode(propertyDescriptor);
    }

    protected String formatRelationProperty(RelationPropertyDescriptor propertyDescriptor) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(propertyDescriptor.getName());
        joiner.add("->");
        joiner.add(propertyDescriptor.getTargetEntityName());
        joiner.add(propertyDescriptor.getCardinality());
        joiner.add(formatFlags(propertyDescriptor));
        if (propertyDescriptor.getMappedBy() != null) {
            joiner.add("mappedBy=" + propertyDescriptor.getMappedBy());
        }
        return joiner.toString();
    }

    protected String formatEnumValues(EnumPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getEnumType().getConstants().values().stream()
                .sorted(Comparator.comparing(EnumValueDescriptor::getName))
                .map(enumValue -> enumValue.getName() + ":" + enumValue.getId())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    protected String formatFlags(EntityPropertyDescriptor propertyDescriptor) {
        List<String> flags = new ArrayList<>();
        if (Boolean.TRUE.equals(propertyDescriptor.getIdentifier())) {
            flags.add("id");
        }
        if (Boolean.TRUE.equals(propertyDescriptor.getPersistent())) {
            flags.add("persistent");
        } else {
            flags.add("transient");
        }
        if (Boolean.TRUE.equals(propertyDescriptor.getMandatory())) {
            flags.add("required");
        } else {
            flags.add("optional");
        }
        return String.join("|", flags);
    }

    protected String formatEnumStorageMode(EnumPropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getEnumStorageMode() != null
                ? " storage=" + propertyDescriptor.getEnumStorageMode()
                : "";
    }
}
