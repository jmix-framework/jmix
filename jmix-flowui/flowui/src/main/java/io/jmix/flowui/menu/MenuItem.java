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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.kit.component.KeyCombination;
import org.dom4j.Element;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main menu item descriptor.
 */
public class MenuItem {

    private MenuItem parent;
    private List<MenuItem> children = new ArrayList<>();

    private String id;

    private String view;

    private String bean;
    private String beanMethod;

    private String classNames;
    private String icon;
    private String title;
    private String description;
    private Element descriptor;
    private boolean separator = false;
    private boolean opened = false;

    private KeyCombination shortcutCombination;
    private boolean isMenu = false;

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

    public boolean isMenu() {
        return isMenu;
    }

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

    public Element getDescriptor() {
        return descriptor;
    }

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

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    @Nullable
    public KeyCombination getShortcutCombination() {
        return shortcutCombination;
    }

    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        this.shortcutCombination = shortcutCombination;
    }

    public boolean isSeparator() {
        return separator || "-".equals(id);
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    @Nullable
    public String getIcon() {
        return icon;
    }

    public void setIcon(@Nullable String icon) {
        this.icon = icon;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean expanded) {
        this.opened = expanded;
    }

    @Nullable
    public String getView() {
        return view;
    }

    public void setView(@Nullable String view) {
        this.view = view;
    }

    @Nullable
    public String getBean() {
        return bean;
    }

    public void setBean(@Nullable String bean) {
        this.bean = bean;
    }

    @Nullable
    public String getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(@Nullable String beanMethod) {
        this.beanMethod = beanMethod;
    }

    public List<MenuItemProperty> getProperties() {
        if (properties == null) {
            return Collections.emptyList();
        }
        return properties;
    }

    public void setProperties(List<MenuItemProperty> properties) {
        this.properties = properties;
    }

    public List<MenuItemParameter> getUrlQueryParameters() {
        if (urlQueryParameters == null) {
            return Collections.emptyList();
        }
        return urlQueryParameters;
    }

    public void setUrlQueryParameters(List<MenuItemParameter> urlQueryParameters) {
        this.urlQueryParameters = urlQueryParameters;
    }

    public List<MenuItemParameter> getRouteParameters() {
        if (routeParameters == null) {
            return Collections.emptyList();
        }
        return routeParameters;
    }

    public void setRouteParameters(List<MenuItemParameter> routeParameters) {
        this.routeParameters = routeParameters;
    }

    public static class MenuItemProperty {

        protected String name;
        protected Object value;

        protected MetaClass entityClass;
        protected Object entityId;
        protected String fetchPlanName;

        public MenuItemProperty(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        public MetaClass getEntityClass() {
            return entityClass;
        }

        public void setEntityClass(MetaClass entityClass) {
            this.entityClass = entityClass;
        }

        public Object getEntityId() {
            return entityId;
        }

        public void setEntityId(Object entityId) {
            this.entityId = entityId;
        }

        public String getFetchPlanName() {
            return fetchPlanName;
        }

        public void setFetchPlanName(String fetchPlanName) {
            this.fetchPlanName = fetchPlanName;
        }
    }

    public static class MenuItemParameter {

        protected String name;
        protected String value;

        public MenuItemParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
