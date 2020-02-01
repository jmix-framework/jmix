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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.jmix.core.*;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.*;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component(Metadata.NAME)
public class MetadataImpl implements Metadata {

    private static final Logger log = LoggerFactory.getLogger(MetadataImpl.class);

    protected volatile Session session;

    protected volatile List<String> rootPackages;

    @Inject
    protected ExtendedEntities extendedEntities;

    @Inject
    protected MetadataTools tools;

    @Inject
    protected Resources resources;

    @Inject
    protected NumberIdSource numberIdSource;

    @Inject
    protected BeanLocator beanLocator;

    @Inject
    protected GlobalConfig config;

    // stores methods in the execution order, all methods are accessible
    protected LoadingCache<Class<?>, List<Method>> postConstructMethodsCache =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<Class<?>, List<Method>>() {
                        @Override
                        public List<Method> load(@Nonnull Class<?> concreteClass) {
                            return getPostConstructMethodsNotCached(concreteClass);
                        }
                    });

    @Inject
    public MetadataImpl(MetadataLoader metadataLoader) {
        rootPackages = metadataLoader.getRootPackages();
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
            assignIdentifier(obj);
            assignUuid(obj);
            createEmbedded(obj);
            invokePostConstructMethods(obj);
            return obj;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Unable to create entity instance", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void assignIdentifier(Entity entity) {
        if (!(entity instanceof BaseGenericIdEntity))
            return;

        MetaClass metaClass = getClass(entity.getClass());

        MetaProperty primaryKeyProperty = tools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProperty != null && tools.isEmbedded(primaryKeyProperty)) {
            // create an instance of embedded ID
            Entity key = create(primaryKeyProperty.getRange().asClass());
            ((BaseGenericIdEntity) entity).setId(key);
        } else {
            if (!config.getEnableIdGenerationForEntitiesInAdditionalDataStores()
                    && !Stores.MAIN.equals(tools.getStoreName(metaClass))) {
                return;
            }
            if (tools.isPersistent(metaClass)) {
                if (entity instanceof BaseLongIdEntity) {
                    ((BaseGenericIdEntity<Long>) entity).setId(numberIdSource.createLongId(getEntityNameForIdGeneration(metaClass)));
                } else if (entity instanceof BaseIntegerIdEntity) {
                    ((BaseGenericIdEntity<Integer>) entity).setId(numberIdSource.createIntegerId(getEntityNameForIdGeneration(metaClass)));
                }
            }
        }
    }

    protected String getEntityNameForIdGeneration(MetaClass metaClass) {
        List<MetaClass> persistentAncestors = metaClass.getAncestors().stream()
                .filter(mc -> tools.isPersistent(mc)) // filter out all mapped superclasses
                .collect(Collectors.toList());
        if (persistentAncestors.size() > 0) {
            MetaClass root = persistentAncestors.get(persistentAncestors.size() - 1);
            Class<?> javaClass = root.getJavaClass();
            Inheritance inheritance = javaClass.getAnnotation(Inheritance.class);
            if (inheritance == null || inheritance.strategy() != InheritanceType.TABLE_PER_CLASS) {
                // use root of inheritance tree if the strategy is JOINED or SINGLE_TABLE because ID is stored in the root table
                return root.getName();
            }
        }
        return metaClass.getName();
    }

    protected void assignUuid(Entity entity) {
        if (entity instanceof HasUuid) {
            ((HasUuid) entity).setUuid(UuidProvider.createUuid());
        }
    }

    protected void createEmbedded(Entity entity) {
        MetaClass metaClass = getClass(entity.getClass());
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass() && tools.isEmbedded(property)) {
                EmbeddedParameters embeddedParameters = property.getAnnotatedElement().getAnnotation(EmbeddedParameters.class);
                if (embeddedParameters != null && !embeddedParameters.nullAllowed()) {
                    MetaClass embeddableMetaClass = property.getRange().asClass();
                    Entity embeddableEntity = create(embeddableMetaClass);
                    entity.setValue(property.getName(), embeddableEntity);
                }
            }
        }
    }

    protected void invokePostConstructMethods(Entity entity) throws InvocationTargetException, IllegalAccessException {
        List<Method> postConstructMethods = postConstructMethodsCache.getUnchecked(entity.getClass());
        // methods are store in the correct execution order
        for (Method method : postConstructMethods) {
            List<Object> params = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                Class<?> parameterClass = parameter.getType();
                try {
                    params.add(beanLocator.get(parameterClass));
                } catch (NoSuchBeanDefinitionException e) {
                    String message = String.format("Unable to create %s entity. Argument of the %s type at the @PostConstruct method is not a bean",
                            entity.getClass().getName(), parameter.getType().getName());
                    throw new IllegalArgumentException(message, e);
                }
            }
            method.invoke(entity, params.toArray());
        }
    }

    protected List<Method> getPostConstructMethodsNotCached(Class<?> clazz) {
        List<Method> postConstructMethods = Collections.emptyList();
        List<String> methodNames = Collections.emptyList();

        while (clazz != Object.class) {
            Method[] classMethods = clazz.getDeclaredMethods();
            for (Method method : classMethods) {
                if (method.isAnnotationPresent(PostConstruct.class)
                        && !methodNames.contains(method.getName())) {
                    if (postConstructMethods.isEmpty()) {
                        postConstructMethods = new ArrayList<>();
                    }
                    postConstructMethods.add(method);

                    if (methodNames.isEmpty()) {
                        methodNames = new ArrayList<>();
                    }
                    methodNames.add(method.getName());
                }
            }

            Class[] interfaces = clazz.getInterfaces();
            for (Class interfaceClazz : interfaces) {
                Method[] interfaceMethods = interfaceClazz.getDeclaredMethods();
                for (Method method : interfaceMethods) {
                    if (method.isAnnotationPresent(PostConstruct.class)
                            && method.isDefault()
                            && !methodNames.contains(method.getName())) {
                        if (postConstructMethods.isEmpty()) {
                            postConstructMethods = new ArrayList<>();
                        }
                        postConstructMethods.add(method);

                        if (methodNames.isEmpty()) {
                            methodNames = new ArrayList<>();
                        }
                        methodNames.add(method.getName());
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        for (Method method : postConstructMethods) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
        }

        return postConstructMethods.isEmpty() ?
                Collections.emptyList() : ImmutableList.copyOf(Lists.reverse(postConstructMethods));
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return __create(entityClass);
    }

    @Override
    public Entity create(MetaClass metaClass) {
        return (Entity) __create(metaClass.getJavaClass());
    }

    @Override
    public Entity create(String entityName) {
        MetaClass metaClass = getSession().getClass(entityName);
        return (Entity) __create(metaClass.getJavaClass());
    }

    @Override
    public List<String> getRootPackages() {
        return Collections.unmodifiableList(rootPackages);
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
