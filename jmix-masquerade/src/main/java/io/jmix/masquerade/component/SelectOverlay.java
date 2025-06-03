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

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;

/**
 * Web-element wrapper for select overlay. Supports items selecting.
 */
public class SelectOverlay extends AbstractOverlay<SelectOverlay, Select> {

    public SelectOverlay(By by, Select parentComponent) {
        super(by, parentComponent);
    }

    /**
     * Selects the item with the passed text in the overlay.
     *
     * @param itemText text of the item to select
     * @return parent select to call fluent API
     */
    public Select select(String itemText) {
        return select(byText(itemText));
    }

    /**
     * Selects the item which should be found by the passed {@link By} selector.
     *
     * @param itemBy {@link By} selector to find and select the overlay item
     * @return parent select to call fluent API
     */
    public Select select(By itemBy) {
        $(byChained(by, itemBy))
                .shouldBe(VISIBLE)
                .click();

        return parentComponent;
    }

    @Override
    public ElementsCollection getVisibleElements() {
        return $$(byChained(by, TagNames.SELECT_ITEM));
    }
}
