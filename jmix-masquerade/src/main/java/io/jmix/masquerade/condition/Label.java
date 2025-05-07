/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.condition;

/**
 * Condition for checking the label value of web-element wrappers for field components.
 */
public class Label extends SpecificCondition {

    protected String labelValue;

    public Label(String labelValue) {
        super("label");
        this.labelValue = labelValue;
    }

    /**
     * @return label value
     */
    public String getValue() {
        return labelValue;
    }

    @Override
    public String toString() {
        return "%s='%s'".formatted(getName(), getValue());
    }
}
