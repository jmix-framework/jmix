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

package io.jmix.flowui.component.tabsheet;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.kit.component.HasSubParts;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

// CAUTION: copied from com.vaadin.flow.component.tabs.TabSheet [last update Vaadin 24.3.1]
@Tag("jmix-tabsheet")
@JsModule("./src/tabsheet/jmix-tabsheet.js")
public class JmixTabSheet extends Component
        implements HasStyle, HasSize, HasThemeVariant<TabSheetVariant>, HasPrefix, HasSuffix,
        ComponentContainer, HasSubParts {

    protected static final String GENERATED_TAB_ID_PREFIX = "tabsheet-tab-";

    protected Tabs tabs = new Tabs();
    protected Map<Tab, Component> tabToContent = new HashMap<>();

    public JmixTabSheet() {
        super();

        initComponent();
    }

    protected void initComponent() {
        SlotUtils.addToSlot(this, "tabs", tabs);

        addSelectedChangeListener(e -> {
            getElement().setProperty("selected", tabs.getSelectedIndex());
            updateContent();
        });
    }

    /**
     * Adds a tab created from the given text and content.
     *
     * @param tabText the text of the tab
     * @param content the content related to the tab
     * @return the created tab
     */
    public Tab add(String tabText, Component content) {
        return add(new Tab(tabText), content);
    }

    /**
     * Adds a tab created from the given tab content and content.
     *
     * @param tabContent the content of the tab
     * @param content    the content related to the tab
     * @return the created tab
     */
    public Tab add(Component tabContent, Component content) {
        return add(new Tab(tabContent), content);
    }

    /**
     * Adds a tab with the given content.
     *
     * @param tab     the tab
     * @param content the content related to the tab
     * @return the added tab
     */
    public Tab add(Tab tab, Component content) {
        return add(tab, content, -1);
    }

    /**
     * Adds a tab with the given content to the given position.
     *
     * @param tab      the tab
     * @param content  the content related to the tab
     * @param position the position where the tab should be added. If negative, the
     *                 tab is added at the end.
     * @return the added tab
     */
    public Tab add(Tab tab, Component content, int position) {
        Preconditions.checkNotNullArgument(tab, "The tab to be added cannot be null");
        Preconditions.checkNotNullArgument(content, "The content to be added cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported. Consider wrapping the Text inside a Div.");
        }

        if (position < 0) {
            tabs.add(tab);
        } else {
            tabs.addTabAtIndex(position, tab);
        }

        // Make sure possible old content related to the same tab gets removed
        if (tabToContent.containsKey(tab)) {
            tabToContent.get(tab).getElement().removeFromParent();
        }

        // On the client, content is associated with a tab by id
        String id = tab.getId()
                .orElse(generateTabId());
        tab.setId(id);
        content.getElement().setAttribute("tab", id);

        tabToContent.put(tab, content);

        updateContent();

        return tab;
    }

    /**
     * Removes a tab.
     *
     * @param tab the non-null tab to be removed
     */
    public void remove(Tab tab) {
        Preconditions.checkNotNullArgument(tab, "The tab to be removed cannot be null");

        Element content = tabToContent.remove(tab).getElement();
        content.removeFromParent();

        tabs.remove(tab);
    }

    /**
     * Removes a tab based on the content
     *
     * @param content the non-null content related to the tab to be removed
     */
    public void remove(Component content) {
        Preconditions.checkNotNullArgument(content, "The content of the tab to be removed cannot be null");

        if (content instanceof Text) {
            throw new IllegalArgumentException(
                    "Text as content is not supported.");
        }

        Tab tab = getTab(content);
        if (tab != null) {
            remove(tab);
        }
    }

    /**
     * Removes the tab at the given position.
     *
     * @param position the position of the tab to be removed
     */
    public void remove(int position) {
        remove(getTabAt(position));
    }

    public Component getContentByTab(Tab tab) {
        return tabToContent.get(tab);
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getChildren()
                .sequential()
                .map(component -> getContentByTab((Tab) component))
                .collect(Collectors.toList());
    }

    /**
     * Gets the child components of tab sheet in {@link Element} tree.
     *
     * @return the stream of tab sheet tab
     */
    @Override
    public Stream<Component> getChildren() {
        return super.getChildren()
                .filter(component -> component instanceof Tabs)
                .flatMap(Component::getChildren);
    }

    /**
     * Gets the zero-based index of the currently selected tab.
     *
     * @return the zero-based index of the selected tab, or -1 if none of the
     * tabs is selected
     */
    public int getSelectedIndex() {
        return tabs.getSelectedIndex();
    }

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex the zero-based index of the selected tab, -1 to unselect all
     */
    public void setSelectedIndex(int selectedIndex) {
        tabs.setSelectedIndex(selectedIndex);
    }

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    public Tab getSelectedTab() {
        return tabs.getSelectedTab();
    }

    /**
     * Selects the given tab.
     *
     * @param selectedTab the tab to select, {@code null} to unselect all
     * @throws IllegalArgumentException if {@code selectedTab} is not a child of this component
     */
    public void setSelectedTab(Tab selectedTab) {
        tabs.setSelectedTab(selectedTab);
    }

    /**
     * Returns the tab at the given position.
     *
     * @param position the position of the tab, must be greater than or equals to 0
     *                 and less than the number of tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException if the index is less than 0 or greater than or equals to the
     *                                  number of tabs
     */
    public Tab getTabAt(int position) {
        return tabs.getTabAt(position);
    }

    /**
     * Returns the index of the given tab.
     *
     * @param tab the tab to look up, can not be <code>null</code>
     * @return the index of the tab or -1 if the tab is not added
     */
    public int getIndexOf(Tab tab) {
        return tabs.indexOf(tab);
    }

    /**
     * Returns the {@link Tab} associated with the given component.
     *
     * @param content the component to look up, can not be <code>null</code>
     * @return The tab instance associated with the given component, or
     * <code>null</code> if the {@link TabSheet} does not contain the
     * component.
     */
    @Nullable
    public Tab getTab(Component content) {
        Preconditions.checkNotNullArgument(content,
                "The component to look for the tab cannot be null");

        return tabToContent.entrySet().stream()
                .filter(entry -> entry.getValue().equals(content))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /**
     * Returns the {@link Component} instance associated with the given tab.
     *
     * @param tab the tab to look up, can not be <code>null</code>
     * @return The component instance associated with the given tab, or
     * <code>null</code> if the {@link TabSheet} does not contain the
     * tab.
     */
    @Nullable
    public Component getComponent(Tab tab) {
        Preconditions.checkNotNullArgument(tab,
                "The tab to look for the component cannot be null");

        return tabToContent.get(tab);
    }

    /**
     * Adds a listener for {@link SelectedChangeEvent}.
     *
     * @param listener the listener to add, not <code>null</code>
     * @return a handle that can be used for removing the listener
     */
    public Registration addSelectedChangeListener(
            ComponentEventListener<SelectedChangeEvent> listener) {

        return tabs.addSelectedChangeListener(event -> {
            listener.onComponentEvent(new SelectedChangeEvent(JmixTabSheet.this,
                    event.getPreviousTab(), event.isFromClient(),
                    event.isInitialSelection()) {
                @Override
                public void unregisterListener() {
                    event.unregisterListener();
                }
            });
        });

    }

    /**
     * Marks the content related to the selected tab as enabled and adds it to
     * the component if it is not already added. All the other content panels
     * are disabled so they can't be interacted with.
     */
    protected void updateContent() {
        for (Map.Entry<Tab, Component> entry : tabToContent.entrySet()) {
            Tab tab = entry.getKey();
            Element content = entry.getValue().getElement();

            if (tab.equals(tabs.getSelectedTab())) {
                if (content.getParent() == null) {
                    getElement().appendChild(content);
                }

                content.setEnabled(true);
            } else {
                // Can't use setEnabled(false) because it would also mark the
                // elements as disabled in the DOM. Navigating between tabs
                // would then briefly show the content as disabled.
                content.getNode().setEnabled(false);
            }
        }
    }

    protected String generateTabId() {
        return GENERATED_TAB_ID_PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        return getChildren()
                .filter(tab -> sameId(tab, name))
                .findAny()
                .orElse(null);
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    public static class SelectedChangeEvent extends ComponentEvent<JmixTabSheet> {
        private final Tab selectedTab;
        private final Tab previousTab;
        private final boolean initialSelection;

        /**
         * Creates a new selected change event.
         *
         * @param source      The TabSheet that fired the event.
         * @param previousTab The previous selected tab.
         * @param fromClient  <code>true</code> for client-side events,
         *                    <code>false</code> otherwise.
         */
        public SelectedChangeEvent(JmixTabSheet source, Tab previousTab,
                                   boolean fromClient, boolean initialSelection) {
            super(source, fromClient);
            this.selectedTab = source.getSelectedTab();
            this.initialSelection = initialSelection;
            this.previousTab = previousTab;
        }

        /**
         * Get selected tab for this event. Can be {@code null} when autoselect
         * is set to false.
         *
         * @return the selected tab for this event
         */
        public Tab getSelectedTab() {
            return this.selectedTab;
        }

        /**
         * Get previous selected tab for this event. Can be {@code null} when
         * autoselect is set to false.
         *
         * @return the selected tab for this event
         */
        public Tab getPreviousTab() {
            return this.previousTab;
        }

        /**
         * Checks if this event is initial TabSheet selection.
         *
         * @return <code>true</code> if the event is initial TabSheet selection,
         * <code>false</code> otherwise
         */
        public boolean isInitialSelection() {
            return this.initialSelection;
        }
    }
}
