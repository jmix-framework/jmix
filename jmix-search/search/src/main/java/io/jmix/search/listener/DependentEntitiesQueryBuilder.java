/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.listener;

import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DependentEntitiesQueryBuilder {

    private String entityName;
    private MetaPropertyPath propertyPath;
    private MetaClass targetMetaClass;
    private Id<?> targetEntityId;

    private int currentEntityIndex;
    private String currentEntityAlias;
    private StringBuilder currentPropertyPathSb;
    private StringBuilder querySb;
    private int propertiesLevels;
    private MetaProperty currentLevelProperty;
    private int currentLevelPropertyIndex;
    private String targetPrimaryKeyName;

    private Map<String, Object> parameters;
    private final MetadataTools metadataTools;

    DependentEntitiesQueryBuilder(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    protected DependentEntitiesQueryBuilder loadEntity(String entityName) {
        this.entityName = entityName;
        return this;
    }

    protected DependentEntitiesQueryBuilder byProperty(MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
        return this;
    }

    protected DependentEntitiesQueryBuilder dependedOn(MetaClass metaClass, Id<?> entityId) {
        this.targetMetaClass = metaClass;
        this.targetEntityId = entityId;
        return this;
    }

    protected DependentEntitiesQuery buildQuery() {
        if (!isDynamic(propertyPath)) {
            initQuery();
            processProperties();
        } else {
            initDynamicQuery();
            processDynamicProperty();
        }
        return new DependentEntitiesQuery(querySb.toString(), parameters);
    }

    private void initDynamicQuery() {
        parameters = new HashMap<>();
        currentEntityIndex = 1;
        currentEntityAlias = "e1";
        targetPrimaryKeyName = metadataTools.getPrimaryKeyName(targetMetaClass);
        initPropertyPathStringBuilderForCurrentEntity();
        //TODO
        querySb = new StringBuilder("select ")
                .append(currentEntityAlias)
                .append(" from ")
                .append(entityName)
                .append(' ')
                .append(currentEntityAlias)
                .append(" where exists (")
                .append("select r from dynat_CategoryAttributeValue r where ")
                //TODO
                .append("r.entityValue.entityId = :ref and ")
                //TODO
                .append("r.entity.entityId = ")
                .append(currentEntityAlias)
                .append(".")
                .append(targetPrimaryKeyName)
                .append(")");

    }

    private boolean isDynamic(MetaPropertyPath propertyPath) {
        //TODO think about
        return propertyPath.getFirstPropertyName().contains("+");
    }

    private void initQuery() {
        parameters = new HashMap<>();
        currentEntityIndex = 1;
        currentEntityAlias = "e1";
        initPropertyPathStringBuilderForCurrentEntity();
        querySb = new StringBuilder("select ")
                .append(currentEntityAlias)
                .append(" from ")
                .append(entityName)
                .append(' ')
                .append(currentEntityAlias);
    }

    private void processProperties() {
        targetPrimaryKeyName = metadataTools.getPrimaryKeyName(targetMetaClass);
        MetaProperty[] metaProperties = propertyPath.getMetaProperties();
        propertiesLevels = metaProperties.length;
        currentLevelPropertyIndex = 0;
        Stream.of(metaProperties).forEach(this::processPropertyLevel);
    }

    private void processDynamicProperty() {
        parameters.put("ref", targetEntityId.getValue());
/*        targetPrimaryKeyName = metadataTools.getPrimaryKeyName(targetMetaClass);
        MetaProperty[] metaProperties = propertyPath.getMetaProperties();
        propertiesLevels = metaProperties.length;
        currentLevelPropertyIndex = 0;
        Stream.of(metaProperties).forEach(this::processPropertyLevel);*/
    }

    private void processPropertyLevel(MetaProperty property) {
        currentLevelProperty = property;

        appendCurrentLevelProperty();
        if (isJoinRequired(property)) {
            joinWithNextEntity();
            initPropertyPathStringBuilderForCurrentEntity();
        }
        if (isLastLevelProperty()) {
            appendWhereBlock();
        }

        currentLevelPropertyIndex++;
    }

    private boolean isLastLevelProperty() {
        return currentLevelPropertyIndex == propertiesLevels - 1;
    }

    private boolean isJoinRequired(MetaProperty property) {
        boolean oneToMany = property.getAnnotatedElement().isAnnotationPresent(OneToMany.class);
        boolean manyToMany = property.getAnnotatedElement().isAnnotationPresent(ManyToMany.class);
        return oneToMany || manyToMany;
    }

    private void appendCurrentLevelProperty() {
        currentPropertyPathSb.append('.').append(currentLevelProperty.getName());
    }

    private void joinWithNextEntity() {
        currentEntityIndex++;
        currentEntityAlias = "e" + currentEntityIndex;
        querySb.append(" join ").append(currentPropertyPathSb).append(' ').append(currentEntityAlias);
    }

    private void initPropertyPathStringBuilderForCurrentEntity() {
        currentPropertyPathSb = new StringBuilder(currentEntityAlias);
    }

    private void appendWhereBlock() {
        querySb.append(" where ").append(currentPropertyPathSb).append('.').append(targetPrimaryKeyName).append(" = :ref");
        parameters.put("ref", targetEntityId.getValue());
    }
}
