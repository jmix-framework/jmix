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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component("search_JsonNodesComparator")
public class JsonNodesComparator {
    private static final Logger log = LoggerFactory.getLogger(JsonNodesComparator.class);

    protected boolean nodeContains(ObjectNode containerNode, ObjectNode contentNode) {
        log.trace("Check if node {} contains {}", containerNode, contentNode);
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = contentNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            String fieldName = entry.getKey();
            log.trace("Check field '{}'", fieldName);
            JsonNode contentFieldValue = entry.getValue();
            JsonNode containerFieldValue;
            if (containerNode.has(fieldName)) {
                log.trace("Container has field '{}'", fieldName);
                containerFieldValue = containerNode.get(fieldName);
            } else {
                log.trace("Container doesn't have field '{}'. STOP - FALSE", fieldName);
                return false;
            }

            if (!contentFieldValue.getNodeType().equals(containerFieldValue.getNodeType())) {
                log.trace("Type of container field ({}) doesn't match the type of content field ({}). STOP - FALSE", containerFieldValue.getNodeType(), contentFieldValue.getNodeType());
                return false;
            }

            if (contentFieldValue.isObject() && containerFieldValue.isObject()) {
                log.trace("Both container and content field is objects - check nested structure");
                boolean nestedResult = nodeContains((ObjectNode) containerFieldValue, (ObjectNode) contentFieldValue);
                if (!nestedResult) {
                    log.trace("Structures of the nested objects ({}) are different. STOP - FALSE", fieldName);
                    return false;
                }
            } else if (!containerFieldValue.equals(contentFieldValue)) {
                log.trace("Content of nodes ({}) is different. STOP - FALSE", fieldName);
                return false;
            }
        }
        log.trace("Structures are the same. TRUE");
        return true;
    }
}