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

package io.jmix.ui.component.jpqlfilter;

import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.datatype.impl.EnumerationImpl;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.component.JpqlFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.ParseException;

@Internal
@Component("ui_JpqlFilterSupport")
public class JpqlFilterSupport {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Returns the prefix for id of {@link JpqlFilter}. This prefix used for internal
     * {@link JpqlFilter} components.
     *
     * @param id an id of jpql filter
     * @return a prefix
     */
    public String getJpqlFilterPrefix(@Nullable String id) {
        StringBuilder stringBuilder = new StringBuilder();
        if (id != null) {
            stringBuilder.append(id);
        } else {
            stringBuilder.append("jpqlFilter");
        }
        stringBuilder.append("_");

        return stringBuilder.toString();
    }

    /**
     * Generates a parameter name
     *
     * @param id             a component id
     * @param parameterClass a parameter class
     * @return a parameter name
     */
    public String generateParameterName(@Nullable String id, @Nullable String parameterClass) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(id)) {
            sb.append(id);
        } else if (parameterClass != null) {
            sb.append(parameterClass);
        }

        sb.append(RandomStringUtils.randomAlphabetic(8));
        return sb.toString().replace(".", "_");
    }

    /**
     * Converts default value of value component to String
     *
     * @param parameterClass the component value type
     * @param value          a default value
     * @return string default value
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    public String formatDefaultValue(Class parameterClass, @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (Entity.class.isAssignableFrom(parameterClass)) {
            return String.valueOf(EntityValues.getId(value));
        } else if (Enumeration.class.isAssignableFrom(parameterClass)) {
            Enumeration<?> enumeration = new EnumerationImpl<>(parameterClass);
            return enumeration.format(value);
        } else if (datatypeRegistry.find(parameterClass) != null) {
            Datatype datatype = datatypeRegistry.get(parameterClass);
            return datatype.format(value);
        }

        return null;
    }

    /**
     * Parses default value for value component from String
     *
     * @param parameterClass the component value type
     * @param value          a string default value
     * @return default value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public Object parseDefaultValue(Class parameterClass, @Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            if (Entity.class.isAssignableFrom(parameterClass)) {
                MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(parameterClass);
                if (idProperty != null && idProperty.getRange().isDatatype()) {
                    Object idValue = idProperty.getRange().asDatatype().parse(value);
                    if (idValue != null) {
                        return dataManager.load(Id.of(idValue, parameterClass))
                                .one();
                    }
                }
            } else if (Enumeration.class.isAssignableFrom(parameterClass)) {
                Enumeration<?> enumeration = new EnumerationImpl<>(parameterClass);
                return enumeration.parse(value);
            } else if (datatypeRegistry.find(parameterClass) != null) {
                Datatype datatype = datatypeRegistry.get(parameterClass);
                return datatype.parse(value);
            }
        } catch (ParseException e) {
            return null;
        }

        return null;
    }
}
