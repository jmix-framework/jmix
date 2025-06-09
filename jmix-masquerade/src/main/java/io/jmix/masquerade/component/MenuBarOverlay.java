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

import com.codeborne.selenide.ElementsCollection;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Web-element wrapper for menu bar overlay.
 *
 * @param <T> overlay dropdown class type
 */
public class MenuBarOverlay<T extends AbstractDropdownComponent<T>> extends AbstractOverlay<MenuBarOverlay<T>, T> {

    public MenuBarOverlay(By by, T parentComponent) {
        super(by, parentComponent);
    }

    /**
     * Clicks on item with passed ID.
     *
     * @param itemId item ID to click
     * @return parent dropdown to call fluent API
     */
    public T clickItem(String itemId) {
        // TODO: kd, replace by byUiTestId after framework changes
        return clickItem(byId(itemId));
    }

    /**
     * Clicks on item found by the passed {@link By} selector.
     *
     * @param itemBy {@link By} selector to find item to click
     * @return parent dropdown to call fluent API
     */
    public T clickItem(By itemBy) {
        $(byChained(by, itemBy))
                .shouldBe(VISIBLE)
                .click();

        return parentComponent;
    }

    @Override
    public ElementsCollection getVisibleElements() {
        return $$(byChained(by, TagNames.MENU_BAR_ITEM));
    }
}
