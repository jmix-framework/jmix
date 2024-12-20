/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui;

import io.jmix.core.ClassManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.ParameterType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component("msgtmp_MessageParameterResolver")
public class MessageParameterResolver {

    protected Map<ParameterType, Class<?>> primitiveParameterTypeMap = Map.of(
            ParameterType.BOOLEAN, Boolean.class,
            ParameterType.DATE, LocalDate.class,
            ParameterType.DATETIME, LocalDateTime.class,
            ParameterType.TEXT, String.class,
            ParameterType.NUMERIC, Double.class,
            ParameterType.TIME, LocalTime.class
    );

    protected ClassManager classManager;
    protected Metadata metadata;
    protected DatatypeRegistry datatypeRegistry;

    public MessageParameterResolver(ClassManager classManager, Metadata metadata, DatatypeRegistry datatypeRegistry) {
        this.classManager = classManager;
        this.metadata = metadata;
        this.datatypeRegistry = datatypeRegistry;
    }

    @Nullable
    public Class<?> resolveClass(MessageTemplateParameter parameter) {
        ParameterType type = parameter.getType();
        Class<?> parameterClass = primitiveParameterTypeMap.get(type);

        if (parameterClass != null) {
            return parameterClass;
        }


        if (type == ParameterType.ENTITY || type == ParameterType.ENTITY_LIST) {
            MetaClass metaClass = metadata.findClass(parameter.getEntityMetaClass());
            return metaClass == null ? null : metaClass.getJavaClass();
        }

        if (type == ParameterType.ENUMERATION && StringUtils.isNotBlank(parameter.getEnumerationClass())) {
            return classManager.loadClass(parameter.getEnumerationClass());
        }

        return null;
    }

    @Nullable
    public Datatype<?> resolveDatatype(MessageTemplateParameter parameter) {
        Class<?> parameterClass = primitiveParameterTypeMap.get(parameter.getType());

        if (parameterClass != null) {
            return datatypeRegistry.get(parameterClass);
        }

        return null;
    }
}
