/*
 * Copyright 2022 Haulmont.
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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class JmixAnnotationJmxAttributeSource extends AnnotationJmxAttributeSource {

    @Nullable
    private final String defaultDomain;

    public JmixAnnotationJmxAttributeSource(@Nullable String defaultDomain) {
        this.defaultDomain = StringUtils.hasText(defaultDomain) ? defaultDomain : null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
    }


    @Override
    public ManagedResource getManagedResource(Class<?> beanClass) throws InvalidMetadataException {
        ManagedResource resource = super.getManagedResource(beanClass);
        if (defaultDomain != null && resource != null && StringUtils.hasText(resource.getObjectName())) {
            resource.setObjectName(defaultDomain + "." + resource.getObjectName());
        }
        return resource;
    }
}
