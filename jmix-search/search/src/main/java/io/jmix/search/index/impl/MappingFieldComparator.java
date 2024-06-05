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

package io.jmix.search.index.impl;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.isNull;

@Component("search_MappingFieldComparator")
public class MappingFieldComparator {
    public static final String TYPE_KEY = "type";

    public boolean isLeafField(@NotNull Map<String, Object> mapping) {
        return mapping.get(TYPE_KEY) != null && mapping.get(TYPE_KEY) instanceof String;
    }

    public IndexMappingComparator.MappingComparingResult compareLeafFields(@NotNull Map<String, Object> searchIndexMapping, @NotNull Map<String, Object> applicationMapping) {
        if (isNull(searchIndexMapping) || isNull(applicationMapping)) throw new NullPointerException();
        return searchIndexMapping.equals(applicationMapping) ? IndexMappingComparator.MappingComparingResult.EQUAL : IndexMappingComparator.MappingComparingResult.NOT_COMPATIBLE;
    }
}
