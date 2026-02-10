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

package io.jmix.flowui.component.usermenu;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;

/**
 * Represents a specific type of {@link UserMenuItem} that is associated with an {@link View}.
 */
public interface ViewUserMenuItem extends UserMenuItem {

    String PROP_TEXT = "text";
    String PROP_ICON = "icon";
    String PROP_OPEN_MODE = "openMode";

    /**
     * Returns the text contained within this user menu item.
     *
     * @return the text of the menu item
     */
    String getText();

    /**
     * Sets the text contained within this user menu item.
     *
     * @param text the text to be set for the menu item
     */
    void setText(String text);

    /**
     * Retrieves the icon associated with this user menu item, if one exists.
     *
     * @return the icon component of this menu item, or {@code null} if no icon is set
     */
    @Nullable
    Component getIcon();

    /**
     * Sets the icon for this user menu item.
     *
     * @param icon the icon to set for this menu item; can be {@code null} to remove the icon
     */
    void setIcon(@Nullable Component icon);

    /**
     * Returns the view open mode.
     *
     * @return the open mode, or {@code null} if not set
     */
    @Nullable
    OpenMode getOpenMode();

    /**
     * Sets the view open mode.
     *
     * @param openMode the open mode to set
     */
    void setOpenMode(@Nullable OpenMode openMode);

    /**
     * Returns the view id if it was set.
     *
     * @return the view id if it was set, otherwise returns {@code null}
     */
    @Nullable
    String getViewId();

    /**
     * Returns the view class if it was set.
     *
     * @return the view class, otherwise returns {@code null}.
     */
    @Nullable
    Class<? extends View<?>> getViewClass();
}
