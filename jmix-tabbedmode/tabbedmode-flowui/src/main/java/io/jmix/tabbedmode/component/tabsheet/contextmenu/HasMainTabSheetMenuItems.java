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

package io.jmix.tabbedmode.component.tabsheet.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import io.jmix.tabbedmode.component.tabsheet.contextmenu.MainTabSheetContextMenu.MainTabSheetContextMenuItemClickEvent;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public interface HasMainTabSheetMenuItems extends Serializable {

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     *
     * @param text          the text content for the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to
     *                      not add listener
     * @return the added {@link MainTabSheetMenuItem} component
     */
    MainTabSheetMenuItem addItem(String text,
                                 @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener);

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     *
     * @param component     the component inside the new item
     * @param clickListener the handler for clicking the new item, can be {@code null} to
     *                      not add listener
     * @return the added {@link MainTabSheetMenuItem} component
     */
    MainTabSheetMenuItem addItem(Component component,
                                 @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener);
}
