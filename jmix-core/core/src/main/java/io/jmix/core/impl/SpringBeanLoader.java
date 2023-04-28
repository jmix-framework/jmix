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

package io.jmix.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import java.util.Collection;

@Component("core_SpringBeanLoader")
public class SpringBeanLoader implements BeanFactoryAware {

    protected DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            this.beanFactory = (DefaultListableBeanFactory) beanFactory;
        }
    }

    public void updateContext(Collection<Class> classes) {
        if (beanFactory != null) {
            boolean needToRefreshRemotingContext = false;
            for (Class<?> clazz : classes) {
                Service serviceAnnotation = clazz.getAnnotation(Service.class);
                Component componentAnnotation = clazz.getAnnotation(Component.class);
                Controller controllerAnnotation = clazz.getAnnotation(Controller.class);

                String beanName = null;
                if (serviceAnnotation != null) {
                    beanName = serviceAnnotation.value();
                } else if (componentAnnotation != null) {
                    beanName = componentAnnotation.value();
                } else if (controllerAnnotation != null) {
                    beanName = controllerAnnotation.value();
                }

                if (StringUtils.isNotBlank(beanName)) {
                    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                    beanDefinition.setBeanClass(clazz);
                    Scope scope = clazz.getAnnotation(Scope.class);
                    if (scope != null) {
                        beanDefinition.setScope(scope.value());
                    }

                    beanFactory.registerBeanDefinition(beanName, beanDefinition);
                }

                if (StringUtils.isNotBlank(beanName)) {
                    needToRefreshRemotingContext = true;
                }
            }

            if (needToRefreshRemotingContext) {
                // todo remoting
//                ApplicationContext remotingContext = RemotingContextHolder.getRemotingApplicationContext();
//                if (remotingContext != null && remotingContext instanceof ConfigurableApplicationContext) {
//                    ((ConfigurableApplicationContext) remotingContext).refresh();
//                }
            }
        }
    }
}
