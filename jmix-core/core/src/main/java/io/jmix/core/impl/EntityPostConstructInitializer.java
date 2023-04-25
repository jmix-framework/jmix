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
import io.jmix.core.EntityInitializer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("core_EntityPostConstructInitializer")
public class EntityPostConstructInitializer implements EntityInitializer {

    @Autowired
    protected ApplicationContext applicationContext;

    // stores methods in the execution order, all methods are accessible
    protected LoadingCache<Class<?>, List<Method>> postConstructMethodsCache =
            CacheBuilder.newBuilder()
                    .build(new CacheLoader<Class<?>, List<Method>>() {
                        @Override
                        public List<Method> load(@Nonnull Class<?> concreteClass) {
                            return getPostConstructMethodsNotCached(concreteClass);
                        }
                    });

    @Override
    public void initEntity(Object entity) {
        try {
            List<Method> postConstructMethods = postConstructMethodsCache.getUnchecked(entity.getClass());
            // methods are store in the correct execution order
            for (Method method : postConstructMethods) {
                List<Object> params = new ArrayList<>();
                for (Parameter parameter : method.getParameters()) {
                    Class<?> parameterClass = parameter.getType();
                    try {
                        params.add(applicationContext.getBean(parameterClass));
                    } catch (NoSuchBeanDefinitionException e) {
                        String message = String.format("Unable to create %s entity. Argument of the %s type at the @PostConstruct method is not a bean",
                                entity.getClass().getName(), parameter.getType().getName());
                        throw new IllegalArgumentException(message, e);
                    }
                }
                method.invoke(entity, params.toArray());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to init entity instance", e);
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
}
