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
import com.google.common.collect.Sets;
import io.jmix.masquerade.condition.CheckedItems;
import io.jmix.masquerade.condition.CheckedItemsContains;
import io.jmix.masquerade.condition.CheckedItemsCount;
import io.jmix.masquerade.condition.SpecificCondition;
import io.jmix.masquerade.sys.TagNames;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.Arrays;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.EXIST;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for checkbox group. Supports selecting or deselecting inner checkboxes,
 * {@link CheckedItems}, {@link CheckedItemsContains}, {@link CheckedItemsCount} condition checking.
 */
public class CheckboxGroup extends AbstractCheckbox<CheckboxGroup> {

    public CheckboxGroup(By by) {
        super(by);
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof CheckedItems checkedItems) {
            List<String> currentCheckboxLabelTexts = getCheckboxLabelTexts();
            return new CheckResult(
                    CollectionUtils.isEqualCollection(currentCheckboxLabelTexts, checkedItems.getValue()),
                    currentCheckboxLabelTexts
            );
        } else if (condition instanceof CheckedItemsCount checkedItemsCount) {
            List<String> currentCheckboxLabelTexts = getCheckboxLabelTexts();
            return new CheckResult(
                    currentCheckboxLabelTexts.size() == checkedItemsCount.getValue(),
                    currentCheckboxLabelTexts
            );
        } else if (condition instanceof CheckedItemsContains checkedItemsContains) {
            List<String> currentCheckboxLabelTexts = getCheckboxLabelTexts();
            return new CheckResult(
                    Sets.newHashSet(currentCheckboxLabelTexts).containsAll(checkedItemsContains.getValue()),
                    currentCheckboxLabelTexts
            );
        }

        return super.check(condition);
    }

    /**
     * Selects checkboxes with the passed labels.
     *
     * @param checkboxLabels checkboxes labels to select
     * @return {@code this} to call fluent API
     */
    public CheckboxGroup select(String... checkboxLabels) {
        return select(mapLabelsToBys(checkboxLabels));
    }

    /**
     * Selects checkboxes which can be found by the passed {@link By} selectors.
     *
     * @param checkboxBys {@link By} selectors to find checkboxes to select
     * @return {@code this} to call fluent API
     */
    public CheckboxGroup select(By... checkboxBys) {
        for (By by : checkboxBys) {
            if (!isCheckboxSelected(by)) {
                getCheckboxInput(by).sendKeys(Keys.SPACE);
            }
        }

        return this;
    }

    /**
     * Deselects checkboxes with the passed labels.
     *
     * @param checkboxLabels checkboxes labels to deselect
     * @return {@code this} to call fluent API
     */
    public CheckboxGroup deselect(String... checkboxLabels) {
        return deselect(mapLabelsToBys(checkboxLabels));
    }

    /**
     * Deselects checkboxes which can be found by the passed {@link By} selectors.
     *
     * @param checkboxBys {@link By} selectors to find checkboxes to deselect
     * @return {@code this} to call fluent API
     */
    public CheckboxGroup deselect(By... checkboxBys) {
        for (By by : checkboxBys) {
            if (isCheckboxSelected(by)) {
                getCheckboxInput(by).sendKeys(Keys.SPACE);
            }
        }

        return this;
    }

    protected By[] mapLabelsToBys(String... labels) {
        return Arrays.stream(labels)
                .map(this::getCheckboxBySelector)
                .toArray(By[]::new);
    }

    protected By getCheckboxBySelector(String checkboxLabel) {
        return byChained(by, xpath("./vaadin-checkbox[label[text()='%s']]".formatted(checkboxLabel)));
    }

    protected SelenideElement getCheckboxInput(By checkboxBy) {
        return $(byChained(checkboxBy, TagNames.INPUT))
                .shouldBe(EXIST);
    }

    protected List<String> getCheckboxLabelTexts() {
        return $$(byChained(by, cssSelector("vaadin-checkbox[checked]"), TagNames.LABEL))
                .texts();
    }

    protected boolean isCheckboxSelected(By checkboxBy) {
        return $(checkboxBy).getDomAttribute("checked") != null;
    }
}
