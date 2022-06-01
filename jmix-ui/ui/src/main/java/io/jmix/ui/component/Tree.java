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
import io.jmix.ui.action.Action;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.TreeItems;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import java.util.EventObject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A component is intended to display hierarchical structures represented by entities referencing themselves.
 *
 * @param <E> an entity type
 */
@StudioComponent(
        caption = "Tree",
        category = "Components",
        xmlElement = "tree",
        icon = "io/jmix/ui/icon/component/tree.svg",
        canvasBehaviour = CanvasBehaviour.TREE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/tree.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "100px"),
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "200px"),
                @StudioProperty(name = "dataContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                        required = true, typeParameter = "E"),
                @StudioProperty(name = "captionProperty", type = PropertyType.PROPERTY_PATH_REF),
                @StudioProperty(name = "hierarchyProperty", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "E",
                        required = true, options = {"to_one", "to_many"}),
                @StudioProperty(name = "multiselect", type = PropertyType.BOOLEAN, defaultValue = "false"),
                @StudioProperty(name = "showOrphans", type = PropertyType.BOOLEAN, defaultValue = "true")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "captionProperty"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "hierarchyProperty"})
        }
)
public interface Tree<E> extends ListComponent<E>, HasButtonsPanel,
        Component.HasCaption, Component.HasIcon, LookupComponent<E>,
        Component.Focusable, HasContextHelp, HasItemCaptionProvider<E>,
        HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "tree";

    static <T> ParameterizedTypeReference<Tree<T>> of(Class<T> itemClass) {
        return new ParameterizedTypeReference<Tree<T>>() {
        };
    }

    /**
     * Expands all tree nodes.
     */
    void expandTree();

    /**
     * Expands all tree nodes that are higher in level that a given item.
     *
     * @param item an item
     */
    void expand(E item);

    /**
     * Collapses tree nodes.
     */
    void collapseTree();

    /**
     * Collapses all tree nodes that are lower in level than a given item.
     *
     * @param item an item
     */
    void collapse(E item);

    /**
     * Expands tree including specified level
     *
     * @param level level of Tree nodes to expand, if passed level = 1 then root items will be expanded
     * @throws IllegalArgumentException if level &lt; 1
     */
    void expandUpTo(int level);

    /**
     * Returns whether an item with given itemId is expanded or collapsed.
     *
     * @param itemId item id to check
     * @return true if the item with itemId is expanded, false if collapsed
     */
    boolean isExpanded(Object itemId);

    /**
     * @return the name of the property which forms the hierarchy
     */
    String getHierarchyProperty();

    /**
     * @return a {@link DataUnit} supported by the Tree
     */
    @Override
    @Nullable
    TreeItems<E> getItems();

    /**
     * Sets a {@link DataUnit} supported by the Tree.
     *
     * @param treeItems {@link DataUnit} supported by the Tree
     */
    void setItems(@Nullable TreeItems<E> treeItems);

    /**
     * Sets the action to be executed when double-clicking inside a tree node.
     *
     * @param action a new action
     */
    void setItemClickAction(@Nullable Action action);

    /**
     * @return an item double-click action
     */
    @Nullable
    Action getItemClickAction();

    /**
     * Sets a single style provider for tree items.
     *
     * @param styleProvider a style provider to set
     */
    void setStyleProvider(@Nullable Function<? super E, String> styleProvider);

    /**
     * Adds a style provider for tree items.
     *
     * @param styleProvider a style provider to add
     */
    void addStyleProvider(Function<? super E, String> styleProvider);

    /**
     * Removes a previously added style provider.
     *
     * @param styleProvider a style provider to remove
     */
    void removeStyleProvider(Function<? super E, String> styleProvider);

    /**
     * Sets the icon provider for the tree.
     *
     * @param iconProvider an icon provider to set
     */
    void setIconProvider(@Nullable Function<? super E, String> iconProvider);

    /**
     * Repaints UI representation of the tree including style providers and icon providers without refreshing
     * the tree data.
     */
    void repaint();

    /**
     * Sets the action to be executed on Enter key press.
     *
     * @param action a new action
     */
    void setEnterPressAction(@Nullable Action action);

    /**
     * @return an Enter key press action
     */
    @Nullable
    Action getEnterPressAction();

    /**
     * @return the currently used {@link SelectionMode}
     */
    SelectionMode getSelectionMode();

    /**
     * Sets the Tree's selection mode.
     *
     * @param selectionMode the selection mode to use
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "SINGLE", options = {"SINGLE", "MULTI", "NONE"})
    void setSelectionMode(SelectionMode selectionMode);

    /**
     * @return {@code true} if context menu is enabled, {@code false} otherwise
     */
    boolean isContextMenuEnabled();

    /**
     * Sets whether or not context menu is enabled. Default value is {@code true}.
     *
     * @param contextMenuEnabled specifies whether context menu is enabled
     */
    void setContextMenuEnabled(boolean contextMenuEnabled);

    /**
     * Sets the description generator that is used for generating tooltip descriptions for items.
     *
     * @param provider the description generator to use or {@code null} to remove a
     *                 previously set provider if any
     */
    void setDescriptionProvider(@Nullable Function<? super E, String> provider);

    /**
     * Sets the description generator that is used for generating HTML tooltip descriptions for items.
     *
     * @param provider    the description generator to use or {@code null} to remove a
     *                    previously set provider if any
     * @param contentMode the content mode for row tooltips
     */
    void setDescriptionProvider(@Nullable Function<? super E, String> provider, ContentMode contentMode);

    /**
     * Gets the item description generator.
     *
     * @return the item description generator
     */
    @Nullable
    Function<E, String> getDescriptionProvider();

    /**
     * @return the current details generator for item details or {@code null} if not set
     */
    @Nullable
    Function<E, Component> getDetailsGenerator();

    /**
     * Sets a new details generator for item details.
     * <p>
     * The currently opened item details will be re-rendered.
     *
     * @param generator the details generator to set
     */
    void setDetailsGenerator(@Nullable Function<E, Component> generator);

    /**
     * Checks whether details are visible for the given item.
     *
     * @param entity the item for which to check details visibility
     * @return {@code true} if the details are visible
     */
    boolean isDetailsVisible(E entity);

    /**
     * Shows or hides the details for a specific item.
     *
     * @param entity  the item for which to set details visibility
     * @param visible {@code true} to show the details, or {@code false} to hide them
     */
    void setDetailsVisible(E entity, boolean visible);

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
     * Sets the height of a row. If -1 (default), the row height is calculated based on the theme for an empty row
     * before the Tree is displayed.
     *
     * @param rowHeight The height of a row in pixels or -1 for automatic calculation
     */
    @StudioProperty(defaultValue = "-1.0")
    @Min(value = -1)
    void setRowHeight(double rowHeight);

    /**
     * @return the content mode of the item captions
     */
    ContentMode getContentMode();

    /**
     * Sets the content mode of the item captions.
     *
     * @param contentMode the content mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "TEXT", options = {"TEXT", "PREFORMATTED", "HTML"})
    void setContentMode(ContentMode contentMode);

    /**
     * Registers a new selection listener.
     *
     * @param listener the listener to register
     */
    Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener);

    enum SelectionMode {
        /**
         * A SelectionMode that supports for only single rows to be selected at a time.
         */
        SINGLE,

        /**
         * A SelectionMode that supports multiple selections to be made.
         */
        MULTI,

        /**
         * A SelectionMode that does not allow for rows to be selected.
         */
        NONE
    }

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
         * @param source         the Tree from which this event originates
         * @param expandedItem   the expanded item
         * @param userOriginated whether this event was triggered by user interaction or programmatically
         */
        public ExpandEvent(Tree<E> source, E expandedItem, boolean userOriginated) {
            super(source);
            this.expandedItem = expandedItem;
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Tree<E> getSource() {
            return (Tree<E>) super.getSource();
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
         * @param source         the Tree from which this event originates
         * @param collapsedItem  the collapsed item
         * @param userOriginated whether this event was triggered by user interaction or programmatically
         */
        public CollapseEvent(Tree<E> source, E collapsedItem, boolean userOriginated) {
            super(source);
            this.collapsedItem = collapsedItem;
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Tree<E> getSource() {
            return (Tree<E>) super.getSource();
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

    /**
     * Event sent when the selection changes. It specifies what in a selection has changed, and where the
     * selection took place.
     */
    class SelectionEvent<E> extends EventObject implements HasUserOriginated {
        protected final Set<E> selected;
        protected final Set<E> oldSelection;
        protected final boolean userOriginated;

        /**
         * Constructor for a selection event.
         *
         * @param component      the DataGrid from which this event originates
         * @param oldSelection   the old set of selected items
         * @param userOriginated {@code true} if an event is a result of user interaction,
         *                       {@code false} if from the API call
         */
        public SelectionEvent(Tree<E> component, Set<E> oldSelection, boolean userOriginated) {
            super(component);
            this.oldSelection = oldSelection;
            this.selected = component.getSelected();
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Tree<E> getSource() {
            return (Tree<E>) super.getSource();
        }

        /**
         * A {@link Set} of all the items that became selected.
         *
         * <em>Note:</em> this excludes all items that might have been previously selected.
         *
         * @return a set of the items that became selected
         */
        public Set<E> getAdded() {
            LinkedHashSet<E> copy = new LinkedHashSet<>(getSelected());
            copy.removeAll(getOldSelection());
            return copy;
        }

        /**
         * A {@link Set} of all the items that became deselected.
         *
         * <em>Note:</em> this excludes all items that might have been previously deselected.
         *
         * @return a set of the items that became deselected
         */
        public Set<E> getRemoved() {
            LinkedHashSet<E> copy = new LinkedHashSet<>(getOldSelection());
            copy.removeAll(getSelected());
            return copy;
        }

        /**
         * A {@link Set} of all the items that are currently selected.
         *
         * @return a set of the items that are currently selected
         */
        public Set<E> getSelected() {
            return selected;
        }

        /**
         * A {@link Set} of all the items that were selected before the selection was changed.
         *
         * @return a set of items selected before the selection was changed
         */
        public Set<E> getOldSelection() {
            return oldSelection;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }
}
