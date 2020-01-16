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

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JmixModulesProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, PriorityOrdered {

    private static final Logger log = LoggerFactory.getLogger(JmixModulesProcessor.class);
    private Environment environment;
    private JmixModules jmixModules;

    public JmixModules getJmixModules() {
        return jmixModules;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<JmixModuleDescriptor> components = new ArrayList<>();
        List<String> componentIds = new ArrayList<>();

        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (!(beanDefinition instanceof AnnotatedBeanDefinition)) {
                continue;
            }
            if (!((AnnotatedBeanDefinition) beanDefinition).getMetadata().hasAnnotation(JmixModule.class.getName())
                    || ((AnnotatedBeanDefinition) beanDefinition).getFactoryMethodMetadata() != null) {
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
            JmixModule componentAnnotation = AnnotationUtils.findAnnotation(beanClass, JmixModule.class);
            if (componentAnnotation == null) {
                continue;
            }
            String compId = getComponentId(componentAnnotation, beanClass);
            if (!componentIds.contains(compId)) {
                componentIds.add(compId);
            }

            JmixModuleDescriptor compDescriptor = components.stream()
                    .filter(descriptor -> descriptor.getId().equals(compId))
                    .findAny()
                    .orElseGet(() -> {
                        JmixModuleDescriptor descriptor = new JmixModuleDescriptor(compId);
                        load(descriptor, componentAnnotation, components);
                        return descriptor;
                    });
            if (!components.contains(compDescriptor))
                components.add(compDescriptor);
        }

        components.sort((c1, c2) -> {
            int res = c1.compareTo(c2);
            if (res != 0)
                return res;
            else
                return componentIds.indexOf(c1.getId()) - componentIds.indexOf(c2.getId());
        });

        log.info("Using Jmix components: {}", components);

        jmixModules = new JmixModules(environment, components);

        if (environment instanceof ConfigurableEnvironment) {
            MutablePropertySources sources = ((ConfigurableEnvironment) environment).getPropertySources();
            sources.addLast(new JmixPropertySource(jmixModules));
        } else {
            throw new IllegalStateException("Not a ConfigurableEnvironment, cannot register JmixModules property source");
        }

    }

    private String getComponentId(JmixModule jmixModule, Class<?> aClass) {
        String compId = jmixModule.id();
        if ("".equals(compId)) {
            compId = aClass.getPackage().getName();
        }
        return compId;
    }

    private void load(JmixModuleDescriptor component, JmixModule componentAnnotation,
                      List<JmixModuleDescriptor> components) {
        for (Class<?> depClass : componentAnnotation.dependsOn()) {
            JmixModule depComponentAnnotation = AnnotationUtils.findAnnotation(depClass, JmixModule.class);
            if (depComponentAnnotation == null) {
                log.warn("Dependency class {} is not annotated with {}, ignoring it", depClass.getName(), JmixModule.class.getName());
                continue;
            }
            String depCompId = getComponentId(depComponentAnnotation, depClass);

            JmixModuleDescriptor depComp = components.stream()
                    .filter(descriptor -> descriptor.getId().equals(depCompId))
                    .findAny()
                    .orElseGet(() -> {
                        JmixModuleDescriptor descriptor = new JmixModuleDescriptor(depCompId);
                        load(descriptor, depComponentAnnotation, components);
                        components.add(descriptor);
                        return descriptor;
                    });
            component.addDependency(depComp);
        }

        for (JmixProperty propertyAnn : componentAnnotation.properties()) {
            component.setProperty(propertyAnn.name(), propertyAnn.value(), propertyAnn.append());
        }
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
        this.environment = environment;
    }

    private static class JmixPropertySource extends EnumerablePropertySource<JmixModules> {

        public JmixPropertySource(JmixModules source) {
            super("JmixModules properties", source);
        }

        @Nonnull
        @Override
        public String[] getPropertyNames() {
            Set<String> propertyNames = new HashSet<>();
            for (JmixModuleDescriptor component : source.getComponents()) {
                propertyNames.addAll(component.getPropertyNames());
            }
            return propertyNames.toArray(new String[0]);
        }

        @Override
        public Object getProperty(String name) {
            return source.getProperty(name);
        }
    }

}
