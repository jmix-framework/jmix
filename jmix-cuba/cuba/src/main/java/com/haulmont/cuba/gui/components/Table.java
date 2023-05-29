/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.columnmanager.ColumnManager;
import com.haulmont.cuba.gui.components.columnmanager.GroupColumnManager;
import com.haulmont.cuba.gui.components.compatibility.TableCellClickListenerWrapper;
import com.haulmont.cuba.gui.components.compatibility.TableColumnCollapseListenerWrapper;
import com.haulmont.cuba.gui.components.data.table.DatasourceTableItems;
import com.haulmont.cuba.gui.components.data.table.SortableDatasourceTableItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.impl.AbstractTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.Table} instead
 */
@Deprecated
@SuppressWarnings("rawtypes")
public interface Table<E extends Entity> extends ListComponent<E>, io.jmix.ui.component.Table<E>, HasSettings,
        HasDataLoadingSettings, HasPresentations, HasRowsCount, RowsCount.RowsCountTarget, LookupComponent<E> {

    static <T extends Entity> TypeToken<Table<T>> of(@SuppressWarnings("unused") Class<T> itemClass) {
        return new TypeToken<Table<T>>() {
        };
    }

    /**
     * Sets {@code CollectionDatasource} as Table data source.
     *
     * @param datasource collection datasource
     * @deprecated Use {@link #setItems(TableItems)} instead
     */
    @Deprecated
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            TableItems<E> tableItems;
            if (datasource instanceof CollectionDatasource.Sortable) {
                tableItems = new SortableDatasourceTableItems((CollectionDatasource.Sortable) datasource);
            } else {
                tableItems = new DatasourceTableItems(datasource);
            }
            setItems(tableItems);
        }
    }

    /**
     * @return collection datasource
     * @deprecated Use {@link #getItems()} instead
     */
    @Deprecated
    default CollectionDatasource getDatasource() {
        TableItems<E> tableItems = getItems();
        return tableItems instanceof DatasourceTableItems
                ? ((DatasourceTableItems) tableItems).getDatasource()
                : null;
    }

    /**
     * @deprecated refresh datasource instead
     */
    @Deprecated
    void refresh();

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
     * @deprecated Use {@link #getInstanceContainer(Object)} instead.
     */
    @Deprecated
    Datasource getItemDatasource(Entity item);

    /**
     * Sorts the table by a column.
     * For example:
     * <pre>table.sortBy(table.getDatasource().getMetaClass().getPropertyPath("name"), ascending);</pre>
     *
     * @param propertyId column indicated by a corresponding {@code MetaPropertyPath} object
     * @param ascending  sort direction
     * @deprecated Use {@link #sort(String, SortDirection)} method
     */
    @Deprecated
    void sortBy(Object propertyId, boolean ascending);

    /**
     * @param columnCollapsedListener a listener to add
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    default void addColumnCollapsedListener(ColumnCollapseListener columnCollapsedListener) {
        addColumnCollapseListener(new TableColumnCollapseListenerWrapper<>(columnCollapsedListener));
    }

    /**
     * @param columnCollapseListener a listener to remove
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    default void removeColumnCollapseListener(ColumnCollapseListener columnCollapseListener) {
        removeColumnCollapseListener(new TableColumnCollapseListenerWrapper<>(columnCollapseListener));
    }

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeColumnCollapseListener(Consumer<ColumnCollapseEvent<E>> listener);

    /**
     * Adds lightweight click handler for column cells.<br>
     * Web specific: cell value will be wrapped in span with cuba-table-clickable-cell style name.<br>
     * You can use .cuba-table-clickable-cell for CSS rules to specify custom representation of cell value.
     *
     * @param columnId      id of column
     * @param clickListener click listener
     * @deprecated Use {@link io.jmix.ui.component.Table.Column#addClickListener(Consumer)} instead
     */
    @Deprecated
    default void setClickListener(String columnId, CellClickListener<? super E> clickListener) {
        //noinspection unchecked
        setCellClickListener(columnId, new TableCellClickListenerWrapper(clickListener));
    }

    /**
     * Adds lightweight click handler for text in column cells.<br>
     * Web specific: cell value will be wrapped in span with cuba-table-clickable-cell style name.<br>
     * You can use .cuba-table-clickable-cell for CSS rules to specify custom representation of cell value.
     * <p>
     * You cannot use cellClickListener for column with maxTextLength attribute, since cellClickListener is
     * already defined to display abbreviated cell text.
     *
     * @param columnId      id of column
     * @param clickListener cell text click listener
     * @deprecated Use {@link io.jmix.ui.component.Table.Column#addClickListener(Consumer)} instead
     */
    @Deprecated
    void setCellClickListener(String columnId, Consumer<CellClickEvent<E>> clickListener);

    /**
     * Removes click listener.
     *
     * @param columnId id of column
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeClickListener(String columnId);

    /**
     * @deprecated Use {@link #addColumnCollapseListener(Consumer)} instead
     */
    @Deprecated
    interface ColumnCollapseListener {
        void columnCollapsed(io.jmix.ui.component.Table.Column collapsedColumn, boolean collapsed);
    }

    /**
     * Lightweight click listener for table cells.
     *
     * @deprecated Use {@link io.jmix.ui.component.Table.Column#addClickListener(Consumer)}  instead.
     */
    @Deprecated
    interface CellClickListener<T> {
        /**
         * @param item     row item
         * @param columnId id of column
         */
        void onClick(T item, String columnId);
    }

    /**
     * Event sent every time the user clicks inside a cell.
     *
     * @param <T> type of a table
     * @deprecated Use {@link io.jmix.ui.component.Table.Column.ClickEvent} instead
     */
    @Deprecated
    class CellClickEvent<T> extends EventObject {
        protected final T item;
        protected final String columnId;

        public CellClickEvent(io.jmix.ui.component.Table<T> source, @Nullable T item, String columnId) {
            super(source);
            this.item = item;
            this.columnId = columnId;
        }

        @SuppressWarnings("unchecked")
        @Override
        public io.jmix.ui.component.Table<T> getSource() {
            return (io.jmix.ui.component.Table<T>) super.getSource();
        }

        /**
         * @return a selected item
         */
        @Nullable
        public T getItem() {
            return item;
        }

        /**
         * @return a column id
         */
        public String getColumnId() {
            return columnId;
        }
    }

    /**
     * @deprecated Use {@link io.jmix.ui.component.Table.Column} instead
     */
    @Deprecated
    class Column<E extends Entity> extends AbstractTable.ColumnImpl<E> {

        protected Class type;
        protected boolean groupAllowed = true;

        @Deprecated
        public Column(Object id) {
            super(id, null);
        }

        @Deprecated
        public Column(String id) {
            super(id, null);
        }

        @Deprecated
        public Column(MetaPropertyPath propertyPath, String caption) {
            super(propertyPath, null);

            this.caption = caption;
        }

        @Deprecated
        public Column(String id, String caption) {
            this((Object) id, caption);
        }

        @Deprecated
        public Column(Object id, String caption) {
            super(id, null);

            this.caption = caption;
        }

        // todo provide Table instance method as replacement
        @Deprecated
        public Column(Class entityClass, String propertyPath) {
            this(
                    AppBeans.get(Metadata.class).getClass(entityClass).getPropertyPath(propertyPath),
                    AppBeans.get(MessageTools.class).getPropertyCaption(
                            AppBeans.get(Metadata.class).getClass(entityClass),
                            propertyPath)
            );
        }

        /**
         * @deprecated Use {@link #setFormatter(Formatter)} instead
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public void setFormatter(Function formatter) {
            super.setFormatter(value -> (String) formatter.apply(value));
        }

        /**
         * @return whether column caption should be interpreted as HTML or not
         * @deprecated Use {@link #isCaptionAsHtml()} instead
         */
        @Deprecated
        public boolean getCaptionAsHtml() {
            return isCaptionAsHtml();
        }

        @Internal
        @Deprecated
        public void fireClickEvent(io.jmix.ui.component.Table.Column.ClickEvent<E> clickEvent) {
            getEventHub().publish(io.jmix.ui.component.Table.Column.ClickEvent.class, clickEvent);
        }

        /**
         * @deprecated Use {@link #getMetaPropertyPath()} instead
         */
        @Deprecated
        @Nullable
        public MetaPropertyPath getBoundProperty() {
            if (id instanceof MetaPropertyPath) {
                return (MetaPropertyPath) id;
            }
            return null;
        }

        /**
         * @deprecated Use {@link #getMetaPropertyPathNN()} instead
         */
        @Deprecated
        @Nonnull
        public MetaPropertyPath getBoundPropertyNN() {
            return ((MetaPropertyPath) id);
        }

        /**
         * @deprecated Use {@link #getMetaPropertyPath()} instead
         */
        @Deprecated
        public Class getType() {
            return type;
        }

        /**
         * @deprecated Use {@link #getMetaPropertyPath()} instead
         */
        @Deprecated
        public void setType(Class type) {
            this.type = type;
        }

        /**
         * @deprecated Use {@link #getStringId()} instead
         */
        @Deprecated
        public String getIdString() {
            if (id instanceof MetaPropertyPath) {
                return id.toString();
            }
            return String.valueOf(id);
        }

        /**
         * @deprecated Use {@link io.jmix.ui.component.GroupTable.GroupColumn#isGroupAllowed()} instead
         */
        @Deprecated
        public boolean isGroupAllowed() {
            return groupAllowed;
        }

        /**
         * @deprecated Use {@link io.jmix.ui.component.GroupTable.GroupColumn#setGroupAllowed(boolean)} instead
         */
        @Deprecated
        public void setGroupAllowed(boolean groupAllowed) {
            this.groupAllowed = groupAllowed;
            if (owner instanceof GroupTable) {
                ((GroupColumnManager) owner).setColumnGroupAllowed(this, groupAllowed);
            }
        }

        /**
         * @deprecated Use {@link #setAggregation(AggregationInfo)} instead
         */
        @Deprecated
        public void removeAggregationProperty() {
            ((ColumnManager) owner).removeAggregationProperty(getStringId());
        }
    }
}
