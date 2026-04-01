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

package io.jmix.rest.impl.service.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jmix.core.Entity;
import io.jmix.core.EntitySerialization;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.rest.exception.RestAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

    @Autowired
    protected EntitySerialization entitySerialization;

    /**
     * Parses the JSON with entities filter and returns an equivalent {@link Condition}. The
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
    public Condition parse(String filterJson, MetaClass metaClass) throws RestFilterParseException {
        LogicalCondition rootCondition = LogicalCondition.and();

        JsonObject filterObject = JsonParser.parseString(filterJson).getAsJsonObject();
        JsonElement conditions = filterObject.get("conditions");

        if (conditions != null && conditions.isJsonArray()) {
            JsonArray conditionsJsonArray = conditions.getAsJsonArray();
            if (!conditionsJsonArray.isEmpty()) {
                for (JsonElement conditionElement : conditionsJsonArray) {
                    JsonObject conditionObject;
                    try {
                        conditionObject = conditionElement.getAsJsonObject();
                    } catch (IllegalStateException e) {
                        throw new RestAPIException("Malformed request JSON data structure",
                                "JSON array element " + conditionElement +
                                        " is not a valid JSON object literal",
                                HttpStatus.BAD_REQUEST);
                    }
                    Condition condition = parseJsonToCondition(conditionObject, metaClass);
                    rootCondition.add(condition);
                }
                return rootCondition;
            }
        }
        return LogicalCondition.and();
    }


    protected Condition parseJsonToCondition(JsonObject jsonConditionObject, MetaClass metaClass) throws RestFilterParseException {
        JsonElement group = jsonConditionObject.get("group");
        if (group != null) {
            return parseGroupCondition(jsonConditionObject, metaClass);
        } else {
            return parsePropertyCondition(jsonConditionObject, metaClass);
        }

    }

    protected Condition parseGroupCondition(JsonObject conditionJsonObject, MetaClass metaClass) throws RestFilterParseException {
        String groupName = conditionJsonObject.get("group").getAsString();

        LogicalCondition groupCondition = switch (groupName.toUpperCase()) {
            case "OR" -> LogicalCondition.or();
            case "AND" -> LogicalCondition.and();
            default -> throw new RestFilterParseException("Invalid conditions group type: " + groupName);
        };


        JsonElement conditions = conditionJsonObject.get("conditions");
        if (conditions != null) {
            for (JsonElement conditionElement : conditions.getAsJsonArray()) {
                Condition childCondition = parseJsonToCondition(conditionElement.getAsJsonObject(), metaClass);
                groupCondition.getConditions().add(childCondition);
            }
        }

        return groupCondition;
    }

    protected Condition parsePropertyCondition(JsonObject conditionJsonObject,
                                               MetaClass metaClass) throws RestFilterParseException {
        PropertyCondition condition = new PropertyCondition();

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

        RestFilterOp op = RestFilterOp.fromJson(operator);

        boolean isValueRequired = op != RestFilterOp.IS_NULL && op != RestFilterOp.NOT_EMPTY;
        JsonElement valueJsonElem = conditionJsonObject.get("value");
        if (valueJsonElem == null && isValueRequired) {
            throw new RestFilterParseException("Field 'value' is not defined for filter condition");
        }

        MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyName);
        if (propertyPath == null) {
            throw new RestFilterParseException("Property for " + metaClass.getName() + " not found: " + propertyName);
        }
        MetaProperty metaProperty = propertyPath.getMetaProperty();


        EnumSet<RestFilterOp> conditionOpsAvailableForJavaType = restFilterOpManager.availableOps(metaProperty.getJavaType());
        if (!conditionOpsAvailableForJavaType.contains(op)) {
            throw new RestFilterParseException("Operator " + operator + " is not available for java type " +
                    metaProperty.getJavaType().getCanonicalName());
        }

        if (shouldAddPkNameToPropertyPath(metaProperty, op, valueJsonElem)) {
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

        if (isValueRequired) {
            Object value;
            if (op == RestFilterOp.IN || op == RestFilterOp.NOT_IN) {
                if (!valueJsonElem.isJsonArray()) {
                    throw new RestFilterParseException("JSON array was expected as a value for condition with operator " + operator);
                }
                List<Object> parsedArrayValues = new ArrayList<>();
                for (JsonElement arrayItemElem : valueJsonElem.getAsJsonArray()) {
                    parsedArrayValues.add(parseValue(metaProperty, arrayItemElem.isJsonObject()
                            ? arrayItemElem.toString()
                            : arrayItemElem.getAsString()));
                }
                value = parsedArrayValues;
            } else {
                value = parseValue(metaProperty, valueJsonElem.isJsonObject()
                        ? valueJsonElem.toString()
                        : valueJsonElem.getAsString());
            }
            condition.setParameterValue(value);
        } else {
            if (op == RestFilterOp.IS_NULL) condition.setParameterValue(false);
            if (op == RestFilterOp.NOT_EMPTY) condition.setParameterValue(true);
        }


        condition.setProperty(propertyName);
        condition.setOperation(op.getConditionOperation());

        condition.setParameterName(PropertyConditionUtils.generateParameterName(propertyName));

        return condition;
    }

    protected boolean shouldAddPkNameToPropertyPath(MetaProperty metaProperty,
                                                    RestFilterOp op,
                                                    @Nullable JsonElement valueJsonElem) {
        return metaProperty.getRange().isClass()
                && Entity.class.isAssignableFrom(metaProperty.getJavaType())
                && !containsEntity(valueJsonElem)
                && op != RestFilterOp.IS_NULL
                && op != RestFilterOp.NOT_EMPTY;
    }

    protected boolean containsEntity(@Nullable JsonElement valueJsonElem) {
        if (valueJsonElem == null) return false;

        if (valueJsonElem.isJsonArray()) {
            JsonArray array = (JsonArray) valueJsonElem;
            return !array.isEmpty() && array.iterator().next().isJsonObject();
        }

        return valueJsonElem.isJsonObject();
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
        } else if (metaProperty.getRange().isClass()) {
            return entitySerialization.entityFromJson(stringValue, metaProperty.getRange().asClass());
        }
        throw new RestFilterParseException("Cannot parse the condition value: " + stringValue);
    }
}
