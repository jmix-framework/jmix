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

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

/**
 * Abstract class for specific conditions.
 * <p>
 * Specific conditions can only be checked by {@link SpecificConditionHandler}.
 * Each {@link SpecificConditionHandler} must implement its own check for each specific condition if it supports it.
 */
public abstract class SpecificCondition extends WebElementCondition {

    protected SpecificCondition(String name) {
        super(name);
    }

    /**
     * Delegates the checking of a specific condition to {@link SpecificConditionHandler} and returns the result.
     *
     * @param driver  selenide driver
     * @param element given WebElement
     * @return {@link CheckResult.Verdict#ACCEPT} if element matches condition,
     * or {@link CheckResult.Verdict#REJECT} if element doesn't match (and we should keep trying until timeout).
     */
    @Override
    public CheckResult check(Driver driver, WebElement element) {
        SpecificConditionHandler handler = SpecificConditionSupport.getHandler();
        if (handler == null) {
            throw new IllegalStateException(
                    "%s must be checked only for %s implementations"
                            .formatted(
                                    SpecificCondition.class.getSimpleName(),
                                    SpecificConditionHandler.class.getSimpleName()
                            )
            );
        }

        return handler.check(this);
    }
}
