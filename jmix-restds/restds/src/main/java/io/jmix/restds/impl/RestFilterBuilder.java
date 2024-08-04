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

@Component("restds_RestFilterBuilder")
public class RestFilterBuilder {

    private static final Logger log = LoggerFactory.getLogger(RestFilterBuilder.class);

    public static final String PARAMETER_FIELD = "parameterName";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Nullable
    public String build(@Nullable String query,
                        @Nullable Condition condition,
                        @Nullable Map<String,Object> parameters) {

        ObjectNode rootNode = null;
        try {
            if (query != null && !query.isBlank()) {
                JsonNode inputNode = objectMapper.readTree(query);
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
                if (parameters != null) {
                    substituteParameters(rootNode, parameters);
                }
            }

            Condition actualCondition = condition == null ? null : condition.actualize(Collections.emptySet(), false);
            if (actualCondition != null) {
                // If the actualized condition does not have AND on top, wrap it again to have the required JSON in the end
                if (!(actualCondition instanceof LogicalCondition logicalCondition) || logicalCondition.getType() != LogicalCondition.Type.AND) {
                    actualCondition = LogicalCondition.and(actualCondition);
                }
                if (rootNode == null) {
                    rootNode = objectMapper.createObjectNode();
                }
                buildJsonNode(rootNode, actualCondition);
                return objectMapper.writeValueAsString(rootNode);
            } else {
                return rootNode == null ? null : objectMapper.writeValueAsString(rootNode);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void substituteParameters(JsonNode node, Map<String, Object> parameters) throws JsonProcessingException {
        // traverse the JSON tree and substitute parameters
        if (node.has(PARAMETER_FIELD)) {
            String parameterName = node.get(PARAMETER_FIELD).asText();
            if (parameters.containsKey(parameterName)) {
                // put the parameter value as a string without quotes
                String parameterValue = objectMapper.writeValueAsString(parameters.get(parameterName));
                if (parameterValue.startsWith("\"") && parameterValue.endsWith("\"")) {
                    parameterValue = parameterValue.substring(1, parameterValue.length() - 1);
                }
                ((ObjectNode) node).put("value", parameterValue);
                // remove the parameter node
                ((ObjectNode) node).remove(PARAMETER_FIELD);
            }
        }
        for (JsonNode childNode : node) {
            substituteParameters(childNode, parameters);
        }
    }

    @Nullable
    public String build(String query) {
        return build(query, null, null);
    }

    @Nullable
    public String build(Condition condition) {
        return build(null, condition, null);
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
