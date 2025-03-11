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

package io.jmix.tabbedmode;

import io.jmix.tabbedmode.component.breadcrumbs.ViewBreadcrumbs;
import io.jmix.tabbedmode.view.MultipleOpen;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.tabmod")
public class TabbedModeProperties {

    /**
     * Maximum number of opened tabs. {@code 0} for unlimited.
     */
    int maxTabCount;

    /**
     * Whatever views should be opened multiple times from the main menu.
     * <p>
     * Note: the {@link MultipleOpen} annotation have precedence for a specific view.
     */
    boolean multipleOpen;

    /**
     * Whether {@link ViewBreadcrumbs} is shown in views.
     */
    boolean showBreadcrumbs;

    /**
     * Whether the default view can be closed using default close buttons provided by a tab or a dialog window.
     */
    boolean defaultViewCloseable;

    /**
     * Shortcut for switching to the next tab in the main view.
     */
    String openNextTabShortcut;

    /**
     * Shortcut for switching to the previous tab in the main view.
     */
    String openPreviousTabShortcut;

    /**
     * Shortcut for closing this tab in the main view.
     */
    String closeThisTabShortcut;

    /**
     * Shortcut for closing other tabs in the main view.
     */
    String closeOtherTabsShortcut;

    /**
     * Shortcut for closing all tabs in the main view.
     */
    String closeAllTabsShortcut;

    public TabbedModeProperties(@DefaultValue("20") int maxTabCount,
                                @DefaultValue("false") boolean multipleOpen,
                                @DefaultValue("true") boolean showBreadcrumbs,
                                @DefaultValue("true") boolean defaultViewCloseable,
                                String closeThisTabShortcut,
                                String closeOtherTabsShortcut,
                                String closeAllTabsShortcut,
                                String openNextTabShortcut,
                                String openPreviousTabShortcut) {
        this.maxTabCount = maxTabCount;
        this.multipleOpen = multipleOpen;
        this.showBreadcrumbs = showBreadcrumbs;
        this.defaultViewCloseable = defaultViewCloseable;
        this.closeThisTabShortcut = closeThisTabShortcut;
        this.closeOtherTabsShortcut = closeOtherTabsShortcut;
        this.closeAllTabsShortcut = closeAllTabsShortcut;
        this.openNextTabShortcut = openNextTabShortcut;
        this.openPreviousTabShortcut = openPreviousTabShortcut;
    }

    /**
     * @see #maxTabCount
     */
    public int getMaxTabCount() {
        return maxTabCount;
    }

    /**
     * @see #multipleOpen
     */
    public boolean isMultipleOpen() {
        return multipleOpen;
    }

    /**
     * @see #showBreadcrumbs
     */
    public boolean isShowBreadcrumbs() {
        return showBreadcrumbs;
    }

    /**
     * @see #defaultViewCloseable
     */
    public boolean isDefaultViewCloseable() {
        return defaultViewCloseable;
    }

    /**
     * @see #closeThisTabShortcut
     */
    public String getCloseThisTabShortcut() {
        return closeThisTabShortcut;
    }

    /**
     * @see #closeOtherTabsShortcut
     */
    public String getCloseOtherTabsShortcut() {
        return closeOtherTabsShortcut;
    }

    /**
     * @see #closeAllTabsShortcut
     */
    public String getCloseAllTabsShortcut() {
        return closeAllTabsShortcut;
    }

    /**
     * @see #openNextTabShortcut
     */
    public String getOpenNextTabShortcut() {
        return openNextTabShortcut;
    }

    /**
     * @see #openPreviousTabShortcut
     */
    public String getOpenPreviousTabShortcut() {
        return openPreviousTabShortcut;
    }
}
