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

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum RestFilterOp implements EnumClass<String> {
    CONTAINS("like"),
    EQUAL("="),
    IN("in"),
    NOT_IN("not in"),
    NOT_EQUAL("<>"),
    GREATER(">"),
    GREATER_OR_EQUAL(">="),
    LESSER("<"),
    LESSER_OR_EQUAL("<="),
    DOES_NOT_CONTAIN("not like"),
    NOT_EMPTY("is not null"),
    STARTS_WITH("like"),
    DATE_INTERVAL(""),
    ENDS_WITH("like"),
    IS_NULL("is null");

    private String forJpql;

    RestFilterOp(String forJpql) {
        this.forJpql = forJpql;
    }

    public String forJpql() {
        return forJpql;
    }

    public static RestFilterOp fromJpqlString(String str) {
        for (RestFilterOp op : values()) {
            if (op.forJpql.equals(str))
                return op;
        }
        throw new UnsupportedOperationException("Unsupported operation: " + str);
    }

    @Override
    public String getId() {
        return name();
    }
}
