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
import java.util.List;

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
 * {@link MappingDefinition#builder()} and {@link MappingDefinitionElement#builder()} should be used to create content.
 * <p>
 * Example:<pre>
 * &#64;JmixEntitySearchIndex(entity = Customer.class)
 * public interface CustomerIndexDefinition {
 *
 *     &#64;ManualMappingDefinition
 *     default MappingDefinition mapping(AutoMappingStrategy autoMappingStrategy,
 *                                       SimplePropertyValueExtractor simplePropertyValueExtractor) {
 *         return MappingDefinition.builder()
 *                 .addElement(
 *                         MappingDefinitionElement.builder()
 *                                 .includeProperties("*")
 *                                 .excludeProperties("name", "description")
 *                                 .withFieldMappingStrategyClass(AutoMappingStrategy.class)
 *                                 .build()
 *                 )
 *                 .addElement(
 *                         MappingDefinitionElement.builder()
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
 *                 .addElement(
 *                         MappingDefinitionElement.builder()
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
 *                 .build();
 *     }
 * }
 * </pre>
 * <p>
 * <b>Note:</b> if definition method has implementation any field-mapping annotations on it will be ignored
 */
public class MappingDefinition {

    protected List<MappingDefinitionElement> elements;

    protected MappingDefinition(MappingDefinitionBuilder builder) {
        this.elements = builder.elements;
    }

    /**
     * Gets all {@link MappingDefinitionElement}
     *
     * @return List of {@link MappingDefinitionElement}
     */
    public List<MappingDefinitionElement> getElements() {
        return elements;
    }

    public static MappingDefinitionBuilder builder() {
        return new MappingDefinitionBuilder();
    }

    public static class MappingDefinitionBuilder {
        private final List<MappingDefinitionElement> elements = new ArrayList<>();

        public MappingDefinitionBuilder addElement(MappingDefinitionElement element) {
            elements.add(element);
            return this;
        }

        public MappingDefinition build() {
            return new MappingDefinition(this);
        }
    }
}
