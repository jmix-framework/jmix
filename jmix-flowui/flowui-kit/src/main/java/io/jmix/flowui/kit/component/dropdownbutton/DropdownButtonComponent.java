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

package io.jmix.flowui.kit.component.dropdownbutton;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasSubParts;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface DropdownButtonComponent extends HasSubParts {

    DropdownButtonItem addItem(String id, Action action);

    DropdownButtonItem addItem(String id, Action action, int index);

    DropdownButtonItem addItem(String id, String text);

    DropdownButtonItem addItem(String id, String text, int index);

    DropdownButtonItem addItem(String id,
                               String text,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener);

    DropdownButtonItem addItem(String id,
                               String text,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                               int index);

    DropdownButtonItem addItem(String id, Component component);

    DropdownButtonItem addItem(String id, Component component, int index);

    DropdownButtonItem addItem(String id,
                               Component component,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener);

    DropdownButtonItem addItem(String id,
                               Component component,
                               Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                               int index);

    @Nullable
    DropdownButtonItem getItem(String itemId);

    List<DropdownButtonItem> getItems();

    void remove(String itemId);

    void remove(DropdownButtonItem item);

    void remove(DropdownButtonItem... items);

    void removeAll();

    void addSeparator();

    void addSeparatorAtIndex(int index);

    void setOpenOnHover(boolean openOnHover);

    boolean isOpenOnHover();

    void setIcon(@Nullable Icon icon);

    @Nullable
    Icon getIcon();

    @Nullable
    @Override
    default Object getSubPart(String name) {
        return getItem(name);
    }
}
