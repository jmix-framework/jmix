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
import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.TagNames;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.List;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for radio button group. Supports selecting radio buttons, {@link Label},
 * {@link Value}, {@link VisibleItems}, {@link VisibleItemsContains} and {@link VisibleItemsCount} condition checking.
 */
public class RadioButtonGroup extends AbstractComponent<RadioButtonGroup> {

    public RadioButtonGroup(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof Label labelCondition) {
            String expectedValue = Strings.nullToEmpty(labelCondition.getValue());

            getLabelDelegate()
                    .shouldBe(VISIBLE)
                    .shouldHave(exactText(expectedValue));
            return CheckResult.accepted();
        } else if (condition instanceof Value valueCondition) {
            String expectedValue = Strings.nullToEmpty(valueCondition.getValue());

            getCheckedRadioButtonLabelElement().shouldHave(exactText(expectedValue));
            return CheckResult.accepted();
        } else if (condition instanceof VisibleItems visibleItems) {
            List<String> currentRadioButtonLabelTexts = getRadioButtonLabelTexts();
            return new CheckResult(
                    CollectionUtils.isEqualCollection(currentRadioButtonLabelTexts, visibleItems.getValue()),
                    currentRadioButtonLabelTexts
            );
        } else if (condition instanceof VisibleItemsCount visibleItemsCount) {
            List<String> currentRadioButtonLabelTexts = getRadioButtonLabelTexts();
            return new CheckResult(
                    currentRadioButtonLabelTexts.size() == visibleItemsCount.getValue(),
                    currentRadioButtonLabelTexts
            );
        } else if (condition instanceof VisibleItemsContains visibleItemsContains) {
            List<String> currentRadioButtonLabelTexts = getRadioButtonLabelTexts();
            return new CheckResult(
                    Sets.newHashSet(currentRadioButtonLabelTexts).containsAll(visibleItemsContains.getValue()),
                    currentRadioButtonLabelTexts
            );
        }

        return super.check(condition);
    }

    /**
     * Selects the radio button with the passed label.
     *
     * @param label label of the radio button to select
     * @return {@code this} to call fluent API
     */
    public RadioButtonGroup select(String label) {
        return select(mapLabelToBy(label));
    }

    /**
     * Selects the radio button by the passed {@link By} selector.
     *
     * @param radioButtonBy {@link By} selector to find the radio button to select
     * @return {@code this} to call fluent API
     */
    public RadioButtonGroup select(By radioButtonBy) {
        getRadioButtonInput(radioButtonBy).sendKeys(Keys.SPACE);
        return this;
    }

    protected By mapLabelToBy(String radioButtonLabel) {
        return byChained(by, xpath("./vaadin-radio-button[label[span[text()='%s']]]".formatted(radioButtonLabel)));
    }

    protected SelenideElement getRadioButtonInput(By radioButtonBy) {
        return $(byChained(radioButtonBy, TagNames.INPUT))
                .shouldBe(EXIST);
    }

    protected SelenideElement getLabelDelegate() {
        return $(byChained(by, xpath("./label")));
    }

    protected SelenideElement getCheckedRadioButtonLabelElement() {
        return $(byChained(by, cssSelector("vaadin-radio-button[checked]"), TagNames.LABEL, TagNames.SPAN));
    }

    protected List<String> getRadioButtonLabelTexts() {
        return $$(byChained(by, cssSelector("vaadin-radio-button"), TagNames.LABEL, TagNames.SPAN))
                .texts();
    }
}
