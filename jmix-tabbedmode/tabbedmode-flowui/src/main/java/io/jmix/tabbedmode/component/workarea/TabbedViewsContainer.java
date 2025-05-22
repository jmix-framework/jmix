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

package io.jmix.tabbedmode.component.workarea;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface TabbedViewsContainer<C extends Component & TabbedViewsContainer<C>> extends HasElement {

    /**
     * Adds a tab created from the given text and content.
     *
     * @param tabText the text of the tab
     * @param content the content related to the tab
     * @return the created tab
     */
    Tab add(String tabText, Component content);

    /**
     * Adds a tab created from the given tab content and content.
     *
     * @param tabContent the content of the tab
     * @param content    the content related to the tab
     * @return the created tab
     */
    Tab add(Component tabContent, Component content);

    /**
     * Adds a tab with the given content.
     *
     * @param tab     the tab
     * @param content the content related to the tab
     * @return the added tab
     */
    Tab add(Tab tab, Component content);

    /**
     * Adds a tab with the given content to the given position.
     *
     * @param tab      the tab
     * @param content  the content related to the tab
     * @param position the position where the tab should be added. If negative, the
     *                 tab is added at the end.
     * @return the added tab
     */
    Tab add(Tab tab, Component content, int position);

    /**
     * Removes a tab.
     *
     * @param tab the non-null tab to be removed
     */
    void remove(Tab tab);

    /**
     * Removes a tab based on the content
     *
     * @param content the non-null content related to the tab to be removed
     */
    void remove(Component content);

    /**
     * Removes the tab at the given position.
     *
     * @param position the position of the tab to be removed
     */
    void remove(int position);

    /**
     * Gets the zero-based index of the currently selected tab.
     *
     * @return the zero-based index of the selected tab, or -1 if none of the
     * tabs is selected
     */
    int getSelectedIndex();

    /**
     * Selects a tab based on its zero-based index.
     *
     * @param selectedIndex the zero-based index of the selected tab, -1 to unselect all
     */
    void setSelectedIndex(int selectedIndex);

    /**
     * Gets the currently selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    @Nullable
    Tab getSelectedTab();

    /**
     * Selects the given tab.
     *
     * @param selectedTab the tab to select, {@code null} to unselect all
     * @throws IllegalArgumentException if {@code selectedTab} is not a child of this component
     */
    void setSelectedTab(@Nullable Tab selectedTab);

    /**
     * Gets the number of tabs.
     *
     * @return the number of tabs
     */
    int getTabCount();

    Stream<Tab> getTabsStream();

    /**
     * Returns the tab at the given position.
     *
     * @param position the position of the tab must be greater than or equals to 0
     *                 and less than the number of tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException if the index is less than 0 or greater than or equals to the
     *                                  number of tabs
     */
    Tab getTabAt(int position);

    /**
     * Returns the index of the given tab.
     *
     * @param tab the tab to look up
     * @return the index of the tab or -1 if the tab is not added
     */
    int getIndexOf(Tab tab);

    /**
     * Returns the {@link Tab} associated with the given component.
     *
     * @param content the component to look up
     * @return The tab instance associated with the given component
     * @throws IllegalArgumentException if tab not found
     */
    default Tab getTab(Component content) {
        Preconditions.checkNotNullArgument(content,
                "The component to look for the tab cannot be null");

        return findTab(content)
                .orElseThrow(() ->
                        new IllegalArgumentException("Not found tab associated with the given component: '%s'"
                                .formatted(content.getId().orElse(""))));
    }

    /**
     * Returns the {@link Tab} associated with the given component.
     *
     * @param content the component to look up
     * @return The tab instance associated with the given component, or an
     * empty {@link  Optional} if the {@link TabSheet} does not contain the
     * {@link Tab} associated with the given component
     */
    Optional<Tab> findTab(Component content);

    /**
     * Returns the {@link Tab} associated with the given ID.
     *
     * @param id the ID of the tab to retrieve
     * @return the {@link Tab} associated with the specified ID
     * @throws IllegalArgumentException if no tab is found with the given ID
     */
    default Tab getTab(String id) {
        return findTab(id).orElseThrow(() ->
                new IllegalArgumentException("Not found tab with id: '%s'".formatted(id)));
    }

    /**
     * Returns the {@link Tab} associated with the given ID.
     *
     * @param id the ID of the tab to retrieve
     * @return the {@link Tab} associated with the specified ID, or an empty
     * {@link Optional} if no tab is found with the given ID
     */
    Optional<Tab> findTab(String id);

    /**
     * Returns the {@link Component} instance associated with the given tab.
     *
     * @param tab the tab to get component
     * @return the component instance associated with the given tab
     * @throws IllegalArgumentException if component not found
     */
    Component getComponent(Tab tab);

    /**
     * Returns the {@link Component} instance associated with the given tab.
     *
     * @param tab the tab to get component
     * @return the component instance associated with the given tab,
     * or an empty optional if not found
     */
    Optional<Component> findComponent(Tab tab);

    /**
     * Returns a stream of {@link Component} instances representing the tab content.
     *
     * @return a stream of {@link Component} instances representing the tab content
     */
    Stream<Component> getTabComponentsStream();

    /**
     * Adds a listener for {@link SelectedChangeEvent}.
     *
     * @param listener the listener to add
     * @return a handle that can be used for removing the listener
     */
    Registration addSelectedChangeListener(ComponentEventListener<SelectedChangeEvent<C>> listener);

    /**
     * Adds a listener for {@link TabsCollectionChangeEvent}.
     *
     * @param listener the listener to add
     * @return a handle that can be used for removing the listener
     */
    Registration addTabsCollectionChangeListener(ComponentEventListener<TabsCollectionChangeEvent<C>> listener);

    /**
     * An event to mark that the selected tab has changed.
     */
    class SelectedChangeEvent<C extends Component & TabbedViewsContainer<C>> extends ComponentEvent<C> {

        protected final Tab selectedTab;
        protected final Tab previousTab;
        protected final boolean initialSelection;

        protected boolean fromClient;

        /**
         * Creates a new selected change event.
         *
         * @param source           the component that fired the event
         * @param previousTab      the previous selected tab
         * @param fromClient       {@code true} for client-side events,
         *                         {@code false} otherwise
         * @param initialSelection {@code true} if the event is initial
         *                         tabs selection, {@code false} otherwise
         */
        public SelectedChangeEvent(C source, @Nullable Tab previousTab,
                                   boolean fromClient, boolean initialSelection) {
            super(source, fromClient);

            this.selectedTab = source.getSelectedTab();
            this.initialSelection = initialSelection;
            this.previousTab = previousTab;
        }

        /**
         * Returns selected tab for this event. Can be {@code null} when autoselect
         * is set to false.
         *
         * @return the selected tab for this event
         */
        @Nullable
        public Tab getSelectedTab() {
            return this.selectedTab;
        }

        /**
         * Returns previous selected tab for this event. Can be {@code null} when
         * autoselect is set to false.
         *
         * @return the selected tab for this event
         */
        @Nullable
        public Tab getPreviousTab() {
            return this.previousTab;
        }

        /**
         * Checks if this event is initial TabSheet selection.
         *
         * @return {@code true} if the event is initial
         * tabs selection, {@code false} otherwise
         */
        public boolean isInitialSelection() {
            return this.initialSelection;
        }
    }

    /**
     * An event to mark that the selected tab has changed.
     */
    class TabsCollectionChangeEvent<C extends Component & TabbedViewsContainer<C>> extends ComponentEvent<C> {

        protected final TabsCollectionChangeType changeType;
        protected final Collection<? extends Tab> changes;

        /**
         * Creates a new tabs collection change event.
         *
         * @param source     the source component
         * @param fromClient {@code true} for client-side events,
         *                   {@code false} otherwise
         * @param changeType the type of change
         * @param changes    changed tabs
         */
        public TabsCollectionChangeEvent(C source, boolean fromClient,
                                         TabsCollectionChangeType changeType,
                                         Collection<? extends Tab> changes) {
            super(source, fromClient);

            this.changeType = changeType;
            this.changes = changes;
        }

        /**
         * @return the type of change
         */
        public TabsCollectionChangeType getChangeType() {
            return changeType;
        }

        /**
         * @return changed tabs
         */
        public Collection<? extends Tab> getChanges() {
            return changes;
        }
    }

    /**
     * Defines the type of tab change.
     */
    enum TabsCollectionChangeType {

        /**
         * Tabs were added to the collection.
         */
        ADD,

        /**
         * Tabs were removed from the collection.
         */
        REMOVE
    }
}
