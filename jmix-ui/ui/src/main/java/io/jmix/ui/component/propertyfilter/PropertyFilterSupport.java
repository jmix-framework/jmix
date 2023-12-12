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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.app.propertyfilter.dateinterval.DateIntervalUtils;
import io.jmix.ui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.PropertyFilter.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.ui.component.PropertyFilter.Operation.*;

@Internal
@Component("ui_PropertyFilterSupport")
public class PropertyFilterSupport {

    private static final Logger log = LoggerFactory.getLogger(PropertyFilterSupport.class);

    protected static final List<Class<?>> dateTimeClasses = ImmutableList.of(
            Date.class, java.sql.Date.class, LocalDate.class, LocalDateTime.class, OffsetDateTime.class);

    protected static final List<Class<?>> timeClasses = ImmutableList.of(
            java.sql.Time.class, LocalTime.class, OffsetTime.class);

    protected Messages messages;
    protected MessageTools messageTools;
    protected MetadataTools metadataTools;
    protected DataManager dataManager;
    protected DatatypeRegistry datatypeRegistry;
    protected DateIntervalUtils dateIntervalUtils;

    @Autowired
    public PropertyFilterSupport(Messages messages,
                                 MessageTools messageTools,
                                 MetadataTools metadataTools,
                                 DataManager dataManager,
                                 DatatypeRegistry datatypeRegistry,
                                 DateIntervalUtils dateIntervalUtils) {
        this.messages = messages;
        this.messageTools = messageTools;
        this.metadataTools = metadataTools;
        this.dataManager = dataManager;
        this.datatypeRegistry = datatypeRegistry;
        this.dateIntervalUtils = dateIntervalUtils;
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
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);

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

        if (mppRange.isEnum()) {
            return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET, IN_LIST, NOT_IN_LIST);
        } else if (mppRange.isClass()) {
            if (mppRange.getCardinality().isMany()) {
                return EnumSet.of(IS_EMPTY, IN_LIST, NOT_IN_LIST);
            } else {
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET, IN_LIST, NOT_IN_LIST);
            }
        } else if (mppRange.isDatatype()) {
            Class<?> type = mppRange.asDatatype().getJavaClass();

            if (String.class.equals(type)) {
                return EnumSet.of(CONTAINS, NOT_CONTAINS, EQUAL, NOT_EQUAL, IS_SET, STARTS_WITH, ENDS_WITH, IN_LIST,
                        NOT_IN_LIST);
            } else if (dateTimeClasses.contains(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET, IN_LIST,
                        NOT_IN_LIST, DATE_INTERVAL);
            } else if (timeClasses.contains(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET,
                        DATE_INTERVAL);
            } else if (Number.class.isAssignableFrom(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IS_SET, IN_LIST,
                        NOT_IN_LIST);
            } else if (Boolean.class.equals(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET);
            } else if (UUID.class.equals(type) || URI.class.equals(type)) {
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET, IN_LIST, NOT_IN_LIST);
            } else {
                log.warn("Cannot find predefined PropertyFilter operations for {} datatype. " +
                        "The default set of operations (EQUAL, NOT_EQUAL, IS_SET) will be used", type);
                return EnumSet.of(EQUAL, NOT_EQUAL, IS_SET);
            }
        }

        throw new UnsupportedOperationException("Unsupported attribute type: " + mpp.getMetaProperty().getJavaType());
    }

    public EnumSet<Operation> getAvailableOperations(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);

        if (mpp == null) {
            throw new UnsupportedOperationException("Unsupported attribute name: " + property);
        }

        return getAvailableOperations(mpp);
    }

    public Operation getDefaultOperation(MetaClass metaClass, String property) {
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPathOrNull(metaClass, property);

        if (mpp == null) {
            throw new UnsupportedOperationException("Unsupported attribute name: " + property);
        }

        if (isStringDatatype(mpp)) {
            return CONTAINS;
        } else if (isCollectionDatatype(mpp)) {
            return IS_EMPTY;
        } else {
            return EQUAL;
        }
    }

    protected boolean isStringDatatype(MetaPropertyPath mpp) {
        Range range = mpp.getMetaProperty().getRange();
        return range.isDatatype() && String.class.equals(range.asDatatype().getJavaClass());
    }

    protected boolean isCollectionDatatype(MetaPropertyPath mpp) {
        Range range = mpp.getMetaProperty().getRange();
        return range.isClass() && range.getCardinality().isMany();
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
                return PropertyCondition.Operation.IS_SET;
            case IN_LIST:
                return PropertyCondition.Operation.IN_LIST;
            case NOT_IN_LIST:
                return PropertyCondition.Operation.NOT_IN_LIST;
            case DATE_INTERVAL:
                return PropertyCondition.Operation.IN_INTERVAL;
            case IS_EMPTY:
                return PropertyCondition.Operation.IS_EMPTY;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    /**
     * Converts default value of value component to String
     *
     * @param metaProperty  an entity attribute associated with filter
     * @param operationType an operation type
     * @param value         a default value
     * @return string default value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public String formatDefaultValue(MetaProperty metaProperty, Type operationType, @Nullable Object value) {
        if (value == null) {
            return null;
        }

        Range range = metaProperty.getRange();
        if (operationType == Type.LIST && value instanceof Collection) {
            if (((Collection<?>) value).isEmpty()) {
                return null;
            }

            return Strings.emptyToNull((String) ((Collection) value).stream()
                    .map(singleValue -> formatSingleDefaultValue(range, singleValue))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(",")));
        } else if (operationType == Type.UNARY) {
            DatatypeRange booleanRange = new DatatypeRange(datatypeRegistry.get(Boolean.class));
            return formatSingleDefaultValue(booleanRange, value);
        } else if (operationType == Type.INTERVAL) {
            return dateIntervalUtils.formatDateInterval((BaseDateInterval) value);
        } else {
            return formatSingleDefaultValue(range, value);
        }
    }

    @Nullable
    protected String formatSingleDefaultValue(Range range, Object value) {
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
     * @param metaProperty  an entity attribute associated with filter
     * @param operationType an operation type
     * @param value         a string default value
     * @return default value
     */
    @Nullable
    public Object parseDefaultValue(MetaProperty metaProperty, Type operationType, @Nullable String value) {
        if (value == null) {
            return null;
        }

        Range range = metaProperty.getRange();
        if (operationType == Type.LIST) {
            return Stream.of(value.split(","))
                    .map(singleValue -> parseSingleDefaultValue(range, singleValue.trim()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else if (operationType == Type.UNARY) {
            DatatypeRange booleanRange = new DatatypeRange(datatypeRegistry.get(Boolean.class));
            return parseSingleDefaultValue(booleanRange, value);
        } else if (operationType == Type.INTERVAL) {
            return dateIntervalUtils.parseDateInterval(value);
        } else {
            return parseSingleDefaultValue(range, value);
        }
    }

    @Nullable
    protected Object parseSingleDefaultValue(Range range, String value) {
        try {
            if (range.isClass()) {
                MetaClass metaClass = range.asClass();
                MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(metaClass);
                if (idProperty != null && idProperty.getRange().isDatatype()) {
                    Object idValue = idProperty.getRange().asDatatype().parse(value);
                    if (idValue != null) {
                        return dataManager.load(Id.of(idValue, metaClass.getJavaClass())).optional()
                                .orElse(null);
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
}
