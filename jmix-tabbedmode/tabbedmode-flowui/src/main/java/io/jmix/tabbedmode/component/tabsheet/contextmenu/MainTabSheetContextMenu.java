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

package io.jmix.tabbedmode.component.tabsheet.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import elemental.json.JsonObject;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;
import io.jmix.tabbedmode.component.tabsheet.JmixMainTabSheet;
import org.springframework.lang.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

public class MainTabSheetContextMenu extends
        ContextMenuBase<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu>
        implements HasMainTabSheetMenuItems {

    private SerializablePredicate<Tab> dynamicContentHandler;

    public MainTabSheetContextMenu() {
    }

    public MainTabSheetContextMenu(JmixMainTabSheet target) {
        setTarget(target);
    }


    public MainTabSheetMenuItem addItemAtIndex(int index, String text) {
        return getItems().size() == index
                ? getMenuManager().addItem(text)
                : getMenuManager().addItemAtIndex(index, text);
    }

    public MainTabSheetMenuItem addItemAtIndex(int index, String text,
                                               @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        MainTabSheetMenuItem menuItem = addItemAtIndex(index, text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    public MainTabSheetMenuItem addItemAtIndex(int index, Component component) {
        return getItems().size() == index
                ? getMenuManager().addItem(component)
                : getMenuManager().addItemAtIndex(index, component);
    }

    public MainTabSheetMenuItem addItemAtIndex(int index, Component component,
                                               @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        MainTabSheetMenuItem menuItem = addItemAtIndex(index, component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    public MainTabSheetMenuItem addItem(String text,
                                        @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        Preconditions.checkNotNullArgument(text);

        MainTabSheetMenuItem menuItem = addItem(text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }

        return menuItem;
    }

    @Override
    public MainTabSheetMenuItem addItem(Component component,
                                        @Nullable ComponentEventListener<MainTabSheetContextMenuItemClickEvent> clickListener) {
        Preconditions.checkNotNullArgument(component);

        MainTabSheetMenuItem menuItem = addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }

        return menuItem;
    }

    @Override
    public void setTarget(Component target) {
        checkArgument(target instanceof JmixMainTabSheet,
                "Only an instance of %s can be used as the target for %s."
                        .formatted(JmixMainTabSheet.class.getSimpleName(),
                                MainTabSheetContextMenu.class.getSimpleName()));

        super.setTarget(target);
    }

    @Override
    public JmixMainTabSheet getTarget() {
        return (JmixMainTabSheet) super.getTarget();
    }

    /**
     * Gets the callback function that is executed before the context menu is
     * opened.
     *
     * <p>
     * The dynamic context handler allows for customizing the contents of the
     * context menu before it is open.
     * </p>
     *
     * @return the callback function that is executed before opening the context
     *         menu, or {@code null} if not specified.
     */
    @Nullable
    public SerializablePredicate<Tab> getDynamicContentHandler() {
        return dynamicContentHandler;
    }

    /**
     * Sets a callback that is executed before the context menu is opened.
     *
     * <p>
     * This callback receives the clicked item (if any) as an input parameter
     * and further can dynamically modify the contents of the context menu. This
     * is useful in situations where the context menu items cannot be known in
     * advance and depend on the specific context (i.e. clicked row) and thus
     * can be configured dynamically.
     *
     * The boolean return value of this callback specifies if the context menu
     * will be opened.
     * </p>
     *
     * @param dynamicContentHandler
     *            the callback function that will be executed before opening the
     *            context menu.
     */
    public void setDynamicContentHandler(@Nullable SerializablePredicate<Tab> dynamicContentHandler) {
        this.dynamicContentHandler = dynamicContentHandler;
    }

    @Override
    protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
        if (getDynamicContentHandler() != null) {
            String tabId = eventDetail.getString("tabId");
            Tab tab = getTarget().getTab(tabId);
            getDynamicContentHandler().test(tab);
        }

        return super.onBeforeOpenMenu(eventDetail);
    }

    @Override
    protected MenuManager<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new JmixMenuManager<>(this, contentReset, MainTabSheetMenuItem::new, MainTabSheetMenuItem.class, null);
    }

    @Override
    protected JmixMenuManager<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu> getMenuManager() {
        return (JmixMenuManager<MainTabSheetContextMenu, MainTabSheetMenuItem, MainTabSheetSubMenu>) super.getMenuManager();
    }

    public static class MainTabSheetContextMenuItemClickEvent extends ComponentEvent<MainTabSheetMenuItem> {

        protected JmixMainTabSheet tabSheet;
        protected Tab tab;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         *                   side, <code>false</code> otherwise
         */
        public MainTabSheetContextMenuItemClickEvent(MainTabSheetMenuItem source, boolean fromClient) {
            super(source, fromClient);

            tabSheet = getSource().getContextMenu().getTarget();

            String tabId = tabSheet.getElement().getProperty("_contextMenuTargetTabId");
            tab = tabSheet.getTab(tabId);
        }

        public JmixMainTabSheet getTabSheet() {
            return tabSheet;
        }

        public Tab getTab() {
            return tab;
        }
    }
}
