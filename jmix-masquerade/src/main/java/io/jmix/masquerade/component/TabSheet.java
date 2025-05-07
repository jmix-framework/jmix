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

import io.jmix.masquerade.Masquerade;
import org.openqa.selenium.By;

import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.JSelectors.byUiTestId;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for tab sheet. Supports working with tab sheet tabs.
 */
public class TabSheet extends AbstractComponent<TabSheet>
        implements Container {

    public TabSheet(By by) {
        super(by);
    }

    /**
     * @param tabUiTestId {@link Masquerade#UI_TEST_ID UI_TEST_ID} attribute value pf the tab sheet tab web-element
     * @return web-element wrapper for tab sheet tab found by the passed ID
     */
    public Tab getTabById(String tabUiTestId) {
        return getTabBy(byUiTestId(tabUiTestId));
    }

    /**
     * @param tabText tab sheet tab text
     * @return web-element wrapper for tab sheet tab found by the passed text
     */
    public Tab getTabByText(String tabText) {
        return getTabBy(getTabByTextBy(tabText));
    }

    /**
     * @param by {@link By} selector to find tab sheet tab
     * @return web-element wrapper for tab sheet tab found by the passed {@link By} selector
     */
    public Tab getTabBy(By by) {
        return new Tab(byChained(getBy(), by));
    }

    protected By getTabByTextBy(String tabText) {
        return xpath("./vaadin-tabs/vaadin-tab[text()='%s']"
                .formatted(tabText));
    }

    /**
     * Web-element wrapper for tab sheet tab. Supports selecting.
     */
    public static class Tab extends AbstractComponent<Tab> {

        public Tab(By by) {
            super(by);
        }

        /**
         * Select this tab in tab sheet.
         *
         * @return {@code this} to call fluent API
         */
        public Tab select() {
            if (getDelegate().getDomAttribute("selected") != null) {
                return this;
            }

            getDelegate()
                    .scrollIntoCenter()
                    .click();

            return this;
        }
    }
}
