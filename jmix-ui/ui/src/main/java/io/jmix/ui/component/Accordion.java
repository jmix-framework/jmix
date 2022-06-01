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
 * An accordion is a component similar to a {@link TabSheet}, but with a vertical orientation and the selected component
 * presented between tabs.
 */
@StudioComponent(
        caption = "Accordion",
        category = "Containers",
        xmlElement = "accordion",
        icon = "io/jmix/ui/icon/container/accordion.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.ACCORDION,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/accordion.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px",
                        initialValue = "100px"),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "100%")
        }
)
public interface Accordion extends ComponentContainer, Component.BelongToFrame, Component.HasCaption,
        Component.HasIcon, Component.Focusable, HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {
    String NAME = "accordion";

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
     */
    @Internal
    Tab addLazyTab(String name, Element descriptor, ComponentLoader loader);

    /**
     * Removes tab.
     *
     * @param name id of the tab to remove
     */
    void removeTab(String name);

    /**
     * Removes all tabs.
     */
    void removeAllTabs();

    /**
     * Gets selected tab. May be null if the accordion does not contain tabs at all.
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
     * Adds a listener that will be notified when a selected tab is changed.
     */
    Subscription addSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener);

    /**
     * SelectedTabChangeEvents are fired when a selected tab is changed.
     */
    class SelectedTabChangeEvent extends EventObject {
        private final Accordion.Tab selectedTab;

        public SelectedTabChangeEvent(Accordion accordion, @Nullable Accordion.Tab selectedTab) {
            super(accordion);
            this.selectedTab = selectedTab;
        }

        @Override
        public Accordion getSource() {
            return (Accordion) super.getSource();
        }

        @Nullable
        public Accordion.Tab getSelectedTab() {
            return selectedTab;
        }
    }

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
    interface Tab extends Component.HasIcon {
        /**
         * Gets tab id.
         */
        String getName();

        /**
         * INTERNAL. Sets tab id.
         */
        @Internal
        void setName(String name);

        /**
         * Gets tab caption.
         */
        String getCaption();

        /**
         * Sets tab caption.
         */
        @StudioProperty(name = "caption", type = PropertyType.LOCALIZED_STRING)
        void setCaption(String caption);

        /**
         * Whether the tab is enabled.
         */
        boolean isEnabled();

        @StudioProperty(name = "enable", defaultValue = "true")
        void setEnabled(boolean enabled);

        /**
         * Whether the tab is visible.
         */
        boolean isVisible();

        @StudioProperty(defaultValue = "true")
        void setVisible(boolean visible);

        /**
         * Sets style for UI element that represents the tab header.
         *
         * @param styleName style
         */
        @StudioProperty(name = "stylename", type = PropertyType.CSS_CLASSNAME_LIST)
        void setStyleName(@Nullable String styleName);

        @Nullable
        String getStyleName();
    }
}