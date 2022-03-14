/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.jpql.generator;

import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.QueryUtils;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("data_PropertyConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class PropertyConditionGenerator implements ConditionGenerator {

    protected MetadataTools metadataTools;
    protected Metadata metadata;

    @Autowired
    public PropertyConditionGenerator(MetadataTools metadataTools, Metadata metadata) {
        this.metadataTools = metadataTools;
        this.metadata = metadata;
    }

    @Override
    public boolean supports(ConditionGenerationContext context) {
        return context.getCondition() instanceof PropertyCondition;
    }

    @Override
    public String generateJoin(ConditionGenerationContext context) {
        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        if (propertyCondition == null||context.getEntityName()==null) {
            return "";
        }

        String propertyName = propertyCondition.getProperty();

        StringBuilder joinBuilder = new StringBuilder();
        StringBuilder joinPropertyBuilder = new StringBuilder(context.entityAlias);
        MetaClass metaClass = metadata.getClass(context.getEntityName());

        while (propertyName.contains(".")) {
            String basePropertyName = StringUtils.substringBefore(propertyName, ".");
            String childProperty = StringUtils.substringAfter(propertyName, ".");

            MetaProperty metaProperty = metaClass.getProperty(basePropertyName);

            if (metaProperty.getRange().getCardinality().isMany()) {
                String joinAlias = basePropertyName.substring(0, 3) + RandomStringUtils.randomAlphabetic(3);
                context.setJoinAlias(joinAlias);
                context.setJoinProperty(childProperty);
                context.setJoinMetaClass(metaProperty.getRange().asClass());
                joinBuilder.append(" join " + joinPropertyBuilder + "." + basePropertyName + " " + joinAlias);
                joinPropertyBuilder = new StringBuilder(joinAlias);
            } else {
                joinPropertyBuilder.append(".").append(basePropertyName);
            }
            if (metaProperty.getRange().isClass()) {
                metaClass = metaProperty.getRange().asClass();
            } else {
                break;
            }

            propertyName = childProperty;
        }

        return joinBuilder.toString();
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        if (propertyCondition == null) {
            return "";
        }

        if (context.getJoinAlias() != null && context.getJoinProperty() != null) {
            String property = getProperty(context.getJoinProperty(), context.getJoinMetaClass().getName());
            return generateWhere(propertyCondition, context.getJoinAlias(), property);
        } else {
            String entityAlias = context.getEntityAlias();
            String property = getProperty(propertyCondition.getProperty(), context.getEntityName());
            return generateWhere(propertyCondition, entityAlias, property);
        }

    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue,
                                         @Nullable String entityName) {
        PropertyCondition propertyCondition = (PropertyCondition) condition;
        if (propertyCondition == null || parameterValue == null) {
            return null;
        }

        if (parameterValue instanceof String) {
            switch (propertyCondition.getOperation()) {
                case PropertyCondition.Operation.CONTAINS:
                case PropertyCondition.Operation.NOT_CONTAINS:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + parameterValue + "%";
                case PropertyCondition.Operation.STARTS_WITH:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + parameterValue + "%";
                case PropertyCondition.Operation.ENDS_WITH:
                    return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + parameterValue;
            }
        } else if (EntityValues.isEntity(parameterValue)
                && isCrossDataStoreReference(propertyCondition.getProperty(), entityName)) {
            return EntityValues.getId(parameterValue);
        }
        return parameterValue;
    }

    protected String generateWhere(PropertyCondition propertyCondition, String entityAlias, String property) {
        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            return String.format("%s.%s %s",
                    entityAlias,
                    property,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition));
        } else if (PropertyConditionUtils.isInIntervalOperation(propertyCondition)) {
            return PropertyConditionUtils.getJpqlOperation(propertyCondition);
        } else {
            return String.format("%s.%s %s :%s",
                    entityAlias,
                    property,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition),
                    propertyCondition.getParameterName());
        }
    }

    protected String getProperty(String property, @Nullable String entityName) {
        if (Strings.isNullOrEmpty(entityName)
                || !isCrossDataStoreReference(property, entityName)) {
            return property;
        }

        MetaClass metaClass = metadata.getClass(entityName);
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);
        if (mpp == null) {
            return property;
        }

        String referenceIdProperty = metadataTools.getCrossDataStoreReferenceIdProperty(
                metaClass.getStore().getName(),
                mpp.getMetaProperty());

        //noinspection ConstantConditions
        return property.lastIndexOf(".") > 0
                ? property.substring(0, property.lastIndexOf(".") + 1) + referenceIdProperty
                : referenceIdProperty;
    }

    protected boolean isCrossDataStoreReference(String property, @Nullable String entityName) {
        if (Strings.isNullOrEmpty(entityName)) {
            return false;
        }

        MetaClass metaClass = metadata.getClass(entityName);
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);
        if (mpp == null) {
            return false;
        }

        return metadataTools.getCrossDataStoreReferenceIdProperty(
                metaClass.getStore().getName(), mpp.getMetaProperty()) != null;
    }
}
