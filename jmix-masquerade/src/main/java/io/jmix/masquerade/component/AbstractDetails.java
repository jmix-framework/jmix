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
import org.openqa.selenium.By;

/**
 * Abstract class for details web-element wrappers. Supports clicking on header and child selection as a
 * {@link Container} implementation.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractDetails<T extends AbstractDetails<T>>
        extends AbstractComponent<T>
        implements Container {

    protected AbstractDetails(By by) {
        super(by);
    }

    /**
     * Clicks on details header to open or close content.
     *
     * @return {@code this} to call fluent API
     */
    public T clickOnHeader() {
        getSummaryElement().click();

        //noinspection unchecked
        return ((T) this);
    }

    @Override
    public By getBy() {
        return by;
    }

    /**
     * @return {@link SelenideElement} for details summary web-element
     */
    public abstract SelenideElement getSummaryElement();
}
