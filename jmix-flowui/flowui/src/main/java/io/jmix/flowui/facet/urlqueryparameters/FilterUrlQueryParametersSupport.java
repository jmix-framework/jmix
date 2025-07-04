/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.urlqueryparameters;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Provides support for parsing and serializing URL query parameters for filtering data.
 */
@Component("flowui_FilterUrlQueryParametersSupport")
public class FilterUrlQueryParametersSupport {

    public static final String SEPARATOR = "_";

    protected DataManager dataManager;
    protected MetadataTools metadataTools;
    protected UrlParamSerializer urlParamSerializer;

    public FilterUrlQueryParametersSupport(DataManager dataManager,
                                           MetadataTools metadataTools,
                                           UrlParamSerializer urlParamSerializer) {
        this.dataManager = dataManager;
        this.metadataTools = metadataTools;
        this.urlParamSerializer = urlParamSerializer;
    }

    /**
     * Parses a provided value string according to the metadata of the given property and its operation type.
     *
     * @param metaClass the MetaClass instance representing the entity containing the property
     * @param property the name of the property to be processed
     * @param operationType the type of operation being used to process the property value
     * @param valueString the string representation of the value to be parsed
     * @return the parsed value for the provided property and operation type
     */
    public Object parseValue(MetaClass metaClass, String property,
                             Operation.Type operationType, String valueString) {
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPath(metaClass, property);

        return switch (operationType) {
            case UNARY -> urlParamSerializer.deserialize(Boolean.class, valueString);
            case VALUE -> parseSingleValue(property, valueString, mpp);
            case LIST -> parseCollectionValue(property, valueString, mpp);
            case INTERVAL -> urlParamSerializer.deserialize(BaseDateInterval.class, valueString);
        };
    }

    /**
     * Parses a provided string value based on the metadata of the specified property.
     * The parsing logic varies depending on the type of the property (datatype, enumeration, or class).
     *
     * @param property the name of the property to be parsed
     * @param valueString the string representation of the value to be parsed
     * @param mpp the MetaPropertyPath that describes the metadata and type details of the property
     * @return the parsed value according to the metadata of the specified property
     * @throws IllegalArgumentException if the entity corresponding to a class-based property is not found
     * @throws IllegalStateException if the property type is unsupported
     */
    public Object parseSingleValue(String property, String valueString, MetaPropertyPath mpp) {
        Range mppRange = mpp.getRange();
        if (mppRange.isDatatype()) {
            Class<?> type = mppRange.asDatatype().getJavaClass();
            return urlParamSerializer.deserialize(type, valueString);

        } else if (mppRange.isEnum()) {
            Class<?> type = mppRange.asEnumeration().getJavaClass();
            String enumString = restoreSeparatorValue(valueString);
            return urlParamSerializer.deserialize(type, enumString);

        } else if (mppRange.isClass()) {
            MetaClass propertyMetaClass = mppRange.asClass();
            MetaProperty idProperty = Objects.requireNonNull(metadataTools.getPrimaryKeyProperty(propertyMetaClass));
            Object idValue = urlParamSerializer.deserialize(idProperty.getJavaType(), valueString);

            return dataManager.load(Id.of(idValue, propertyMetaClass.getJavaClass()))
                    .optional().orElseThrow(() ->
                            new IllegalArgumentException(String.format("Entity with type '%s' and id '%s' isn't found",
                                    propertyMetaClass.getJavaClass(), idValue)));

        } else {
            throw new IllegalStateException("Unsupported property: " + property);
        }
    }

    /**
     * Parses a collection value string into a list of parsed objects based on the metadata
     * of the specified property. The input string is split by commas, and each value is individually
     * parsed using the provided property metadata.
     *
     * @param property the name of the property to which the collection value corresponds
     * @param collectionValueString the comma-separated string representation of the collection values
     * @param mpp the MetaPropertyPath that describes the metadata and type details of the property
     * @return a list of parsed objects corresponding to the provided collection value string
     */
    public Object parseCollectionValue(String property, String collectionValueString, MetaPropertyPath mpp) {
        return Arrays.stream(collectionValueString.split(","))
                .map(valueString -> parseSingleValue(property, valueString, mpp))
                .toList();
    }

    public Object getSerializableValue(@Nullable Object value) {
        if (value == null) {
            return "";
        } else if (EntityValues.isEntity(value)) {
            Object id = EntityValues.getId(value);
            return id != null ? id : "";
        } else if (value instanceof Enum) {
            return replaceSeparatorValue(((Enum<?>) value).name());
        } else if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::getSerializableValue).toList();
        }

        return value;
    }

    /**
     * Replaces occurrences of the predefined separator within the provided value
     * with a hyphen ("-").
     *
     * @param value the input string in which the separator needs to be replaced
     * @return a new string with the separator replaced by a hyphen
     */
    public String replaceSeparatorValue(String value) {
        return value.replace(SEPARATOR, "-");
    }

    /**
     * Restores a string by replacing occurrences of the hyphen ("-") character
     * with an underscore ("_").
     *
     * @param value the input string in which hyphens will be replaced with underscores
     * @return a new string with all hyphens replaced with underscores
     */
    public String restoreSeparatorValue(String value) {
        return value.replace("-", "_");
    }
}
