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
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Abstract class for field web-element wrappers. Supports {@link Value}, {@link ValueContains},
 * {@link Label} condition checking.
 *
 * @param <T>
 */
public abstract class AbstractField<T extends AbstractField<T>> extends AbstractComponent<T> {

    protected AbstractField(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        SelenideElement inputImpl = getInputDelegate();

        if (condition instanceof Value valueCondition) {
            String expectedValue = Strings.nullToEmpty(valueCondition.getValue());

            inputImpl.shouldHave(Condition.exactValue(expectedValue));
        } else if (condition instanceof ValueContains valueContains) {
            String expectedValue = Strings.nullToEmpty(valueContains.getValue());

            inputImpl.shouldHave(Condition.value(expectedValue));
        } else if (condition instanceof Label labelCondition) {
            String expectedValue = Strings.nullToEmpty(labelCondition.getValue());

            getLabelDelegate()
                    .shouldBe(VISIBLE)
                    .shouldHave(Condition.exactText(expectedValue));
        } else {
            throw new UnsupportedConditionException(condition, this);
        }

        return CheckResult.accepted();
    }

    /**
     * @return {@link SelenideElement} of an input web-element
     */
    protected SelenideElement getInputDelegate() {
        return $(byChained(by, TagNames.INPUT))
                .shouldBe(VISIBLE);
    }

    /**
     * @return {@link SelenideElement} of a label web-element
     */
    protected SelenideElement getLabelDelegate() {
        return $(byChained(by, TagNames.LABEL));
    }
}
