/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.service.filter;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.QueryUtils;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

/**
 * Class for REST API search filter JSON parsing
 */
@Component("rest_RestFilterParser")
public class RestFilterParser {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected RestFilterOpManager restFilterOpManager;

    /**
     * Parses the JSON with entities filter and returns an object with JPQL query string and query parameters. The
     * method expects a JSON object like this:
     * <p>
     * <pre>
     * {
     *  "conditions": [
     *      {
     *          "group": "OR",
     *          "conditions": [
     *              {
     *                  "property": "stringField",
     *                  "operator": "&lt;&gt;",
     *                  "value": "stringValue"
     *              },
     *              {
     *                  "property": "intField",
     *                  "operator": "&gt;",
     *                  "value": 100
     *              }
     *          ]
     *      },
     *      {
     *          "property": "booleanField",
     *          "operator": "=",
     *          "value": true
     *      }
     *  ]
     * }
     * </pre>
     * <p>
     * Conditions here may be of two types: property condition and group condition (AND and OR) . Root conditions are
     * automatically placed to the group condition of type AND.
     */
    public RestFilterParseResult parse(String filterJson, MetaClass metaClass) throws RestFilterParseException {
        RestFilterGroupCondition rootCondition = new RestFilterGroupCondition();
        rootCondition.setType(RestFilterGroupCondition.Type.AND);

        JsonObject filterObject = new JsonParser().parse(filterJson).getAsJsonObject();
        JsonElement conditions = filterObject.get("conditions");
        if (conditions != null && conditions.isJsonArray()) {
            JsonArray conditionsJsonArray = conditions.getAsJsonArray();
            if (conditionsJsonArray.size() != 0) {
                for (JsonElement conditionElement : conditionsJsonArray) {
                    JsonObject conditionObject = conditionElement.getAsJsonObject();
                    RestFilterCondition restFilterCondition = parseConditionObject(conditionObject, metaClass);
                    rootCondition.getConditions().add(restFilterCondition);
                }
                Map<String, Object> queryParameters = new HashMap<>();
                collectQueryParameters(rootCondition, queryParameters);

                return new RestFilterParseResult(rootCondition.toJpql(), queryParameters);
            }
        }

        return new RestFilterParseResult(null, null);
    }

    protected void collectQueryParameters(RestFilterCondition condition, Map<String, Object> queryParameters) {
        if (condition instanceof RestFilterPropertyCondition) {
            //queryParamName can be empty, e.g. for notEmpty operator
            if (!Strings.isNullOrEmpty(((RestFilterPropertyCondition) condition).getQueryParamName())) {
                queryParameters.put(((RestFilterPropertyCondition) condition).getQueryParamName(), ((RestFilterPropertyCondition) condition).getValue());
            }
        } else if (condition instanceof RestFilterGroupCondition) {
            for (RestFilterCondition childCondition : ((RestFilterGroupCondition) condition).getConditions()) {
                collectQueryParameters(childCondition, queryParameters);
            }
        }
    }

    protected RestFilterCondition parseConditionObject(JsonObject jsonConditionObject, MetaClass metaClass) throws RestFilterParseException {
        JsonElement group = jsonConditionObject.get("group");
        if (group != null) {
            return parseGroupCondition(jsonConditionObject, metaClass);
        } else {
            return parsePropertyCondition(jsonConditionObject, metaClass);
        }

    }

    protected RestFilterGroupCondition parseGroupCondition(JsonObject conditionJsonObject, MetaClass metaClass) throws RestFilterParseException {
        JsonElement group = conditionJsonObject.get("group");
        String groupName = group.getAsString();
        RestFilterGroupCondition.Type type;
        try {
            type = RestFilterGroupCondition.Type.valueOf(groupName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RestFilterParseException("Invalid conditions group type: " + groupName);
        }
        RestFilterGroupCondition groupCondition = new RestFilterGroupCondition();
        groupCondition.setType(type);

        JsonElement conditions = conditionJsonObject.get("conditions");
        if (conditions != null) {
            for (JsonElement conditionElement : conditions.getAsJsonArray()) {
                RestFilterCondition childCondition = parseConditionObject(conditionElement.getAsJsonObject(), metaClass);
                groupCondition.getConditions().add(childCondition);
            }
        }

        return groupCondition;
    }

    protected RestFilterPropertyCondition parsePropertyCondition(JsonObject conditionJsonObject,
                                                                 MetaClass metaClass) throws RestFilterParseException {
        RestFilterPropertyCondition condition = new RestFilterPropertyCondition();

        JsonElement propertyJsonElem = conditionJsonObject.get("property");
        if (propertyJsonElem == null) {
            throw new RestFilterParseException("Field 'property' is not defined for filter condition");
        }
        String propertyName = propertyJsonElem.getAsString();

        JsonElement operatorJsonElem = conditionJsonObject.get("operator");
        if (operatorJsonElem == null) {
            throw new RestFilterParseException("Field 'operator' is not defined for filter condition");
        }
        String operator = operatorJsonElem.getAsString();
        RestFilterOp op = findOperator(operator);

        boolean isValueRequired = (op != RestFilterOp.NOT_EMPTY && op != RestFilterOp.IS_NULL);
        JsonElement valueJsonElem = conditionJsonObject.get("value");
        if (valueJsonElem == null && isValueRequired) {
            throw new RestFilterParseException("Field 'value' is not defined for filter condition");
        }

        MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyName);
        if (propertyPath == null) {
            throw new RestFilterParseException("Property for " + metaClass.getName() + " not found: " + propertyName);
        }
        MetaProperty metaProperty = propertyPath.getMetaProperty();

        EnumSet<RestFilterOp> opsAvailableForJavaType = restFilterOpManager.availableOps(metaProperty.getJavaType());
        if (!opsAvailableForJavaType.contains(op)) {
            throw new RestFilterParseException("Operator " + operator + " is not available for java type " +
                    metaProperty.getJavaType().getCanonicalName());
        }

        if (metaProperty.getRange().isClass()) {
            if (Entity.class.isAssignableFrom(metaProperty.getJavaType())) {
                MetaClass _metaClass = metadata.getClass(metaProperty.getJavaType());
                MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(_metaClass);
                String pkName = primaryKeyProperty.getName();
                propertyName += "." + pkName;
                propertyPath = metaClass.getPropertyPath(propertyName);
                if (propertyPath == null) {
                    throw new RestFilterParseException("Property " + propertyName + " for " + metaClass.getName() + " not found");
                }
                metaProperty = propertyPath.getMetaProperty();
            }
        }

        if (isValueRequired) {
            Object value = null;
            if (op == RestFilterOp.IN || op == RestFilterOp.NOT_IN) {
                if (!valueJsonElem.isJsonArray()) {
                    throw new RestFilterParseException("JSON array was expected as a value for condition with operator " + operator);
                }
                List<Object> parsedArrayValues = new ArrayList<>();
                for (JsonElement arrayItemElem : valueJsonElem.getAsJsonArray()) {
                    parsedArrayValues.add(parseValue(metaProperty, arrayItemElem.getAsString()));
                }
                value = parsedArrayValues;
            } else {
                value = parseValue(metaProperty, valueJsonElem.getAsString());
            }
            condition.setValue(transformValue(value, op));
            condition.setQueryParamName(generateQueryParamName());
        }

        condition.setPropertyName(propertyName);
        condition.setOperator(op);

        return condition;
    }

    protected Object parseValue(MetaProperty metaProperty, String stringValue) throws RestFilterParseException {
        if (metaProperty.getRange().isDatatype()) {
            try {
                return metaProperty.getRange().asDatatype().parse(stringValue);
            } catch (ParseException e) {
                throw new RestFilterParseException("Cannot parse property value: " + stringValue, e);
            }
        } else if (metaProperty.getRange().isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) metaProperty.getJavaType(), stringValue);
            } catch (IllegalArgumentException e) {
                throw new RestFilterParseException("Cannot parse enum value: " + stringValue, e);
            }
        }
        throw new RestFilterParseException("Cannot parse the condition value: " + stringValue);
    }

    protected RestFilterOp findOperator(String stringOp) throws RestFilterParseException {
        switch (stringOp) {
            case "=":
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "<>":
                return RestFilterOp.fromJpqlString(stringOp);
            case "startsWith":
                return RestFilterOp.STARTS_WITH;
            case "endsWith":
                return RestFilterOp.ENDS_WITH;
            case "contains":
                return RestFilterOp.CONTAINS;
            case "doesNotContain":
                return RestFilterOp.DOES_NOT_CONTAIN;
            case "in":
                return RestFilterOp.IN;
            case "notIn":
                return RestFilterOp.NOT_IN;
            case "notEmpty":
                return RestFilterOp.NOT_EMPTY;
            case "isNull":
                return RestFilterOp.IS_NULL;
        }
        throw new RestFilterParseException("Operator is not supported: " + stringOp);
    }

    protected Object transformValue(Object value, RestFilterOp operator) {
        switch (operator) {
            case CONTAINS:
            case DOES_NOT_CONTAIN:
                return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + QueryUtils.escapeForLike((String) value) + "%";
            case STARTS_WITH:
                return QueryUtils.CASE_INSENSITIVE_MARKER + QueryUtils.escapeForLike((String) value) + "%";
            case ENDS_WITH:
                return QueryUtils.CASE_INSENSITIVE_MARKER + "%" + QueryUtils.escapeForLike((String) value);
        }

        return value;
    }

    protected String generateQueryParamName() {
        return RandomStringUtils.randomAlphabetic(10);
    }
}
