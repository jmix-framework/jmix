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

    public TabbedModeProperties(@DefaultValue("20") int maxTabCount,
                                @DefaultValue("false") boolean multipleOpen,
                                @DefaultValue("true") boolean showBreadcrumbs) {
        this.maxTabCount = maxTabCount;
        this.multipleOpen = multipleOpen;
        this.showBreadcrumbs = showBreadcrumbs;
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
}
