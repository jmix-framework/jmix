/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.menubar.JmixMenuBarState;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmixMenuBar extends com.vaadin.ui.MenuBar {

    protected Map<MenuItem, String> shortcuts = null;
    protected Map<MenuItem, String> jmixIds = null;

    @Override
    protected JmixMenuBarState getState() {
        return (JmixMenuBarState) super.getState();
    }

    @Override
    protected JmixMenuBarState getState(boolean markAsDirty) {
        return (JmixMenuBarState) super.getState(markAsDirty);
    }

    public boolean isVertical() {
        return getState(false).vertical;
    }

    public void setVertical(boolean useMoreMenuItem) {
        if (useMoreMenuItem != isVertical()) {
            getState().vertical = useMoreMenuItem;
        }
    }

    public void setShortcut(MenuItem item, String str) {
        if (shortcuts == null) {
            shortcuts = new HashMap<>(4);
        }

        shortcuts.remove(item);
        shortcuts.put(item, str);
    }

    public void setJTestId(MenuItem item, String id) {
        if (jmixIds == null) {
            jmixIds = new HashMap<>();
        }
        jmixIds.put(item, id);
    }

    @Override
    protected void paintAdditionalItemParams(PaintTarget target, MenuItem item) throws PaintException {
        if (shortcuts != null && shortcuts.containsKey(item)) {
            String shortcut = shortcuts.get(item);
            if (shortcut != null) {
                target.addAttribute("shortcut", shortcut);
            }
        }
        if (jmixIds != null && jmixIds.containsKey(item)) {
            String idValue = jmixIds.get(item);
            if (idValue != null) {
                target.addAttribute("cid", idValue);
            }
        }
    }

    public MenuItem createMenuItem(String caption, @Nullable Resource icon, @Nullable Command command) {
        return new MenuItem(caption, icon, command) {
            @Override
            public List<MenuItem> getChildren() {
                if (itsChildren == null) {
                    itsChildren = new ArrayList<>();
                }

                return itsChildren;
            }
        };
    }

    public void addMenuItem(MenuItem item) {
        if (item.getText() == null) {
            throw new IllegalArgumentException("MenuItem caption cannot be null");
        }
        menuItems.add(item);

        markAsDirty();
    }

    public void addMenuItem(MenuItem item, int index) {
        if (item.getText() == null) {
            throw new IllegalArgumentException("MenuItem caption cannot be null");
        }
        menuItems.add(index, item);

        markAsDirty();
    }

    public void removeMenuItem(MenuItem item) {
        removeItem(item);
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public boolean hasMenuItems() {
        return !menuItems.isEmpty();
    }

    public MenuItem createSeparator() {
        MenuBar.MenuItem menuItem = createMenuItem("", null, null);
        setMenuItemSeparator(menuItem, true);
        return menuItem;
    }
}