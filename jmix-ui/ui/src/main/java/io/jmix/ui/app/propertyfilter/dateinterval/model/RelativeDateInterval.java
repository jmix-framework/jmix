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

package io.jmix.ui.app.propertyfilter.dateinterval.model;

/**
 * Class describes date interval with relative date and time moments {@link Type#RELATIVE}.
 */
public class RelativeDateInterval implements BaseDateInterval {

    protected final Operation operation;
    protected final String relativeDateTimeMomentName;

    public RelativeDateInterval(Operation operation, String relativeDateTimeMomentName) {
        this.operation = operation;
        this.relativeDateTimeMomentName = relativeDateTimeMomentName;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getOperationValue() {
        return operation.getValue();
    }

    public String getRelativeDateTimeMomentName() {
        return relativeDateTimeMomentName;
    }

    @Override
    public Type getType() {
        return Type.RELATIVE;
    }

    @Override
    public String apply(String property) {
        return String.format("{E}.%s %s %s", property, operation.getValue(), relativeDateTimeMomentName);
    }

    /**
     * Operation that can be available for relative date and time moments.
     */
    public enum Operation {

        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER(">"),
        GREATER_OR_EQUAL(">="),
        LESS("<"),
        LESS_OR_EQUAL("<=");

        String value;

        Operation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Operation fromValue(String value) {
            for (Operation operation : values()) {
                if (operation.getValue().equals(value)) {
                    return operation;
                }
            }
            throw new IllegalArgumentException("There is no Operation for the value: '" + value + "'");
        }
    }
}
