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

/**
 * Base interface for number type validators which contains all numeric restrictions.
 */
public interface NumberConstraint {

    /**
     * @param max max value
     * @return true if value less than or equal to max
     */
    boolean isMax(long max);

    /**
     * @param min min value
     * @return true if value greater than or equal to min
     */
    boolean isMin(long min);

    /**
     * @param integer  value of integer part
     * @param fraction value of fraction part
     * @return true if value within accepted range
     */
    boolean isDigits(int integer, int fraction);

    /**
     * @param max       max value
     * @param inclusive inclusive option, true by default
     * @return true if value less than or equal to max (depends on inclusive option)
     */
    boolean isDecimalMax(BigDecimal max, boolean inclusive);

    /**
     * @param min       min value
     * @param inclusive inclusive option, true by default
     * @return true if value less than or equal to min (depends on inclusive option)
     */
    boolean isDecimalMin(BigDecimal min, boolean inclusive);

    /**
     * @return true if value is less than or equal to 0
     */
    boolean isNegativeOrZero();

    /**
     * @return true if value is strictly less than 0
     */
    boolean isNegative();

    /**
     * @return true if value is greater than or equal to 0
     */
    boolean isPositiveOrZero();

    /**
     * @return true if value is strictly greater than 0
     */
    boolean isPositive();

    /**
     * @param max       max value
     * @param inclusive inclusive option, true by default
     * @return true if value less than or equal to max (depends on inclusive option)
     */
    boolean isDoubleMax(Double max, boolean inclusive);

    /**
     * @param min       min value
     * @param inclusive inclusive option, true by default
     * @return true if value less than or equal to min (depends on inclusive option)
     */
    boolean isDoubleMin(Double min, boolean inclusive);
}
