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

package io.jmix.reports.libintegration;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.util.converter.AbstractObjectToStringConverter;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.Nullable;
import java.text.ParseException;

public class JmixObjectToStringConverter extends AbstractObjectToStringConverter {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected MetadataTools metadataTools;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Nullable
    public String convertToString(Class parameterClass, @Nullable Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            Entity entity = (Entity) paramValue;
            return String.valueOf(entity.__getEntityEntry().getEntityId());
        } else {
            Datatype datatype = datatypeRegistry.find(parameterClass);
            if (datatype != null) {
                return datatype.format(paramValue);
            } else {
                return String.valueOf(paramValue);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Nullable
    public Object convertFromString(Class parameterClass, @Nullable String paramValueStr) {
        if (paramValueStr == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (Entity.class.isAssignableFrom(parameterClass)) {
            MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(parameterClass);
            if (idProperty == null) {
                return null;
            }

            Object idValue;

            if (idProperty.getRange().isDatatype()) {
                try {
                    idValue = idProperty.getRange().asDatatype().parse(paramValueStr);
                } catch (ParseException e) {
                    throw new ReportingException(
                            String.format("Couldn't read id from [%s] with value [%s] and datatype [%s].",
                                    parameterClass.getSimpleName(), paramValueStr, idProperty.getRange().asDatatype()));
                }

                if (idValue != null) {
                    return dataManager.load(parameterClass)
                            .id(Id.of(idValue, parameterClass))
                            .fetchPlan(FetchPlan.BASE)
                            .optional()
                            .orElse(null);
                }
            } else if (idProperty.getRange().isClass()) {
                throw new ReportingException(
                        String.format("Unsupported composite primary key in [%s] with value [%s]",
                                parameterClass.getSimpleName(), paramValueStr));
            }

            return null;
        } else {
            Datatype datatype = datatypeRegistry.find(parameterClass);
            if (datatype != null) {
                try {
                    return datatype.parse(paramValueStr);
                } catch (ParseException e) {
                    throw new ReportingException(
                            String.format("Couldn't read value [%s] with datatype [%s].",
                                    paramValueStr, datatype));
                }
            } else {
                return convertFromStringUnresolved(parameterClass, paramValueStr);
            }
        }
    }
}
