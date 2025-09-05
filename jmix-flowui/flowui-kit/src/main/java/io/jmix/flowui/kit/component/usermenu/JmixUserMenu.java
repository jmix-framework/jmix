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

package io.jmix.flowui.kit.component.usermenu;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;
import io.jmix.flowui.kit.component.menubar.JmixSubMenu;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem.HasClickListener;
import io.jmix.flowui.kit.component.usermenu.UserMenuItem.HasSubMenu;
import io.jmix.flowui.kit.event.EventBus;
import jakarta.annotation.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A component that displays a user menu as a dropdown.
 *
 * @param <USER> the type of the user associated with the menu
 */
public class JmixUserMenu<USER> extends Composite<JmixMenuBar>
        implements HasTextMenuItems, HasActionMenuItems, HasComponentMenuItems,
        HasEnabled, HasTitle, HasOverlayClassName, HasSubParts,
        HasThemeVariant<UserMenuVariant>, Focusable<JmixUserMenu<USER>> {

    protected static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    protected static final String ATTRIBUTE_JMIX_ROLE_VALUE = "jmix-user-menu";
    protected static final String BASE_CLASS_NAME = "jmix-user-menu";
    protected static final String OVERLAY_CLASS_NAME = BASE_CLASS_NAME + "-overlay";

    protected JmixMenuItem userMenuItem;

    protected HasComponents headerWrapper;

    protected Renderer<USER> buttonRenderer;
    protected Renderer<USER> headerRenderer;

    protected USER user;

    protected JmixUserMenuItemsDelegate itemsDelegate;

    public JmixUserMenu() {
        initUserMenuItem();
    }

    protected void initUserMenuItem() {
        userMenuItem = getContent().addItem("");
        userMenuItem.setClassName(BASE_CLASS_NAME + "-button");
    }

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar content = super.initContent();
        content.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, ATTRIBUTE_JMIX_ROLE_VALUE);
        content.setOverlayClassName(OVERLAY_CLASS_NAME);

        return content;
    }

    protected JmixSubMenu getSubMenu() {
        return userMenuItem.getSubMenu();
    }

    /**
     * Returns the current user associated with the user menu.
     *
     * @return the current user, or {@code null} if no user is set
     */
    @Nullable
    public USER getUser() {
        return user;
    }

    /**
     * Sets the user for the user menu. If the given user is different
     * from the currently set user, the change is applied, an internal
     * user change handler is called, and a {@link UserChangedEvent} is fired.
     *
     * @param user the user to be set; can be {@code null} to clear the current user
     */
    public void setUser(@Nullable USER user) {
        if (!Objects.equals(this.user, user)) {
            this.user = user;
            userChangedInternal();

            fireEvent(new UserChangedEvent<>(this, user, false));
        }
    }

    protected void userChangedInternal() {
        updateButton();
        updateHeader();
    }

    public void setButtonRenderer(@Nullable Renderer<USER> buttonRenderer) {
        this.buttonRenderer = buttonRenderer;

        updateButton();
    }

    protected void updateButton() {
        userMenuItem.removeAll();

        Component buttonContent = buttonRenderer != null
                ? buttonRenderer.render(user)
                : null;

        if (buttonContent != null) {
            userMenuItem.add(buttonContent);
        }
    }

    /**
     * Sets the function to generate the header's content based on the current user.
     *
     * @param headerRenderer a function that takes a user object and returns
     *                       a {@link Component} to be used as the header;
     *                       can be {@code null}, in which case no header
     *                       content will be displayed
     */
    public void setHeaderRenderer(@Nullable Renderer<USER> headerRenderer) {
        this.headerRenderer = headerRenderer;

        updateHeader();
    }

    protected void updateHeader() {
        if (headerWrapper != null) {
            headerWrapper.removeAll();
        }

        Component headerContent = headerRenderer != null
                ? headerRenderer.render(user)
                : null;

        if (headerContent != null) {
            if (headerWrapper == null) {
                headerWrapper = createHeaderWrapper();
                getSubMenu().addComponentAtIndex(0, (Component) headerWrapper);
            }

            headerWrapper.add(headerContent);
        } else if (headerWrapper != null) {
            getSubMenu().remove((Component) headerWrapper);
            headerWrapper = null;
        }
    }

    protected HasComponents createHeaderWrapper() {
        Div wrapper = new Div();
        wrapper.setClassName(BASE_CLASS_NAME + "-header");

        return wrapper;
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text) {
        return getItemsDelegate().addTextItem(id, text);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, int index) {
        return getItemsDelegate().addTextItem(id, text, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text,
                                        Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return getItemsDelegate().addTextItem(id, text, listener);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text,
                                        Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                        int index) {
        return getItemsDelegate().addTextItem(id, text, listener, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, Component icon) {
        return getItemsDelegate().addTextItem(id, text, icon);
    }

    @Override
    public TextUserMenuItem addTextItem(String id, String text, Component icon, int index) {
        return getItemsDelegate().addTextItem(id, text, icon, index);
    }

    @Override
    public TextUserMenuItem addTextItem(String id,
                                        String text, Component icon,
                                        Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
        return getItemsDelegate().addTextItem(id, text, icon, listener);
    }

    @Override
    public TextUserMenuItem addTextItem(String id,
                                        String text, Component icon,
                                        Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                        int index) {
        return getItemsDelegate().addTextItem(id, text, icon, listener, index);
    }

    @Override
    public ActionUserMenuItem addActionItem(String id, Action action) {
        return getItemsDelegate().addActionItem(id, action);
    }

    @Override
    public ActionUserMenuItem addActionItem(String id, Action action, int index) {
        return getItemsDelegate().addActionItem(id, action, index);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content) {
        return getItemsDelegate().addComponentItem(id, content);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content, int index) {
        return getItemsDelegate().addComponentItem(id, content, index);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content,
                                                  Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener) {
        return getItemsDelegate().addComponentItem(id, content, listener);
    }

    @Override
    public ComponentUserMenuItem addComponentItem(String id, Component content,
                                                  Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                                  int index) {
        return getItemsDelegate().addComponentItem(id, content, listener, index);
    }

    @Override
    public void addSeparator() {
        getItemsDelegate().addSeparator();
    }

    @Override
    public void addSeparatorAtIndex(int index) {
        getItemsDelegate().addSeparatorAtIndex(index);
    }

    @Override
    public Optional<UserMenuItem> findItem(String itemId) {
        return getItemsDelegate().findItem(itemId);
    }

    @Override
    public UserMenuItem getItem(String itemId) {
        return getItemsDelegate().getItem(itemId);
    }

    @Nullable
    @Override
    public UserMenuItem getSubPart(String name) {
        return findItem(name).orElse(null);
    }

    @Override
    public List<UserMenuItem> getItems() {
        return getItemsDelegate().getItems();
    }

    @Override
    public void remove(String itemId) {
        getItemsDelegate().remove(itemId);
    }

    @Override
    public void remove(UserMenuItem menuItem) {
        getItemsDelegate().remove(menuItem);
    }

    @Override
    public void removeAll() {
        getItemsDelegate().removeAll();
    }

    /**
     * Determines whether the user menu opens on hover.
     *
     * @return {@code true} if the menu opens on hover, {@code false} otherwise
     */
    public boolean isOpenOnHover() {
        return getContent().isOpenOnHover();
    }

    /**
     * Sets whether the user menu should open when hovered.
     *
     * @param openOnHover {@code true} to make the menu open on hover, {@code false} to disable this behavior
     */
    public void setOpenOnHover(boolean openOnHover) {
        getContent().setOpenOnHover(openOnHover);
    }

    @Override
    public void setOverlayClassName(@Nullable String overlayClassName) {
        HasOverlayClassName.super.setOverlayClassName(OVERLAY_CLASS_NAME + " " +
                Strings.nullToEmpty(overlayClassName));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addUserChangedListener(ComponentEventListener<UserChangedEvent<USER>> listener) {
        return getEventBus().addListener(UserChangedEvent.class, ((ComponentEventListener) listener));
    }

    protected JmixUserMenuItemsDelegate getItemsDelegate() {
        if (itemsDelegate == null) {
            itemsDelegate = createUserMenuItemsDelegate(getSubMenu());
        }

        return itemsDelegate;
    }

    /* For internal use only. */
    protected JmixUserMenuItemsDelegate createUserMenuItemsDelegate(JmixSubMenu subMenu) {
        return new JmixUserMenuItemsDelegate(this, subMenu);
    }

    /* For internal use only. */
    protected HasSubMenu.SubMenu createSubMenu(JmixSubMenu subMenu) {
        return new JmixUserMenuSubMenu(this, subMenu);
    }

    /**
     * Represents an event that is triggered when the user associated with the
     * {@link JmixUserMenu} component changes.
     *
     * @param <USER> the type of the user associated with the menu
     */
    public static class UserChangedEvent<USER> extends ComponentEvent<JmixUserMenu<USER>> {

        protected final USER user;

        /**
         * Creates a new user changed event.
         *
         * @param source     the source component
         * @param fromClient {@code true} if the event originated from the client
         *                   side, {@code false} otherwise
         */
        public UserChangedEvent(JmixUserMenu<USER> source, @Nullable USER user, boolean fromClient) {
            super(source, fromClient);

            this.user = user;
        }

        /**
         * Returns the user associated with the event.
         *
         * @return the user associated with the event, or {@code null} if no user is associated
         */
        @Nullable
        public USER getUser() {
            return user;
        }
    }

    @FunctionalInterface
    public interface Renderer<T> {

        @Nullable
        Component render(@Nullable T item);
    }

    protected static class TextUserMenuItemImpl extends AbstractTextUserMenuItem implements TextUserMenuItem {

        public TextUserMenuItemImpl(String id,
                                    JmixUserMenu<?> userMenu,
                                    JmixMenuItem item,
                                    String text) {
            super(id, userMenu, item, text);
        }

        @Override
        public String getText() {
            return super.getText();
        }

        @Override
        public void setText(String text) {
            String oldValue = getText();
            if (!Objects.equals(oldValue, text)) {
                super.setText(text);
                firePropertyChange(TextUserMenuItem.PROP_TEXT, oldValue, text);
            }
        }

        @Nullable
        @Override
        public Component getIcon() {
            return super.getIcon();
        }

        @Override
        public void setIcon(@Nullable Component icon) {
            Component oldValue = getIcon();
            if (!Objects.equals(oldValue, icon)) {
                super.setIcon(icon);
                firePropertyChange(TextUserMenuItem.PROP_ICON, oldValue, icon);
            }
        }

        @Override
        public SubMenu getSubMenu() {
            return super.getSubMenu();
        }

        @Override
        public Registration addClickListener(Consumer<ClickEvent<TextUserMenuItem>> listener) {
            return super.addClickListenerInternal(listener);
        }
    }

    protected static abstract class AbstractTextUserMenuItem extends AbstractUserMenuItem {

        protected String text;
        protected Component icon;

        public AbstractTextUserMenuItem(String id,
                                        JmixUserMenu<?> userMenu,
                                        JmixMenuItem item,
                                        String text) {
            super(id, userMenu, item);

            this.text = text;
        }

        protected String getText() {
            return text;
        }

        protected void setText(String text) {
            Preconditions.checkArgument(text != null, "Text cannot be null");

            this.text = text;
            updateContent(text, icon);
        }

        @Nullable
        protected Component getIcon() {
            return icon;
        }

        protected void setIcon(@Nullable Component icon) {
            if (Objects.equals(this.icon, icon)) {
                return;
            }

            if (this.icon != null) {
                item.remove(this.icon);
            }

            this.icon = icon;
            if (icon != null) {
                item.addComponentAsFirst(icon);
            }
        }
    }

    protected static class ActionUserMenuItemImpl extends AbstractUserMenuItem
            implements ActionUserMenuItem {

        protected final Action action;

        public ActionUserMenuItemImpl(String id,
                                      JmixUserMenu<?> userMenu,
                                      JmixMenuItem item,
                                      Action action) {
            super(id, userMenu, item);

            this.action = action;
            initItem(action);
        }

        protected void initItem(Action action) {
            if (action.getIcon() != null) {
                item.addComponentAsFirst(action.getIcon());
            }

            item.setEnabled(action.isEnabled());
            item.setVisible(action.isVisible());

            item.addClickListener(this::onItemClick);
            action.addPropertyChangeListener(this::onActionPropertyChange);
        }

        protected void onItemClick(ClickEvent<MenuItem> event) {
            action.actionPerform(event.getSource());
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
                default:
            }
        }

        @Override
        public Action getAction() {
            return action;
        }

        @Override
        public HasSubMenu.SubMenu getSubMenu() {
            return super.getSubMenu();
        }
    }

    protected static class ComponentUserMenuItemImpl extends AbstractUserMenuItem
            implements ComponentUserMenuItem {

        protected Component content;

        public ComponentUserMenuItemImpl(String id,
                                         JmixUserMenu<?> userMenu,
                                         JmixMenuItem item,
                                         Component content) {
            super(id, userMenu, item);

            this.content = content;
        }

        @Override
        public Component getContent() {
            return content;
        }

        @Override
        public void setContent(Component content) {
            Preconditions.checkArgument(content != null, "Content cannot be null");

            if (Objects.equals(this.content, content)) {
                return;
            }

            item.removeAll();
            item.add(content);

            this.content = content;
        }

        @Override
        public SubMenu getSubMenu() {
            return super.getSubMenu();
        }

        @Override
        public Registration addClickListener(Consumer<ClickEvent<ComponentUserMenuItem>> listener) {
            return super.addClickListenerInternal(listener);
        }
    }

    protected static abstract class AbstractUserMenuItem implements UserMenuItem, HasMenuItem {

        protected final String id;
        protected final JmixUserMenu<?> userMenu;
        protected final JmixMenuItem item;

        protected HasSubMenu.SubMenu subMenu;

        protected Registration menuItemClickListenerRegistration;

        private EventBus eventBus;

        public AbstractUserMenuItem(String id, JmixUserMenu<?> userMenu, JmixMenuItem item) {
            this.id = id;
            this.userMenu = userMenu;
            this.item = item;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public Element getElement() {
            return item.getElement();
        }

        @Override
        public boolean isVisible() {
            return item.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            boolean oldValue = isVisible();
            if (!Objects.equals(oldValue, visible)) {
                item.setVisible(visible);
                firePropertyChange(UserMenuItem.PROP_VISIBLE, oldValue, visible);
            }
        }

        @Override
        public boolean isEnabled() {
            return item.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            boolean oldValue = isEnabled();
            if (!Objects.equals(oldValue, enabled)) {
                item.setEnabled(enabled);
                firePropertyChange(UserMenuItem.PROP_ENABLED, oldValue, enabled);
            }
        }

        @Override
        public boolean isCheckable() {
            return item.isCheckable();
        }

        @Override
        public void setCheckable(boolean checkable) {
            boolean oldValue = isCheckable();
            if (!Objects.equals(oldValue, checkable)) {
                item.setCheckable(checkable);
                firePropertyChange(UserMenuItem.PROP_CHECKABLE, oldValue, checkable);
            }
        }

        @Override
        public boolean isChecked() {
            return item.isChecked();
        }

        @Override
        public void setChecked(boolean checked) {
            boolean oldValue = isChecked();
            if (!Objects.equals(oldValue, checked)) {
                item.setChecked(checked);
                firePropertyChange(UserMenuItem.PROP_CHECKED, oldValue, checked);
            }
        }

        @Override
        public Registration addPropertyChangeListener(Consumer<PropertyChangeEvent> listener) {
            return getEventBus().addListener(PropertyChangeEvent.class, listener);
        }

        protected HasSubMenu.SubMenu getSubMenu() {
            if (subMenu == null) {
                subMenu = createSubMenu();
            }

            return subMenu;
        }

        @Nullable
        @Override
        public UserMenuItem getSubPart(String name) {
            if (subMenu != null) {
                return subMenu.findItem(name).orElse(null);
            }

            return null;
        }

        protected HasSubMenu.SubMenu createSubMenu() {
            // Delegate creation for easier overriding in subclasses
            return userMenu.createSubMenu(item.getSubMenu());
        }

        protected <ITEM extends UserMenuItem> Registration addClickListenerInternal(
                Consumer<HasClickListener.ClickEvent<ITEM>> listener) {
            if (menuItemClickListenerRegistration == null) {
                menuItemClickListenerRegistration = item.addClickListener(e -> {
                    HasClickListener.ClickEvent<?> event = new HasClickListener.ClickEvent<>(this);
                    getEventBus().fireEvent(event);
                });
            }

            //noinspection unchecked,rawtypes
            getEventBus().addListener(HasClickListener.ClickEvent.class, (Consumer) listener);

            return () -> internalRemoveClickListener(listener);
        }

        protected <ITEM extends UserMenuItem> void internalRemoveClickListener(
                Consumer<HasClickListener.ClickEvent<ITEM>> listener) {
            //noinspection unchecked,rawtypes
            getEventBus().removeListener(HasClickListener.ClickEvent.class, (Consumer) listener);

            if (!getEventBus().hasListener(HasClickListener.ClickEvent.class)) {
                menuItemClickListenerRegistration.remove();
                menuItemClickListenerRegistration = null;
            }
        }

        protected void updateContent(@Nullable String text, @Nullable Component icon) {
            item.removeAll();

            item.setText(text);
            if (icon != null) {
                item.addComponentAsFirst(icon);
            }
        }

        protected EventBus getEventBus() {
            if (eventBus == null) {
                eventBus = new EventBus();
            }

            return eventBus;
        }

        protected boolean hasListener(Class<? extends EventObject> eventType) {
            return eventBus != null && eventBus.hasListener(eventType);
        }

        protected void firePropertyChange(String propertyName, @Nullable Object oldValue, @Nullable Object newValue) {
            if (hasListener(PropertyChangeEvent.class)) {
                PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
                getEventBus().fireEvent(event);
            }
        }

        @Override
        public String toString() {
            return "%s{id='%s'}".formatted(getClass().getName(), id);
        }
    }

    protected static class JmixUserMenuSubMenu implements HasSubMenu.SubMenu {

        protected final JmixUserMenu<?> userMenu;
        protected final JmixSubMenu subMenu;

        protected JmixUserMenuItemsDelegate itemsDelegate;

        public JmixUserMenuSubMenu(JmixUserMenu<?> userMenu, JmixSubMenu subMenu) {
            this.userMenu = userMenu;
            this.subMenu = subMenu;
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text) {
            return getItemsDelegate().addTextItem(id, text);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text, int index) {
            return getItemsDelegate().addTextItem(id, text, index);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text,
                                            Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
            return getItemsDelegate().addTextItem(id, text, listener);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text,
                                            Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                            int index) {
            return getItemsDelegate().addTextItem(id, text, listener, index);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text, Component icon) {
            return getItemsDelegate().addTextItem(id, text, icon);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text, Component icon, int index) {
            return getItemsDelegate().addTextItem(id, text, icon, index);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text, Component icon,
                                            Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener) {
            return getItemsDelegate().addTextItem(id, text, icon, listener);
        }

        @Override
        public TextUserMenuItem addTextItem(String id, String text, Component icon,
                                            Consumer<HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                                            int index) {
            return getItemsDelegate().addTextItem(id, text, icon, listener, index);
        }

        @Override
        public ActionUserMenuItem addActionItem(String id, Action action) {
            return getItemsDelegate().addActionItem(id, action);
        }

        @Override
        public ActionUserMenuItem addActionItem(String id, Action action, int index) {
            return getItemsDelegate().addActionItem(id, action, index);
        }

        @Override
        public ComponentUserMenuItem addComponentItem(String id, Component content) {
            return getItemsDelegate().addComponentItem(id, content);
        }

        @Override
        public ComponentUserMenuItem addComponentItem(String id, Component content, int index) {
            return getItemsDelegate().addComponentItem(id, content, index);
        }

        @Override
        public ComponentUserMenuItem addComponentItem(String id, Component content,
                                                      Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener) {
            return getItemsDelegate().addComponentItem(id, content, listener);
        }

        @Override
        public ComponentUserMenuItem addComponentItem(String id, Component content,
                                                      Consumer<HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                                      int index) {
            return getItemsDelegate().addComponentItem(id, content, listener, index);
        }

        @Override
        public void addSeparator() {
            getItemsDelegate().addSeparator();
        }

        @Override
        public void addSeparatorAtIndex(int index) {
            getItemsDelegate().addSeparatorAtIndex(index);
        }

        @Override
        public Optional<UserMenuItem> findItem(String itemId) {
            return getItemsDelegate().findItem(itemId);
        }

        @Override
        public UserMenuItem getItem(String itemId) {
            return getItemsDelegate().getItem(itemId);
        }

        @Override
        public List<UserMenuItem> getItems() {
            return getItemsDelegate().getItems();
        }

        @Override
        public void remove(String itemId) {
            getItemsDelegate().remove(itemId);
        }

        @Override
        public void remove(UserMenuItem menuItem) {
            getItemsDelegate().remove(menuItem);
        }

        @Override
        public void removeAll() {
            getItemsDelegate().removeAll();
        }

        protected JmixUserMenuItemsDelegate getItemsDelegate() {
            if (itemsDelegate == null) {
                itemsDelegate = createUserMenuItemsDelegate();
            }

            return itemsDelegate;
        }

        protected JmixUserMenuItemsDelegate createUserMenuItemsDelegate() {
            // Delegate creation for easier overriding in subclasses
            return userMenu.createUserMenuItemsDelegate(subMenu);
        }
    }

    /**
     * Represents a component that has an associated {@link MenuItem}.
     * This interface is typically used to provide access to the linked menu item
     * for components or entities that function as a user menu item.
     */
    protected interface HasMenuItem {

        /**
         * Returns the associated {@link MenuItem}.
         *
         * @return the {@link MenuItem} instance linked to this user menu item
         */
        MenuItem getItem();
    }
}
