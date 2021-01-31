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
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.PropertyFilter.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.ParseException;
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
    protected DataManager dataManager;

    @Autowired
    public PropertyFilterSupport(Messages messages,
                                 MessageTools messageTools,
                                 MetadataTools metadataTools,
                                 DataManager dataManager) {
        this.messages = messages;
        this.messageTools = messageTools;
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
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
        StringBuilder sb = new StringBuilder(getPropertyFilterCaption(metaClass, property));

        if (operationCaptionVisible) {
            sb.append(" ").append(getOperationCaption(operation));
        }

        return sb.toString();
    }

    /**
     * Returns default caption for {@link PropertyFilter}.
     *
     * @param metaClass an entity meta class associated with property filter
     * @param property  an entity attribute associated with property filter
     */
    public String getPropertyFilterCaption(MetaClass metaClass, String property) {
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

        if (mpp == null) {
            throw new UnsupportedOperationException("Unsupported attribute name: " + property);
        }

        return getAvailableOperations(mpp);
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

    /**
     * Converts default value of value component to String
     *
     * @param metaProperty an entity attribute associated with filter
     * @param value        a default value
     * @return string default value
     */
    @Nullable
    public String formatDefaultValue(MetaProperty metaProperty, @Nullable Object value) {
        if (value == null) {
            return null;
        }

        Range range = metaProperty.getRange();

        if (range.isClass()) {
            return String.valueOf(EntityValues.getId(value));
        } else if (range.isEnum()) {
            return range.asEnumeration().format(value);
        } else if (range.isDatatype()) {
            return range.asDatatype().format(value);
        }

        return null;
    }

    /**
     * Parses default value for value component from String
     *
     * @param metaProperty an entity attribute associated with filter
     * @param value        a string default value
     * @return default value
     */
    @Nullable
    public Object parseDefaultValue(MetaProperty metaProperty, @Nullable String value) {
        if (value == null) {
            return null;
        }

        Range range = metaProperty.getRange();

        try {
            if (range.isClass()) {
                MetaClass metaClass = range.asClass();
                MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(metaClass);
                if (idProperty != null && idProperty.getRange().isDatatype()) {
                    Object idValue = idProperty.getRange().asDatatype().parse(value);
                    if (idValue != null) {
                        return dataManager.load(Id.of(idValue, metaClass.getJavaClass()))
                                .one();
                    }
                }
            } else if (range.isEnum()) {
                return range.asEnumeration().parse(value);
            } else if (range.isDatatype()) {
                return range.asDatatype().parse(value);
            }
        } catch (ParseException e) {
            return null;
        }

        return null;
    }

    @Nullable
    protected MetaPropertyPath resolveMetaPropertyPath(MetaClass metaClass, String property) {
        return metaClass.getPropertyPath(property);
    }
}
