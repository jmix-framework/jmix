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

public class SessionImpl implements Session {

    private Map<String, MetaClass> classByName = new HashMap<>();
    private Map<Class, MetaClass> classByClass = new HashMap<>();

    @Nullable
    @Override
    public MetaClass getClass(String name) {
        return classByName.get(name);
    }

    @Override
    public MetaClass getClassNN(String name) {
        MetaClass metaClass = getClass(name);
        if (metaClass == null) {
            throw new IllegalArgumentException("MetaClass not found for " + name);
        }
        return metaClass;
    }

    @Nullable
    @Override
    public MetaClass getClass(Class<?> clazz) {
        return classByClass.get(clazz);
    }

    @Override
    public MetaClass getClassNN(Class<?> clazz) {
        MetaClass metaClass = getClass(clazz);
        if (metaClass == null) {
            throw new IllegalArgumentException("MetaClass not found for " + clazz);
        }
        return metaClass;
    }

    @Override
    public Collection<MetaClass> getClasses() {
        return classByName.values();
    }

    public void registerClass(MetaClassImpl clazz) {
        classByName.put(clazz.getName(), clazz);
        if (clazz.getJavaClass() != null) {
            classByClass.put(clazz.getJavaClass(), clazz);
        }
    }

    public void registerClass(String name, Class javaClass, MetaClassImpl clazz) {
        classByName.put(name, clazz);
        classByClass.put(javaClass, clazz);
    }
}