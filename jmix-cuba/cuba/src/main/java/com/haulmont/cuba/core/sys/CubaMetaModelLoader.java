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

package com.haulmont.cuba.core.sys;

import com.google.common.collect.ImmutableList;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.entity.HasUuid;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;
import io.jmix.core.Stores;
import com.haulmont.cuba.core.entity.Versioned;
import io.jmix.core.impl.MetaModelLoader;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class CubaMetaModelLoader extends MetaModelLoader {

    protected static final List<Class> LEGACY_SYSTEM_INTERFACES = ImmutableList.of(
            Creatable.class,
            Updatable.class,
            SoftDelete.class,
            HasUuid.class,
            Versioned.class
    );


    public CubaMetaModelLoader(DatatypeRegistry datatypes, Stores stores, FormatStringsRegistry formatStringsRegistry) {
        super(datatypes, stores, formatStringsRegistry);
    }

    @Override
    protected boolean isSystem(Field field, MetaProperty metaProperty) {
        if (propertyBelongsTo(field, metaProperty, LEGACY_SYSTEM_INTERFACES)) {
            return true;
        }
        return super.isSystem(field, metaProperty);
    }

    protected boolean propertyBelongsTo(Field field, MetaProperty metaProperty, List<Class> systemInterfaces) {
        String getterName = "get" + StringUtils.capitalize(metaProperty.getName());

        Class<?> aClass = field.getDeclaringClass();
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(aClass);
        for (Class intf : allInterfaces) {
            Method[] methods = intf.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(getterName) && method.getParameterTypes().length == 0) {
                    if (systemInterfaces.contains(intf))
                        return true;
                }
            }
        }
        return false;
    }
}