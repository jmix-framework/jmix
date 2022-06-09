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
package io.jmix.ui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.ContainerType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * TabSheet component interface.
 */
@StudioComponent(
        caption = "TabSheet",
        category = "Containers",
        xmlElement = "tabSheet",
        icon = "io/jmix/ui/icon/container/tabSheet.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.TAB_SHEET,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/tab-sheet.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px",
                        initialValue = "100px"),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface TabSheet extends ComponentContainer, Component.BelongToFrame, Component.HasIcon, Component.HasCaption,
        Component.Focusable, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "tabSheet";

    /**
     * Adds a new tab to the component.
     *
     * @param name      id of the new tab
     * @param component a component that will be the content of the new tab
     * @return the new tab
     */
    Tab addTab(String name, Component component);

    /**
     * INTERNAL. Adds a new lazy tab to the component.
     *
     * @param name       id of the new tab
     * @param descriptor the element descriptor
     * @param loader     the component loader
     * @return the new tab
     */
    @Internal
    Tab addLazyTab(String name, Element descriptor, ComponentLoader loader);

    /**
     * Removes a tab.
     *
     * @param name id of the tab to remove
     */
    void removeTab(String name);

    /**
     * Removes all tabs.
     */
    void removeAllTabs();

    /**
     * Gets selected tab. May be null if the tabsheet does not contain tabs at all.
     *
     * @return a selected tab instance
     */
    @Nullable
    Tab getSelectedTab();

    /**
     * Sets selected tab.
     *
     * @param tab tab instance
     */
    @StudioElement
    void setSelectedTab(Tab tab);

    /**
     * Sets selected tab.
     *
     * @param name tab id
     */
    void setSelectedTab(String name);

    /**
     * Gets tab with the provided id.
     *
     * @param name tab id
     * @return tab instance
     */
    @Nullable
    Tab getTab(String name);

    /**
     * Gets a component that is a content of the tab.
     *
     * @param name tab id
     * @return tab content
     */
    Component getTabComponent(String name);

    /**
     * Gets all tabs.
     *
     * @return the collection of tab instances
     */
    Collection<Tab> getTabs();

    /**
     * @return true if the tab captions are rendered as HTML, false if rendered as plain text
     */
    boolean isTabCaptionsAsHtml();

    /**
     * Sets whether HTML is allowed in the tab captions.
     *
     * @param tabCaptionsAsHtml true if the tab captions are rendered as HTML, false if rendered as plain text
     */
    @StudioProperty(defaultValue = "false")
    void setTabCaptionsAsHtml(boolean tabCaptionsAsHtml);

    /**
     * @return true if the tabs are shown in the UI, false otherwise
     */
    boolean isTabsVisible();

    /**
     * Sets whether the tab selection part should be shown in the UI.
     *
     * @param tabsVisible true if the tabs should be shown in the UI, false otherwise
     */
    @StudioProperty(defaultValue = "true")
    void setTabsVisible(boolean tabsVisible);

    /**
     * Adds a listener that will be notified when a selected tab is changed.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener);

    /**
     * Tab interface.
     */
    @StudioElement(
            caption = "Tab",
            xmlElement = "tab",
            icon = "io/jmix/ui/icon/element/tab.svg",
            defaultProperty = "id"
    )
    @StudioProperties(
            properties = {
                    @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(name = "lazy", type = PropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(name = "spacing", type = PropertyType.BOOLEAN, defaultValue = "false",
                            initialValue = "true"),
                    @StudioProperty(name = "margin", type = PropertyType.MARGIN, defaultValue = "false")
            }
    )
    interface Tab extends Component.HasIcon, Component.HasCaption {
        /**
         * @return tab id.
         */
        String getName();

        /**
         * INTERNAL. Sets tab id.
         */
        @Internal
        void setName(String name);

        /**
         * Returns the availability status for the tab. A disabled tab is shown as such in the tab bar and cannot be
         * selected.
         *
         * @return true if tab is enabled, false otherwise
         */
        boolean isEnabled();

        /**
         * Sets the availability status for the tab. A disabled tab is shown as such in the tab bar and cannot be
         * selected.
         *
         * @param enabled true if tab is enabled, false otherwise
         */
        @StudioProperty(name = "enable", defaultValue = "true")
        void setEnabled(boolean enabled);

        /**
         * Returns the visibility status for the tab. An invisible tab is not shown in the tab bar and cannot be
         * selected.
         *
         * @return true if tab is visible, false otherwise
         */
        boolean isVisible();

        /**
         * Sets the visibility status for the tab. An invisible tab is not shown in the tab bar and cannot be selected,
         * selection is changed automatically when there is an attempt to select an invisible tab.
         *
         * @param visible true if tab is visible, false otherwise
         */
        @StudioProperty(defaultValue = "true")
        void setVisible(boolean visible);

        /**
         * Returns the closability status for the tab.
         *
         * @return true if the tab should be closable from the UI, false otherwise
         */
        boolean isClosable();

        /**
         * Sets the closability status for the tab. It controls if a close button is shown to the user or not.
         * A closable tab can be closed by the user through the user interface.
         *
         * @param closable true if the tab should be closable from the UI, false otherwise
         */
        @StudioProperty(defaultValue = "false")
        void setClosable(boolean closable);

        /**
         * Sets a handler that can override the close behavior if {@link #isClosable()} is true.
         * Default action just removes the tab.
         *
         * @param tabCloseHandler tab close handler
         */
        void setCloseHandler(@Nullable TabCloseHandler tabCloseHandler);

        /**
         * @return a tab close handler
         */
        @Nullable
        TabCloseHandler getCloseHandler();

        /**
         * Sets style for UI element that represents the tab header.
         *
         * @param styleName style name
         */
        @StudioProperty(name = "stylename", type = PropertyType.CSS_CLASSNAME_LIST)
        void setStyleName(@Nullable String styleName);

        /**
         * Returns the style for UI element that represents the tab header.
         *
         * @return the style name or {@code null} if no style name has been set
         */
        @Nullable
        String getStyleName();
    }

    /**
     * Handler that overrides the default behavior if {@link Tab#isClosable()} is true and a user clicks the close
     * button.
     */
    interface TabCloseHandler {
        void onTabClose(Tab tab);
    }

    /**
     * SelectedTabChangeEvents are fired when a selected tab is changed.
     */
    class SelectedTabChangeEvent extends EventObject implements HasUserOriginated {
        private final Tab selectedTab;
        protected final boolean userOriginated;

        public SelectedTabChangeEvent(TabSheet tabSheet, Tab selectedTab) {
            this(tabSheet, selectedTab, false);
        }

        public SelectedTabChangeEvent(Object source, Tab selectedTab, boolean userOriginated) {
            super(source);
            this.selectedTab = selectedTab;
            this.userOriginated = userOriginated;
        }

        @Override
        public TabSheet getSource() {
            return (TabSheet) super.getSource();
        }

        /**
         * @return a selected tab
         */
        public Tab getSelectedTab() {
            return selectedTab;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }
}