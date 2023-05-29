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

package io.jmix.core;

import io.jmix.core.annotation.Internal;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.JmixModulesSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Internal
public class JmixModulesProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {

    private static final Logger log = LoggerFactory.getLogger(JmixModulesProcessor.class);
    private ConfigurableEnvironment environment;
    private JmixModules jmixModules;

    public JmixModules getJmixModules() {
        return jmixModules;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<JmixModuleDescriptor> modules = new ArrayList<>();
        Map<String, Class<?>> idToClassMap = new HashMap<>();

        List<JmixModuleDescriptor> leafModules = new ArrayList<>();

        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (!(beanDefinition instanceof AnnotatedBeanDefinition)
                    || ((AnnotatedBeanDefinition) beanDefinition).getFactoryMethodMetadata() != null) {
                continue;
            }
            AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
            if (!(annotationMetadata.hasAnnotation(JmixModule.class.getName())
                    || annotationMetadata.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication")
                    || annotationMetadata.hasAnnotation("org.springframework.boot.autoconfigure.EnableAutoConfiguration"))) {
                    continue;
            }
            String beanClassName = beanDefinition.getBeanClassName();

            ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
            if (beanClassLoader == null) {
                throw new RuntimeException("BeanClassLoader is null");
            }
            Class<?> beanClass;
            try {
                beanClass = beanClassLoader.loadClass(beanClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            JmixModule moduleAnnotation = beanClass.getAnnotation(JmixModule.class);
            String moduleId = getModuleId(moduleAnnotation, beanClass);

            Class<?> aClass = idToClassMap.get(moduleId);
            if (aClass != null && aClass != beanClass) {
                throw new IllegalStateException(String.format(
                        "Duplicated Jmix module id '%s' is provided by %s and %s. " +
                        "Consider using @JmixModule.id attribute to specify a unique module id.",
                        moduleId, aClass.getName(), beanClass.getName()));
            }
            idToClassMap.put(moduleId, beanClass);

            JmixModuleDescriptor module = modules.stream()
                    .filter(descriptor -> descriptor.getId().equals(moduleId))
                    .findAny()
                    .orElseGet(() -> {
                        JmixModuleDescriptor descriptor = new JmixModuleDescriptor(moduleId, beanClass.getPackage().getName());
                        load(descriptor, moduleAnnotation, modules);
                        return descriptor;
                    });
            if (!modules.contains(module))
                modules.add(module);

            if (isDependingOnAll(moduleAnnotation)) {
                leafModules.add(module);
            }
        }

        for (JmixModuleDescriptor leafModule : leafModules) {
            for (JmixModuleDescriptor module : modules) {
                if (!leafModules.contains(module)) {
                    leafModule.addDependency(module);
                }
            }
        }

        List<JmixModuleDescriptor> sortedModules = JmixModulesSorter.sort(modules);

        log.info("Using Jmix modules: {}", sortedModules);

        jmixModules = new JmixModules(sortedModules, environment);

        for (int i = sortedModules.size() - 1; i >= 0; i--) {
            JmixModuleDescriptor module = sortedModules.get(i);
            PropertySource source = module.getPropertySource();
            if (source != null) {
                environment.getPropertySources().addLast(source);
            }
        }
    }

    private boolean isDependingOnAll(@Nullable JmixModule moduleAnnotation) {
        return moduleAnnotation == null
                || (moduleAnnotation.dependsOn().length == 1 && moduleAnnotation.dependsOn()[0] == JmixModule.AllModules.class);
    }

    private String getModuleId(@Nullable JmixModule jmixModule, Class<?> aClass) {
        String moduleId = "";
        if (jmixModule != null) {
            moduleId = jmixModule.id();
        }
        if ("".equals(moduleId)) {
            moduleId = aClass.getPackage().getName();
        }
        return moduleId;
    }

    private void load(JmixModuleDescriptor module, @Nullable JmixModule moduleAnnotation,
                      List<JmixModuleDescriptor> modules) {
        if (!isDependingOnAll(moduleAnnotation)) {
            for (Class<?> depClass : moduleAnnotation.dependsOn()) {
                JmixModule depModuleAnnotation = depClass.getAnnotation(JmixModule.class);
                if (depModuleAnnotation == null) {
                    log.warn("Dependency class {} is not annotated with {}, ignoring it", depClass.getName(), JmixModule.class.getName());
                    continue;
                }
                String depModuleId = getModuleId(depModuleAnnotation, depClass);

                JmixModuleDescriptor depModule = modules.stream()
                        .filter(descriptor -> descriptor.getId().equals(depModuleId))
                        .findAny()
                        .orElseGet(() -> {
                            JmixModuleDescriptor descriptor = new JmixModuleDescriptor(depModuleId, depClass.getPackage().getName());
                            load(descriptor, depModuleAnnotation, modules);
                            modules.add(descriptor);
                            return descriptor;
                        });
                module.addDependency(depModule);
            }
        }
        module.setPropertySource(environment.getPropertySources().remove(module.getId()));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1000; // to be before ConfigurationClassPostProcessor
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
