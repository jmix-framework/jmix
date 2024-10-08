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
 *
 */

package io.jmix.rest.impl.service.filter;

import io.jmix.core.querycondition.PropertyCondition;

public enum RestFilterOp {

    EQUAL("=", PropertyCondition.Operation.EQUAL),
    IN("in", PropertyCondition.Operation.IN_LIST),
    NOT_IN("notIn", PropertyCondition.Operation.NOT_IN_LIST),
    NOT_EQUAL("<>", PropertyCondition.Operation.NOT_EQUAL),
    GREATER(">", PropertyCondition.Operation.GREATER),
    GREATER_OR_EQUAL(">=", PropertyCondition.Operation.GREATER_OR_EQUAL),
    LESSER("<", PropertyCondition.Operation.LESS),
    LESSER_OR_EQUAL("<=", PropertyCondition.Operation.LESS_OR_EQUAL),
    DOES_NOT_CONTAIN("doesNotContain", PropertyCondition.Operation.NOT_CONTAINS),
    NOT_EMPTY("notEmpty", PropertyCondition.Operation.IS_SET),
    IS_NULL("isNull", PropertyCondition.Operation.IS_SET),
    STARTS_WITH("startsWith", PropertyCondition.Operation.STARTS_WITH),
    CONTAINS("contains", PropertyCondition.Operation.CONTAINS),
    ENDS_WITH("endsWith", PropertyCondition.Operation.ENDS_WITH);


    private String conditionOperation;
    private String jsonValue;

    RestFilterOp(String jsonValue, String conditionOperation) {
        this.jsonValue = jsonValue;
        this.conditionOperation = conditionOperation;
    }

    public static RestFilterOp fromJson(String jsonValue) throws RestFilterParseException {
        for (RestFilterOp op : values()) {
            if (op.jsonValue.equals(jsonValue))
                return op;
        }
        throw new RestFilterParseException("Operator is not supported: " + jsonValue);
    }


    public String getConditionOperation(){
        return conditionOperation;
    }
}
