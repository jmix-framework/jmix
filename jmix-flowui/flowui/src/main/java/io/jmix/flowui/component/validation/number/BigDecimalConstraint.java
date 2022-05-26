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

public class BigDecimalConstraint implements NumberConstraint {

    protected BigDecimal value;

    public BigDecimalConstraint(BigDecimal value) {
        this.value = value;
    }

    @Override
    public boolean isMax(long max) {
        return compareValueWith(max) <= 0;
    }

    @Override
    public boolean isMin(long min) {
        return compareValueWith(min) >= 0;
    }

    @Override
    public boolean isDigits(int integer, int fraction) {
        BigDecimal bigDecimal = value.stripTrailingZeros();

        int integerLength = bigDecimal.precision() - bigDecimal.scale();
        int fractionLength = Math.max(bigDecimal.scale(), 0);

        return integer >= integerLength && fraction >= fractionLength;
    }

    @Override
    public boolean isDecimalMax(BigDecimal max, boolean inclusive) {
        if (inclusive) {
            return compareValueWith(max) <= 0;
        } else {
            return compareValueWith(max) < 0;
        }
    }

    @Override
    public boolean isDecimalMin(BigDecimal min, boolean inclusive) {
        if (inclusive) {
            return compareValueWith(min) >= 0;
        } else {
            return compareValueWith(min) > 0;
        }
    }

    @Override
    public boolean isNegativeOrZero() {
        return value.signum() <= 0;
    }

    @Override
    public boolean isNegative() {
        return value.signum() < 0;
    }

    @Override
    public boolean isPositiveOrZero() {
        return value.signum() >= 0;
    }

    @Override
    public boolean isPositive() {
        return value.signum() > 0;
    }

    @Override
    public boolean isDoubleMax(Double max, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDoubleMin(Double min, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    protected int compareValueWith(long val) {
        return this.value.compareTo(BigDecimal.valueOf(val));
    }

    private int compareValueWith(BigDecimal val) {
        return this.value.compareTo(val);
    }
}
