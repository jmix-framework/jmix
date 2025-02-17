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
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.EventObject;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface TabbedViewsContainer<C extends TabbedViewsContainer<C>> extends HasElement {

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

    // TODO: gg, JavaDoc
    Set<Tab> getTabs();

    /**
     * Returns the tab at the given position.
     *
     * @param position the position of the tab, must be greater than or equals to 0
     *                 and less than the number of tabs
     * @return The tab at the given index
     * @throws IllegalArgumentException if the index is less than 0 or greater than or equals to the
     *                                  number of tabs
     */
    Tab getTabAt(int position);

    /**
     * Returns the index of the given tab.
     *
     * @param tab the tab to look up, can not be <code>null</code>
     * @return the index of the tab or -1 if the tab is not added
     */
    int getIndexOf(Tab tab);

    /**
     * Returns the {@link Tab} associated with the given component.
     *
     * @param content the component to look up, can not be <code>null</code>
     * @return The tab instance associated with the given component, or
     * <code>null</code> if the {@link TabSheet} does not contain the
     * component.
     */
    Tab getTab(Component content);

    // TODO: gg, JavaDoc
    Optional<Tab> findTab(Component content);

    // TODO: gg, JavaDoc
    Tab getTab(String id);

    // TODO: gg, JavaDoc
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

    // TODO: gg, JavaDoc
    Stream<Component> getTabComponentsStream();

    // TODO: gg, JavaDoc
    Collection<Component> getTabComponents();

    /**
     * Adds a listener for {@link SelectedChangeEvent}.
     *
     * @param listener the listener to add
     * @return a handle that can be used for removing the listener
     */
    Registration addSelectedChangeListener(Consumer<SelectedChangeEvent<C>> listener);

    /**
     * An event to mark that the selected tab has changed.
     */
    class SelectedChangeEvent<C extends TabbedViewsContainer<C>> extends EventObject {

        protected final Tab selectedTab;
        protected final Tab previousTab;
        protected final boolean initialSelection;

        protected boolean fromClient;

        /**
         * Creates a new selected change event.
         *
         * @param source      The TabSheet that fired the event.
         * @param previousTab The previous selected tab.
         * @param fromClient  <code>true</code> for client-side events,
         *                    <code>false</code> otherwise.
         */
        public SelectedChangeEvent(C source, Tab previousTab,
                                   boolean fromClient, boolean initialSelection) {
            super(source);
            this.selectedTab = source.getSelectedTab();
            this.initialSelection = initialSelection;
            this.previousTab = previousTab;
            this.fromClient = fromClient;
        }

        @SuppressWarnings("unchecked")
        @Override
        public C getSource() {
            return (C) super.getSource();
        }

        /**
         * Checks if this event originated from the client side.
         *
         * @return <code>true</code> if the event originated from the client side,
         * <code>false</code> otherwise
         */
        public boolean isFromClient() {
            return fromClient;
        }

        /**
         * Get selected tab for this event. Can be {@code null} when autoselect
         * is set to false.
         *
         * @return the selected tab for this event
         */
        @Nullable
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
