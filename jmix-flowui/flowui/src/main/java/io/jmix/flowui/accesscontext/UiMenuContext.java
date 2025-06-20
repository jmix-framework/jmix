/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.accesscontext;

import io.jmix.core.accesscontext.AccessContext;
import io.jmix.flowui.menu.MenuItem;

import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

/**
 * Defines authorization point for UI menu items.
 */
public class UiMenuContext implements AccessContext {
    protected final MenuItem menuItem;
    protected boolean permitted = true;

    public UiMenuContext(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    /**
     * Retrieves the identifier of the associated menu item.
     *
     * @return the menu item ID
     */
    public String getMenuItemId() {
        return menuItem.getId();
    }

    /**
     * Retrieves the associated menu item.
     *
     * @return the {@link MenuItem} instance associated with the context
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     * Determines whether access is permitted in the current context.
     *
     * @return {@code true} if access is permitted, {@code false} otherwise
     */
    public boolean isPermitted() {
        return permitted;
    }

    /**
     * Denies access for the associated menu item by setting the permission state to false.
     * This method is used to explicitly restrict access within the current context.
     */
    public void setDenied() {
        this.permitted = false;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        return !permitted ? "menu item: " + menuItem.getId() : null;
    }
}
