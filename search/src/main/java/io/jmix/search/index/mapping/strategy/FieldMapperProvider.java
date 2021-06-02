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

package io.jmix.search.index.mapping.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("search_FieldMapperProvider")
public class FieldMapperProvider {

    protected Map<Class<? extends FieldMapper>, FieldMapper> registry;

    @Autowired
    public FieldMapperProvider(List<FieldMapper> fieldMappers) {
        registry = fieldMappers.stream().collect(Collectors.toMap(FieldMapper::getClass, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends FieldMapper> T getFieldMapper(Class<T> fieldMapperClass) {
        return (T) registry.get(fieldMapperClass);
    }
}
