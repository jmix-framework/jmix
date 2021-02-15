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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_AutoMappingStrategy")
public class AutoMappingStrategy implements FieldMappingStrategy {

    protected AutoMapFieldMapperResolver autoMapFieldMapperResolver;

    @Autowired
    public AutoMappingStrategy(AutoMapFieldMapperResolver autoMapFieldMapperResolver) {
       this.autoMapFieldMapperResolver = autoMapFieldMapperResolver;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isSupported(MetaPropertyPath propertyPath) {
        return autoMapFieldMapperResolver.hasFieldMapper(propertyPath)
                && autoMapFieldMapperResolver.hasValueMapper(propertyPath);
    }

    @Override
    public FieldConfiguration createFieldConfiguration(MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        FieldMapper fieldMapper = autoMapFieldMapperResolver.getFieldMapper(propertyPath)
                .orElseThrow(() -> new RuntimeException("Property '" + propertyPath + "' is not supported"));
        ObjectNode jsonConfig = fieldMapper.createJsonConfiguration(parameters);
        return new NativeFieldConfiguration(jsonConfig);
    }

    @Override
    public ValueMapper getValueMapper(MetaPropertyPath propertyPath) {
        return autoMapFieldMapperResolver.getValueMapper(propertyPath)
                .orElseThrow(() -> new RuntimeException("Property '" + propertyPath + "' is not supported"));
    }
}
