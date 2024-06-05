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

package io.jmix.flowui.sys.autowire;

import com.vaadin.flow.component.Composite;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.view.Install;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public abstract class AbstractInstallDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(AbstractInstallDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected AbstractInstallDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(DependencyInjector.AutowireContext<?> autowireContext) {
        Composite<?> target = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> targetClass = target.getClass();

        List<AnnotatedMethod<Install>> installMethods = reflectionCacheManager.getInstallMethods(targetClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Install> annotatedMethod : installMethods) {
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, target, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Install> annotatedMethod,
                                Composite<?> composite,
                                Collection<Object> autowired) {
        Install annotation = annotatedMethod.getAnnotation();

        Object targetInstance = getInstallTargetInstance(composite, annotation);

        if (targetInstance == null) {
            if (annotation.required()) {
                throw new DevelopmentException(
                        String.format("Unable to find @%s target for method %s in %s",
                                Install.class.getSimpleName(), annotatedMethod.getMethod(), composite.getClass()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Install.class.getSimpleName(), annotatedMethod.getMethod().getName(), composite.getClass());

            return;
        }

        Class<?> instanceClass = targetInstance.getClass();
        Method installMethod = annotatedMethod.getMethod();

        MethodHandle targetSetterMethod = AutowireUtils.getInstallTargetSetterMethod(annotation, composite,
                instanceClass, installMethod, reflectionCacheManager);
        Class<?> targetParameterType = targetSetterMethod.type().parameterList().get(1);

        Object handler = AutowireUtils.createInstallHandler(this.getClass(), targetParameterType,
                composite, installMethod);

        try {
            targetSetterMethod.invoke(targetInstance, handler);
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Unable to set declarative @%s handler for %s",
                    Install.class.getSimpleName(), installMethod), e);
        }

        autowired.add(annotatedMethod);
    }

    @Nullable
    protected abstract Object getInstallTargetInstance(Composite<?> component, Install annotation);
}
