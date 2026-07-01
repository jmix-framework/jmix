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

import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.component.FullCalendar;

/**
 * The sub menu of context menu in {@link FullCalendar}.
 */
public class FullCalendarSubMenu extends SubMenuBase<FullCalendarContextMenu, FullCalendarMenuItem,
        FullCalendarSubMenu> {

    protected final SerializableRunnable contentReset;

    public FullCalendarSubMenu(FullCalendarMenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);

        Preconditions.checkNotNullArgument(contentReset);

        this.contentReset = contentReset;
    }

    @Override
    protected MenuManager<FullCalendarContextMenu, FullCalendarMenuItem, FullCalendarSubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(), contentReset, FullCalendarMenuItem::new,
                FullCalendarMenuItem.class, getParentMenuItem());
    }
}
