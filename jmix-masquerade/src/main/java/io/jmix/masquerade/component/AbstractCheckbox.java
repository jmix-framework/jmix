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
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.jmix.masquerade.condition.Label;
import io.jmix.masquerade.condition.SpecificCondition;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.xpath;

/**
 * Abstract class for checkbox-like web-element wrappers. Supports {@link Label} condition checking.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractCheckbox<T extends AbstractComponent<T>> extends AbstractComponent<T> {

    protected AbstractCheckbox(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof Label labelCondition) {
            String expectedValue = Strings.nullToEmpty(labelCondition.getValue());

            getLabelDelegate()
                    .shouldBe(VISIBLE)
                    .shouldHave(Condition.exactText(expectedValue));
            return CheckResult.accepted();
        }

        return super.check(condition);
    }

    /**
     * @return {@link SelenideElement} of a label web-element
     */
    protected SelenideElement getLabelDelegate() {
        return $(byChained(by, xpath("./label")));
    }
}
