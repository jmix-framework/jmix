/*
 * Copyright 2025 Haulmont.
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

package io.jmix.restds.impl.service;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.restds.annotation.RemoteService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class RemoteServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final JmixModules jmixModules;

    public RemoteServiceBeanFactoryPostProcessor(JmixModules jmixModules) {
        this.jmixModules = jmixModules;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Create scanner that includes interfaces
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(RemoteService.class));

        jmixModules.getAll().stream()
                .map(JmixModuleDescriptor::getBasePackage)
                .forEach(basePackage -> {
                    scanner.findCandidateComponents(basePackage).forEach(beanDefinition -> {
                        try {
                            Class<?> serviceInterface = Class.forName(beanDefinition.getBeanClassName());
                            registerRemoteServiceBean((BeanDefinitionRegistry) beanFactory, serviceInterface);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
    }

    private void registerRemoteServiceBean(BeanDefinitionRegistry registry, Class<?> serviceInterface) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(RemoteServiceProxyFactoryBean.class);
        beanDefinition.getPropertyValues().add("serviceInterface", serviceInterface);

        String beanName = serviceInterface.getSimpleName().substring(0, 1).toLowerCase() + 
                         serviceInterface.getSimpleName().substring(1);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}