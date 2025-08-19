/*
 * Copyright 2021 Haulmont.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes details of mapping for entity property or group of properties.
 * Equivalent of single field-mapping annotation.
 */
@Deprecated
public class MappingDefinitionElement extends StaticAttributesConfigurationGroup {

    protected MappingDefinitionElement(StaticAttributeGroupBuilder builder) {
        super(builder);
    }
}
