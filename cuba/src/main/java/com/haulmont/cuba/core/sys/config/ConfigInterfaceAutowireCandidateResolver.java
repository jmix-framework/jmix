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

package com.haulmont.cuba.core.sys.config;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.config.Config;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Field;

public class ConfigInterfaceAutowireCandidateResolver implements AutowireCandidateResolver {

    private AutowireCandidateResolver delegate;
    private Configuration configInterfaces;

    public ConfigInterfaceAutowireCandidateResolver(AutowireCandidateResolver delegate, Configuration configuration) {
        this.delegate = delegate;
        this.configInterfaces = configuration;
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        return delegate.isRequired(descriptor);
    }

    @Override
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        return delegate.hasQualifier(descriptor);
    }

    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        return delegate.getSuggestedValue(descriptor);
    }

    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Field field = descriptor.getField();
        if (field != null && Config.class.isAssignableFrom(field.getType())) {
            return getConfig(field.getType());
        }
        MethodParameter methodParam = descriptor.getMethodParameter();
        if (methodParam != null && Config.class.isAssignableFrom(methodParam.getParameterType())) {
            return getConfig(methodParam.getParameterType());
        }

        return delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
    }

    @SuppressWarnings("unchecked")
    protected Object getConfig(Class configClass) {
        return configInterfaces.getConfig((Class<? extends Config>) configClass);
    }
}
