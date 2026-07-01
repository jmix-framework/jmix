/*
 * Copyright 2026 Haulmont.
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

package io.jmix.fullcalendarflowui.component.contextmenu;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.contextmenu.HasFullCalendarMenuItems.FullCalendarClickContextMenuItemEvent;

/**
 * Menu item of context menu - {@link FullCalendarContextMenu}.
 */
public class FullCalendarMenuItem extends MenuItemBase<FullCalendarContextMenu, FullCalendarMenuItem, FullCalendarSubMenu> {

    protected final SerializableRunnable contentReset;

    public FullCalendarMenuItem(FullCalendarContextMenu contextMenu, SerializableRunnable contentReset) {
        super(contextMenu);
        Preconditions.checkNotNullArgument(contextMenu);
        Preconditions.checkNotNullArgument(contentReset);

        this.contentReset = contentReset;
    }

    /**
     * Adds menu item click listener.
     *
     * @param clickListener click listener to add
     * @return a registration object for removing an event listener added to menu item
     */
    public Registration addMenuItemClickListener(
            ComponentEventListener<FullCalendarClickContextMenuItemEvent> clickListener) {
        return getElement().addEventListener("click", event ->
                clickListener.onComponentEvent(new FullCalendarClickContextMenuItemEvent(this, true)));
    }

    @Override
    protected FullCalendarSubMenu createSubMenu() {
        return new FullCalendarSubMenu(this, contentReset);
    }
}
