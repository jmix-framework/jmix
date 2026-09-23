/*
 * Copyright 2026 Haulmont.
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

import io.jmix.masquerade.component.RangeSlider;

/**
 * Condition for checking the start and end values of range components web-element wrappers
 * (e.g. {@link RangeSlider}).
 */
public class RangeValue extends SpecificCondition {

    protected String startValue;
    protected String endValue;

    public RangeValue(String startValue, String endValue) {
        super("rangeValue");

        this.startValue = startValue;
        this.endValue = endValue;
    }

    /**
     * @return start value as a string presentation value
     */
    public String getStartValue() {
        return startValue;
    }

    /**
     * @return end value as a string presentation value
     */
    public String getEndValue() {
        return endValue;
    }

    @Override
    public String toString() {
        return "%s='[%s, %s]'".formatted(getName(), getStartValue(), getEndValue());
    }
}
