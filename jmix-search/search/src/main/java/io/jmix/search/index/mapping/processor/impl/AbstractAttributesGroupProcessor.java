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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.mapping.AttributesGroupConfiguration;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.index.mapping.processor.AttributesGroupProcessor;
import io.jmix.search.utils.PropertyTools;

import java.util.List;

/**
 * Abstract base class for implementing {@link AttributesGroupProcessor}.
 * <p>
 * This class provides a foundational implementation for processing instances of
 * {@link AttributesGroupConfiguration} by utilizing {@link PropertyTools}.
 * Subclasses should provide concrete behavior for processing attributes groups
 * based on specific types of {@link AttributesGroupConfiguration}.
 *
 * @param <G> the type of {@link AttributesGroupConfiguration} processed by this processor
 *            implementation
 */
public abstract class AbstractAttributesGroupProcessor<G extends AttributesGroupConfiguration>
        implements AttributesGroupProcessor<G> {

    protected final PropertyTools propertyTools;

    protected AbstractAttributesGroupProcessor(PropertyTools propertyTools) {
        this.propertyTools = propertyTools;
    }

    public List<MappingFieldDescriptor> processAttributesGroup(MetaClass metaClass,
                                                               AttributesGroupConfiguration group,
                                                               ExtendedSearchSettings extendedSearchSettings) {
        if (!getConfigurationClass().isInstance(group)) {
            throw new IllegalArgumentException("Invalid configuration type. Expected: " + getConfigurationClass().getName() +
                    ", but was: " + group.getClass().getName());
        }
        return processAttributesGroupInternal(metaClass, (G) group, extendedSearchSettings);
    }

    protected abstract List<MappingFieldDescriptor> processAttributesGroupInternal(MetaClass metaClass,
                                                                                   G group,
                                                                                   ExtendedSearchSettings extendedSearchSettings);
}
