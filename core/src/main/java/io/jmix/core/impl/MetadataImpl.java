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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

@Component(Metadata.NAME)
public class MetadataImpl implements Metadata {

    protected volatile Session session;

//    protected volatile List<String> rootPackages;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Inject
    protected MetadataTools tools;

    @Inject
    protected Resources resources;

    @Autowired(required = false)
    protected List<EntityInitializer> entityInitializers;

    @Inject
    public MetadataImpl(MetadataLoader metadataLoader) {
//        rootPackages = metadataLoader.getRootPackages();
        session = metadataLoader.getSession();
    }

//    protected void initMetadata(ContextRefreshedEvent event) {
//        if (session != null) {
//            log.warn("Repetitive initialization\n" + StackTrace.asString());
//            return;
//        }
//
//        log.info("Initializing metadata");
//        long startTime = System.currentTimeMillis();
//
//        MetadataLoader metadataLoader = (MetadataLoader) event.getApplicationContext().getBean(MetadataLoader.NAME);
//        metadataLoader.loadMetadata();
//        rootPackages = metadataLoader.getRootPackages();
//        session = metadataLoader.getSession();
//        SessionImpl.setSerializationSupportSession(session);
//
//        log.info("Metadata initialized in {} ms", System.currentTimeMillis() - startTime);
//    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public MetaClass getClass(Entity entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        return session.getClass(entity.getClass());
    }

    protected <T extends Entity> T __create(Class<T> entityClass) {
        @SuppressWarnings("unchecked")
        Class<T> extClass = extendedEntities.getEffectiveClass(entityClass);
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
    public <T extends Entity> T create(Class<T> entityClass) {
        return __create(entityClass);
    }

    @Override
    public Entity create(MetaClass metaClass) {
        return __create(metaClass.getJavaClass());
    }

    @Override
    public Entity create(String entityName) {
        MetaClass metaClass = getSession().getClass(entityName);
        return __create(metaClass.getJavaClass());
    }

    @Nullable
    @Override
    public MetaClass findClass(String name) {
        return getSession().findClass(name);
    }

    @Override
    public MetaClass getClass(String name) {
        return getSession().findClass(name);
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
