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

package io.jmix.restds.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class GenericRestFilterBuilder {

    private static final Logger log = LoggerFactory.getLogger(GenericRestFilterBuilder.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Nullable
    public String build(@Nullable String jsonConditions,
                        Condition rootCondition,
                        Map<String,Object> parameters) {

        ObjectNode rootNode = null;
        try {

        if (jsonConditions != null && !jsonConditions.isBlank()) {
            JsonNode inputNode = objectMapper.readTree(jsonConditions);
            JsonNode conditionsNode = inputNode.get("conditions");
            if (conditionsNode != null && conditionsNode.isArray()) {
                rootNode = (ObjectNode) inputNode;
            } else {
                rootNode = objectMapper.createObjectNode();
                if (inputNode.isArray()) {
                    rootNode.set("conditions", inputNode);
                } else {
                    ArrayNode arrayNode = objectMapper.createArrayNode();
                    arrayNode.add(inputNode);
                    rootNode.set("conditions", arrayNode);
                }
            }
        }

        Condition condition = rootCondition.actualize(Collections.emptySet(), false);
        if (condition == null)
            return rootNode == null ? null : objectMapper.writeValueAsString(rootNode);;

        // If the actualized condition does not have AND on top, wrap it again to have the required JSON in the end
        if (!(condition instanceof LogicalCondition logicalCondition) || logicalCondition.getType() != LogicalCondition.Type.AND) {
            condition = LogicalCondition.and(condition);
        }

        buildJsonNode(rootNode, condition);
        return objectMapper.writeValueAsString(rootNode);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String build(String jsonConditions) {
        try {
            JsonNode inputNode = objectMapper.readTree(jsonConditions);
            JsonNode conditionsNode = inputNode.get("conditions");
            if (conditionsNode != null && conditionsNode.isArray()) {
                return objectMapper.writeValueAsString(inputNode);
            } else {
                ObjectNode rootNode = objectMapper.createObjectNode();
                if (inputNode.isArray()) {
                    rootNode.set("conditions", inputNode);
                } else {
                    ArrayNode arrayNode = objectMapper.createArrayNode();
                    arrayNode.add(inputNode);
                    rootNode.set("conditions", arrayNode);
                }
                return objectMapper.writeValueAsString(rootNode);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public String build(Condition rootCondition) {
        Condition condition = rootCondition.actualize(Collections.emptySet(), false);
        if (condition == null)
            return null;

        // If the actualized condition does not have AND on top, wrap it again to have the required JSON in the end
        if (!(condition instanceof LogicalCondition logicalCondition) || logicalCondition.getType() != LogicalCondition.Type.AND) {
            condition = LogicalCondition.and(condition);
        }

        ObjectNode rootNode = objectMapper.createObjectNode();
        buildJsonNode(rootNode, condition);
        try {
            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildJsonNode(ObjectNode node, Condition condition) {
        if (condition instanceof LogicalCondition logicalCondition) {
            node.put("group", logicalCondition.getType().name().toLowerCase());
            JsonNode arrayNode = node.get("conditions");
            if (arrayNode == null || !arrayNode.isArray()) {
                arrayNode = objectMapper.createArrayNode();
                node.set("conditions", arrayNode);
            }
            for (Condition nestedCondition : logicalCondition.getConditions()) {
                ObjectNode nestedNode = objectMapper.createObjectNode();
                buildJsonNode(nestedNode, nestedCondition);
                ((ArrayNode) arrayNode).add(nestedNode);
            }
        } else if (condition instanceof PropertyCondition propertyCondition) {
            String restOperator = toRestOperator(propertyCondition.getOperation());
            if (restOperator != null) {
                node.put("property", propertyCondition.getProperty());
                node.put("operator", restOperator);
                node.put("value", String.valueOf(propertyCondition.getParameterValue()));
            } else {
                log.warn("Unsupported condition operation: {}", propertyCondition.getOperation());
            }
        } else {
            log.warn("Unsupported Condition type: {}", condition.getClass());
        }
    }

    @Nullable
    private String toRestOperator(String operation) {
        return switch (operation) {
            case PropertyCondition.Operation.EQUAL -> "=";
            case PropertyCondition.Operation.NOT_EQUAL -> "<>";
            case PropertyCondition.Operation.GREATER -> ">";
            case PropertyCondition.Operation.GREATER_OR_EQUAL -> ">=";
            case PropertyCondition.Operation.LESS -> "<";
            case PropertyCondition.Operation.LESS_OR_EQUAL -> "<=";
            case PropertyCondition.Operation.CONTAINS -> "contains";
            case PropertyCondition.Operation.NOT_CONTAINS -> "doesNotContain";
            case PropertyCondition.Operation.IS_SET -> "notEmpty";
            case PropertyCondition.Operation.STARTS_WITH -> "startsWith";
            case PropertyCondition.Operation.ENDS_WITH -> "endsWith";
            case PropertyCondition.Operation.IN_LIST -> "in";
            case PropertyCondition.Operation.NOT_IN_LIST -> "notIn";
            default -> null;
        };
    }
}
