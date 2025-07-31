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

package io.jmix.search.index.mapping.processor.impl;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO javadoc
 */
@Component
public class InstanceNameRelatedPropertiesResolver {

    private static final Logger log = LoggerFactory.getLogger(InstanceNameRelatedPropertiesResolver.class);

    protected final InstanceNameProvider instanceNameProvider;

    public InstanceNameRelatedPropertiesResolver(InstanceNameProvider instanceNameProvider) {
        this.instanceNameProvider = instanceNameProvider;
    }

    public List<MetaPropertyPath> resolveInstanceNameRelatedProperties(MetaClass metaClass, @Nullable MetaPropertyPath rootPropertyPath) {
        MetaProperty[] rootProperties = rootPropertyPath == null ? null : rootPropertyPath.getMetaProperties();
        return instanceNameProvider.getInstanceNameRelatedProperties(metaClass, true)
                .stream()
                .map(property -> {
                    if (rootProperties == null) {
                        return new MetaPropertyPath(metaClass, property);
                    } else {
                        MetaProperty[] extendedPropertiesArray = Arrays.copyOf(rootProperties, rootProperties.length + 1);
                        extendedPropertiesArray[extendedPropertiesArray.length - 1] = property;
                        return new MetaPropertyPath(rootPropertyPath.getMetaClass(), extendedPropertiesArray);
                    }

                })
                .collect(Collectors.toList());
    }

    public List<MetaPropertyPath> resolveInstanceNameRelatedProperties(MetaPropertyPath propertyPath) {
        List<MetaPropertyPath> instanceNameRelatedProperties;
        if (propertyPath.getRange().isClass()) {
            instanceNameRelatedProperties = resolveInstanceNameRelatedProperties(propertyPath.getRange().asClass(), propertyPath);
            log.debug("Properties related to Instance Name ({}): {}", propertyPath, instanceNameRelatedProperties);
        } else {
            instanceNameRelatedProperties = Collections.emptyList();
        }
        return instanceNameRelatedProperties;
    }
}
