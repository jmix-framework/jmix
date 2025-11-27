/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.mapping;

import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.index.annotation.ManualMappingDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains information about indexed properties defined within index definition interface
 * marked with {@link JmixEntitySearchIndex}
 * <p>
 * It can be created automatically according to field-mapping annotations used in index definition
 * or manually within mapping method implementation.
 * Such method should fulfil the following requirements:
 * <ul>
 *     <li>Annotated with {@link ManualMappingDefinition}</li>
 *     <li>with any name</li>
 *     <li>default</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>With Spring beans as parameters</li>
 * </ul>
 * <p>
 * {@link MappingDefinition#builder()}, {@link StaticAttributesGroupConfiguration#builder()}
 * and {@link DynamicAttributesGroupConfiguration#builder()} should be used to create content.
 * <p>
 * Example:<pre>
 * &#64;JmixEntitySearchIndex(entity = Customer.class)
 * public interface CustomerIndexDefinition {
 *
 *     &#64;ManualMappingDefinition
 *     default MappingDefinition mapping(AutoMappingStrategy autoMappingStrategy,
 *                                       SimplePropertyValueExtractor simplePropertyValueExtractor) {
 *         return MappingDefinition.builder()
 *                 .addStaticAttributesGroup(
 *                         StaticAttributesGroupConfiguration.builder()
 *                                 .includeProperties("*")
 *                                 .excludeProperties("name", "description")
 *                                 .withFieldMappingStrategyClass(AutoMappingStrategy.class)
 *                                 .build()
 *                 )
 *                 .addStaticAttributesGroup(
 *                         StaticAttributesGroupConfiguration.builder()
 *                                 .includeProperties("name")
 *                                 .withFieldMappingStrategy(autoMappingStrategy)
 *                                 .withFieldConfiguration(
 *                                         "{\n" +
 *                                         "    \"type\": \"text\",\n" +
 *                                         "    \"analyzer\": \"standard\",\n" +
 *                                         "    \"boost\": 2\n" +
 *                                         "}"
 *                                 )
 *                                 .build()
 *                 )
 *                 .addStaticAttributesGroup(
 *                         StaticAttributesGroupConfiguration.builder()
 *                                 .includeProperties("description")
 *                                 .withFieldConfiguration(
 *                                         "{\n" +
 *                                         "    \"type\": \"text\",\n" +
 *                                         "    \"analyzer\": \"english\"\n" +
 *                                         "}"
 *                                 )
 *                                 .withPropertyValueExtractor(simplePropertyValueExtractor)
 *                                 .withOrder(1)
 *                                 .build()
 *                 )
 *                 .addDynamicAttributesGroup(
 *                         DynamicAttributesGroupConfiguration.builder()
 *                                 .excludeProperties("prefix1*")
 *                                 .excludeCategories("categoryPrefix1*")
 *                                 .withFieldMappingStrategyClass(AutoMappingStrategy.class)
 *                                 .build()
 *                 )
 *                 .addDynamicAttributesGroup(
 *                         DynamicAttributesGroupConfiguration.builder()
 *                                 .excludeProperties("prefix2*", "*infix*")
 *                                 .withReferenceAttributesIndexingMode(ReferenceAttributesIndexingMode.NONE)
 *                                 .addParameter("analyzer", "english")
 *                                 .withPropertyValueExtractor(simplePropertyValueExtractor)
 *                                 .withFieldConfiguration(
 *                                         "{\n" +
 *                                         "    \"type\": \"text\",\n" +
 *                                         "    \"analyzer\": \"english\"\n" +
 *                                         "}"
 *                                 ) *
 *                                 .build()
 *                 )
 *                 .build();
 *     }
 * }
 * </pre>
 * <p>
 * <b>Note:</b> if definition method has implementation any field-mapping annotations on it will be ignored
 */
public class MappingDefinition {

    protected Map<Class<? extends AttributesGroupConfiguration>, List<? extends AttributesGroupConfiguration>> attributesGroupConfigurationMap =
            new HashMap<>();

    protected MappingDefinition(MappingDefinitionBuilder builder) {
        attributesGroupConfigurationMap.put(StaticAttributesGroupConfiguration.class, builder.staticGroups);
        attributesGroupConfigurationMap.put(DynamicAttributesGroupConfiguration.class, builder.dynamicGroups);
    }

    /**
     * Gets all {@link StaticAttributesGroupConfiguration}
     * @deprecated use the {@link MappingDefinition#getMappingConfigurations(Class)}}
     *
     * @return List of {@link MappingDefinitionElement}
     */
    @Deprecated
    public List<StaticAttributesGroupConfiguration> getElements() {
        return (List<StaticAttributesGroupConfiguration>) attributesGroupConfigurationMap.get(StaticAttributesGroupConfiguration.class);
    }

    public <T extends AttributesGroupConfiguration> List<T> getMappingConfigurations(Class<T> configurationType) {
        return (List<T>) attributesGroupConfigurationMap.get(configurationType);
    }

    public static MappingDefinitionBuilder builder() {
        return new MappingDefinitionBuilder();
    }

    public static class MappingDefinitionBuilder {

        private final List<StaticAttributesGroupConfiguration> staticGroups = new ArrayList<>();
        private final List<DynamicAttributesGroupConfiguration> dynamicGroups = new ArrayList<>();

        @Deprecated
        public MappingDefinitionBuilder addElement(StaticAttributesGroupConfiguration element) {
            staticGroups.add(element);
            return this;
        }

        public MappingDefinitionBuilder addStaticAttributesGroup(StaticAttributesGroupConfiguration group) {
            staticGroups.add(group);
            return this;
        }

        public MappingDefinitionBuilder addDynamicAttributesGroup(DynamicAttributesGroupConfiguration group) {
            dynamicGroups.add(group);
            return this;
        }

        public MappingDefinition build() {
            return new MappingDefinition(this);
        }
    }
}
