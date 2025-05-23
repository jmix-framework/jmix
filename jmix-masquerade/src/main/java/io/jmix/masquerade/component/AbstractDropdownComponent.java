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

import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.sys.TagNames.MENU_BAR_OVERLAY;
import static org.openqa.selenium.By.cssSelector;

/**
 * Abstract class for web-element wrappers with dropdowns. Supports clicking on overlay button
 * to open {@link MenuBarOverlay}.
 *
 * @param <T> inheritor class type
 */
public abstract class AbstractDropdownComponent<T extends AbstractDropdownComponent<T>> extends AbstractComponent<T> {

    protected AbstractDropdownComponent(By by) {
        super(by);
    }

    /**
     * Clicks on overlay button to open an {@link MenuBarOverlay overlay}.
     *
     * @return {@code this} to call fluent API
     */
    public MenuBarOverlay<T> clickItemsOverlay() {
        getOpenOverlayButtonElement().shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .click();

        return getMenuBarOverlay();
    }

    /**
     * @return current opened {@link MenuBarOverlay overlay} web-element wrapper
     */
    public MenuBarOverlay<T> getItemsOverlay() {
        return getMenuBarOverlay()
                .shouldBe(VISIBLE);
    }

    /**
     * @return {@link SelenideElement} web-element wrapper for open overlay button
     */
    protected SelenideElement getOpenOverlayButtonElement() {
        return $(byChained(by, cssSelector("[last-visible]")));
    }

    /**
     * @return current opened {@link MenuBarOverlay overlay wrapper} for the dropdown
     */
    protected MenuBarOverlay<T> getMenuBarOverlay() {
        //noinspection unchecked
        return new MenuBarOverlay<>(MENU_BAR_OVERLAY, ((T) this));
    }
}
