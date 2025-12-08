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

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Strings;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import static io.jmix.masquerade.JConditions.*;

/**
 * Abstract class for combobox-like web-element wrappers. Supports value typing and single value selecting.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractComboBox<T extends AbstractComboBox<T>>
        extends AbstractOverlayComponent<T, ComboBoxOverlay<T>> {

    protected AbstractComboBox(By by) {
        super(by);
    }

    /**
     * Selects single value in the {@link ComboBoxOverlay}.
     *
     * @param value value to select
     * @return {@code this} to call fluent API
     */
    protected T selectSingleValue(String value) {
        type(value);

        if (!Strings.isNullOrEmpty(value)) {
            getItemsOverlay().select(value);
        }

        //noinspection unchecked
        return (T) this;
    }

    /**
     * Types the passed value into the combobox input field.
     *
     * @param value value to type
     * @return {@code this} to call fluent API
     */
    public T type(String value) {
        SelenideElement inputDelegate = getInputDelegate();
        inputDelegate.shouldBe(VISIBLE)
                .shouldNotBe(READONLY)
                .shouldBe(ENABLED)
                .click();

        if (!Strings.isNullOrEmpty(inputDelegate.getValue())) {
            inputDelegate.clear();
        }

        if (!Strings.isNullOrEmpty(value)) {
            inputDelegate.sendKeys(value);
        } else {
            inputDelegate.pressEnter();
        }

        //noinspection unchecked
        return (T) this;
    }

    /**
     * @return current opened {@link ComboBoxOverlay overlay wrapper} for the combobox
     */
    @Override
    protected ComboBoxOverlay<T> getItemsOverlayElement() {
        //noinspection unchecked
        return new ComboBoxOverlay<>(TagNames.COMBO_BOX_OVERLAY, ((T) this), TagNames.COMBO_BOX_ITEM);
    }
}
