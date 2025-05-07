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

package io.jmix.masquerade.component;

import com.codeborne.selenide.CheckResult;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.condition.TimeValue;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.exactValue;
import static io.jmix.masquerade.JConditions.*;

/**
 * Web-element wrapper for time picker. Supports setting value and {@link TimeValue} condition checking.
 */
public class TimePicker extends AbstractField<TimePicker> {

    public TimePicker(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof TimeValue timeValue) {
            getInputDelegate().shouldHave(exactValue(timeValue.getValue()));
            return CheckResult.accepted();
        }

        return super.check(condition);
    }

    /**
     * Sets the value to the time picker input field.
     *
     * @param value time value as a string presentation value
     * @return {@code this} to call fluent API
     */
    public TimePicker setValue(String value) {
        getInputDelegate()
                .shouldBe((VISIBLE))
                .shouldBe(ENABLED)
                .shouldNotBe(READONLY)
                .setValue(value)
                .pressEnter();

        return this;
    }
}
