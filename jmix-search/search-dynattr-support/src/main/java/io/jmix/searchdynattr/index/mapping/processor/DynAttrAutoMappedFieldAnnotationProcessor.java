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

package io.jmix.searchdynattr.index.mapping.processor;

import io.jmix.search.index.mapping.processor.impl.AutoMappedFieldAnnotationProcessor;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategy;
import io.jmix.searchdynattr.index.mapping.strategy.DynAttrExtendedAutoMappingStrategy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("search_dynattr_support_DynAttrAutoMappedFieldAnnotationProcessor")
public class DynAttrAutoMappedFieldAnnotationProcessor extends AutoMappedFieldAnnotationProcessor {
    @Override
    protected Class<? extends FieldMappingStrategy> getFieldMappingStrategyClass() {
        return DynAttrExtendedAutoMappingStrategy.class;
    }
}
