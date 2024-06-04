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
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.InstallTargetHandler;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * An injection that autowired methods that are annotated by the {@link Install} annotation.
 * These can be installations to the view or to the components in the view.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 20)
@Component("flowui_ViewInstallDependencyInjector")
public class ViewInstallDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(ViewInstallDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    public ViewInstallDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext autowireContext) {
        ViewAutowireContext viewAutowireContext = (ViewAutowireContext) autowireContext;
        View<?> view = viewAutowireContext.getView();
        //noinspection rawtypes
        Class<? extends View> viewClass = view.getClass();

        List<AnnotatedMethod<Install>> installMethods = reflectionCacheManager.getViewInstallMethods(viewClass);
        Collection<Object> autowired = viewAutowireContext.getAutowired();

        for (AnnotatedMethod<Install> annotatedMethod : installMethods) {
            // skip already autowired elements
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, view, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Install> annotatedMethod, View<?> view, Collection<Object> autowired) {
        Install annotation = annotatedMethod.getAnnotation();

        Object targetInstance = getInstallTargetInstance(view, annotation);

        if (targetInstance == null) {
            if (annotation.required()) {
                throw new DevelopmentException(
                        String.format("Unable to find @%s target for method %s in %s",
                                Install.class.getSimpleName(), annotatedMethod.getMethod(), view.getClass()));
            }

            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Install.class.getSimpleName(), annotatedMethod.getMethod().getName(), view.getClass());

            return;
        }

        Class<?> instanceClass = targetInstance.getClass();
        Method installMethod = annotatedMethod.getMethod();

        MethodHandle targetSetterMethod = AutowireUtils.getInstallTargetSetterMethod(annotation, view,
                instanceClass, installMethod, reflectionCacheManager);
        Class<?> targetParameterType = targetSetterMethod.type().parameterList().get(1);

        Object handler = null;
        if (targetInstance instanceof InstallTargetHandler installTargetHandler) {
            handler = installTargetHandler.createInstallHandler(targetParameterType, view, installMethod);
        }

        if (handler == null) {
            handler = AutowireUtils.createInstallHandler(getClass(), targetParameterType, view, installMethod);
        }

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
    protected Object getInstallTargetInstance(View<?> controller, Install annotation) {
        return AutowireUtils.getTargetInstance(annotation, controller,
                ViewDescriptorUtils.getInferredProvideId(annotation), annotation.target());
    }

    @Override
    public boolean isApplicable(AutowireContext autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
