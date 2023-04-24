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

package io.jmix.flowui.facet.queryparameters;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Objects;

@Component("flowui_FilterQueryParametersSupport")
public class FilterQueryParametersSupport {

    public static final String SEPARATOR = "_";

    protected DataManager dataManager;
    protected MetadataTools metadataTools;
    protected UrlParamSerializer urlParamSerializer;

    public FilterQueryParametersSupport(DataManager dataManager,
                                        MetadataTools metadataTools,
                                        UrlParamSerializer urlParamSerializer) {
        this.dataManager = dataManager;
        this.metadataTools = metadataTools;
        this.urlParamSerializer = urlParamSerializer;
    }

    public Object parseValue(MetaClass metaClass, String property,
                             Operation.Type operationType, String valueString) {
        MetaPropertyPath mpp = metadataTools.resolveMetaPropertyPath(metaClass, property);

        switch (operationType) {
            case UNARY:
                return urlParamSerializer.deserialize(Boolean.class, valueString);
            case VALUE:
                return parseSingleValue(property, valueString, mpp);
            case LIST:
            case INTERVAL:
                throw new UnsupportedOperationException("Not implemented yet");
            default:
                throw new IllegalArgumentException("Unknown operation type: " + operationType);
        }
    }

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

    public Object getSerializableValue(@Nullable Object value) {
        if (value == null) {
            return "";
        } else if (EntityValues.isEntity(value)) {
            Object id = EntityValues.getId(value);
            return id != null ? id : "";
        } else if (value instanceof Enum) {
            return replaceSeparatorValue(((Enum<?>) value).name());
        } else {
            return value;
        }
    }

    public String replaceSeparatorValue(String value) {
        return value.replace(SEPARATOR, "-");
    }

    public String restoreSeparatorValue(String value) {
        return value.replace("-", "_");
    }
}
