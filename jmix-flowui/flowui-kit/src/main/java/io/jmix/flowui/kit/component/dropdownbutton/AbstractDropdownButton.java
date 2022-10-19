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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.event.EventBus;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractDropdownButton extends Composite<JmixMenuBar>
        implements DropdownButtonComponent, AttachNotifier, DetachNotifier,
        HasTitle, HasSize, HasTheme, HasEnabled, HasStyle, HasText {

    protected static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";

    protected boolean explicitTitle = false;
    protected List<HasMenuItem> items = new ArrayList<>();

    protected JmixMenuItem dropdownItem;
    protected Icon iconComponent;

    protected abstract JmixMenuItem getDropdownItem();

    @Override
    public DropdownButtonItem addItem(String id, Action action) {
        return addItem(id, action, -1);
    }

    @Override
    public DropdownButtonItem addItem(String id, Action action, int index) {
        ActionItemImpl actionItem = new ActionItemImpl(
                id,
                action,
                this,
                content -> createComponentMenuItem(id, content, index)
        );

        addItemInternal(actionItem, index);
        return actionItem;
    }

    @Override
    public DropdownButtonItem addItem(String id, String text) {
        return addItem(id, text, -1);
    }

    @Override
    public DropdownButtonItem addItem(String id, String text, int index) {
        TextItemImpl textItem = new TextItemImpl(
                id,
                text,
                this,
                content -> createTextMenuItem(id, content, index)
        );


        addItemInternal(textItem, index);
        return textItem;
    }

    @Override
    public DropdownButtonItem addItem(String id,
                                      String text,
                                      Consumer<DropdownButtonItem.ClickEvent> componentEventListener) {
        return addItem(id, text, componentEventListener, -1);
    }

    @Override
    public DropdownButtonItem addItem(String id,
                                      String text,
                                      Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                                      int index) {
        HasMenuItem textItem = (HasMenuItem) addItem(id, text, index);

        textItem.addClickListener(componentEventListener);

        return textItem;
    }

    @Override
    public DropdownButtonItem addItem(String id, Component component) {
        return addItem(id, component, -1);
    }

    @Override
    public DropdownButtonItem addItem(String id, Component component, int index) {
        ComponentItemImpl componentItem = new ComponentItemImpl(
                id,
                component,
                this,
                content -> createComponentMenuItem(id, content, index)
        );

        addItemInternal(componentItem, index);
        return componentItem;
    }

    @Override
    public DropdownButtonItem addItem(String id,
                                      Component component,
                                      Consumer<DropdownButtonItem.ClickEvent> componentEventListener) {
        return addItem(id, component, componentEventListener, -1);
    }

    @Override
    public DropdownButtonItem addItem(String id,
                                      Component component,
                                      Consumer<DropdownButtonItem.ClickEvent> componentEventListener,
                                      int index) {
        HasMenuItem componentItem = (HasMenuItem) addItem(id, component, index);

        componentItem.addClickListener(componentEventListener);

        return componentItem;
    }

    protected MenuItem createComponentMenuItem(String id, Component content, int index) {
        JmixSubMenu subMenu = getDropdownItem().getSubMenu();

        MenuItem item = index < 0
                ? subMenu.addItem(content)
                : subMenu.addItemAtIndex(index, content);

        item.setId(id);
        return item;
    }

    protected MenuItem createTextMenuItem(String id, String text, int index) {
        JmixSubMenu subMenu = getDropdownItem().getSubMenu();

        MenuItem item = index < 0
                ? subMenu.addItem(text)
                : subMenu.addItemAtIndex(index, text);

        item.setId(id);
        return item;
    }

    protected void addItemInternal(HasMenuItem item, int index) {
        if (index < 0) {
            items.add(item);
        } else {
            items.add(index, item);
        }
    }

    @Nullable
    @Override
    public DropdownButtonItem getItem(String itemId) {
        return items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<DropdownButtonItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void remove(String itemId) {
        DropdownButtonItem item = getItem(itemId);
        if (item != null) {
            remove(item);
        }
    }

    @Override
    public void remove(DropdownButtonItem item) {
        if (item instanceof HasMenuItem) {
            HasMenuItem menuItem = (HasMenuItem) item;
            if (items.remove(menuItem)) {
                getDropdownItem().getSubMenu().remove(menuItem.getItem());
            }
        } else {
            throw new IllegalStateException(
                    String.format("%s is not contains item", getClass().getSimpleName()));
        }
    }

    @Override
    public void remove(DropdownButtonItem... items) {
        Arrays.stream(items).forEach(this::remove);
    }

    @Override
    public void removeAll() {
        items.clear();
        getDropdownItem().getSubMenu().removeAll();
    }

    @Override
    public void addSeparator() {
        getDropdownItem().getSubMenu().add(new Hr());
    }

    @Override
    public void addSeparatorAtIndex(int index) {
        getDropdownItem().getSubMenu().addComponentAtIndex(index, new Hr());
    }

    @Override
    public Registration addAttachListener(ComponentEventListener<AttachEvent> listener) {
        return getContent().addAttachListener(listener);
    }

    @Override
    public boolean isAttached() {
        return getContent().isAttached();
    }

    @Override
    public Registration addDetachListener(ComponentEventListener<DetachEvent> listener) {
        return getContent().addDetachListener(listener);
    }

    @Override
    public void setTitle(@Nullable String title) {
        explicitTitle = true;

        setTitleInternal(title);
    }

    protected void setTitleInternal(@Nullable String title) {
        HasTitle.super.setTitle(title);
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return iconComponent;
    }

    @Override
    public void setClassName(String className) {
        getContent().setClassName(className);
    }

    @Override
    public void setClassName(String className, boolean set) {
        getContent().setClassName(className, set);
    }

    @Override
    public String getClassName() {
        return getContent().getClassName();
    }

    @Override
    public void addClassName(String className) {
        getContent().addClassName(className);
    }

    @Override
    public boolean removeClassName(String className) {
        return getContent().removeClassName(className);
    }

    @Override
    public ClassList getClassNames() {
        return getContent().getClassNames();
    }

    @Override
    public void addClassNames(String... classNames) {
        getContent().addClassNames(classNames);
    }

    @Override
    public void removeClassNames(String... classNames) {
        getContent().removeClassNames(classNames);
    }

    @Override
    public boolean hasClassName(String className) {
        return getContent().hasClassName(className);
    }

    @Override
    public void setThemeName(String themeName) {
        getContent().setThemeName(themeName);
    }

    @Override
    public void setThemeName(String themeName, boolean set) {
        getContent().setThemeName(themeName, set);
    }

    @Override
    public String getThemeName() {
        return getContent().getThemeName();
    }

    @Override
    public void addThemeName(String themeName) {
        getContent().addThemeName(themeName);
    }

    @Override
    public boolean removeThemeName(String themeName) {
        return getContent().removeThemeName(themeName);
    }

    @Override
    public boolean hasThemeName(String themeName) {
        return getContent().hasThemeName(themeName);
    }

    @Override
    public ThemeList getThemeNames() {
        return getContent().getThemeNames();
    }

    @Override
    public void addThemeNames(String... themeNames) {
        getContent().addThemeNames(themeNames);
    }

    @Override
    public void removeThemeNames(String... themeNames) {
        getContent().removeThemeNames(themeNames);
    }

    @Override
    public void setOpenOnHover(boolean openOnHover) {
        getContent().setOpenOnHover(openOnHover);
    }

    @Override
    public boolean isOpenOnHover() {
        return getContent().isOpenOnHover();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getContent().setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return getContent().isEnabled();
    }

    @Override
    public void setWidth(String width) {
        getContent().setWidth(width);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        getContent().setWidth(width, unit);
    }

    @Override
    public void setMinWidth(String minWidth) {
        getContent().setMinWidth(minWidth);
    }

    @Override
    public void setMinWidth(float minWidth, Unit unit) {
        getContent().setMinWidth(minWidth, unit);
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        getContent().setMaxWidth(maxWidth);
    }

    @Override
    public void setMaxWidth(float maxWidth, Unit unit) {
        getContent().setMaxWidth(maxWidth, unit);
    }

    @Override
    public String getWidth() {
        return getContent().getWidth();
    }

    @Override
    public String getMinWidth() {
        return getContent().getMinWidth();
    }

    @Override
    public String getMaxWidth() {
        return getContent().getMaxWidth();
    }

    @Override
    public Optional<Unit> getWidthUnit() {
        return getContent().getWidthUnit();
    }

    @Override
    public void setHeight(String height) {
        getContent().setHeight(height);
    }

    @Override
    public void setHeight(float height, Unit unit) {
        getContent().setHeight(height, unit);
    }

    @Override
    public void setMinHeight(String minHeight) {
        getContent().setMinHeight(minHeight);
    }

    @Override
    public void setMinHeight(float minHeight, Unit unit) {
        getContent().setMinHeight(minHeight, unit);
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        getContent().setMaxHeight(maxHeight);
    }

    @Override
    public void setMaxHeight(float maxHeight, Unit unit) {
        getContent().setMaxHeight(maxHeight, unit);
    }

    @Override
    public String getHeight() {
        return getContent().getHeight();
    }

    @Override
    public String getMinHeight() {
        return getContent().getMinHeight();
    }

    @Override
    public String getMaxHeight() {
        return getContent().getMaxHeight();
    }

    @Override
    public Optional<Unit> getHeightUnit() {
        return getContent().getHeightUnit();
    }

    @Override
    public void setSizeFull() {
        getContent().setSizeFull();
    }

    @Override
    public void setWidthFull() {
        getContent().setWidthFull();
    }

    @Override
    public void setHeightFull() {
        getContent().setHeightFull();
    }

    @Override
    public void setSizeUndefined() {
        getContent().setSizeUndefined();
    }

    protected static class ActionItemImpl extends AbstractDropdownButtonItem
            implements ActionItem {

        protected final static String ACTION_ITEM_ICON_CLASS_NAME = "jmix-dropdown-button-item-icon";

        protected Icon iconComponent;
        protected Div actionLayout;

        protected Action action;

        public ActionItemImpl(String id,
                              Action action,
                              DropdownButtonComponent parent,
                              MenuItemProvider<Div> actionMenuItemProvider) {
            super(id, actionMenuItemProvider.createMenuItem(new Div()), parent);

            this.actionLayout = (Div) item.getChildren().findAny()
                    .orElseThrow(() -> new IllegalStateException("MenuItem's content is undefined"));
            this.action = action;

            setupAction();
        }

        @Override
        public Action getAction() {
            return action;
        }

        protected void setupAction() {
            setEnabled(action.isEnabled());
            setVisible(action.isVisible());
            updateContent(action.getText(), action.getIcon());

            item.addClickListener(this::onItemClick);
            action.addPropertyChangeListener(this::onActionPropertyChange);
        }

        protected void onItemClick(com.vaadin.flow.component.ClickEvent<MenuItem> event) {
            this.action.actionPerform(event.getSource());
        }

        protected void onActionPropertyChange(PropertyChangeEvent event) {
            switch (event.getPropertyName()) {
                case Action.PROP_TEXT:
                case Action.PROP_ICON:
                    updateContent(action.getText(), action.getIcon());
                    break;
                case Action.PROP_ENABLED:
                    setEnabled((Boolean) event.getNewValue());
                    break;
                case Action.PROP_VISIBLE:
                    setVisible((Boolean) event.getNewValue());
                    break;
            }
        }

        protected void updateContent(String text, Icon icon) {
            actionLayout.setText(text);

            if (icon != null && icon.getElement().isTextNode()) {
                throw new IllegalArgumentException(
                        "Text node can't be used as an icon.");
            }

            iconComponent = icon;
            if (icon != null) {
                actionLayout.addComponentAsFirst(iconComponent);
            }

            updateThemeAttribute();
        }

        protected void updateThemeAttribute() {
            if (iconComponent != null) {
                item.addThemeNames("icon");

                iconComponent.addClassName(ACTION_ITEM_ICON_CLASS_NAME);
            } else {
                item.removeThemeNames("icon");
            }
        }
    }

    protected static class ComponentItemImpl extends AbstractDropdownButtonItem
            implements ComponentItem {

        protected Component content;

        public ComponentItemImpl(String id,
                                 Component content,
                                 DropdownButtonComponent parent,
                                 MenuItemProvider<Component> componentMenuItemProvider) {
            super(id, componentMenuItemProvider.createMenuItem(content), parent);

            this.content = content;
        }

        @Override
        public void setContent(Component content) {
            item.removeAll();
            item.add(content);

            this.content = content;
        }

        @Override
        public Component getContent() {
            return content;
        }

    }

    protected static class TextItemImpl extends AbstractDropdownButtonItem
            implements TextItem {

        protected String text;

        public TextItemImpl(String id,
                            @Nullable String text,
                            DropdownButtonComponent parent,
                            MenuItemProvider<String> textMenuItemProvider) {
            super(id, textMenuItemProvider.createMenuItem(text), parent);

            this.text = text;
        }

        @Override
        public void setText(String text) {
            this.text = text;
            item.setText(text);
        }

        @Override
        public String getText() {
            return text;
        }

    }

    protected abstract static class AbstractDropdownButtonItem
            implements HasMenuItem, DropdownButtonItem {

        protected String id;
        protected MenuItem item;
        protected DropdownButtonComponent parent;

        protected Registration menuItemClickListenerRegistration;
        private EventBus eventBus;

        public AbstractDropdownButtonItem(String id, MenuItem item, DropdownButtonComponent parent) {
            this.id = id;
            this.item = item;
            this.parent = parent;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setItem(MenuItem item) {
            this.item = item;
        }

        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public DropdownButtonComponent getParent() {
            return parent;
        }

        @Override
        public void setVisible(boolean visible) {
            if (item != null) {
                item.setVisible(visible);
            }
        }

        @Override
        public boolean isVisible() {
            return item != null && item.isVisible();
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (item != null) {
                item.setEnabled(enabled);
            }
        }

        @Override
        public boolean isEnabled() {
            return item != null && item.isEnabled();
        }

        @Override
        public Registration addClickListener(Consumer<ClickEvent> listener) {
            if (menuItemClickListenerRegistration == null) {
                menuItemClickListenerRegistration = item.addClickListener(e -> {
                    ClickEvent event = new ClickEvent(this);
                    getEventBus().fireEvent(event);
                });
            }

            getEventBus().addListener(ClickEvent.class, listener);

            return () -> internalRemoveDropdownButtonItemClickListener(listener);
        }

        protected void internalRemoveDropdownButtonItemClickListener(Consumer<ClickEvent> listener) {
            getEventBus().removeListener(ClickEvent.class, listener);

            if (!getEventBus().hasListener(ClickEvent.class)) {
                menuItemClickListenerRegistration.remove();
                menuItemClickListenerRegistration = null;
            }
        }

        protected EventBus getEventBus() {
            if (eventBus == null) {
                eventBus = new EventBus();
            }

            return eventBus;
        }
    }

    protected interface HasMenuItem extends DropdownButtonItem {

        void setItem(MenuItem item);

        MenuItem getItem();
    }

    protected interface MenuItemProvider<T> {

        MenuItem createMenuItem(T content);
    }
}
