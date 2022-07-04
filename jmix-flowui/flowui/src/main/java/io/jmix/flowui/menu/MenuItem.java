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
package io.jmix.flowui.menu;

import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main menu item descriptor
 */
public class MenuItem {

    private MenuItem parent;
    private List<MenuItem> children = new ArrayList<>();

    private String id;
    private String view;
    private String className;
    private String icon;
    private String title;
    private String description;
    private Element descriptor;
    private boolean opened = false;

    private boolean isMenu = false;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

//    public KeyCombination getShortcut() {
//        return shortcut;
//    }

//    public void setShortcut(@Nullable KeyCombination shortcut) {
//        this.shortcut = shortcut;
//    }

//    public boolean isSeparator() {
//        return separator || "-".equals(id);
//    }

//    public void setSeparator(boolean separator) {
//        this.separator = separator;
//    }

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

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

//    public String getRunnableClass() {
//        return runnableClass;
//    }

//    public void setRunnableClass(String runnableClass) {
//        this.runnableClass = runnableClass;
//    }

//    public String getBean() {
//        return bean;
//    }

//    public void setBean(String bean) {
//        this.bean = bean;
//    }

//    public String getBeanMethod() {
//        return beanMethod;
//    }

//    public void setBeanMethod(String beanMethod) {
//        this.beanMethod = beanMethod;
//    }
}
