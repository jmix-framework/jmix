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

package io.jmix.search.index.mapping.processor;

import io.jmix.search.index.annotation.JmixEntitySearchIndex;

import java.util.List;

/**
 * Contains information about indexed properties defined via field-mapping annotations on methods
 * within index definition interface marked with {@link JmixEntitySearchIndex}
 * Also it can be directly created in method implementation.
 * Method should fulfil the following requirements:
 * <ul>
 *     <li>Static</li>
 *     <li>With return type - {@link MappingDefinition}</li>
 *     <li>Without parameters</li>
 * </ul>
 * <p><b>Note:</b> if definition method has implementation any field-mapping annotations on it will be ignored
 */
public class MappingDefinition { //todo create builder

    protected List<MappingDefinitionElement> elements;

    public MappingDefinition() {
    }

    /**
     * Gets all {@link MappingDefinitionElement}
     *
     * @return List of {@link MappingDefinitionElement}
     */
    public List<MappingDefinitionElement> getElements() {
        return elements;
    }

    //todo should be removed after builder is created
    public void setElements(List<MappingDefinitionElement> elements) {
        this.elements = elements;
    }
}
