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
import io.jmix.flowui.sys.delegate.InstalledSupplier;
import io.jmix.flowui.view.Supply;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractSupplyDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(AbstractSupplyDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    protected AbstractSupplyDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        Composite<?> target = autowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends Composite> targetClass = target.getClass();

        List<AnnotatedMethod<Supply>> supplyMethods = reflectionCacheManager.getSupplyMethods(targetClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Supply> annotatedMethod : supplyMethods) {
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, target, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Supply> annotatedMethod,
                                Composite<?> composite, Collection<Object> autowired) {
        Supply annotation = annotatedMethod.getAnnotation();
        Object targetInstance = getSupplyTargetInstance(composite, annotation);

        if (targetInstance == null) {
            if (annotation.required()) {
                throw new DevelopmentException(
                        String.format("Unable to find @%s composite for method '%s' in '%s'",
                                Supply.class.getSimpleName(), annotatedMethod.getMethod(), composite.getClass()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and composite not found",
                    Supply.class.getSimpleName(), annotatedMethod.getMethod().getName(), composite.getClass());

            return;
        }

        Class<?> instanceClass = targetInstance.getClass();
        Method supplyMethod = annotatedMethod.getMethod();

        MethodHandle targetSetterMethod = getSupplyTargetSetterMethod(annotatedMethod, instanceClass);
        Supplier<?> supplier = createSupplierInstance(composite, supplyMethod);

        try {
            targetSetterMethod.invoke(targetInstance, supplier.get());
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Unable to set declarative @%s supplier for %s",
                    Supply.class.getSimpleName(), supplyMethod), e);
        }

        autowired.add(annotatedMethod);
    }

    @Nullable
    protected abstract Object getSupplyTargetInstance(Composite<?> composite, Supply annotation);

    protected MethodHandle getSupplyTargetSetterMethod(AnnotatedMethod<Supply> annotatedMethod,
                                                       Class<?> instanceClass) {
        Supply annotation = annotatedMethod.getAnnotation();
        String subjectProperty = annotation.type() != Object.class
                ? StringUtils.uncapitalize(annotation.type().getSimpleName())
                : annotation.subject();

        String subjectSetterName = "set" + StringUtils.capitalize(subjectProperty);

        MethodHandle targetSetterMethod = reflectionCacheManager.getTargetSupplyMethod(
                instanceClass, subjectSetterName, annotatedMethod.getMethod().getReturnType()
        );

        if (targetSetterMethod == null) {
            throw new DevelopmentException(
                    String.format("Unable to find @%s target method '%s' in '%s'",
                            Supply.class.getSimpleName(), subjectProperty, instanceClass)
            );
        }

        return targetSetterMethod;
    }

    protected Supplier<?> createSupplierInstance(Composite<?> composite, Method method) {
        return new InstalledSupplier(composite, method);
    }
}
