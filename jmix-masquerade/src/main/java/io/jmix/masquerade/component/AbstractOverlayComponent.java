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

import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;

/**
 * Abstract class for web-element wrappers that supports opening {@link AbstractOverlay}. Supports clicking on
 * overlay open button.
 *
 * @param <C> inheritor class type
 * @param <O> overlay class type
 */
public abstract class AbstractOverlayComponent<C extends AbstractOverlayComponent<C, O>, O extends AbstractOverlay<O, C>>
        extends AbstractField<C> {

    protected static final String TOGGLE_BUTTON_CSS = "[part='toggle-button']";

    public AbstractOverlayComponent(By by) {
        super(by);
    }

    /**
     * Clicks on open overlay button.
     *
     * @return {@link O overlay} web-element wrapper
     */
    public O clickItemsOverlay() {
        $(shadowCss(TOGGLE_BUTTON_CSS, getHostCssSelector()))
                .shouldBe(VISIBLE)
                .click();

        return getItemsOverlayElement();
    }

    /**
     * @return current opened {@link O overlay} web-element wrapper
     */
    public O getItemsOverlay() {
        return getItemsOverlayElement()
                .shouldBe(VISIBLE);
    }

    protected String getHostCssSelector() {
        return "[%s='%s']".formatted(
                UI_TEST_ID,
                getDelegate().getDomAttribute(UI_TEST_ID));
    }

    /**
     * @return web-element wrapper for {@link O overlay}
     */
    protected abstract O getItemsOverlayElement();
}
