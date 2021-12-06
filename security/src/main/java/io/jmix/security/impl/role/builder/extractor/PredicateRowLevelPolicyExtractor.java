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

package io.jmix.security.impl.role.builder.extractor;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.method.ContextArgumentResolverComposite;
import io.jmix.core.impl.method.MethodArgumentsProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPredicate;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;

@Component("sec_InMemoryRowLevelPolicyExtractor")
public class PredicateRowLevelPolicyExtractor implements RowLevelPolicyExtractor {

    private static final Logger log = LoggerFactory.getLogger(PredicateRowLevelPolicyExtractor.class);

    protected Metadata metadata;
    protected ConcurrentMap<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    private final MethodArgumentsProvider methodArgumentsProvider;

    @Autowired
    public PredicateRowLevelPolicyExtractor(Metadata metadata, ContextArgumentResolverComposite resolvers) {
        this.metadata = metadata;
        this.methodArgumentsProvider = new MethodArgumentsProvider(resolvers);
    }

    @Override
    public Collection<RowLevelPolicy> extractRowLevelPolicies(Method method) {
        Set<RowLevelPolicy> policies = new HashSet<>();
        //todo MG check parameter types
        PredicateRowLevelPolicy[] annotations = method.getAnnotationsByType(PredicateRowLevelPolicy.class);
        for (PredicateRowLevelPolicy annotation : annotations) {
            for (RowLevelPolicyAction action : annotation.actions()) {
                Class<?> entityClass = annotation.entityClass();
                MetaClass metaClass = metadata.getClass(entityClass);
                try {
                    Object proxyObject = null;
                    if (!Modifier.isStatic(method.getModifiers())) {
                        proxyObject = proxyCache.computeIfAbsent(method.getDeclaringClass(), this::createProxy);
                    }

                    Object policyFunction;
                    if (method.getParameterCount() == 0) {
                        policyFunction = method.invoke(proxyObject);
                    } else {
                        policyFunction = method.invoke(proxyObject, methodArgumentsProvider.getMethodArgumentValues(method));
                        log.warn("Methods with PredicateRowLevelPolicy annotation should not have any arguments. Role methods " +
                                "with arguments will be unsupported in future releases. Use methods that return RowLevelBiPredicate " +
                                "instead.");
                    }

                    RowLevelBiPredicate<Object, ApplicationContext> biPredicate;
                    if (policyFunction instanceof RowLevelPredicate) {
                        biPredicate = (entity, accessContext) -> ((RowLevelPredicate) policyFunction).test(entity);
                    } else if (policyFunction instanceof RowLevelBiPredicate) {
                        biPredicate = (RowLevelBiPredicate) policyFunction;
                    } else {
                        log.error("PredicateRowLevelPolicy method should return either RowLevelPredicate or RowLevelBiPredicate");
                        continue;
                    }

                    RowLevelPolicy rowLevelPolicy = new RowLevelPolicy(metaClass.getName(),
                            action,
                            biPredicate,
                            Collections.singletonMap("uniqueKey", UUID.randomUUID().toString()));
                    policies.add(rowLevelPolicy);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot evaluate row level policy predicate", e);
                }
            }
        }
        return policies;
    }

    protected Object createProxy(Class<?> ownerClass) {
        if (ownerClass.isInterface()) {
            ClassLoader classLoader = ownerClass.getClassLoader();
            return Proxy.newProxyInstance(classLoader, new Class[]{ownerClass},
                    (proxy, method, args) -> invokeProxyMethod(ownerClass, proxy, method, args));
        } else {
            try {
                return ReflectionHelper.newInstance(ownerClass);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(String.format("Cannot create Role [%s] proxy", ownerClass), e);
            }
        }
    }

    @Nullable
    protected Object invokeProxyMethod(Class<?> ownerClass, Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            try {
                if (SystemUtils.IS_JAVA_1_8) {
                    // hack to invoke default method of an interface reflectively
                    Constructor<MethodHandles.Lookup> lookupConstructor =
                            MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookupConstructor.isAccessible()) {
                        lookupConstructor.setAccessible(true);
                    }
                    return lookupConstructor.newInstance(ownerClass, MethodHandles.Lookup.PRIVATE)
                            .unreflectSpecial(method, ownerClass)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                } else {
                    return MethodHandles.lookup()
                            .findSpecial(ownerClass, method.getName(), MethodType.methodType(method.getReturnType(),
                                    method.getParameterTypes()), ownerClass)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                }
            } catch (Throwable throwable) {
                throw new RuntimeException("Error invoking default method of Role interface", throwable);
            }
        } else {
            return null;
        }
    }
}
