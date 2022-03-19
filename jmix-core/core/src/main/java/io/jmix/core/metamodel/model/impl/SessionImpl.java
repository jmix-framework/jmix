/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.metamodel.model.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SessionImpl implements Session {

    private Map<String, MetaClass> classByName = new HashMap<>();
    private Map<Class, MetaClass> classByClass = new HashMap<>();

    @Nullable
    @Override
    public MetaClass findClass(String name) {
        return classByName.get(name);
    }

    @Override
    public MetaClass getClass(String name) {
        MetaClass metaClass = findClass(name);
        if (metaClass == null) {
            throw new IllegalArgumentException("MetaClass not found for " + name);
        }
        return metaClass;
    }

    @Nullable
    @Override
    public MetaClass findClass(Class<?> javaClass) {
        // ToDo: Hibernate change - temporary solution to HibernateProxy problem
        MetaClass metaClass = classByClass.get(javaClass);
        if (metaClass == null && javaClass.getSimpleName().contains("HibernateProxy")) {
            metaClass = classByClass.get(javaClass.getSuperclass());
        }
        return metaClass;
    }

    @Override
    public MetaClass getClass(Class<?> javaClass) {
        MetaClass metaClass = findClass(javaClass);
        if (metaClass == null) {
            throw new IllegalArgumentException("MetaClass not found for " + javaClass);
        }
        return metaClass;
    }

    @Override
    public Collection<MetaClass> getClasses() {
        return classByName.values().stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void registerClass(MetaClassImpl metaClass) {
        classByName.put(metaClass.getName(), metaClass);
        if (metaClass.getJavaClass() != null) {
            classByClass.put(metaClass.getJavaClass(), metaClass);
        }
    }

    public void registerClass(String name, Class javaClass, MetaClassImpl metaClass) {
        classByName.put(name, metaClass);
        classByClass.put(javaClass, metaClass);
    }
}
