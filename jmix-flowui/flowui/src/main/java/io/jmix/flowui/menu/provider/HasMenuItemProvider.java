/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.menu.provider;

import io.jmix.flowui.kit.component.menu.MenuItem;
import org.springframework.lang.Nullable;

/**
 * Represents menus that have menu item providers
 * @param <T> menu item type
 */
public interface HasMenuItemProvider<T extends MenuItem> {

    /**
     * @return menu item provider
     */
    @Nullable
    MenuItemProvider<T> getMenuItemProvider();

    /**
     * Sets menu item provider
     * @param menuItemProvider menu item provider to set
     */
    void setMenuItemProvider(@Nullable MenuItemProvider<T> menuItemProvider);
}
