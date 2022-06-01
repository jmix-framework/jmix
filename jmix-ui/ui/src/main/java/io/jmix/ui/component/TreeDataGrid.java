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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.data.TreeDataGridItems;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A DataGrid component for displaying hierarchical tabular data.
 *
 * @param <E> row item type
 */
@StudioComponent(
        caption = "TreeDataGrid",
        category = "Components",
        xmlElement = "treeDataGrid",
        icon = "io/jmix/ui/icon/component/treeDataGrid.svg",
        canvasBehaviour = CanvasBehaviour.TABLE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/tree-data-grid.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "hierarchyProperty", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "E",
                        required = true, options = {"to_one", "to_many"}),
                @StudioProperty(name = "showOrphans", type = PropertyType.BOOLEAN, defaultValue = "true")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "hierarchyProperty"})
        }
)
public interface TreeDataGrid<E> extends DataGrid<E> {

    String NAME = "treeDataGrid";

    static <T> ParameterizedTypeReference<TreeDataGrid<T>> of(Class<T> itemClass) {
        return new ParameterizedTypeReference<TreeDataGrid<T>>() {};
    }

    /**
     * Returns the hierarchy level of an item.
     *
     * @param item the item to get level
     * @return the level of the given item
     */
    int getLevel(E item);

    @Nullable
    @Override
    TreeDataGridItems<E> getItems();

    /**
     * Get the currently set hierarchy column. The hierarchy column is a column
     * that displays the hierarchy of this DataGrid's data.
     *
     * @return the currently set hierarchy column, or {@code null} if no column has been explicitly set
     */
    @Nullable
    Column<E> getHierarchyColumn();

    /**
     * Set the column that displays the hierarchy of this DataGrid's data.
     * By default the hierarchy will be displayed in the first column.
     * <p>
     * Setting a hierarchy column by calling this method also sets the column to be visible and not hidable.
     * <p>
     * <strong>Note:</strong> Changing the Renderer of the hierarchy column is not supported.
     *
     * @param id the column id to use for displaying hierarchy
     */
    void setHierarchyColumn(String id);

    /**
     * Set the column that displays the hierarchy of this DataGrid's data.
     * By default the hierarchy will be displayed in the first column.
     * <p>
     * Setting a hierarchy column by calling this method also sets the column to be visible and not hidable.
     * <p>
     * <strong>Note:</strong> Changing the Renderer of the hierarchy column is not supported.
     *
     * @param column the column to use for displaying hierarchy
     */
    void setHierarchyColumn(Column<E> column);

    /**
     * @return the item collapse allowed provider
     */
    Predicate<E> getItemCollapseAllowedProvider();

    /**
     * Sets the item collapse allowed provider for this TreeDataGrid.
     * The provider should return {@code true} for any item that the user can collapse.
     * <p>
     * <strong>Note:</strong> This callback will be accessed often when sending
     * data to the client. The callback should not do any costly operations.
     *
     * @param provider the item collapse allowed provider, not {@code null}
     */
    void setItemCollapseAllowedProvider(Predicate<E> provider);

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have any
     * children, does nothing.
     *
     * @param items the items to expand
     * @see TreeDataGrid#expand(Collection)
     */
    default void expand(E... items) {
        expand(Arrays.asList(items));
    }

    /**
     * Expands the given items.
     * <p>
     * If an item is currently expanded, does nothing. If an item does not have any
     * children, does nothing.
     *
     * @param items the items to expand
     * @see TreeDataGrid#expand(Object[]) 
     */
    void expand(Collection<E> items);

    /**
     * Expands the given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its descendant,
     * meaning that {@code expandRecursively(items, 0)} expands only the given items
     * while {@code expandRecursively(items, 2)} expands the given items as well as their
     * children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items the items to expand recursively
     * @param depth the maximum depth of recursion
     * @see #expandRecursively(Stream, int)
     */
    default void expandRecursively(Collection<E> items, int depth) {
        expandRecursively(items.stream(), depth);
    }

    /**
     * Expands all items.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     */
    void expandAll();

    /**
     * Expands the given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its descendant,
     * meaning that {@code expandRecursively(items, 0)} expands only the given items
     * while {@code expandRecursively(items, 2)} expands the given items as well as their
     * children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     *
     * @param items the items to expand recursively
     * @param depth the maximum depth of recursion
     * @see #expandRecursively(Collection, int)
     */
    void expandRecursively(Stream<E> items, int depth);

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items the items to collapse
     * @see TreeDataGrid#collapse(Collection)
     */
    default void collapse(E... items) {
        collapse(Arrays.asList(items));
    }

    /**
     * Collapse the given items.
     * <p>
     * For items that are already collapsed, does nothing.
     *
     * @param items the items to collapse
     * @see TreeDataGrid#collapse(Object[]) 
     */
    void collapse(Collection<E> items);

    /**
     * Collapse the given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items the items to collapse recursively
     * @param depth the maximum depth of recursion
     * @see #collapseRecursively(Stream, int)
     */
    default void collapseRecursively(Collection<E> items, int depth) {
        collapseRecursively(items.stream(), depth);
    }

    /**
     * Collapse the given items and their children recursively until the given depth.
     * <p>
     * {@code depth} describes the maximum distance between a given item and its
     * descendant, meaning that {@code collapseRecursively(items, 0)} collapses
     * only the given items while {@code collapseRecursively(items, 2)}
     * collapses the given items as well as their children and grandchildren.
     * <p>
     * This method will <i>not</i> fire events for collapsed nodes.
     *
     * @param items the items to collapse recursively
     * @param depth the maximum depth of recursion
     * @see #collapseRecursively(Collection, int)
     */
    void collapseRecursively(Stream<E> items, int depth);

    /**
     * Collapses all items.
     * <p>
     * This method will <i>not</i> fire events for expanded nodes.
     */
    void collapseAll();

    /**
     * Returns whether a given item is expanded or collapsed.
     *
     * @param item the item to check
     * @return {@code true} if the item is expanded, {@code false} otherwise
     */
    boolean isExpanded(E item);

    /**
     * Registers a new expand listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Subscription addExpandListener(Consumer<ExpandEvent<E>> listener);

    /**
     * Registers a new collapse listener.
     *
     * @param listener the listener to be added
     * @return a registration object for removing an event listener added to a source
     */
    Subscription addCollapseListener(Consumer<CollapseEvent<E>> listener);

    /**
     * An event that is fired when an item is expanded.
     *
     * @param <E> item type
     */
    class ExpandEvent<E> extends EventObject implements HasUserOriginated {

        protected final E expandedItem;
        protected final boolean userOriginated;

        /**
         * Constructor for the expand event.
         *
         * @param source         the TreeDataGrid from which this event originates
         * @param expandedItem   the expanded item
         * @param userOriginated whether this event was triggered by user interaction or programmatically
         */
        public ExpandEvent(TreeDataGrid<E> source, E expandedItem, boolean userOriginated) {
            super(source);
            this.expandedItem = expandedItem;
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TreeDataGrid<E> getSource() {
            return (TreeDataGrid<E>) super.getSource();
        }

        /**
         * @return the expanded item
         */
        public E getExpandedItem() {
            return expandedItem;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }

    /**
     * An event that is fired when an item is collapsed.
     *
     * @param <E> item type
     */
    class CollapseEvent<E> extends EventObject implements HasUserOriginated {

        protected final E collapsedItem;
        protected final boolean userOriginated;

        /**
         * Constructor for the collapse event.
         *
         * @param source         the TreeDataGrid from which this event originates
         * @param collapsedItem  the collapsed item
         * @param userOriginated whether this event was triggered by user interaction or programmatically
         */
        public CollapseEvent(TreeDataGrid<E> source, E collapsedItem, boolean userOriginated) {
            super(source);
            this.collapsedItem = collapsedItem;
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TreeDataGrid<E> getSource() {
            return (TreeDataGrid<E>) super.getSource();
        }

        /**
         * @return the collapsed item
         */
        public E getCollapsedItem() {
            return collapsedItem;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }
}
