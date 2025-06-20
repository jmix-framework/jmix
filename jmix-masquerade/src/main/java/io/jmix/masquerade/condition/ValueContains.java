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
 * Condition for checking contained value for web-element wrappers for components that can have a value.
 */
public class ValueContains extends SpecificCondition {

    protected String expectedValueSubstring;

    public ValueContains(String expectedValueSubstring) {
        super("valueContains");
        this.expectedValueSubstring = expectedValueSubstring;
    }

    /**
     * @return value
     */
    public String getValue() {
        return expectedValueSubstring;
    }

    @Override
    public String toString() {
        return "%s='%s'".formatted(getName(), getValue());
    }
}
