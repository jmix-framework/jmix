/*
 * Copyright 2026 Haulmont.
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
import com.codeborne.selenide.SetValueOptions;
import org.openqa.selenium.By;

import static com.codeborne.selenide.SetValueMethod.JS;
import static io.jmix.masquerade.JConditions.*;

/**
 * Abstract class for slider web-element wrappers. Supports setting value.
 * <p>
 * A slider is rendered as a native range input, which ignores the typed text, so the value is set by JavaScript.
 * The {@code disabled} and {@code readonly} states are kept on the slider web-element itself instead of its input,
 * hence they are checked on the wrapped web-element.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractSlider<T extends AbstractSlider<T>> extends AbstractField<T> {

    protected AbstractSlider(By by) {
        super(by);
    }

    /**
     * Sets the value to the passed input web-element of the slider.
     *
     * @param input input web-element of the slider
     * @param value value as a string presentation value
     */
    protected void setValueInternal(SelenideElement input, String value) {
        getDelegate()
                .shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .shouldNotBe(READONLY);

        input.setValue(SetValueOptions.withText(value).usingMethod(JS));
    }
}
