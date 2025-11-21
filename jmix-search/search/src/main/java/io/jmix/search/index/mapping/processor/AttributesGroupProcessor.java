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

package io.jmix.search.index.mapping.processor;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.index.mapping.AttributesGroupConfiguration;
import io.jmix.search.index.mapping.ExtendedSearchSettings;
import io.jmix.search.index.mapping.MappingFieldDescriptor;

import java.util.List;

/**
 * Defines the contract for processing groups of attributes specified by {@link AttributesGroupConfiguration}.
 * <p>
 * Implementations of this interface are responsible for generating a list of {@link MappingFieldDescriptor}
 * objects that describe the mapping of entity attributes to index fields, based on the provided configuration
 * and settings.
 *
 * @param <G> the type of {@link AttributesGroupConfiguration} processed by this processor
 */
public interface AttributesGroupProcessor<G extends AttributesGroupConfiguration> {

    Class<G> getConfigurationClass();

    List<MappingFieldDescriptor> processAttributesGroup(MetaClass metaClass,
                                                        AttributesGroupConfiguration group,
                                                        ExtendedSearchSettings extendedSearchSettings);
}
