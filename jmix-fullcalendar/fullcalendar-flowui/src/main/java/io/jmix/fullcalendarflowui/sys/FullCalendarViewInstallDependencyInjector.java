/*
 * Copyright (c) Haulmont 2024. All Rights Reserved.
 * Use is subject to license terms.
 */

package io.jmix.fullcalendarflowui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.AutowireUtils;
import io.jmix.flowui.sys.autowire.DependencyInjector;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager.AnnotatedMethod;
import io.jmix.flowui.sys.autowire.ViewAutowireContext;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An injection that autowired methods that are annotated by the {@link Install} annotation
 * for the {@link FullCalendar} elements.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 110)
@Component("fcalen_FullCalendarViewInstallDependencyInjector")
public class FullCalendarViewInstallDependencyInjector implements DependencyInjector {

    private static final Logger log = LoggerFactory.getLogger(FullCalendarViewInstallDependencyInjector.class);

    protected ReflectionCacheManager reflectionCacheManager;

    public FullCalendarViewInstallDependencyInjector(ReflectionCacheManager reflectionCacheManager) {
        this.reflectionCacheManager = reflectionCacheManager;
    }

    @Override
    public void autowire(AutowireContext<?> autowireContext) {
        ViewAutowireContext viewAutowireContext = (ViewAutowireContext) autowireContext;
        View<?> view = viewAutowireContext.getTarget();
        //noinspection rawtypes
        Class<? extends View> viewClass = view.getClass();

        List<AnnotatedMethod<Install>> installMethods = reflectionCacheManager.getInstallMethods(viewClass);
        Collection<Object> autowired = autowireContext.getAutowired();

        for (AnnotatedMethod<Install> annotatedMethod : installMethods) {
            // skip already autowired elements
            if (!autowired.contains(annotatedMethod)) {
                doAutowiring(annotatedMethod, view, autowired);
            }
        }
    }

    protected void doAutowiring(AnnotatedMethod<Install> annotatedMethod, View<?> view, Collection<Object> autowired) {
        Install annotation = annotatedMethod.getAnnotation();

        if (!Target.COMPONENT.equals(annotation.target()) || Strings.isNullOrEmpty(annotation.to())) {
            return;
        }

        String targetId = getTargetId(annotation);
        Object targetInstance = getInstallTargetInstance(view, targetId);

        if (targetInstance == null) {
            String[] path = ValuePathHelper.parse(targetId);
            String prefix = path[0];

            Optional<FullCalendar> calendar = UiComponentUtils.findComponent(view, prefix)
                    .filter(c -> c instanceof FullCalendar)
                    .map(c -> (FullCalendar) c);
            if (calendar.isPresent() && annotation.required()) {
                throw new DevelopmentException(
                        "Unable to find @%s target for method %s in %s".formatted(
                                Install.class.getSimpleName(), annotatedMethod.getMethod(), view.getClass()
                        )
                );
            }

            // we should skip the injection if the target instance for the FullCalendar is not found
            // if the injection has an incorrect signature,
            // then an exception will be thrown in any case by other injectors if needed
            log.trace("Skip @{} method {} of {} : it is not required and target not found",
                    Install.class.getSimpleName(), annotatedMethod.getMethod().getName(), view.getClass());
            return;
        }

        Class<?> instanceClass = targetInstance.getClass();
        Method installMethod = annotatedMethod.getMethod();

        MethodHandle targetSetterMethod =
                AutowireUtils.getInstallTargetSetterMethod(annotation, view,
                        instanceClass, installMethod, reflectionCacheManager);
        Class<?> targetParameterType = targetSetterMethod.type().parameterList().get(1);

        Object handler = AutowireUtils.createInstallHandler(getClass(), targetParameterType, view, installMethod);

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
    protected Object getInstallTargetInstance(View<?> controller, String targetId) {
        String[] path = ValuePathHelper.parse(targetId);
        if (path.length == 1) {
            return UiComponentUtils.findComponent(controller, path[0])
                    .filter(c -> c instanceof FullCalendar)
                    .orElse(null);
        }
        if (path.length == 2) {
            Optional<FullCalendar> calendar = UiComponentUtils.findComponent(controller, path[0])
                    .filter(c -> c instanceof FullCalendar)
                    .map(c -> ((FullCalendar) c));

            return calendar.map(fullCalendar -> fullCalendar.getEventProvider(path[1])).orElse(null);
        }
        return null;
    }

    protected String getTargetId(Install annotation) {
        return ViewDescriptorUtils.getInferredProvideId(annotation);
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof ViewAutowireContext;
    }
}
