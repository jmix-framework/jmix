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

import javax.annotation.Nullable;

public class FlowuiMenuContext implements AccessContext {
    protected final MenuItem menuItem;
    protected boolean permitted = true;

    public FlowuiMenuContext(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getMenuItemId() {
        return menuItem.getId();
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public boolean isPermitted() {
        return permitted;
    }

    public void setDenied() {
        this.permitted = false;
    }

    @Nullable
    @Override
    public String explainConstraints() {
        return !permitted ? "menu item: " + menuItem.getId() : null;
    }
}
