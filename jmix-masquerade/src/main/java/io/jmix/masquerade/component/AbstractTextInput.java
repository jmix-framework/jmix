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

import org.openqa.selenium.By;

import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.READONLY;

/**
 * Abstract class for fields web-element wrapper that supports text input. Supports setting text value.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractTextInput<T extends AbstractTextInput<T>> extends AbstractField<T> {

    protected AbstractTextInput(By by) {
        super(by);
    }

    public T setValue(String value) {
        getInputDelegate()
                .shouldBe(ENABLED)
                .shouldNotBe(READONLY)
                .setValue(value);

        //noinspection unchecked
        return ((T) this);
    }
}
