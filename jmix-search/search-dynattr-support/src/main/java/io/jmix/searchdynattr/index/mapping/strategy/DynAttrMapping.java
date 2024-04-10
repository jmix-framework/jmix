/*
 * Copyright 2024 Haulmont.
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

package io.jmix.searchdynattr.index.mapping.strategy;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.FieldConfiguration;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;

import java.util.Map;

public class DynAttrMapping implements FieldMappingStrategy {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isSupported(MetaPropertyPath propertyPath) {
        return false;
    }

    @Override
    public FieldConfiguration createFieldConfiguration(MetaPropertyPath propertyPath, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public PropertyValueExtractor getPropertyValueExtractor(MetaPropertyPath propertyPath) {
        return null;
    }
}
