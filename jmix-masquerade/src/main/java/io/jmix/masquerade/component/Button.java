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
import static io.jmix.masquerade.JConditions.VISIBLE;

/**
 * Web-element wrapper for button. Supports clicking and text checking.
 */
public class Button extends AbstractComponent<Button> {

    public Button(By by) {
        super(by);
    }

    /**
     * Clicks on the button if it is possible.
     *
     * @return {@code this} to call fluent API
     */
    public Button click() {
        getDelegate().shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();
        return this;
    }
}
