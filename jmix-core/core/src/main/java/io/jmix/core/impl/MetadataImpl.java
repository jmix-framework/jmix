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

package io.jmix.core.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.HasInstanceMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

@Component("core_Metadata")
public class MetadataImpl implements Metadata {

    protected volatile Session session;

    @Autowired
    protected MetadataTools tools;

    @Autowired
    protected Resources resources;

    @Autowired(required = false)
    protected List<EntityInitializer> entityInitializers;

    @Autowired
    public MetadataImpl(MetadataLoader metadataLoader) {
        session = metadataLoader.getSession();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public MetaClass getClass(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        if (entity instanceof HasInstanceMetaClass && ((HasInstanceMetaClass) entity).hasInstanceMetaClass()) {
            return ((HasInstanceMetaClass) entity).getInstanceMetaClass();
        }
        return session.getClass(entity.getClass());
    }

    protected <T> T internalCreate(Class<T> entityClass) {
        Class<T> extClass = getSession().getClass(entityClass).getJavaClass();
        try {
            T obj = extClass.getDeclaredConstructor().newInstance();

            if (entityInitializers != null) {
                for (EntityInitializer initializer : entityInitializers) {
                    initializer.initEntity(obj);
                }
            }

            return obj;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Unable to create entity instance", e);
        }
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return internalCreate(entityClass);
    }

    @Override
    public Object create(MetaClass metaClass) {
        return internalCreate(metaClass.getJavaClass());
    }

    @Override
    public Object create(String entityName) {
        MetaClass metaClass = getSession().getClass(entityName);
        return internalCreate(metaClass.getJavaClass());
    }

    @Nullable
    @Override
    public MetaClass findClass(String name) {
        return getSession().findClass(name);
    }

    @Override
    public MetaClass getClass(String name) {
        return getSession().getClass(name);
    }

    @Nullable
    @Override
    public MetaClass findClass(Class<?> javaClass) {
        return getSession().findClass(javaClass);
    }

    @Override
    public MetaClass getClass(Class<?> javaClass) {
        return getSession().getClass(javaClass);
    }

    @Override
    public Collection<MetaClass> getClasses() {
        return getSession().getClasses();
    }
}
