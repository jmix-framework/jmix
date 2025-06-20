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
import com.google.common.collect.Sets;
import io.jmix.masquerade.condition.*;
import io.jmix.masquerade.sys.TagNames;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static org.openqa.selenium.By.xpath;

/**
 * Abstract class for multi-select combobox web-element wrappers. Supports setting value, {@link SelectedItems},
 * {@link SelectedItemsContains}, {@link SelectedItemsCount} and {@link Label} condition checking.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractMultiSelectComboBox<T extends AbstractMultiSelectComboBox<T>> extends AbstractComboBox<T> {

    protected AbstractMultiSelectComboBox(By by) {
        super(by);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ComboBoxOverlay<T> getItemsOverlayElement() {
        return new ComboBoxOverlay<>(
                TagNames.MULTI_SELECT_COMBO_BOX_OVERLAY,
                (T) this,
                TagNames.MULTI_SELECT_COMBO_BOX_ITEM
        );
    }

    /**
     * Sets a value to multi-select combobox.
     *
     * @param value value items to set
     * @return {@code this} to call fluent API
     */
    @SuppressWarnings("unchecked")
    public T setValue(String... value) {
        setValue(List.of(value));
        return (T) this;
    }

    /**
     * Sets a value to multi-select combobox.
     *
     * @param value list of value items to set
     * @return {@code this} to call fluent API
     */
    @SuppressWarnings("unchecked")
    public T setValue(List<String> value) {
        clearValue();

        if (value.isEmpty()) {
            return (T) this;
        }

        value.forEach(this::selectSingleValue);

        closeOverlay();
        return (T) this;
    }

    @Override
    public CheckResult check(SpecificCondition condition) {
        if (condition instanceof SelectedItems selectedItemsCondition) {
            List<String> currentValue = getValue();
            return new CheckResult(
                    CollectionUtils.isEqualCollection(currentValue, selectedItemsCondition.getValue()), currentValue
            );
        } else if (condition instanceof SelectedItemsContains selectedItemsContainsCondition) {
            List<String> currentValue = getValue();
            return new CheckResult(
                    Sets.newHashSet(currentValue).containsAll(selectedItemsContainsCondition.getValue()),
                    currentValue
            );
        } else if (condition instanceof SelectedItemsCount selectedItemsCountCondition) {
            List<String> currentValue = getValue();
            return new CheckResult(
                    currentValue.size() == selectedItemsCountCondition.getValue(),
                    currentValue.size()
            );
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

    protected void clearValue() {
        while ($$(byChained(by, xpath("./vaadin-multi-select-combo-box-chip[not(@hidden)]")))
                .stream()
                .findAny()
                .isPresent()) {

            // double backspace to remove chip
            getInputDelegate().sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        }
    }

    protected List<String> getValue() {
        List<String> valueSet = new ArrayList<>();

        ComboBoxOverlay<T> overlay = clickItemsOverlay();
        By overlayBy = overlay.getBy();

        Iterator<SelenideElement> elementIterator = getElementIterator(overlayBy);
        elementIterator.forEachRemaining(element -> {
            if (element.getDomAttribute("selected") != null) {
                valueSet.add(element.text());
            }
        });

        closeOverlay();

        return valueSet;
    }

    protected void closeOverlay() {
        // to close overlay
        getDelegate().pressEscape();
    }

    /**
     * Creates and returns iterator for multi-select combobox overlay items.
     *
     * @param overlayBy {@link By} selector for multi-select combobox overlay
     * @return iterator for overlay items
     */
    protected Iterator<SelenideElement> getElementIterator(By overlayBy) {
        return new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return getMultiSelectComboBoxItemElementByIndex(overlayBy, index).exists();
            }

            @Override
            public SelenideElement next() {
                SelenideElement item = getMultiSelectComboBoxItemElementByIndex(overlayBy, index++);
                item.sendKeys(Keys.ARROW_DOWN);
                return item;
            }

            private SelenideElement getMultiSelectComboBoxItemElementByIndex(By overlayBy, int index) {
                return $(byChained(overlayBy,
                        xpath(".//vaadin-multi-select-combo-box-item[@id='vaadin-multi-select-combo-box-item-%s']"
                                .formatted(index))));
            }
        };
    }
}
