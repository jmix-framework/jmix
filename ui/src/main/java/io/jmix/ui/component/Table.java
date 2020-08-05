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

import com.google.common.reflect.TypeToken;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.columnmanager.ColumnManager;
import io.jmix.ui.component.columnmanager.GroupColumnManager;
import io.jmix.ui.component.compatibility.TableCellClickListenerWrapper;
import io.jmix.ui.component.compatibility.TableColumnCollapseListenerWrapper;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.model.InstanceContainer;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Table UI component bound to entity type.
 *
 * @param <E> row item type
 */
public interface Table<E extends JmixEntity>
        extends
            ListComponent<E>, Component.Editable, HasButtonsPanel, HasTablePresentations, Component.HasCaption,
            HasContextHelp, Component.HasIcon, LookupComponent<E>, Component.Focusable, HasSubParts, HasHtmlCaption,
            HasHtmlDescription, HasHtmlSanitizer, HasTablePagination {

    enum ColumnAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    String NAME = "table";

    static <T extends JmixEntity> TypeToken<Table<T>> of(@SuppressWarnings("unused") Class<T> itemClass) {
        return new TypeToken<Table<T>>() {};
    }

    List<Column<E>> getColumns();

    @Nullable
    Column<E> getColumn(String id);

    /**
     * Adds the given column to Table.
     * <p>
     * Note that column id should be an instance of {@link MetaPropertyPath}.
     *
     * @param column the column to add
     * @see #addColumn(Column, int)
     */
    void addColumn(Column<E> column);

    /**
     * Adds the given column at the specified index to Table.
     * <p>
     * Note that column id should be an instance of {@link MetaPropertyPath}.
     *
     * @param column the column to add
     * @param index  index of a new column
     * @see #addColumn(Column)
     */
    void addColumn(Column<E> column, int index);

    /**
     * Removes the given column from Table or do nothing if column is {@code null}.
     *
     * @param column the column to remove
     */
    void removeColumn(Column<E> column);

    /**
     * Returns a map with aggregation results, where keys are table column ids and values are aggregation values.
     *
     * @return map with aggregation results
     */
    Map<Object, Object> getAggregationResults();

    void setItems(@Nullable TableItems<E> tableItems);
    @Override
    @Nullable
    TableItems<E> getItems();

    void setRequired(Column<E> column, boolean required, String message);

    /**
     * @deprecated automatic validation of Table is not supported
     */
    @Deprecated
    default void addValidator(Column column, Consumer<?> validator) {
        LoggerFactory.getLogger(Table.class).warn("Field.Validator for Table is not supported");
    }
    /**
     * @deprecated automatic validation of Table is not supported
     */
    @Deprecated
    default void addValidator(Consumer<?> validator) {
        LoggerFactory.getLogger(Table.class).warn("Field.Validator for Table is not supported");
    }

    /**
     * Enable or disable text selection in Table cells.
     * Set true to enable.
     */
    void setTextSelectionEnabled(boolean value);
    /**
     * @return true if text selection is enabled.
     */
    boolean isTextSelectionEnabled();

    /**
     * Assign action to be executed on double click inside a table row.
     * <p>If such action is not set, the table responds to double click by trying to find and execute the following
     * actions:
     * <ul>
     *     <li>action, assigned to Enter key press by setting its {@code shortcut} property</li>
     *     <li>action named "edit"</li>
     *     <li>action named "view"</li>
     * </ul>
     * If one of these actions is found and it is enabled, it is executed.
     */
    void setItemClickAction(Action action);
    @Nullable
    Action getItemClickAction();

    /**
     * Assign action to be executed on Enter key press.
     * <p>If such action is not set, the table responds to pressing Enter by trying to find and execute the following
     * actions:
     * <ul>
     *     <li>action, assigned by {@link #setItemClickAction(Action)}</li>
     *     <li>action, assigned to Enter key press by setting its {@code shortcut} property</li>
     *     <li>action named "edit"</li>
     *     <li>action named "view"</li>
     * </ul>
     * If one of these actions is found and it is enabled, it is executed.
     */
    void setEnterPressAction(Action action);
    @Nullable
    Action getEnterPressAction();

    List<Column> getNotCollapsedColumns();

    void setSortable(boolean sortable);
    boolean isSortable();

    /**
     * Enables or disables automatic scroll to a selected row after table update.
     */
    void setAutoScrolling(boolean autoScroll);

    /**
     * @return whether automatic scroll to a selected row is enabled for this table.
     */
    boolean isAutoScrolling();

    void setAggregatable(boolean aggregatable);
    boolean isAggregatable();

    void setShowTotalAggregation(boolean showAggregation);
    boolean isShowTotalAggregation();

    void setColumnReorderingAllowed(boolean columnReorderingAllowed);
    boolean getColumnReorderingAllowed();

    void setColumnControlVisible(boolean columnCollapsingAllowed);
    boolean getColumnControlVisible();

    /**
     * Set focus on inner field of editable/generated column.
     *
     * @param entity   entity
     * @param columnId column id
     */
    void requestFocus(E entity, String columnId);

    /**
     * Scroll table to specified row.
     *
     * @param entity   entity
     */
    void scrollTo(E entity);

    /**
     * Sort the table by a column.
     * For example:
     * <pre>table.sortBy(table.getDatasource().getMetaClass().getPropertyPath("name"), ascending);</pre>
     *
     * @param propertyId    column indicated by a corresponding {@code MetaPropertyPath} object
     * @param ascending     sort direction
     * @deprecated Use {@link Table#sort(String, SortDirection)} method
     */
    @Deprecated
    void sortBy(Object propertyId, boolean ascending);

    /**
     * Sorts the Table data for passed column id in the chosen sort direction.
     *
     * @param columnId  id of the column to sort
     * @param direction sort direction
     */
    void sort(String columnId, SortDirection direction);

    /**
     * @return current sort information or null if no column is sorted
     */
    @Nullable
    SortInfo getSortInfo();

    void selectAll();

    boolean isMultiLineCells();
    void setMultiLineCells(boolean multiLineCells);

    boolean isContextMenuEnabled();
    void setContextMenuEnabled(boolean contextMenuEnabled);

    /**
     * Set width of row header column. Row header shows icons if Icon Provider is specified.
     *
     * @param width width of row header column in px
     */
    void setRowHeaderWidth(int width);
    int getRowHeaderWidth();

    void setMultiSelect(boolean multiselect);

    /**
     * @deprecated refresh datasource instead
     */
    @Deprecated
    void refresh();

    /**
     * Repaint UI representation of the table (columns, generated columns) without refreshing the table data
     */
    void repaint();

    @Nullable
    @Override
    default Object getSubPart(String name) {
        Column<E> column = getColumn(name);
        if (column != null) {
            return column;
        }

        return getAction(name);
    }

    /**
     * Sets a message to the middle of Table body that should be appeared when Table is empty.
     *
     * @param message message that appears when Table is empty
     */
    void setEmptyStateMessage(@Nullable String message);

    /**
     * @return message that should be appeared when Table is empty
     */
    @Nullable
    String getEmptyStateMessage();

    /**
     * Sets a link message to the middle of Table body that should be appeared when Table is empty.
     *
     * @param linkMessage message that appears when Table is empty
     * @see #setEmptyStateLinkClickHandler(Consumer)
     */
    void setEmptyStateLinkMessage(@Nullable String linkMessage);

    /**
     * @return link message that should be appeared when Table is empty
     */
    @Nullable
    String getEmptyStateLinkMessage();

    /**
     * Sets click handler for link message. Link message can be shown when Table is empty.
     *
     * @param handler handler to set
     * @see #setEmptyStateLinkMessage(String)
     */
    void setEmptyStateLinkClickHandler(@Nullable Consumer<EmptyStateClickEvent<E>> handler);

    /**
     * @return click handler for link message
     */
    @Nullable
    Consumer<EmptyStateClickEvent<E>> getEmptyStateLinkClickHandler();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    interface ColumnCollapseListener {
        void columnCollapsed(Column collapsedColumn, boolean collapsed);
    }

    /**
     * @param columnCollapsedListener a listener to add
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    default void addColumnCollapsedListener(ColumnCollapseListener columnCollapsedListener) {
        addColumnCollapseListener(new TableColumnCollapseListenerWrapper(columnCollapsedListener));
    }

    /**
     * @param columnCollapseListener a listener to remove
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    default void removeColumnCollapseListener(ColumnCollapseListener columnCollapseListener) {
        removeColumnCollapseListener(new TableColumnCollapseListenerWrapper(columnCollapseListener));
    }

    /**
     * Adds a listener for column collapse events.
     *
     * @param listener a listener to add
     * @return a {@link Subscription} object
     */
    Subscription addColumnCollapseListener(Consumer<ColumnCollapseEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeColumnCollapseListener(Consumer<ColumnCollapseEvent> listener);

    /**
     * Event sent every time column collapse state changes.
     *
     * @param <E> type of a table
     */
    class ColumnCollapseEvent<E extends JmixEntity> extends EventObject {
        protected final Column column;
        protected final boolean collapsed;

        public ColumnCollapseEvent(Table<E> source, Column column, boolean collapsed) {
            super(source);
            this.column = column;
            this.collapsed = collapsed;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<E> getSource() {
            return (Table<E>) super.getSource();
        }

        public Column getColumn() {
            return column;
        }

        public boolean isCollapsed() {
            return collapsed;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    enum RowHeaderMode {
        NONE,
        ICON
    }

    void setRowHeaderMode(RowHeaderMode mode);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    enum AggregationStyle {
        TOP,
        BOTTOM
    }

    void setAggregationStyle(AggregationStyle aggregationStyle);
    AggregationStyle getAggregationStyle();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Allows to define different styles for table cells.
     */
    interface StyleProvider<E extends JmixEntity> {
        /**
         * Called by {@link Table} to get a style for row or cell.<br>
         * All unhandled exceptions from StyleProvider in Web components by default are logged with ERROR level
         * and not shown to users.
         *
         * @param entity   an entity instance represented by the current row
         * @param property column identifier if getting a style for a cell, or null if getting the style for a row
         * @return style name or null to apply the default
         */
        String getStyleName(E entity, @Nullable String property);
    }

    /**
     * Set the cell style provider for the table.<br>
     * All style providers added before this call will be removed.
     */
    void setStyleProvider(@Nullable StyleProvider<? super E> styleProvider);

    /**
     * Add style provider for the table.<br>
     * Table can use several providers to obtain many style names for cells and rows.
     */
    void addStyleProvider(StyleProvider<? super E> styleProvider);
    /**
     * Remove style provider for the table.
     */
    void removeStyleProvider(StyleProvider<? super E> styleProvider);

    /**
     * Set the row icon provider for the table.
     *
     * @see #setRowHeaderWidth(int)
     */
    void setIconProvider(@Nullable Function<? super E, String> iconProvider);

    /**
     * Sets the item description provider that is used for generating tooltip descriptions for items.
     * <p>
     * All unhandled exceptions from ItemDescriptionProvider in Web components by default are logged with ERROR level
     * and not shown to users.
     *
     * @param provider the item description provider to use or {@code null} to remove a
     *                 previously set provider if any
     */
    void setItemDescriptionProvider(@Nullable BiFunction<? super E, String, String> provider);

    /**
     * Gets the item description provider.
     *
     * @return the item description provider
     */
    @Nullable
    BiFunction<? super E, String, String> getItemDescriptionProvider();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method returns the datasource which contains the provided item. It can be used in data-aware components,
     * created in generated columns. <br>
     *
     * <b>Do not save to final variables, just get it from table when you need.</b>
     *
     * <pre>{@code
     * modelsTable.addGeneratedColumn(
     *     "numberOfSeats",
     *     new Table.ColumnGenerator<Model>() {
     *         public Component generateCell(Model entity) {
     *             LookupField lookupField = componentsFactory.createComponent(LookupField.NAME);
     *             lookupField.setDatasource(modelsTable.getItemDatasource(entity), "numberOfSeats");
     *             lookupField.setOptionsList(Arrays.asList(2, 4, 5));
     *             lookupField.setWidth("100px");
     *             return lookupField;
     *         }
     *     }
     * );
     * }</pre>
     *
     * @param item entity item
     * @return datasource containing the item
     * @deprecated Use {@link #getInstanceContainer(JmixEntity)} instead.
     */
    /*
    TODO: legacy-ui
    @Deprecated
    Datasource getItemDatasource(Entity item);*/

    /**
     * This method returns the InstanceContainer which contains the provided item. It can be used in data-aware components,
     * created in generated columns. <br>
     *
     * <b>Do not save to final variables, just get it from table when you need.</b>
     *
     * <pre>{@code
     * carsTable.addGeneratedColumn("name", car -> {
     *     TextField<String> textField = uiComponents.create(TextField.NAME);
     *     textField.setValueSource(new ContainerValueSource<>(carsTable.getInstanceContainer(car),"name"));
     *     textField.setValue(car.getName());
     *     return textField;
     * });
     * }</pre>
     *
     * @param item entity item
     * @return InstanceContainer containing the item
     */
    InstanceContainer<E> getInstanceContainer(E item);

    /**
     * Allows rendering of an arbitrary {@link Component} inside a table cell.
     */
    interface ColumnGenerator<E extends JmixEntity> {
        /**
         * Called by {@link Table} when rendering a column for which the generator was created.
         *
         * @param entity an entity instance represented by the current row
         * @return a component to be rendered inside of the cell
         */
        @Nullable
        Component generateCell(E entity);
    }

    /**
     * Allows set Printable representation for column in Excel export. <br>
     * If for column specified Printable then value for Excel cell gets from Printable representation.
     *
     * @param <E> type of item
     * @param <P> type of printable value, e.g. String/Date/Integer/Double/BigDecimal
     */
    interface Printable<E extends JmixEntity, P> {
        P getValue(E item);
    }

    /**
     * Column generator, which supports print to Excel.
     *
     * @param <E> entity type
     * @param <P> printable value type
     */
    interface PrintableColumnGenerator<E extends JmixEntity, P> extends ColumnGenerator<E>, Printable<E, P> {
    }

    /**
     * Add a generated column to the table.
     *
     * @param columnId  column identifier as defined in XML descriptor. May or may not correspond to an entity property.
     * @param generator column generator instance
     */
    void addGeneratedColumn(String columnId, ColumnGenerator<? super E> generator);

    /**
     * Adds a generated column at the specified index to Table.
     *
     * @param columnId  column identifier as defined in XML descriptor. May correspond to an entity property
     * @param index     index of a new column
     * @param generator column generator instance
     */
    void addGeneratedColumn(String columnId, int index, ColumnGenerator<? super E> generator);

    /**
     * Add a generated column to the table.
     * <br> This method useful for desktop UI. Table can make additional look, feel and performance tweaks
     * if it knows the class of components that will be generated.
     *
     * @param columnId       column identifier as defined in XML descriptor. May or may not correspond to an entity property.
     * @param generator      column generator instance
     * @param componentClass class of components that generator will provide
     */
    void addGeneratedColumn(String columnId, ColumnGenerator<? super E> generator, Class<? extends Component> componentClass);

    void removeGeneratedColumn(String columnId);

    /**
     * Adds {@link Printable} representation for column. <br>
     * Explicitly added Printable will be used instead of inherited from generated column.
     *
     * @param columnId  column id
     * @param printable printable representation
     */
    void addPrintable(String columnId, Printable<? super E, ?> printable);

    /**
     * Removes {@link Printable} representation of column. <br>
     * Unable to remove Printable representation inherited from generated column.
     *
     * @param columnId column id
     */
    void removePrintable(String columnId);

    /**
     * Get {@link Printable} representation for column.
     *
     * @param column table column
     * @return printable
     */
    @Nullable
    Printable getPrintable(Table.Column column);

    /**
     * Get {@link Printable} representation for column.
     *
     * @param columnId column id
     * @return printable
     */
    @Nullable
    Printable getPrintable(String columnId);

    /**
     * Add lightweight click handler for column cells.<br>
     * Web specific: cell value will be wrapped in span with cuba-table-clickable-cell style name.<br>
     * You can use .cuba-table-clickable-cell for CSS rules to specify custom representation of cell value.
     *
     * @param columnId      id of column
     * @param clickListener click listener
     * @deprecated Use {@link Column#addClickListener(Consumer)} instead
     */
    @Deprecated
    default void setClickListener(String columnId, CellClickListener<? super E> clickListener) {
        //noinspection unchecked
        setCellClickListener(columnId, new TableCellClickListenerWrapper(clickListener));
    }

    /**
     * Add lightweight click handler for text in column cells.<br>
     * Web specific: cell value will be wrapped in span with cuba-table-clickable-cell style name.<br>
     * You can use .cuba-table-clickable-cell for CSS rules to specify custom representation of cell value.
     * <p>
     * You cannot use cellClickListener for column with maxTextLength attribute, since cellClickListener is
     * already defined to display abbreviated cell text.
     *
     * @param columnId      id of column
     * @param clickListener cell text click listener
     * @deprecated Use {@link Column#addClickListener(Consumer)} instead
     */
    @Deprecated
    void setCellClickListener(String columnId, Consumer<CellClickEvent<E>> clickListener);

    /**
     * Remove click listener.
     *
     * @param columnId id of column
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeClickListener(String columnId);

    /**
     * Lightweight click listener for table cells.
     *
     * @deprecated Use {@link Column#addClickListener(Consumer)}  instead.
     */
    @Deprecated
    interface CellClickListener<T extends JmixEntity> {
        /**
         * @param item     row item
         * @param columnId id of column
         */
        void onClick(T item, String columnId);
    }

    class CellClickEvent<T extends JmixEntity> extends EventObject {
        protected final T item;
        protected final String columnId;

        public CellClickEvent(Table<T> source, @Nullable T item, String columnId) {
            super(source);
            this.item = item;
            this.columnId = columnId;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<T> getSource() {
            return (Table<T>) super.getSource();
        }

        @Nullable
        public T getItem() {
            return item;
        }

        public String getColumnId() {
            return columnId;
        }
    }

    /**
     * Set aggregation distribution provider to handle distribution of data on rows. Supports only TOP
     * aggregation style.
     *
     * @param distributionProvider distribution provider
     */
    void setAggregationDistributionProvider(@Nullable AggregationDistributionProvider<E> distributionProvider);

    /**
     * @return aggregation distribution provider
     */
    @Nullable
    AggregationDistributionProvider<E> getAggregationDistributionProvider();

    /**
     * Show popup inside of Table, relative to last cell click event.<br>
     * Call this method from {@link io.jmix.ui.component.Table.CellClickListener} implementation.
     *
     * @param popupComponent popup content
     */
    void showCustomPopup(Component popupComponent);

    /**
     * Show autocloseable popup view with actions, relative to last cell click event.<br>
     * Call this method from {@link io.jmix.ui.component.Table.CellClickListener} implementation.<br>
     * Autocloseable means that after any click on action popup will be closed.
     *
     * @param actions actions
     */
    void showCustomPopupActions(List<Action> actions);

    /**
     * Set visibility for table header
     */
    void setColumnHeaderVisible(boolean columnHeaderVisible);
    boolean isColumnHeaderVisible();

    /**
     * Hide or show selection
     */
    void setShowSelection(boolean showSelection);

    /**
     * @return true if selection is visible
     */
    boolean isShowSelection();

    class SortInfo {
        protected final Object propertyId;
        protected final boolean ascending;

        /**
         * Constructor for a SortInfo object.
         *
         * @param propertyId column indicated by a corresponding {@code MetaPropertyPath} object
         * @param ascending  sort direction
         */
        public SortInfo(Object propertyId, boolean ascending) {
            this.propertyId = propertyId;
            this.ascending = ascending;
        }

        /**
         * @return the property Id
         */
        public Object getPropertyId() {
            return propertyId;
        }

        /**
         * @return a sort direction value
         */
        public boolean getAscending() {
            return ascending;
        }
    }

    /**
     * Describes sorting direction.
     */
    enum SortDirection {
        /**
         * Ascending (e.g. A-Z, 1..9) sort order
         */
        ASCENDING,

        /**
         * Descending (e.g. Z-A, 9..1) sort order
         */
        DESCENDING
    }

    /**
     * Registers a new selection listener
     *
     * @param listener the listener to register
     */
    Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener);

    /**
     * Event sent when the selection changes. It specifies what in a selection has changed, and where the
     * selection took place.
     */
    class SelectionEvent<E extends JmixEntity> extends EventObject implements HasUserOriginated {
        protected final Set<E> selected;
        protected final boolean userOriginated;

        /**
         * Constructor for a selection event.
         *
         * @param component the Table from which this event originates
         * @param selected  items that are currently selected
         */
        public SelectionEvent(Table<E> component, Set<E> selected, boolean userOriginated) {
            super(component);
            this.selected = selected;
            this.userOriginated = userOriginated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<E> getSource() {
            return (Table<E>) super.getSource();
        }

        /**
         * A {@link Set} of all the items that are currently selected.
         *
         * @return a List of the items that are currently selected
         */
        public Set<E> getSelected() {
            return selected;
        }

        @Override
        public boolean isUserOriginated() {
            return userOriginated;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    class Column<T extends JmixEntity> implements HasXmlDescriptor, HasCaption, HasHtmlCaption, HasFormatter {

        private static final Logger log = LoggerFactory.getLogger(Table.class);

        protected Object id;
        protected String caption;
        protected String description;
        protected String valueDescription;
        protected boolean editable;
        protected Formatter formatter;
        protected Integer width;
        protected boolean collapsed;
        protected boolean groupAllowed = true;
        protected boolean sortable = true;
        protected AggregationInfo aggregation;
        protected Integer maxTextLength;
        protected ColumnAlignment alignment;
        protected boolean captionAsHtml;
        protected Float expandRatio;

        protected Function<T, Object> valueProvider;

        protected Class type;
        protected Element element;

        // vaadin8 add a separate interface for notifying parent
        protected Table<T> owner;

        private EventHub eventHub;

        public Column(Object id) {
            this.id = id;
        }

        public Column(String id) {
            checkNotNullArgument(id);

            this.id = id;
        }

        public Column(MetaPropertyPath propertyPath, String caption) {
            checkNotNullArgument(propertyPath);

            this.id = propertyPath;
            this.caption = caption;
            this.type = propertyPath.getRangeJavaClass();
        }

        public Column(String id, String caption) {
            this.id = id;
            this.caption = caption;
        }

        public Object getId() {
            return id;
        }

        @Nullable
        public MetaPropertyPath getBoundProperty() {
            if (id instanceof MetaPropertyPath) {
                return (MetaPropertyPath) id;
            }
            return null;
        }

        public MetaPropertyPath getBoundPropertyNN() {
            return ((MetaPropertyPath) id);
        }

        public String getStringId() {
            if (id instanceof MetaPropertyPath) {
                return ((MetaPropertyPath) id).toPathString();
            }
            return String.valueOf(id);
        }

        public void setColumnGenerator(Function<T, Component> columnGenerator) {
            if (owner != null) {
                owner.addGeneratedColumn(getStringId(), columnGenerator::apply);
            }
        }

        /**
         * @return value provider function
         */
        @Nullable
        public Function<T, Object> getValueProvider() {
            return valueProvider;
        }

        /**
         * Sets value provider for the column. Value provider can be called 0 or more times depending on visibility
         * of a cell and value type. Return type must be the same as {@link #getType()} of the column.
         *
         * @param valueProvider a callback interface for providing column values from a given source
         */
        public void setValueProvider(@Nullable Function<T, Object> valueProvider) {
            this.valueProvider = valueProvider;
        }

        @Nullable
        public MetaPropertyPath getMetaPropertyPath() {
            if (id instanceof MetaPropertyPath) {
                return (MetaPropertyPath) id;
            }
            return null;
        }

        public MetaPropertyPath getMetaPropertyPathNN() {
            if (id instanceof MetaPropertyPath) {
                return (MetaPropertyPath) id;
            }
            throw new IllegalStateException("Column is not bound to meta property " + id);
        }

        public String getIdString() {
            if (id instanceof MetaPropertyPath) {
                return id.toString();
            }
            return String.valueOf(id);
        }

        @Nullable
        @Override
        public String getCaption() {
            return caption;
        }

        @Override
        public void setCaption(@Nullable String caption) {
            this.caption = caption;
            if (owner != null) {
                ((ColumnManager) owner).setColumnCaption(this, caption);
            }
        }

        @Nullable
        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public void setDescription(@Nullable String description) {
            this.description = description;
            if (owner != null) {
                ((ColumnManager) owner).setColumnDescription(this, description);
            }
        }

        public String getValueDescription() {
            return valueDescription;
        }

        public void setValueDescription(String valueDescription) {
            this.valueDescription = valueDescription;
        }

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
            if (owner != null) {
                log.warn("Changing editable for column in runtime is not supported");
            }
        }

        public Class getType() {
            return type;
        }

        public void setType(Class type) {
            this.type = type;
        }

        @Nullable
        @Override
        public Formatter getFormatter() {
            return formatter;
        }

        @Override
        public void setFormatter(@Nullable Formatter formatter) {
            this.formatter = formatter;
        }

        @Nullable
        public ColumnAlignment getAlignment() {
            return alignment;
        }

        public void setAlignment(@Nullable ColumnAlignment alignment) {
            this.alignment = alignment;
            if (alignment != null && owner != null) {
                ((ColumnManager) owner).setColumnAlignment(this, alignment);
            }
        }

        @Nullable
        public Integer getWidth() {
            return width;
        }

        public void setWidth(@Nullable Integer width) {
            this.width = width;
            if (width != null && owner != null) {
                ((ColumnManager) owner).setColumnWidth(this, width);
            }
        }

        public boolean isCollapsed() {
            return collapsed;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            if (owner != null) {
                ((ColumnManager) owner).setColumnCollapsed(this, collapsed);
            }
        }

        public boolean isGroupAllowed() {
            return groupAllowed;
        }

        public void setGroupAllowed(boolean groupAllowed) {
            this.groupAllowed = groupAllowed;
            if (owner instanceof GroupTable) {
                ((GroupColumnManager) owner).setColumnGroupAllowed(this, groupAllowed);
            }
        }

        public boolean isSortable() {
            return sortable;
        }

        public void setSortable(boolean sortable) {
            this.sortable = sortable;
            if (owner != null) {
                ((ColumnManager) owner).setColumnSortable(this, sortable);
            }
        }

        @Nullable
        public AggregationInfo getAggregation() {
            return aggregation;
        }

        public void setAggregation(AggregationInfo aggregation) {
            this.aggregation = aggregation;
            if (owner != null) {
                ((ColumnManager) owner).addAggregationProperty(this, aggregation.getType());
            }
        }

        @Nullable
        public Integer getMaxTextLength() {
            return maxTextLength;
        }

        public void setMaxTextLength(Integer maxTextLength) {
            this.maxTextLength = maxTextLength;
        }

        @Nullable
        public Table getOwner() {
            return owner;
        }

        public void setOwner(@Nullable Table owner) {
            this.owner = owner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Column column = (Column) o;

            return id.equals(column.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Nullable
        @Override
        public Element getXmlDescriptor() {
            return element;
        }

        @Override
        public void setXmlDescriptor(@Nullable Element element) {
            this.element = element;
        }

        public boolean isAggregationEditable() {
            return aggregation != null && aggregation.isEditable();
        }

        @Override
        public String toString() {
            return id == null ? super.toString() : id.toString();
        }

        /**
         * Sets whether column caption should be interpreted as HTML or not.
         *
         * @param captionAsHtml interpret caption as HTML
         */
        @Override
        public void setCaptionAsHtml(boolean captionAsHtml) {
            this.captionAsHtml = captionAsHtml;
            if (owner != null) {
                ((ColumnManager) owner).setColumnCaptionAsHtml(this, captionAsHtml);
            }
        }

        /**
         * @return whether column caption should be interpreted as HTML or not
         * @deprecated Use {@link #isCaptionAsHtml()} instead
         */
        @Deprecated
        public boolean getCaptionAsHtml() {
            return isCaptionAsHtml();
        }

        @Override
        public boolean isCaptionAsHtml() {
            return captionAsHtml;
        }

        /**
         * Removes the column from aggregation cells list.
         */
        public void removeAggregationProperty() {
            ((ColumnManager) owner).removeAggregationProperty(getStringId());
        }

        /**
         * Sets expand ration for the given column.
         *
         * @param ratio ratio
         */
        public void setExpandRatio(@Nullable Float ratio) {
            this.expandRatio = ratio;
            if (ratio != null && owner != null) {
                ((ColumnManager) owner).setColumnExpandRatio(this, ratio);
            }
        }

        /**
         * @return expand ratio for the column
         */
        @Nullable
        public Float getExpandRatio() {
            return expandRatio;
        }

        /**
         * Adds a click listener for column.
         *
         * @param listener a listener to add
         * @return subscription
         */
        public Subscription addClickListener(Consumer<ClickEvent<T>> listener) {
            getEventHub().subscribe(ClickEvent.class, (Consumer) listener);

            if (owner != null) {
                ((ColumnManager) owner).addCellClickListener(getStringId());
            }

            return () -> removeClickListener(listener);
        }

        /**
         * INTERNAL.
         * <p>
         * Fires a {@link ClickEvent} for all click listeners.
         *
         * @param event event to be fired
         */
        public void fireClickEvent(ClickEvent<T> event) {
            getEventHub().publish(ClickEvent.class, event);
        }

        /**
         * Removes a given click listener.
         *
         * @param listener a listener to remove
         */
        protected void removeClickListener(Consumer<ClickEvent<T>> listener) {
            getEventHub().unsubscribe(ClickEvent.class, (Consumer) listener);

            if (owner != null) {
                ((ColumnManager) owner).removeCellClickListener(getStringId());
            }
        }

        /**
         * @return the EventHub for the column
         */
        protected EventHub getEventHub() {
            if (eventHub == null) {
                eventHub = new EventHub();
            }
            return eventHub;
        }

        /**
         * An event is fired when the user clicks inside the table cell that belongs to the current column.
         *
         * @param <E> an entity class
         */
        public static class ClickEvent<E extends JmixEntity> extends EventObject {

            protected final E item;
            protected final boolean isText;

            /**
             * Constructor for a click event.
             *
             * @param source the Table column from which this event originates
             * @param item   an entity instance represented by the clicked row
             * @param isText {@code true} if the user clicks on text inside the table cell, {@code false} otherwise
             */
            public ClickEvent(Table.Column<E> source, E item, boolean isText) {
                super(source);
                this.item = item;
                this.isText = isText;
            }

            /**
             * @return the Table column from which this event originates
             */
            @SuppressWarnings("unchecked")
            @Override
            public Table.Column<E> getSource() {
                return (Table.Column<E>) super.getSource();
            }

            /**
             * @return an entity instance represented by the clicked row
             */
            public E getItem() {
                return item;
            }

            /**
             * @return {@code true} if the user clicks on text inside the table cell, {@code false} otherwise
             */
            public boolean isText() {
                return isText;
            }
        }
    }

    /**
     * Special component for generated columns which will be rendered as simple text cell.
     * Very useful for heavy tables to decrease rendering time in browser.
     */
    class PlainTextCell implements Component {

        protected Component parent;
        protected String text;

        public PlainTextCell(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Nullable
        @Override
        public String getId() {
            return text;
        }

        @Override
        public void setId(@Nullable String id) {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public Component getParent() {
            return parent;
        }

        @Override
        public void setParent(@Nullable Component parent) {
            this.parent = parent;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isResponsive() {
            return false;
        }

        @Override
        public void setResponsive(boolean responsive) {
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public void setVisible(boolean visible) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isVisibleRecursive() {
            return true;
        }

        @Override
        public boolean isEnabledRecursive() {
            return true;
        }

        @Override
        public float getHeight() {
            return -1;
        }

        @Override
        public SizeUnit getHeightSizeUnit() {
            return SizeUnit.PIXELS;
        }

        @Override
        public void setHeight(@Nullable String height) {
            throw new UnsupportedOperationException();
        }

        @Override
        public float getWidth() {
            return -1;
        }

        @Override
        public SizeUnit getWidthSizeUnit() {
            return SizeUnit.PIXELS;
        }

        @Override
        public void setWidth(@Nullable String width) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Alignment getAlignment() {
            return Alignment.TOP_LEFT;
        }

        @Override
        public void setAlignment(Alignment alignment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getStyleName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addStyleName(String styleName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeStyleName(String styleName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <X> X unwrap(Class<X> internalComponentClass) {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public <X> X unwrapOrNull(Class<X> internalComponentClass) {
            return null;
        }

        @Override
        public <X> void withUnwrapped(Class<X> internalComponentClass, Consumer<X> action) {
        }

        @Override
        public <X> X unwrapComposition(Class<X> internalCompositionClass) {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public <X> X unwrapCompositionOrNull(Class<X> internalCompositionClass) {
            return null;
        }

        @Override
        public <X> void withUnwrappedComposition(Class<X> internalCompositionClass, Consumer<X> action) {
        }
    }

    /**
     * Allows to handle a group or total aggregation value changes.
     */
    interface AggregationDistributionProvider<E> {

        /**
         * Invoked when a group or total aggregation value is changed.
         *
         * @param context context
         */
        void onDistribution(AggregationDistributionContext<E> context);
    }

    /**
     * Object that contains information about aggregation distribution.
     *
     * @param <E> entity type
     */
    class AggregationDistributionContext<E> {
        protected Column column;
        protected Object value;
        protected Collection<E> scope;
        protected boolean isTotalAggregation;

        public AggregationDistributionContext(Column column, Object value, Collection<E> scope,
                                              boolean isTotalAggregation) {
            this.column = column;
            this.value = value;
            this.scope = scope;
            this.isTotalAggregation = isTotalAggregation;
        }

        public Column getColumn() {
            return column;
        }

        public String getColumnId() {
            return column.getIdString();
        }

        public Object getValue() {
            return value;
        }

        public Collection<E> getScope() {
            return scope;
        }

        public boolean isTotalAggregation() {
            return isTotalAggregation;
        }
    }

    /**
     * Describes empty state link click event.
     *
     * @param <E> entity class
     * @see #setEmptyStateLinkMessage(String)
     * @see #setEmptyStateLinkClickHandler(Consumer)
     */
    class EmptyStateClickEvent<E extends JmixEntity> extends EventObject {

        public EmptyStateClickEvent(Table<E> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<E> getSource() {
            return (Table<E>) super.getSource();
        }
    }

}
