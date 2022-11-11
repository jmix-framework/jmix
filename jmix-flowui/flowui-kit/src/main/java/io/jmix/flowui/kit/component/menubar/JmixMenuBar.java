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

package io.jmix.flowui.kit.component.menubar;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuItemsArrayGenerator;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;

import java.util.List;
import java.util.stream.Stream;

public class JmixMenuBar extends MenuBar
        implements HasMenuItemsEnhanced {

    protected JmixMenuManager<MenuBar, MenuItem, SubMenu> menuManager;
    protected MenuItemsArrayGenerator<MenuItem> menuItemsArrayGenerator;

    protected boolean updateScheduled = false;

    public JmixMenuBar() {
        initComponent();
    }

    protected void initComponent() {
        menuItemsArrayGenerator = new MenuItemsArrayGenerator<>(this);

        menuManager = new JmixMenuManager<>(this, this::resetContent,
                (menu, contentReset) -> new JmixMenuBarRootItem(this, contentReset),
                MenuItem.class, null);

        addAttachListener(this::attachListener);
    }

    public JmixMenuItem addItem(String text) {
        return (JmixMenuItem) menuManager.addItem(text);
    }

    public JmixMenuItem addItem(Component component) {
        return (JmixMenuItem) menuManager.addItem(component);
    }

    @Override
    public JmixMenuItem addItem(String text,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) menuManager.addItem(text, clickListener);
    }

    public JmixMenuItem addItem(Component component,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) menuManager.addItem(component, clickListener);
    }

    public JmixMenuItem addItemAtIndex(int index, String text) {
        return (JmixMenuItem) menuManager.addItemAtIndex(index, text);
    }

    public JmixMenuItem addItemAtIndex(int index, Component component) {
        return (JmixMenuItem) menuManager.addItemAtIndex(index, component);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, String text,
                                       ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) menuManager.addItemAtIndex(index, text, clickListener);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, Component component,
                                       ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) menuManager.addItemAtIndex(index, component, clickListener);
    }

    @Override
    public List<MenuItem> getItems() {
        return menuManager.getItems();
    }

    @Override
    public void remove(MenuItem... items) {
        menuManager.remove(items);
    }

    @Override
    public void removeAll() {
        menuManager.removeAll();
    }

    @Override
    public Stream<Component> getChildren() {
        return menuManager.getChildren();
    }

    void resetContent() {
        menuItemsArrayGenerator.generate();
        updateButtons();
    }

    void updateButtons() {
        if (updateScheduled) {
            return;
        }
        runBeforeClientResponse(ui -> {
            // When calling `generateItems` without providing a node id, it will
            // use the previously generated items tree, only updating the
            // disabled and hidden properties of the root items = the menu bar
            // buttons.
            getElement().executeJs("this.$connector.generateItems()");
            updateScheduled = false;
        });
        updateScheduled = true;
    }

    protected void attachListener(AttachEvent attachEvent) {
        resetContent();
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }
}
