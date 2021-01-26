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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReferenceFieldMapper extends BaseFieldMapper {

    @Override
    public Set<String> getSupportedParameters() {
        return new HashSet<>();
    }

    @Override
    public ObjectNode createJsonConfiguration(Map<String, Object> parameters) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.putObject("properties").putObject("_instance_name").put("type", "text");
        return root;
    }
}
