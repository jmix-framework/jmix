/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.testassist.vaadin;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.spring.SpringVaadinServletService;
import com.vaadin.flow.spring.annotation.VaadinTaskExecutor;
import io.jmix.flowui.view.View;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Disables atmosphere because testing server-side {@link View} does not require
 * connection with client-side.
 */
public class TestSpringVaadinServletService extends SpringVaadinServletService {

    protected ApplicationContext context;

    public TestSpringVaadinServletService(VaadinServlet servlet,
                                          DeploymentConfiguration deploymentConfiguration,
                                          ApplicationContext context) {
        super(servlet, deploymentConfiguration, context);

        this.context = context;
    }

    @Override
    protected boolean isAtmosphereAvailable() {
        return false;
    }

    /*
     * CAUTION! Copied from com.vaadin.flow.spring.SpringVaadinServletService#createDefaultExecutor() Last update 25.1.0.
     */
    @Override
    protected Executor createDefaultExecutor() {
        Set<String> candidates = Arrays
                .stream(context.getBeanNamesForType(TaskExecutor.class))
                .collect(Collectors.toCollection(HashSet::new));

        // No executor beans defined, fallback to Vaadin's default
        if (candidates.isEmpty()) {
            return super.createDefaultExecutor();
        }

        // Check for @VaadinTaskExecutor annotated beans, filter for
        // TaskExecutors types, and warn if the annotated bean is of an
        // unexpected type.
        Set<String> annotatedBeans = new HashSet<>(Set.of(
                context.getBeanNamesForAnnotation(VaadinTaskExecutor.class)));
        Set<String> invalidAnnotatedTypes = annotatedBeans.stream()
                .filter(beanName -> !candidates.contains(beanName))
                .collect(Collectors.toSet());
        if (!invalidAnnotatedTypes.isEmpty()) {
            LoggerFactory.getLogger(SpringVaadinServletService.class.getName())
                    .warn("Found beans with @{} annotation but not of type {}: {}. "
                                    + "Remove the annotation from the bean definition.",
                            VaadinTaskExecutor.class.getSimpleName(),
                            TaskExecutor.class.getSimpleName(),
                            invalidAnnotatedTypes);
            annotatedBeans.removeAll(invalidAnnotatedTypes);
        }

        // Retain only the Vaadin specific executors if they are defined
        if (candidates.contains(VaadinTaskExecutor.NAME)
                || !annotatedBeans.isEmpty()) {
            candidates.removeIf(name -> !annotatedBeans.contains(name)
                    && !name.equals(VaadinTaskExecutor.NAME));
        }

        if (candidates.size() > 1) {
            // Gives preference to regular executors over schedulers when both
            // types are present.
            Map<Boolean, List<String>> byType = candidates.stream()
                    .collect(Collectors.partitioningBy(name -> context
                            .isTypeMatch(name, TaskScheduler.class)));
            if (!byType.get(true).isEmpty() && !byType.get(false).isEmpty()) {
                // Remove TaskScheduler's from candidates list
                byType.get(true).forEach(candidates::remove);
            }
        }

        if (candidates.size() > 1) {
            // Remove Spring default executor to select an application defined
            // bean
            candidates.remove("applicationTaskExecutor");
        }
        if (candidates.size() > 1) {
            // Jmix: do not init multipleExecutorCandidates
            // to avoid Vaadin checks. Only the default executor
            // will be used.
            //multipleExecutorCandidates = candidates;
        }
        return context.getBean(candidates.iterator().next(),
                TaskExecutor.class);
    }
}
