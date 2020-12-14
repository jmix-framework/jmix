/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.propertyfilter;

import com.google.common.collect.ImmutableList;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.PropertyFilter.Operation;
import io.jmix.ui.component.UiComponentsGenerator;
import io.jmix.ui.component.factory.PropertyFilterComponentGenerationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static io.jmix.ui.component.PropertyFilter.Operation.*;

@Internal
@Component("ui_PropertyFilterSupport")
public class PropertyFilterSupport {

    protected static final List<Class<?>> dateTimeClasses = ImmutableList.of(
            Date.class, java.sql.Date.class, LocalDate.class, LocalDateTime.class, OffsetDateTime.class);

    protected static final List<Class<?>> timeClasses = ImmutableList.of(
            java.sql.Time.class, LocalTime.class, OffsetTime.class);

    protected Messages messages;
    protected MessageTools messageTools;
    protected MetadataTools metadataTools;
    protected UiComponentsGenerator uiComponentsGenerator;

    public PropertyFilterSupport(Messages messages,
                                 MessageTools messageTools,
                                 MetadataTools metadataTools,
                                 UiComponentsGenerator uiComponentsGenerator) {
        this.messages = messages;
        this.messageTools = messageTools;
        this.metadataTools = metadataTools;
        this.uiComponentsGenerator = uiComponentsGenerator;
    }

    public String getOperationCaption(Operation operation) {
        return messages.getMessage("propertyFilter.Operation." + operation.name());
    }

    /**
     * Returns the prefix for id of {@link PropertyFilter}. This prefix used for internal
     * {@link PropertyFilter} components.
     *
     * @param id       an id of property filter
     * @param property a property
     * @return a prefix
     */
    public String getPropertyFilterPrefix(@Nullable String id, String property) {
        StringBuilder stringBuilder = new StringBuilder();
        if (id != null) {
            stringBuilder.append(id);
        } else {
            stringBuilder.append("propertyFilter_");
            stringBuilder.append(property);
        }
        stringBuilder.append("_");

        return stringBuilder.toString();
    }

    /**
     * Returns default caption for {@link PropertyFilter}.
     * <p>
     * Default caption consist of the related entity property caption and the operation caption (if the operation
     * caption is configured to be visible), e.g. "Last name contains".
     *
     * @param metaClass               an entity meta class associated with property filter
     * @param property                an entity attribute associated with property filter
     * @param operation               operation for which to show caption
     * @param operationCaptionVisible whether to show operation caption
     */
    public String getPropertyFilterCaption(MetaClass metaClass, String property,
                                           Operation operation, boolean operationCaptionVisible) {
        MetaPropertyPath mpp = metaClass.getPropertyPath(property);

        if (mpp == null) {
            return property;
        } else {
            MetaProperty[] metaProperties = mpp.getMetaProperties();
            StringBuilder sb = new StringBuilder();

            MetaPropertyPath parentMpp = null;
            MetaClass tempMetaClass;

            for (int i = 0; i < metaProperties.length; i++) {
                if (i == 0) {
                    parentMpp = new MetaPropertyPath(metaClass, metaProperties[i]);
                    tempMetaClass = metaClass;
                } else {
                    parentMpp = new MetaPropertyPath(parentMpp, metaProperties[i]);
                    tempMetaClass = metadataTools.getPropertyEnclosingMetaClass(parentMpp);
                }

                sb.append(messageTools.getPropertyCaption(tempMetaClass, metaProperties[i].getName()));
                if (i < metaProperties.length - 1) {
                    sb.append(".");
                }
            }
            if (operationCaptionVisible) {
                sb.append(" ").append(getOperationCaption(operation));
            }

            return sb.toString();
        }
    }

    public EnumSet<Operation> getAvailableOperations(MetaPropertyPath mpp) {
        Range mppRange = mpp.getRange();

        if (mppRange.isClass() || mppRange.isEnum()) {
            return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET); // TODO: add IN, NOT_IN,
        } else if (mppRange.isDatatype()) {
            Class<?> type = mppRange.asDatatype().getJavaClass();

            if (String.class.equals(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, CONTAINS, NOT_CONTAINS, IS_SET, STARTS_WITH, ENDS_WITH); // TODO: add IN, NOT_IN,
            } else if (dateTimeClasses.contains(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET);  // TODO: add IN, NOT_IN, DATE_INTERVAL
            } else if (timeClasses.contains(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET); // TODO: add DATE_INTERVAL
            } else if (Number.class.isAssignableFrom(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET); // TODO: add IN, NOT_IN,
            } else if (Boolean.class.equals(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET);
            } else if (UUID.class.equals(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET); // TODO: add IN, NOT_IN,
            }

        }

        throw new UnsupportedOperationException("Unsupported attribute type: " + mpp.getMetaProperty().getJavaType());
    }

    public EnumSet<Operation> getAvailableOperations(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = resolveMetaPropertyPath(metaClass, property);
        return getAvailableOperations(mpp);
    }

    public HasValue generateValueComponent(MetaClass metaClass,
                                           String property,
                                           Operation operation,
                                           @Nullable String ownerId) {
        ComponentGenerationContext context =
                new PropertyFilterComponentGenerationContext(metaClass, property, operation);
        context.setTargetClass(PropertyFilter.class);

        HasValue valueComponent = ((HasValue) uiComponentsGenerator.generate(context));
        valueComponent.setId(getPropertyFilterPrefix(ownerId, property) + "valueComponent");

        return ((HasValue) uiComponentsGenerator.generate(context));
    }

    public String toPropertyConditionOperation(Operation operation) {
        switch (operation) {
            case EQUAL:
                return PropertyCondition.Operation.EQUAL;
            case NOT_EQUAL:
                return PropertyCondition.Operation.NOT_EQUAL;
            case GREATER:
                return PropertyCondition.Operation.GREATER;
            case GREATER_OR_EQUAL:
                return PropertyCondition.Operation.GREATER_OR_EQUAL;
            case LESS:
                return PropertyCondition.Operation.LESS;
            case LESS_OR_EQUAL:
                return PropertyCondition.Operation.LESS_OR_EQUAL;
            case CONTAINS:
                return PropertyCondition.Operation.CONTAINS;
            case NOT_CONTAINS:
                return PropertyCondition.Operation.NOT_CONTAINS;
            case STARTS_WITH:
                return PropertyCondition.Operation.STARTS_WITH;
            case ENDS_WITH:
                return PropertyCondition.Operation.ENDS_WITH;
            case IS_SET:
                return PropertyCondition.Operation.IS_NOT_NULL;
            case IS_NOT_SET:
                return PropertyCondition.Operation.IS_NULL;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    public Operation fromPropertyConditionOperation(String conditionOperation) {
        switch (conditionOperation) {
            case PropertyCondition.Operation.EQUAL:
                return EQUAL;
            case PropertyCondition.Operation.NOT_EQUAL:
                return NOT_EQUAL;
            case PropertyCondition.Operation.GREATER:
                return GREATER;
            case PropertyCondition.Operation.GREATER_OR_EQUAL:
                return GREATER_OR_EQUAL;
            case PropertyCondition.Operation.LESS:
                return LESS;
            case PropertyCondition.Operation.LESS_OR_EQUAL:
                return LESS_OR_EQUAL;
            case PropertyCondition.Operation.CONTAINS:
                return CONTAINS;
            case PropertyCondition.Operation.NOT_CONTAINS:
                return NOT_CONTAINS;
            case PropertyCondition.Operation.STARTS_WITH:
                return STARTS_WITH;
            case PropertyCondition.Operation.ENDS_WITH:
                return ENDS_WITH;
            case PropertyCondition.Operation.IS_NOT_NULL:
                return IS_SET;
            case PropertyCondition.Operation.IS_NULL:
                return IS_NOT_SET;
            default:
                throw new IllegalArgumentException("Unknown condition operation: " + conditionOperation);
        }
    }

    public String getValueComponentName(HasValue<?> valueComponent) {
        try {
            return (String) valueComponent.getClass().getField("NAME").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("Class '%s' doesn't have NAME field",
                    valueComponent.getClass().getName()));
        }
    }

    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        return metaClass.getPropertyPath(property);
    }
}
