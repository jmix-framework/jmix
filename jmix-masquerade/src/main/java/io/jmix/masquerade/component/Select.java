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
        if (getItemsOverlayElement().getDelegate().isDisplayed()) {
            return getItemsOverlay().select(value);
        }

        return clickItemsOverlay().select(value);
    }

    @Override
    public SpecificCheck resolve(SpecificCondition condition) {
        if (condition instanceof Value valueCondition) {
            String expectedValue = Strings.nullToEmpty(valueCondition.getValue());

            if (expectedValue.isEmpty()) {
                // An empty expected value means no item is selected, i.e. the value element must be absent.
                SelenideElement valueElement = $(byChained(by, TagNames.SELECT_VALUE_BUTTON, TagNames.SELECT_ITEM));
                boolean noValueSelected = !valueElement.is(EXIST);
                return SpecificCheck.of(noValueSelected, noValueSelected ? "" : valueElement.text());
            }

            return SpecificCheck.of(getInputDelegate(), Condition.exactValue(expectedValue));
        } else if (condition instanceof ValueContains valueContains) {
            String expectedValue = Strings.nullToEmpty(valueContains.getValue());

            return SpecificCheck.of(getInputDelegate(), Condition.value(expectedValue));
        } else if (condition instanceof Label labelCondition) {
            String expectedValue = Strings.nullToEmpty(labelCondition.getValue());

            return SpecificCheck.of(
                    getLabelDelegate().shouldBe(VISIBLE),
                    Condition.exactText(expectedValue)
            );
        } else {
            throw new UnsupportedConditionException(condition, this);
        }
    }

    @Override
    protected SelenideElement getInputDelegate() {
        return $(byChained(by, TagNames.SELECT_VALUE_BUTTON, TagNames.SELECT_ITEM))
                .shouldBe(VISIBLE);
    }

    @Override
    protected SelectOverlay getItemsOverlayElement() {
        return new SelectOverlay(byChained(by, TagNames.SELECT_OVERLAY), this);
    }
}
