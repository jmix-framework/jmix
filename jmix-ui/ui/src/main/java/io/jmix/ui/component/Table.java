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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Table UI component bound to entity type.
 *
 * @param <E> row item type
 */
@StudioComponent(
        caption = "Table",
        category = "Components",
        xmlElement = "table",
        icon = "io/jmix/ui/icon/component/table.svg",
        canvasBehaviour = CanvasBehaviour.TABLE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/table.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                        typeParameter = "E"),
                @StudioProperty(name = "metaClass", type = PropertyType.ENTITY_NAME, typeParameter = "E"),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "200px"),
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "100px"),
                @StudioProperty(name = "editable", type = PropertyType.BOOLEAN, defaultValue = "false")
        }
)
public interface Table<E>
        extends
        ListComponent<E>, Component.Editable, HasButtonsPanel, HasTablePresentations, Component.HasCaption,
        HasContextHelp, Component.HasIcon, LookupComponent<E>, Component.Focusable, HasSubParts, HasHtmlCaption,
        HasHtmlDescription, HasHtmlSanitizer, HasPagination, HasMinSize {

    String NAME = "table";

    static <T> ParameterizedTypeReference<Table<T>> of(Class<T> itemClass) {
        return new ParameterizedTypeReference<Table<T>>() {
        };
    }

    /**
     * Returns a copy of currently configured columns in their current visual
     * order in this Table.
     *
     * @return unmodifiable copy of current columns
     * @see #getNotCollapsedColumns()
     */
    @StudioElementsGroup(
            xmlElement = "columns",
            caption = "Columns",
            icon = "io/jmix/ui/icon/element/columns.svg"
    )
    List<Column<E>> getColumns();

    /**
     * Returns a column by id.
     *
     * @param id the column id
     * @return the column or {@code null} if not found
     */
    @Nullable
    Column<E> getColumn(String id);

    /**
     * Adds the given column to Table.
     * <p>
     * Note that column id should be an instance of {@link MetaPropertyPath}.
     *
     * @param column the column to add
     * @see #addColumn(Column, int)
     * @see #addColumn(Object)
     * @see #addColumn(Object, int)
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
     * @see #addColumn(Object)
     * @see #addColumn(Object, int)
     */
    void addColumn(Column<E> column, int index);

    /**
     * Creates new column with given Id, then adds this column to Table.
     *
     * @param id the column id or the instance of {@link MetaPropertyPath} representing a relative path
     *           to a property from certain {@link MetaClass}
     * @return the newly created column
     * @see #addColumn(Column)
     * @see #addColumn(Column, int)
     * @see #addColumn(Object, int)
     */
    Column<E> addColumn(Object id);

    /**
     * Creates new column with given Id at the specified index, then adds this column to Table.
     *
     * @param id    the column id or the instance of {@link MetaPropertyPath} representing a relative path
     *              to a property from certain {@link MetaClass}
     * @param index index of a new column
     * @return the newly created column
     * @see #addColumn(Column)
     * @see #addColumn(Column, int)
     * @see #addColumn(Object)
     */
    Column<E> addColumn(Object id, int index);

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

    /**
     * Sets an instance of {@link TableItems} as the Table data source.
     *
     * @param tableItems the Table data source
     */
    void setItems(@Nullable TableItems<E> tableItems);

    /**
     * @return the Table data source
     */
    @Override
    @Nullable
    TableItems<E> getItems();

    /**
     * Sets whether the component inside a column must contain a non-null value.
     *
     * @param column   a column
     * @param required required
     * @param message  required message
     */
    void setRequired(Column<E> column, boolean required, String message);

    /**
     * Sets whether text selection in Table cells is enabled.
     *
     * @param textSelectionEnabled whether text selection in Table cells is enabled
     */
    @StudioProperty(defaultValue = "false")
    void setTextSelectionEnabled(boolean textSelectionEnabled);

    /**
     * @return whether text selection in Table cells is enabled
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

    /**
     * @return an action that is performed when the user double-clicks inside a table row
     * @see #setItemClickAction(Action)
     */
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

    /**
     * @return an action to be executed on Enter key press, assigned by {@link #setEnterPressAction(Action)}
     */
    @Nullable
    Action getEnterPressAction();

    /**
     * @return a list of visible columns
     */
    List<Column> getNotCollapsedColumns();

    /**
     * Defines if sortable attribute can be changed for individual column or not. Default value is {@code true}.
     *
     * @param sortable {@code true} if individual column sortable
     *                 attribute can be set to {@code true}, {@code false} otherwise
     */
    @StudioProperty(defaultValue = "true")
    void setSortable(boolean sortable);

    /**
     * @return {@code true} if individual column sortable
     * attribute can be set to {@code true}, {@code false} otherwise
     */
    boolean isSortable();

    /**
     * Sets whether aggregation is enabled. Default value is false.
     *
     * @param aggregatable whether aggregation is enabled.
     */
    @StudioProperty(defaultValue = "false")
    void setAggregatable(boolean aggregatable);

    /**
     * @return true if the Table is aggregatable
     */
    boolean isAggregatable();

    /**
     * Shows in which aggregation the changes occurred: in the total or group.
     *
     * @param showAggregation {@code true} if the aggregation column should show
     *                        changes in total aggregation, {@code false} if in
     *                        the group aggregation
     */
    @StudioProperty(defaultValue = "true")
    void setShowTotalAggregation(boolean showAggregation);

    /**
     * @return {@code true} if the aggregation column should show changes in
     * total aggregation, {@code false} if in the group aggregation
     */
    boolean isShowTotalAggregation();

    /**
     * Sets whether or not column reordering is allowed. Default value is {@code true}.
     *
     * @param columnReorderingAllowed specifies whether column reordering is allowed
     */
    @StudioProperty(name = "reorderingAllowed", defaultValue = "true")
    void setColumnReorderingAllowed(boolean columnReorderingAllowed);

    /**
     * Returns whether column reordering is allowed. Default value is {@code true}.
     *
     * @return {@code true} if reordering is allowed
     */
    boolean getColumnReorderingAllowed();

    /**
     * Registers a new column reorder listener.
     *
     * @param listener the listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addColumnReorderListener(Consumer<ColumnReorderEvent<E>> listener);

    /**
     * Sets whether user can hide columns using the columnControlButton dropdown on
     * the right side of the table header.
     *
     * @param columnControlVisible whether user can hide columns using the
     *                             columnControlButton dropdown on the right side
     *                             of the table header
     */
    @StudioProperty(defaultValue = "true")
    void setColumnControlVisible(boolean columnControlVisible);

    /**
     * @return whether user can hide columns using the columnControlButton dropdown on
     * the right side of the table header
     */
    boolean getColumnControlVisible();

    /**
     * Sets focus on inner field of editable/generated column.
     *
     * @param entity   entity
     * @param columnId column id
     */
    void requestFocus(E entity, String columnId);

    /**
     * Scrolls table to specified row.
     *
     * @param entity entity
     */
    void scrollTo(E entity);

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

    /**
     * Marks all the items in the current data source as selected.
     */
    void selectAll();

    /**
     * @return whether multi-line display is enabled for cells containing several lines of text
     */
    boolean isMultiLineCells();

    /**
     * Sets whether multi-line display is enabled for cells containing several lines of text.
     * The default value is false.
     *
     * @param multiLineCells whether multi-line display is enabled for cells containing several
     *                       lines of text
     */
    @StudioProperty(defaultValue = "false")
    void setMultiLineCells(boolean multiLineCells);

    /**
     * @return whether context menu is enabled
     */
    boolean isContextMenuEnabled();

    /**
     * Sets whether context menu is enabled.
     *
     * @param contextMenuEnabled whether context menu is enabled
     */
    @StudioProperty(defaultValue = "true")
    void setContextMenuEnabled(boolean contextMenuEnabled);

    /**
     * Sets the width of row header column. Row header shows icons if Icon Provider is specified.
     *
     * @param width width of row header column in px
     */
    void setRowHeaderWidth(int width);

    /**
     * @return width of row header column in px
     */
    int getRowHeaderWidth();

    /**
     * Sets whether multiple selection mode is enabled.
     *
     * @param multiselect whether multiple selection mode is enabled
     */
    @StudioProperty(name = "multiselect", defaultValue = "false")
    void setMultiSelect(boolean multiselect);

    /**
     * Repaints UI representation of the table (columns, generated columns) without refreshing the table data.
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
    @StudioProperty(name = "emptyStateMessage", type = PropertyType.LOCALIZED_STRING)
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
    @StudioProperty(name = "emptyStateLinkMessage", type = PropertyType.LOCALIZED_STRING)
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

    /**
     * @return {@code min-height} CSS property value of the Table (not a composition) or {@code null} if not set
     */
    @Nullable
    @Override
    Float getMinHeight();

    /**
     * @return unit size of {@code min-height} CSS property value of the Table (not a composition)
     */
    @Nullable
    @Override
    SizeUnit getMinHeightSizeUnit();

    /**
     * Sets {@code minHeight} CSS property value to the Table (not a composition). To set CSS properties to the
     * composition use {@code css} attribute in the XML descriptor or
     * {@link HtmlAttributes#applyCss(Component, String)}.
     *
     * @param minHeight property value
     */
    @Override
    void setMinHeight(@Nullable String minHeight);

    /**
     * @return {@code min-width} CSS property value of the Table (not a composition) or {@code null} if not set
     */
    @Nullable
    @Override
    Float getMinWidth();

    /**
     * @return unit size of {@code min-width} CSS property value of the Table (not a composition)
     */
    @Nullable
    @Override
    SizeUnit getMinWidthSizeUnit();

    /**
     * Sets {@code minWidth} CSS property value to the Table (not a composition). To set CSS properties to the
     * composition use {@code css} attribute in the XML descriptor or
     * {@link HtmlAttributes#applyCss(Component, String)}.
     *
     * @param minWidth property value
     */
    @Override
    void setMinWidth(@Nullable String minWidth);

    /**
     * Adds a listener for column collapse events.
     *
     * @param listener a listener to add
     * @return a {@link Subscription} object
     */
    Subscription addColumnCollapseListener(Consumer<ColumnCollapseEvent<E>> listener);

    /**
     * Sets the row header mode.
     *
     * @param mode row header mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "NONE", options = {"NONE", "ICON"})
    void setRowHeaderMode(RowHeaderMode mode);

    /**
     * Sets the location of the aggregation row.
     *
     * @param aggregationStyle the location of the aggregation row
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "TOP", options = {"TOP", "BOTTOM"})
    void setAggregationStyle(AggregationStyle aggregationStyle);

    /**
     * @return the location of the aggregation row
     */
    AggregationStyle getAggregationStyle();

    /**
     * Sets the cell style provider for the table.<br>
     * All style providers added before this call will be removed.
     *
     * @param styleProvider a style provider to set
     */
    void setStyleProvider(@Nullable StyleProvider<? super E> styleProvider);

    /**
     * Add style provider for the table.<br>
     * Table can use several providers to obtain many style names for cells and rows.
     *
     * @param styleProvider a style provider to add
     */
    void addStyleProvider(StyleProvider<? super E> styleProvider);

    /**
     * Removes style provider for the table.
     *
     * @param styleProvider a style provider to remove
     */
    void removeStyleProvider(StyleProvider<? super E> styleProvider);

    /**
     * Sets the row icon provider for the Table.
     *
     * @param iconProvider an icon provider to set
     * @see #setRowHeaderMode(RowHeaderMode)
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

    /**
     * This method returns the InstanceContainer which contains the provided item.
     * It can be used in data-aware components, created in generated columns. <br>
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
     * Adds a generated column to the table.
     *
     * @param columnId  column identifier as defined in XML descriptor.
     *                  May or may not correspond to an entity property.
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
     * Adds a generated column to the table.
     * <br> This method useful for desktop UI. Table can make additional look, feel and performance tweaks
     * if it knows the class of components that will be generated.
     *
     * @param columnId       column identifier as defined in XML descriptor.
     *                       May or may not correspond to an entity property.
     * @param generator      column generator instance
     * @param componentClass class of components that generator will provide
     */
    void addGeneratedColumn(String columnId, ColumnGenerator<? super E> generator, Class<? extends Component> componentClass);

    /**
     * Removes generated column from the Table by column id.
     *
     * @param columnId the column id
     */
    void removeGeneratedColumn(String columnId);

    /**
     * Adds {@link Printable} representation for column. <br>
     * Explicitly added Printable will be used instead of inherited from generated column.
     *
     * @param columnId  column id
     * @param printable printable representation
     * @deprecated Use export action APIs instead.
     */
    @Deprecated
    void addPrintable(String columnId, Printable<? super E, ?> printable);

    /**
     * Removes {@link Printable} representation of column. <br>
     * Unable to remove Printable representation inherited from generated column.
     *
     * @param columnId column id
     * @deprecated Use export action APIs instead.
     */
    @Deprecated
    void removePrintable(String columnId);

    /**
     * Gets {@link Printable} representation for column.
     *
     * @param column table column
     * @return printable
     * @deprecated Use export action APIs instead.
     */
    @Nullable
    @Deprecated
    Printable getPrintable(Column column);

    /**
     * Gets {@link Printable} representation for column.
     *
     * @param columnId column id
     * @return printable
     * @deprecated Use export action APIs instead.
     */
    @Nullable
    @Deprecated
    Printable getPrintable(String columnId);

    /**
     * Sets aggregation distribution provider to handle distribution of data on rows. Supports only TOP
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
     * Shows popup inside of Table, relative to last cell click event.<br>
     * Call this method from {@link io.jmix.ui.component.Table.Column.ClickEvent} implementation.
     *
     * @param popupComponent popup content
     */
    void showCustomPopup(Component popupComponent);

    /**
     * Shows autocloseable popup view with actions, relative to last cell click event.<br>
     * Call this method from {@link io.jmix.ui.component.Table.Column.ClickEvent} implementation.<br>
     * Autocloseable means that after any click on action popup will be closed.
     *
     * @param actions actions
     */
    void showCustomPopupActions(List<Action> actions);

    /**
     * Sets whether table header is displayed.
     *
     * @param columnHeaderVisible whether table header is displayed
     */
    @StudioProperty(defaultValue = "true")
    void setColumnHeaderVisible(boolean columnHeaderVisible);

    /**
     * @return whether table header is displayed
     */
    boolean isColumnHeaderVisible();

    /**
     * Sets whether a current row is highlighted.
     *
     * @param showSelection whether a current row is highlighted
     */
    @StudioProperty(defaultValue = "true")
    void setShowSelection(boolean showSelection);

    /**
     * @return whether a current row is highlighted
     */
    boolean isShowSelection();

    /**
     * Registers a new selection listener
     *
     * @param listener the listener to register
     */
    Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Table column.
     * <p>
     * Use {@link Table#addColumn(Object)} and {@link Table#addColumn(Object, int)} methods to create
     * and add column to the Table.
     *
     * @param <E> row item type
     */
    @StudioElement(
            xmlElement = "column",
            caption = "Column",
            icon = "io/jmix/ui/icon/element/column.svg",
            unsupportedTarget = {"io.jmix.ui.component.GroupTable"}
    )
    @StudioProperties(
            properties = {
                    @StudioProperty(name = "id", type = PropertyType.COLUMN_ID, required = true),
                    @StudioProperty(name = "dateFormat", type = PropertyType.DATE_FORMAT),
                    @StudioProperty(name = "link", type = PropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(name = "linkScreenId", type = PropertyType.SCREEN_ID,
                            options = {"io.jmix.ui.screen.EditorScreen"}),
                    @StudioProperty(name = "linkScreenOpenMode", type = PropertyType.SCREEN_OPEN_MODE),
                    @StudioProperty(name = "required", type = PropertyType.BOOLEAN, defaultValue = "false"),
                    @StudioProperty(name = "requiredMessage", type = PropertyType.LOCALIZED_STRING),
                    @StudioProperty(name = "resolution", type = PropertyType.ENUMERATION,
                            options = {"SEC", "MIN", "HOUR", "DAY", "MONTH", "YEAR"}),
                    @StudioProperty(name = "sort", type = PropertyType.ENUMERATION,
                            options = {"ASCENDING", "DESCENDING"}),
                    @StudioProperty(name = "visible", type = PropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(name = "optionsContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                            typeParameter = "E")
            }
    )
    interface Column<E> extends HasXmlDescriptor, HasHtmlCaption, HasFormatter {

        /**
         * Returns a column identifier. It could be a {@link String} or an instance of {@link MetaPropertyPath}.
         *
         * @return id of a column
         */
        Object getId();

        /**
         * Returns a column identifier as a {@link String}. If the id is an instance of {@link MetaPropertyPath},
         * then the {@link MetaPropertyPath#toPathString()} will be returned.
         *
         * @return a column identifier as a string
         */
        String getStringId();

        /**
         * @return the instance of {@link MetaPropertyPath} representing a relative path
         * to a property from certain MetaClass or null
         */
        @Nullable
        MetaPropertyPath getMetaPropertyPath();

        /**
         * @return the instance of {@link MetaPropertyPath} representing a relative path
         * to a property from certain MetaClass
         */
        MetaPropertyPath getMetaPropertyPathNN();

        /**
         * @return the Table this column belongs to
         */
        @Nullable
        Table<E> getOwner();

        /**
         * @param owner the Table this column belongs to
         */
        void setOwner(@Nullable Table<E> owner);

        /**
         * Intended to install instance from screen controller. For instance:
         * <pre>
         *      &#64;Install(to = "filesTable.content", subject = "columnGenerator")
         *      private Object filesTableContentColumnGenerator(DataGrid.ColumnGeneratorEvent&lt;File&gt; event) {
         *          LinkButton linkButton = uiComponents.create(LinkButton.class);
         *          linkButton.setCaption("Open content screen");
         *          linkButton.addClickListener(this::onLinkClickListener);
         *          return linkButton;
         *      }
         * </pre>
         *
         * @param columnGenerator column generator instance
         */
        default void setColumnGenerator(Function<E, Component> columnGenerator) {
            if (getOwner() != null) {
                getOwner().addGeneratedColumn(getStringId(), columnGenerator::apply);
            }
        }

        /**
         * @return a value provider or null
         */
        @Nullable
        Function<E, Object> getValueProvider();

        /**
         * Sets value provider for the column. Value provider can be called 0 or more times depending on visibility
         * of a cell and value type. Return type must be the same as type of the column.
         *
         * @param valueProvider a callback interface for providing column values from a given source
         */
        void setValueProvider(@Nullable Function<E, Object> valueProvider);

        /**
         * @return a hint which is displayed in a popup when a user hovers the mouse cursor on the aggregated value
         */
        @Nullable
        String getValueDescription();

        /**
         * Defines a hint which is displayed in a popup when a user hovers the mouse cursor on the aggregated value.
         * For the operations listed above (SUM, AVG, COUNT, MIN, MAX), popup hints are already available by default.
         *
         * @param valueDescription a hint
         * @see io.jmix.ui.widget.data.AggregationContainer.Type
         */
        void setValueDescription(@Nullable String valueDescription);

        /**
         * Returns whether editing is allowed for the corresponding column in the table.
         *
         * @return whether editing is allowed for the corresponding column in the table
         */
        boolean isEditable();

        /**
         * Sets whether editing is allowed for the corresponding column in the table.
         *
         * <em>NOTE:</em> changing this property at runtime is not supported.
         *
         * @param editable whether editing is allowed for the corresponding column in the table
         */
        @StudioProperty(defaultValue = "false")
        void setEditable(boolean editable);

        /**
         * @return a text alignment of column cells
         */
        @Nullable
        ColumnAlignment getAlignment();

        /**
         * Sets a text alignment of column cells. The default alignment is {@link ColumnAlignment#LEFT}.
         *
         * @param alignment a text alignment of column cells
         */
        @StudioProperty(name = "align", type = PropertyType.ENUMERATION, defaultValue = "LEFT",
                options = {"LEFT", "CENTER", "RIGHT"})
        void setAlignment(@Nullable ColumnAlignment alignment);

        /**
         * Returns default column width. May contain only numeric values in pixels.
         *
         * @return default column width
         */
        @Nullable
        Integer getWidth();

        /**
         * Sets default column width. May contain only numeric values in pixels.
         *
         * @param width default column width
         */
        @StudioProperty(defaultValue = "0")
        @PositiveOrZero
        void setWidth(@Nullable Integer width);

        /**
         * @return {@code true} if the column is currently hidden, {@code false} otherwise
         */
        boolean isCollapsed();

        /**
         * Hides or shows the column. By default columns are visible before
         * explicitly hiding them.
         *
         * @param collapsed {@code true} to hide the column, {@code false} to show
         */
        @StudioProperty(defaultValue = "false")
        void setCollapsed(boolean collapsed);

        /**
         * Returns whether the user can sort the data by this column.
         *
         * @return {@code true} if the column is sortable by the user, {@code false} otherwise
         */
        boolean isSortable();

        /**
         * Sets whether this column is sortable by the user. The Table can be
         * sorted by a sortable column by clicking or tapping the column's
         * default header.
         *
         * @param sortable {@code true} if the user should be able to sort the
         *                 column, {@code false} otherwise
         * @see Table#setSortable(boolean)
         */
        @StudioProperty(defaultValue = "true")
        void setSortable(boolean sortable);

        /**
         * @return the maximum number of characters in a cell
         */
        @Nullable
        Integer getMaxTextLength();

        /**
         * Limits the number of characters in a cell. If the difference between the actual and the maximum
         * allowed number of characters does not exceed the 10 character threshold, the extra characters
         * remain unhidden. To see the entire record, users need to click on its visible part.
         *
         * @param maxTextLength the maximum number of characters in a cell
         * @see io.jmix.ui.component.table.AbbreviatedCellClickListener
         */
        @StudioProperty(defaultValue = "0")
        void setMaxTextLength(@Nullable Integer maxTextLength);

        /**
         * @return an aggregation info
         */
        @Nullable
        AggregationInfo getAggregation();

        /**
         * Sets an aggregation info in order to perform aggregation for this column.
         *
         * @param aggregation aggregation info
         */
        @StudioElement
        void setAggregation(@Nullable AggregationInfo aggregation);

        /**
         * When the aggregation is editable in conjunction with using
         * the {@link #setAggregationDistributionProvider(AggregationDistributionProvider)} method,
         * this allows users to implement algorithms for distributing data between table rows.
         *
         * @return whether aggregation info is editable
         */
        boolean isAggregationEditable();

        /**
         * Sets the ratio with which the column expands. The default value is -1.
         * <p>
         * By default (without expand ratios) the excess space is divided
         * proportionally to columns natural widths.
         *
         * @param ratio the expand ratio of this column. {@code 0} to not have it
         *              expand at all. A negative number to clear the expand
         *              value.
         */
        @StudioProperty(name = "expandRatio", defaultValue = "-1.0")
        void setExpandRatio(float ratio);

        /**
         * @return the ratio with which the column expands
         */
        float getExpandRatio();

        /**
         * Adds a click listener for column.
         *
         * @param listener a listener to add
         * @return a registration object for removing an event listener
         */
        Subscription addClickListener(Consumer<Column.ClickEvent<E>> listener);

        /**
         * An event is fired when the user clicks inside the table cell that belongs to the current column.
         *
         * @param <E> an entity class
         */
        class ClickEvent<E> extends EventObject {

            protected final E item;
            protected final boolean isText;

            /**
             * Constructor for a click event.
             *
             * @param source the Table column from which this event originates
             * @param item   an entity instance represented by the clicked row
             * @param isText {@code true} if the user clicks on text inside the table cell, {@code false} otherwise
             */
            public ClickEvent(Column<E> source, E item, boolean isText) {
                super(source);
                this.item = item;
                this.isText = isText;
            }

            /**
             * @return the Table column from which this event originates
             */
            @SuppressWarnings("unchecked")
            @Override
            public Column<E> getSource() {
                return (Column<E>) super.getSource();
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
     * Column alignment.
     */
    enum ColumnAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Row header mode.
     */
    enum RowHeaderMode {
        NONE,
        ICON
    }

    /**
     * The location of the aggregation row.
     */
    enum AggregationStyle {
        TOP,
        BOTTOM
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
     * Allows to define different styles for table cells.
     */
    interface StyleProvider<E> {
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
     * Allows rendering of an arbitrary {@link Component} inside a table cell.
     */
    interface ColumnGenerator<E> {
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
     * @deprecated Use export action APIs instead.
     */
    @Deprecated
    interface Printable<E, P> {
        P getValue(E item);
    }

    /**
     * Column generator, which supports print to Excel.
     *
     * @param <E> entity type
     * @param <P> printable value type
     * @deprecated Use export action APIs instead.
     */
    @Deprecated
    interface PrintableColumnGenerator<E, P> extends ColumnGenerator<E>, Printable<E, P> {
    }

    /**
     * Object that contains information about column sorting.
     */
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
     * Object that contains information about aggregation distribution.
     *
     * @param <E> entity type
     */
    class AggregationDistributionContext<E> {
        protected Column column;
        protected Object value;
        protected Collection<E> scope;
        protected boolean isTotalAggregation;

        public AggregationDistributionContext(Column column, @Nullable Object value, Collection<E> scope,
                                              boolean isTotalAggregation) {
            this.column = column;
            this.value = value;
            this.scope = scope;
            this.isTotalAggregation = isTotalAggregation;
        }

        /**
         * @return a column
         */
        public Column getColumn() {
            return column;
        }

        /**
         * @return a column id
         */
        public String getColumnId() {
            return column.getStringId();
        }

        /**
         * @return the new aggregation value
         */
        @Nullable
        public Object getValue() {
            return value;
        }

        /**
         * @return a collection of entities that will be affected by changed aggregation
         */
        public Collection<E> getScope() {
            return scope;
        }

        /**
         * @return true if the total aggregation is shown, false if group aggregation is shown
         */
        public boolean isTotalAggregation() {
            return isTotalAggregation;
        }
    }

    /**
     * An event that is fired when a columns are reordered by the user.
     */
    class ColumnReorderEvent<E> extends EventObject {

        public ColumnReorderEvent(Table<E> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<E> getSource() {
            return (Table<E>) super.getSource();
        }
    }

    /**
     * Event sent every time column collapse state changes.
     *
     * @param <E> type of a table
     */
    class ColumnCollapseEvent<E> extends EventObject {
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

        /**
         * @return a column
         */
        public Column getColumn() {
            return column;
        }

        /**
         * @return whether the column is collapsed
         */
        public boolean isCollapsed() {
            return collapsed;
        }
    }

    /**
     * Event sent when the selection changes. It specifies what in a selection has changed, and where the
     * selection took place.
     */
    class SelectionEvent<E> extends EventObject implements HasUserOriginated {
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

    /**
     * Describes empty state link click event.
     *
     * @param <E> entity class
     * @see #setEmptyStateLinkMessage(String)
     * @see #setEmptyStateLinkClickHandler(Consumer)
     */
    class EmptyStateClickEvent<E> extends EventObject {

        public EmptyStateClickEvent(Table<E> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Table<E> getSource() {
            return (Table<E>) super.getSource();
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
}
