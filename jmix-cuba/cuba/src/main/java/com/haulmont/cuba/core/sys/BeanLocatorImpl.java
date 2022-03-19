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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.global.BeanLocator;
import io.jmix.core.common.util.Preconditions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component(BeanLocator.NAME)
public class BeanLocatorImpl implements BeanLocator, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <T> T get(Class<T> beanType) {
        Preconditions.checkNotNullArgument(beanType, "beanType is null");
        return applicationContext.getBean(beanType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String name) {
        Preconditions.checkNotNullArgument(name, "name is null");
        return (T) applicationContext.getBean(name);
    }

    @Override
    public <T> T get(String name, Class<T> beanType) {
        Preconditions.checkNotNullArgument(name, "name is null");
        return applicationContext.getBean(name, beanType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPrototype(String name, Object... args) {
        Preconditions.checkNotNullArgument(name, "name is null");
        return (T) applicationContext.getBean(name, args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPrototype(Class<T> beanType, Object... args) {
        Preconditions.checkNotNullArgument(beanType, "beanType is null");
        return applicationContext.getBean(beanType, args);
    }

    @Override
    public <T> Map<String, T> getAll(Class<T> beanType) {
        return applicationContext.getBeansOfType(beanType);
    }

    @Override
    public boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
