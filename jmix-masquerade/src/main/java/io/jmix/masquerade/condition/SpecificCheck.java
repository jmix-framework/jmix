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

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;

/**
 * Description of how a {@link SpecificCondition} must be checked, produced by a {@link SpecificConditionHandler}.
 * <p>
 * A handler only <i>describes</i> the check and never asserts on a value mismatch by itself. The actual
 * ACCEPT/REJECT {@link CheckResult} verdict is computed centrally by {@link SpecificCondition#check}, so that
 * specific conditions can be negated ({@code shouldNot*}) and composed ({@code and}/{@code or}).
 */
public interface SpecificCheck {

    /**
     * Computes the verdict of this check.
     *
     * @param driver selenide driver
     * @return verdict of the check
     */
    CheckResult evaluate(Driver driver);

    /**
     * Creates a check that evaluates the given standard Selenide {@code condition} against the {@code element}.
     *
     * @param element   element to evaluate the condition against
     * @param condition standard Selenide condition to evaluate
     * @return element-backed check
     */
    static SpecificCheck of(SelenideElement element, WebElementCondition condition) {
        return new ElementCheck(element, condition);
    }

    /**
     * Creates a check with a precomputed verdict, for conditions that are not backed by a single Selenide
     * condition (e.g. collection conditions).
     *
     * @param matched     whether the condition is matched
     * @param actualValue actual value to report in case of a mismatch
     * @return precomputed check
     */
    static SpecificCheck of(boolean matched, Object actualValue) {
        return new ComputedCheck(matched, actualValue);
    }

    /**
     * {@link SpecificCheck} that evaluates a standard Selenide {@link WebElementCondition} against an element.
     *
     * @param element   element to evaluate the condition against
     * @param condition standard Selenide condition to evaluate
     */
    record ElementCheck(SelenideElement element, WebElementCondition condition) implements SpecificCheck {

        @Override
        public CheckResult evaluate(Driver driver) {
            return condition.check(driver, element.getWrappedElement());
        }
    }

    /**
     * {@link SpecificCheck} with a precomputed verdict.
     *
     * @param matched     whether the condition is matched
     * @param actualValue actual value to report in case of a mismatch
     */
    record ComputedCheck(boolean matched, Object actualValue) implements SpecificCheck {

        @Override
        public CheckResult evaluate(Driver driver) {
            return new CheckResult(matched, actualValue);
        }
    }
}
