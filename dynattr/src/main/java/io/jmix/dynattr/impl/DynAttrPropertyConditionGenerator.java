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

package io.jmix.dynattr.impl;

import io.jmix.core.Entity;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.ReferenceToEntitySupport;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext;
import io.jmix.data.impl.jpql.generator.PropertyConditionGenerator;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.DynAttrUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Component("data_DynAttrPropertyConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE - 20)
public class DynAttrPropertyConditionGenerator extends PropertyConditionGenerator {

    protected ReferenceToEntitySupport referenceToEntitySupport;
    protected DynAttrMetadata dynAttrMetadata;
    protected MetadataTools metadataTools;
    protected Metadata metadata;

    @Autowired
    public DynAttrPropertyConditionGenerator(ReferenceToEntitySupport referenceToEntitySupport,
                                             DynAttrMetadata dynAttrMetadata,
                                             MetadataTools metadataTools,
                                             Metadata metadata) {
        this.referenceToEntitySupport = referenceToEntitySupport;
        this.dynAttrMetadata = dynAttrMetadata;
        this.metadataTools = metadataTools;
        this.metadata = metadata;
    }

    @Override
    public boolean supports(ConditionGenerationContext context) {
        if (!(context.getCondition() instanceof PropertyCondition)) {
            return false;
        }

        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        String[] properties = propertyCondition.getProperty().split("\\.");
        for (String property : properties) {
            if (DynAttrUtils.isDynamicAttributeProperty(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        PropertyCondition condition = (PropertyCondition) context.getCondition();
        if (condition == null) {
            return "";
        }

        String[] properties = condition.getProperty().split("\\.");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            if (DynAttrUtils.isDynamicAttributeProperty(property)) {
                String propertyPath = sb.toString();
                String dynAttrPropertyPath = i == 0 ? property : propertyPath.substring(1) + "." + property;

                String entityName = context.getEntityName();
                MetaClass entityMetaClass = metadata.findClass(entityName);
                if (entityMetaClass != null) {
                    MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(entityMetaClass, dynAttrPropertyPath);
                    if (mpp != null && mpp.getMetaProperty() instanceof DynAttrMetaProperty) {
                        return generateWhere(propertyPath, context, (DynAttrMetaProperty) mpp.getMetaProperty());
                    }
                }
            }

            sb.append(".");
            sb.append(property);
        }

        return "";
    }

    protected String generateWhere(String entityPropertyPath, ConditionGenerationContext context,
                                   DynAttrMetaProperty metaProperty) {
        PropertyCondition condition = (PropertyCondition) context.getCondition();
        if (condition == null) {
            return "";
        }

        String cavEntityId = referenceToEntitySupport.getReferenceIdPropertyName(metaProperty.getDomain());
        String cavAlias = "cav" + RandomStringUtils.randomAlphabetic(5);

        String parameterName = condition.getParameterName();
        String valueFieldName = getValueFieldName(metaProperty);
        String entityAlias = context.getEntityAlias();

        String attributeId = dynAttrMetadata.getAttributeByCode(metaProperty.getDomain(),
                DynAttrUtils.getAttributeCodeFromProperty(metaProperty.getName()))
                .map(AttributeDefinition::getId)
                .orElse("");
        String operation = PropertyConditionUtils.getJpqlOperation(condition);
        if (!PropertyConditionUtils.isUnaryOperation(condition)
                && !PropertyConditionUtils.isInIntervalOperation(condition)) {
            operation = operation + " :" + parameterName;
        }

        String formattedOperation = cavAlias + "." + valueFieldName + " " + operation;

        if (PropertyConditionUtils.isInIntervalOperation(condition)) {
            formattedOperation = new StringBuilder(operation)
                    .replace(operation.indexOf("{"), operation.indexOf(","), cavAlias + "." + valueFieldName)
                    .toString();
        }

        return "(exists (select " + cavAlias + " from dynat_CategoryAttributeValue " + cavAlias +
                " where " + cavAlias + ".entity." + cavEntityId + "=" + entityAlias + entityPropertyPath + ".id and "
                + formattedOperation + " and " + cavAlias + ".categoryAttribute.id='" + attributeId + "'))";
    }

    protected String getValueFieldName(MetaProperty metaProperty) {
        Class<?> javaClass = metaProperty.getJavaType();
        String valueFieldName = "stringValue";
        if (Entity.class.isAssignableFrom(javaClass)) {
            valueFieldName = "entityValue." + referenceToEntitySupport.getReferenceIdPropertyName(metaProperty.getRange().asClass());
        } else if (String.class.isAssignableFrom(javaClass)) {
            valueFieldName = "stringValue";
        } else if (Integer.class.isAssignableFrom(javaClass)) {
            valueFieldName = "intValue";
        } else if (Double.class.isAssignableFrom(javaClass)) {
            valueFieldName = "doubleValue";
        } else if (BigDecimal.class.isAssignableFrom(javaClass)) {
            valueFieldName = "decimalValue";
        } else if (Boolean.class.isAssignableFrom(javaClass)) {
            valueFieldName = "booleanValue";
        } else if (Date.class.isAssignableFrom(javaClass)) {
            valueFieldName = "dateValue";
        } else if (LocalDate.class.isAssignableFrom(javaClass)) {
            valueFieldName = "dateWithoutTimeValue";
        }

        return valueFieldName;
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue) {
        PropertyCondition propertyCondition = (PropertyCondition) condition;
        if (propertyCondition == null || parameterValue == null) {
            return null;
        }

        if (EntityValues.isEntity(parameterValue)) {
            return EntityValues.getIdOrEntity(parameterValue);
        } else {
            return super.generateParameterValue(condition, parameterValue);
        }
    }
}
