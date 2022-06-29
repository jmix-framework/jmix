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
package io.jmix.ui.menu;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.KeyCombination;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main menu item descriptor
 */
public class MenuItem {

    private MenuItem parent;
    private List<MenuItem> children = new ArrayList<>();

    private String id;
    private String screen;
    private String runnableClass;
    private String bean;
    private String beanMethod;
    private String stylename;
    private String icon;
    private String caption;
    private String description;
    private Element descriptor;
    private boolean separator = false;
    private boolean expanded = false;

    private KeyCombination shortcut;
    private boolean isMenu = false;

    protected List<MenuItemProperty> properties;

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
     * @return a raw string from menu XML config, can be a reference to localization message, e.g. {@code mainMsg://menuitem.caption}
     * @see MenuConfig#getItemCaption(MenuItem)
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the given {@code caption} to menu item.
     *
     * @param caption menu item caption
     * @see MenuConfig#getItemCaption(MenuItem)
     */
    public void setCaption(String caption) {
        this.caption = caption;
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

    public String getStylename() {
        return stylename;
    }

    public void setStylename(String stylename) {
        this.stylename = stylename;
    }

    public KeyCombination getShortcut() {
        return shortcut;
    }

    public void setShortcut(@Nullable KeyCombination shortcut) {
        this.shortcut = shortcut;
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

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getRunnableClass() {
        return runnableClass;
    }

    public void setRunnableClass(String runnableClass) {
        this.runnableClass = runnableClass;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(String beanMethod) {
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

    protected static class MenuItemProperty {

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
}
