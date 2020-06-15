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

package io.jmix.security.role.builder;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.model.Role;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.role.builder.extractor.ResourcePolicyExtractor;
import io.jmix.security.role.builder.extractor.RowLevelPolicyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component(AnnotatedRoleBuilder.NAME)
public class AnnotatedRoleBuilderImpl implements AnnotatedRoleBuilder {

    protected Collection<ResourcePolicyExtractor> resourcePolicyExtractors;
    private Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors;

    @Autowired
    public AnnotatedRoleBuilderImpl(Collection<ResourcePolicyExtractor> resourcePolicyExtractors,
                                    Collection<RowLevelPolicyExtractor> rowLevelPolicyExtractors) {
        this.resourcePolicyExtractors = resourcePolicyExtractors;
        this.rowLevelPolicyExtractors = rowLevelPolicyExtractors;
    }

    @Override
    public Role createRole(String className) {
        Class<?> roleClass;
        try {
            roleClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find role class: " + className);
        }

        io.jmix.security.role.annotation.Role roleAnnotation = roleClass.getAnnotation(io.jmix.security.role.annotation.Role.class);
        Set<ResourcePolicy> resourcePolicies = new HashSet<>();
        Set<RowLevelPolicy> rowLevelPolicies = new HashSet<>();

        Method[] methods = roleClass.getMethods();
        for (Method method : methods) {
            for (ResourcePolicyExtractor policyExtractor : resourcePolicyExtractors) {
                resourcePolicies.addAll(policyExtractor.extractResourcePolicies(method));
            }

            for (RowLevelPolicyExtractor policyExtractor : rowLevelPolicyExtractors) {
                rowLevelPolicies.addAll(policyExtractor.extractRowLevelPolicies(method));
            }
        }

        Role role = new Role();
        role.setName(roleAnnotation.name());
        role.setCode(roleAnnotation.code());
        role.setScope(roleAnnotation.scope());
        role.setResourcePolicies(resourcePolicies);
        role.setRowLevelPolicies(rowLevelPolicies);
        return role;
    }

//    private Collection<ResourcePolicy> buildResourcePoliciesFromDefaultMethod(Method defaultMethod) {
//        Class<?> declaringClass = defaultMethod.getDeclaringClass();
//        InvocationHandler invocationHandler = (proxy, method, args) -> MethodHandles.lookup()
//                .findSpecial(declaringClass,
//                        defaultMethod.getName(),
//                        MethodType.methodType(
//                                Collection.class,
//                                new Class[0]),
//                        declaringClass)
//                .bindTo(proxy)
//                .invokeWithArguments(args);
//
//        Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(),
//                new Class[]{declaringClass},
//                invocationHandler);
//
//        try {
//            //todo check return type
//            return (Collection<ResourcePolicy>) defaultMethod.invoke(proxyInstance);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException("Cannot invoke interface default method", e);
//        }
//    }

//    private Collection<ResourcePolicy> buildResourcePoliciesFromDefaultMethod(Method defaultMethod) {
//        Class<?> roleInterface = defaultMethod.getDeclaringClass();
//        InvocationHandler invocationHandler = (proxy, method, args) -> {
//            try {
//                if (SystemUtils.IS_JAVA_1_8) {
//                    // hack to invoke default method of an interface reflectively
//                    Constructor<MethodHandles.Lookup> lookupConstructor =
//                            MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
//                    if (!lookupConstructor.isAccessible()) {
//                        lookupConstructor.setAccessible(true);
//                    }
//                    return lookupConstructor.newInstance(roleInterface, MethodHandles.Lookup.PRIVATE)
//                            .unreflectSpecial(defaultMethod, roleInterface)
//                            .bindTo(proxy)
//                            .invokeWithArguments(args);
//                } else {
//                    return MethodHandles.lookup()
//                            .findSpecial(roleInterface, defaultMethod.getName(), MethodType.methodType(defaultMethod.getReturnType(),
//                                    defaultMethod.getParameterTypes()), roleInterface)
//                            .bindTo(proxy)
//                            .invokeWithArguments(args);
//                }
//            } catch (Throwable throwable) {
//                throw new RuntimeException("Error invoking default method of config interface", throwable);
//            }
//        };
//
//        Object proxyInstance = Proxy.newProxyInstance(this.getClass().getClassLoader(),
//                new Class[]{roleInterface},
//                invocationHandler);
//
//        try {
//            //todo check return type
//            return (Collection<ResourcePolicy>) defaultMethod.invoke(proxyInstance);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException("Cannot invoke interface default method", e);
//        }
//    }

}
