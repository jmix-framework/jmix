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

import io.jmix.masquerade.condition.Value;
import org.openqa.selenium.By;

/**
 * Web-element wrapper for slider. Also, can be used for wrapping slider-like web-elements with a single value:
 * {@code integerSlider}, {@code decimalSlider}. Supports setting value and {@link Value} condition checking.
 */
public class Slider extends AbstractSlider<Slider> {

    public Slider(By by) {
        super(by);
    }

    /**
     * Sets the value to the slider.
     *
     * @param value value as a string presentation value
     * @return {@code this} to call fluent API
     */
    public Slider setValue(String value) {
        setValueInternal(getInputDelegate(), value);

        return this;
    }
}
