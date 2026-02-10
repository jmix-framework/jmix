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
package io.jmix.flowui.menu;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main menu item descriptor.
 */
public class MenuItem {

    protected MenuItem parent;
    protected List<MenuItem> children = new ArrayList<>();

    protected String id;

    protected String view;

    protected String bean;
    protected String beanMethod;

    protected String classNames;
    protected Component icon;
    protected String iconName;
    protected String title;
    protected String description;
    protected Element descriptor;
    protected boolean separator = false;
    protected boolean opened = false;
    protected boolean visible = true;

    protected KeyCombination shortcutCombination;
    protected boolean isMenu = false;

    protected List<MenuItemProperty> properties;
    protected List<MenuItemParameter> urlQueryParameters;
    protected List<MenuItemParameter> routeParameters;

    public MenuItem(@Nullable MenuItem parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public MenuItem(String id) {
        this(null, id);
    }

    /**
     * Returns whether the menu item represents a menu that contains other menu items.
     *
     * @return {@code true} if the menu item is a menu; {@code false} otherwise.
     */
    public boolean isMenu() {
        return isMenu;
    }

    /**
     * Sets whether this menu item represents a menu that contains other menu items.
     *
     * @param isMenu {@code true} if the menu item is a menu; {@code false} otherwise
     */
    public void setMenu(boolean isMenu) {
        this.isMenu = isMenu;
    }

    /**
     * Parent item. Null if this is root item.
     */
    @Nullable
    public MenuItem getParent() {
        return parent;
    }

    /**
     * Children items
     */
    public List<MenuItem> getChildren() {
        return children;
    }

    /**
     * Menu item ID as defined in <code>menu-config.xml</code>
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the descriptor of this menu item.
     *
     * @return the {@link Element} representing the descriptor
     */
    public Element getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the descriptor of this menu item.
     *
     * @param descriptor the {@link Element} representing the descriptor
     */
    public void setDescriptor(Element descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return a raw string from menu XML config, can be a reference to localization message, e.g. {@code mainMsg://menuitem.title}
     * @see MenuConfig#getItemTitle(MenuItem)
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the given {@code caption} to menu item.
     *
     * @param caption menu item caption
     * @see MenuConfig#getItemTitle(MenuItem)
     */
    public void setTitle(String caption) {
        this.title = caption;
    }

    /**
     * @return a raw string from menu XML config, can be a reference to localization message, e.g. {@code mainMsg://menuitem.description}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the given {@code description} to menu item.
     *
     * @param description menu item description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the CSS class names associated with this menu item.
     *
     * @return a string containing the CSS class names
     */
    public String getClassNames() {
        return classNames;
    }

    /**
     * Sets the CSS class names associated with this menu item.
     *
     * @param classNames a string containing the CSS class names to be set for this menu item
     */
    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    /**
     * Returns the keyboard shortcut combination associated with this menu item.
     *
     * @return the {@link KeyCombination} representing the shortcut combination,
     * or {@code null} if no shortcut is defined
     */
    @Nullable
    public KeyCombination getShortcutCombination() {
        return shortcutCombination;
    }

    /**
     * Sets the keyboard shortcut combination associated with this menu item.
     *
     * @param shortcutCombination the {@link KeyCombination} representing the shortcut combination,
     *                            or {@code null} if no shortcut is to be defined
     */
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        this.shortcutCombination = shortcutCombination;
    }

    /**
     * Returns whether the menu item is a separator.
     *
     * @return {@code true} if the menu item is a separator;
     * {@code false} otherwise.
     */
    public boolean isSeparator() {
        return separator || "-".equals(id);
    }

    /**
     * Sets whether this menu item is a separator.
     *
     * @param separator {@code true} if the menu item is a separator; {@code false} otherwise
     */
    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    /**
     * Returns the icon associated with this menu item.
     *
     * @return the icon as a string, or {@code null} if no icon is associated
     * @deprecated use {@link #getIconComponent()} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    @Nullable
    public String getIcon() {
        return !Strings.isNullOrEmpty(iconName)
                ? iconName
                : icon instanceof Icon iconComponent ? iconComponent.getIcon() : null;
    }

    /**
     * Sets the icon associated with this menu item.
     *
     * @param iconName the icon to associate with the menu item, or {@code null} if no icon is to be set
     * @deprecated use {@link #setIconComponent(Component)} instead
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public void setIcon(@Nullable String iconName) {
        this.iconName = iconName;
        setIconComponent(Strings.isNullOrEmpty(iconName)
                ? null
                : ComponentUtils.parseIcon(iconName));
    }

    @Nullable
    public Component getIconComponent() {
        return icon;
    }

    public void setIconComponent(@Nullable Component icon) {
        this.icon = icon;
    }

    /**
     * Returns whether this menu item is currently in an expanded (opened) state.
     *
     * @return {@code true} if the menu item is opened; {@code false} otherwise.
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Sets whether this menu item is currently in an expanded (opened) state.
     *
     * @param expanded {@code true} if the menu item is to be opened; {@code false} otherwise
     */
    public void setOpened(boolean expanded) {
        this.opened = expanded;
    }

    /**
     * Returns whether this menu item is visible.
     *
     * @return {@code true} if the menu item is visible; {@code false} otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether this menu item is visible.
     *
     * @param visible {@code true} if the menu item is to be visible; {@code false} otherwise
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the view id associated with this menu item.
     *
     * @return the view id, or {@code null} if no view is associated
     */
    @Nullable
    public String getView() {
        return view;
    }

    /**
     * Sets the view ID associated with this menu item.
     *
     * @param view the view ID to associate with this menu item,
     *             or {@code null} if no view is to be associated
     */
    public void setView(@Nullable String view) {
        this.view = view;
    }

    /**
     * Returns the bean name associated with this menu item.
     *
     * @return the bean name, or {@code null} if no bean is associated
     */
    @Nullable
    public String getBean() {
        return bean;
    }

    /**
     * Sets the bean name associated with this menu item.
     *
     * @param bean the bean name to associate with this menu item, or {@code null}
     *             if no bean is to be associated
     */
    public void setBean(@Nullable String bean) {
        this.bean = bean;
    }

    /**
     * Returns the bean method name associated with this menu item.
     *
     * @return the bean method name, or {@code null} if no bean method is associated
     */
    @Nullable
    public String getBeanMethod() {
        return beanMethod;
    }

    /**
     * Sets the bean method name associated with this menu item.
     *
     * @param beanMethod the bean method name to associate with this menu item,
     *                   or {@code null} if no bean method is to be associated
     */
    public void setBeanMethod(@Nullable String beanMethod) {
        this.beanMethod = beanMethod;
    }

    /**
     * Returns the list of properties associated with this menu item.
     *
     * @return the list of {@link MenuItemProperty} objects
     */
    public List<MenuItemProperty> getProperties() {
        if (properties == null) {
            return Collections.emptyList();
        }

        return properties;
    }

    /**
     * Sets the list of properties associated with this menu item.
     *
     * @param properties the list of {@link MenuItemProperty} objects to associate with this menu item,
     *                   or {@code null} if no properties are to be set
     */
    public void setProperties(List<MenuItemProperty> properties) {
        this.properties = properties;
    }

    /**
     * Returns the list of URL query parameters associated with this menu item.
     * If no parameters are associated, an empty list is returned.
     *
     * @return a list of {@link MenuItemParameter} objects representing
     * the URL query parameters
     */
    public List<MenuItemParameter> getUrlQueryParameters() {
        if (urlQueryParameters == null) {
            return Collections.emptyList();
        }

        return urlQueryParameters;
    }

    /**
     * Sets the list of URL query parameters associated with this menu item.
     *
     * @param urlQueryParameters a list of {@link MenuItemParameter} objects representing
     *                           the URL query parameters to associate with this menu item
     */
    public void setUrlQueryParameters(List<MenuItemParameter> urlQueryParameters) {
        this.urlQueryParameters = urlQueryParameters;
    }

    /**
     * Returns the list of route parameters associated with this menu item.
     * If no parameters are associated, an empty list is returned.
     *
     * @return a list of {@link MenuItemParameter} objects representing
     * the route parameters
     */
    public List<MenuItemParameter> getRouteParameters() {
        if (routeParameters == null) {
            return Collections.emptyList();
        }
        return routeParameters;
    }

    /**
     * Sets the list of route parameters associated with this menu item.
     *
     * @param routeParameters a list of {@link MenuItemParameter} objects representing
     *                        the route parameters to associate with this menu item
     */
    public void setRouteParameters(List<MenuItemParameter> routeParameters) {
        this.routeParameters = routeParameters;
    }

    /**
     * Represents a property of a {@code MenuItem} within a menu configuration.
     * A property can contain a name and associated value, along with metadata
     * such as entity class, entity ID, and fetch plan name.
     */
    public static class MenuItemProperty {

        protected String name;
        protected Object value;

        protected MetaClass entityClass;
        protected Object entityId;
        protected String fetchPlanName;

        public MenuItemProperty(String name) {
            this.name = name;
        }

        /**
         * Returns the name associated with this property.
         *
         * @return the name of the property
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the value associated with this property.
         *
         * @return the value of the property, or {@code null} if no value is specified.
         */
        @Nullable
        public Object getValue() {
            return value;
        }

        /**
         * Sets the value associated with this property.
         *
         * @param value the value to associate with this property, or {@code null} to unset the value
         */
        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        /**
         * Returns the {@link MetaClass} associated with this property.
         *
         * @return the {@link MetaClass} of the entity, or {@code null} if no entity class is specified
         */
        public MetaClass getEntityClass() {
            return entityClass;
        }

        /**
         * Sets the {@link MetaClass} associated with this property.
         *
         * @param entityClass the {@link MetaClass} to be associated with this property, or {@code null} to remove
         *                    the association with any entity class
         */
        public void setEntityClass(MetaClass entityClass) {
            this.entityClass = entityClass;
        }

        /**
         * Returns the entity ID associated with this property.
         *
         * @return the entity ID associated with this property
         */
        public Object getEntityId() {
            return entityId;
        }

        /**
         * Sets the entity ID associated with this property.
         *
         * @param entityId the entity ID to associate with this property
         */
        public void setEntityId(Object entityId) {
            this.entityId = entityId;
        }

        /**
         * Returns the fetch plan name associated with this property.
         *
         * @return the fetch plan name
         */
        public String getFetchPlanName() {
            return fetchPlanName;
        }

        /**
         * Sets the fetch plan name associated with this property.
         *
         * @param fetchPlanName the fetch plan name to be associated with this property
         */
        public void setFetchPlanName(String fetchPlanName) {
            this.fetchPlanName = fetchPlanName;
        }
    }

    /**
     * Represents a parameter associated with a menu item, consisting of
     * a name and a value.
     */
    public static class MenuItemParameter {

        protected String name;
        protected String value;

        public MenuItemParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns the parameter name.
         *
         * @return the parameter name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the parameter value.
         *
         * @return the parameter value
         */
        public String getValue() {
            return value;
        }
    }
}
