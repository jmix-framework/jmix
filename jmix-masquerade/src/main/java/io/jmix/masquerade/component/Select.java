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
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Web-element wrapper for select. Supports setting value, {@link Value},
 * {@link ValueContains} and {@link Label} condition checking.
 */
public class Select extends AbstractOverlayComponent<Select, SelectOverlay> {

    public Select(By by) {
        super(by);
    }

    /**
     * Selects value in the {@link SelectOverlay}.
     *
     * @param value value to select
     * @return {@code this} to call fluent API
     */
    public Select setValue(String value) {
        if (getItemsOverlayElement().getDelegate().exists()) {
            return getItemsOverlay().select(value);
        }

        return clickItemsOverlay().select(value);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof Value valueCondition) {
            String expectedValue = Strings.nullToEmpty(valueCondition.getValue());

            if (expectedValue.isEmpty()) {
                getInputDelegate().shouldNotBe(EXIST);
            } else {
                getInputDelegate().shouldHave(Condition.exactValue(expectedValue));
            }

        } else if (condition instanceof ValueContains valueContains) {
            String expectedValue = Strings.nullToEmpty(valueContains.getValue());

            getInputDelegate().shouldHave(Condition.value(expectedValue));
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

    @Override
    protected SelenideElement getInputDelegate() {
        return $(byChained(by, TagNames.SELECT_ITEM))
                .shouldBe(VISIBLE);
    }

    @Override
    protected SelectOverlay getItemsOverlayElement() {
        return new SelectOverlay(TagNames.SELECT_OVERLAY, this);
    }
}
