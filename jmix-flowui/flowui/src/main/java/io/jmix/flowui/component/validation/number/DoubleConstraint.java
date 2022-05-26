/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.validation.number;

import java.math.BigDecimal;

public class DoubleConstraint implements NumberConstraint {

    protected Double value;

    public DoubleConstraint(Double value) {
        this.value = value;
    }

    @Override
    public boolean isMax(long max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMin(long min) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDigits(int integer, int fraction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDecimalMax(BigDecimal max, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDecimalMin(BigDecimal min, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNegativeOrZero() {
        return value <= 0;
    }

    @Override
    public boolean isNegative() {
        return value < 0;
    }

    @Override
    public boolean isPositiveOrZero() {
        return value >= 0;
    }

    @Override
    public boolean isPositive() {
        return value > 0;
    }

    @Override
    public boolean isDoubleMax(Double max, boolean inclusive) {
        if (inclusive) {
            return compareValueWith(max) <= 0;
        } else {
            return compareValueWith(max) < 0;
        }
    }

    @Override
    public boolean isDoubleMin(Double min, boolean inclusive) {
        if (inclusive) {
            return compareValueWith(min) >= 0;
        } else {
            return compareValueWith(min) > 0;
        }
    }

    private int compareValueWith(Double val) {
        return this.value.compareTo(val);
    }
}
