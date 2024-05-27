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

import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.sys.delegate.InstalledSupplier;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * An injection that autowires methods that are annotated by the {@link Supply} annotation.
 * These can be suppliers for the view or for the components on the view.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
@Component("flowui_ViewSupplyDependencyInjector")
public class ViewSupplyDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(ViewSupplyDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    public ViewSupplyDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext autowireContext) {
        ViewAutowireContext viewAutowireContext = (ViewAutowireContext) autowireContext;
        View<?> view = viewAutowireContext.getView();
        //noinspection rawtypes
        Class<? extends View> viewClass = view.getClass();

        List<AnnotatedMethod<Supply>> supplyMethods = reflectionCacheManager.getViewSupplyMethods(viewClass);
        Collection<Object> autowired = viewAutowireContext.getAutowired();

        for (AnnotatedMethod<Supply> annotatedMethod : supplyMethods) {
            // skip already autowired elements
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, view, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Supply> annotatedMethod, View<?> view, Collection<Object> autowired) {
        Supply annotation = annotatedMethod.getAnnotation();
        Object targetInstance = getSupplyTargetInstance(view, annotation);

        if (targetInstance == null) {
            if (annotation.required()) {
                throw new DevelopmentException(
                        String.format("Unable to find @%s target for method '%s' in '%s'",
                                Supply.class.getSimpleName(), annotatedMethod.getMethod(), view.getClass()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Supply.class.getSimpleName(), annotatedMethod.getMethod().getName(), view.getClass());

            return;
        }

        Class<?> instanceClass = targetInstance.getClass();
        Method supplyMethod = annotatedMethod.getMethod();

        MethodHandle targetSetterMethod = getSupplyTargetSetterMethod(annotatedMethod, instanceClass);
        Supplier<?> supplier = createSupplierInstance(view, supplyMethod);

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
    protected Object getSupplyTargetInstance(View<?> controller, Supply annotation) {
        return AutowireUtils.getTargetInstance(annotation, controller,
                ViewDescriptorUtils.getInferredProvideId(annotation), annotation.target());
    }

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

    protected Supplier<?> createSupplierInstance(View<?> controller, Method method) {
        return new InstalledSupplier(controller, method);
    }

    @Override
    public boolean isApplicable(AutowireContext autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
