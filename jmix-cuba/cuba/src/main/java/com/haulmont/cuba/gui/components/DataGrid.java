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
import com.haulmont.cuba.gui.components.data.datagrid.DatasourceDataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.SortableDatasourceDataGridItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasFormatter;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.formatter.Formatter;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <E> entity
 * @deprecated Use {@link io.jmix.ui.component.DataGrid} instead
 */
@SuppressWarnings("rawtypes, unchecked")
@Deprecated
public interface DataGrid<E extends Entity> extends ListComponent<E>, io.jmix.ui.component.DataGrid<E>,
        HasSettings, HasDataLoadingSettings, HasRowsCount, RowsCount.RowsCountTarget, LookupComponent<E> {

    static <T extends Entity> TypeToken<DataGrid<T>> of(Class<T> itemClass) {
        return new TypeToken<DataGrid<T>>() {
        };
    }

    /**
     * @return the DataGrid data source
     * @deprecated use {@link #getItems()} instead
     */
    @Deprecated
    default CollectionDatasource getDatasource() {
        DataGridItems<E> dataGridItems = getItems();
        return dataGridItems instanceof DatasourceDataGridItems
                ? ((DatasourceDataGridItems) dataGridItems).getDatasource()
                : null;
    }

    /**
     * Sets an instance of {@code CollectionDatasource} as the DataGrid data source.
     *
     * @param datasource the DataGrid data source, not null
     * @deprecated use {@link #setItems(DataGridItems)} instead
     */

    @Deprecated
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            DataGridItems<E> dataGridItems;
            if (datasource instanceof CollectionDatasource.Sortable) {
                dataGridItems = new SortableDatasourceDataGridItems<>((CollectionDatasource.Sortable) datasource);
            } else {
                dataGridItems = new DatasourceDataGridItems<>(datasource);
            }
            setItems(dataGridItems);
        }
    }

    /**
     * Gets the id of the item that is currently being edited.
     *
     * @return the id of the item that is currently being edited, or
     * {@code null} if no item is being edited at the moment
     * @deprecated use {@link #getEditedItem()} instead
     */
    @Deprecated
    @Nullable
    Object getEditedItemId();

    /**
     * Opens the editor interface for the provided item Id. Scrolls the Grid to
     * bring the item to view if it is not already visible.
     *
     * @param itemId the id of the item to edit
     * @throws IllegalStateException    if the editor is not enabled or already editing an item in buffered mode
     * @throws IllegalArgumentException if datasource doesn't contain item with given id
     * @see #setEditorEnabled(boolean)
     * @deprecated Use {@link #edit(Object)}
     */
    @Deprecated
    void editItem(Object itemId);

    /**
     * Removes a previously registered DataGrid editor pre commit listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeEditorPreCommitListener(Consumer<EditorPreCommitEvent<E>> listener);

    /**
     * Removes a previously registered DataGrid editor post commit listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeEditorPostCommitListener(Consumer<EditorPostCommitEvent<E>> listener);

    /**
     * Removes a previously registered DataGrid editor close listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeEditorCloseListener(Consumer<EditorCloseEvent<E>> listener);

    /**
     * Removes a previously registered DataGrid editor open listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeEditorOpenListener(Consumer<EditorOpenEvent<E>> listener);

    /**
     * Removes a previously registered column collapsing change listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeColumnCollapsingChangeListener(Consumer<ColumnCollapsingChangeEvent> listener);

    /**
     * Removes a previously registered column reorder listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeColumnReorderListener(Consumer<ColumnReorderEvent> listener);

    /**
     * Removes a previously registered column resize listener.
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeColumnResizeListener(Consumer<ColumnResizeEvent> listener);

    /**
     * Removes a previously registered selection change listener
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeSelectionListener(Consumer<SelectionEvent<E>> listener);

    /**
     * Removes a previously registered sort order change listener
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeSortListener(Consumer<SortEvent> listener);

    /**
     * Removes a previously registered context click listener
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeContextClickListener(Consumer<ContextClickEvent> listener);

    /**
     * Removes a previously registered item click listener
     *
     * @param listener the listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeItemClickListener(Consumer<ItemClickEvent<E>> listener);

    /**
     * Allows to define different styles for DataGrid rows.
     *
     * @deprecated use {@link Function} instead
     */
    @Deprecated
    interface RowStyleProvider<E extends Entity> extends Function<E, String> {
    }

    /**
     * Allows to define different styles for DataGrid cells.
     *
     * @deprecated use {@link DataGrid.Column#setStyleProvider(Function)} instead
     */
    @Deprecated
    interface CellStyleProvider<E extends Entity> {
        /**
         * Called by {@link io.jmix.ui.component.DataGrid} to get a style for cell.
         *
         * @param entity   an entity instance represented by the current row
         * @param columnId id of the DataGrid column
         * @return style name or null to apply the default
         */
        String getStyleName(E entity, String columnId);
    }

    /**
     * Adds style provider for the DataGrid cells.
     * <p>
     * DataGrid can use several providers to obtain many style names for cells.
     *
     * @deprecated use {@link DataGrid.Column#setStyleProvider(Function)} instead
     */
    @Deprecated
    void addCellStyleProvider(CellStyleProvider<? super E> styleProvider);

    /**
     * Removes style provider for the DataGrid cells.
     *
     * @deprecated use {@link DataGrid.Column#setStyleProvider(Function)} instead
     */
    @Deprecated
    void removeCellStyleProvider(CellStyleProvider<? super E> styleProvider);

    /**
     * A callback interface for generating optional descriptions (tooltips) for
     * DataGrid cells. If a cell has both a {@link RowDescriptionProvider row
     * description} and a cell description, the latter has precedence.
     *
     * @deprecated use {@link DataGrid.Column#getDescriptionProvider()} instead
     */
    @Deprecated
    interface CellDescriptionProvider<E extends Entity> {

        /**
         * Called by DataGrid to generate a description (tooltip) for a cell. The
         * description may contain HTML markup.
         *
         * @param entity   an entity instance represented by the current row
         * @param columnId id of the DataGrid column
         * @return the cell description or {@code null} for no description
         */
        String getDescription(E entity, String columnId);
    }

    /**
     * Returns the {@code CellDescriptionProvider} instance used to generate
     * descriptions (tooltips) for DataGrid cells.
     *
     * @return the description provider or {@code null} if no provider is set
     * @deprecated use {@link DataGrid.Column#getDescriptionProvider()} instead
     */
    @Deprecated
    @Nullable
    CellDescriptionProvider<E> getCellDescriptionProvider();

    /**
     * Sets the {@code CellDescriptionProvider} instance for generating
     * optional descriptions (tooltips) for individual DataGrid cells. If a
     * {@link RowDescriptionProvider} is also set, the row description it
     * generates is displayed for cells for which {@code provider} returns null.
     *
     * @param provider the description provider to use or {@code null} to remove a
     *                 previously set provider if any
     * @deprecated use {@link DataGrid.Column#setDescriptionProvider(Function)} instead
     */
    @Deprecated
    void setCellDescriptionProvider(@Nullable CellDescriptionProvider<? super E> provider);

    /**
     * A callback interface for generating optional descriptions (tooltips) for
     * DataGrid rows. If a description is generated for a row, it is used for
     * all the cells in the row for which a {@link CellDescriptionProvider cell
     * description} is not generated.
     *
     * @deprecated use {@link Function} instead
     */
    @Deprecated
    interface RowDescriptionProvider<E extends Entity> extends Function<E, String> {
    }

    /**
     * Interface that implements conversion between a model and a presentation type.
     *
     * @param <P> the presentation type. Must be compatible with what
     *            {@link #getPresentationType()} returns.
     * @param <M> the model type. Must be compatible with what
     *            {@link #getModelType()} returns.
     * @deprecated Use {@link io.jmix.ui.component.DataGrid.Column#setRenderer(Renderer, Function)}
     * and presentation provider represented by a {@link Function} instead
     */
    @Deprecated
    interface Converter<P, M> {

        /**
         * Converts the given value from target type to source type.
         *
         * @param value      the value to convert, compatible with the target type.
         *                   Can be null
         * @param targetType the requested type of the return value
         * @param locale     the locale to use for conversion. Can be null
         * @return the converted value compatible with the source type
         */
        M convertToModel(@Nullable P value, Class<? extends M> targetType, @Nullable Locale locale);

        /**
         * Converts the given value from source type to target type.
         *
         * @param value      the value to convert, compatible with the target type.
         *                   Can be null
         * @param targetType the requested type of the return value
         * @param locale     the locale to use for conversion. Can be null
         * @return the converted value compatible with the source type
         */
        P convertToPresentation(@Nullable M value, Class<? extends P> targetType, @Nullable Locale locale);

        /**
         * The source type of the converter.
         * <p>
         * Values of this type can be passed to
         * {@link #convertToPresentation(Object, Class, Locale)}.
         *
         * @return The source type
         */
        Class<M> getModelType();

        /**
         * The target type of the converter.
         * <p>
         * Values of this type can be passed to
         * {@link #convertToModel(Object, Class, Locale)}.
         *
         * @return The target type
         */
        Class<P> getPresentationType();
    }

    /**
     * Field generator that generates component for column in {@link io.jmix.ui.component.DataGrid} editor.
     */
    @Deprecated
    interface ColumnEditorFieldGenerator {
        /**
         * Generates component for {@link io.jmix.ui.component.DataGrid} editor.
         *
         * @param datasource editing item datasource
         * @param property   editing item property
         * @return generated component
         */

        Field createField(Datasource datasource, String property);
    }

    /**
     * @param <E> DataGrid data type
     * @param <T> column data type
     */
    interface ColumnGenerator<E extends Entity, T> {
        /**
         * Returns value for given event.
         *
         * @param event an event providing more information
         * @return generated value
         */
        T getValue(ColumnGeneratorEvent<E> event);

        /**
         * Return column type for this generator.
         *
         * @return type of generated property
         */
        Class<T> getType();
    }

    /**
     * INTERNAL
     * <p>
     * ColumnGenerator that is used for declaratively installed generators.
     *
     * @param <E> DataGrid data type
     * @param <T> Column data type
     */
    @Internal
    interface GenericColumnGenerator<E extends Entity, T> {

        /**
         * Returns value for given event.
         *
         * @param event an event providing more information
         * @return generated value
         */
        T getValue(ColumnGeneratorEvent<E> event);
    }

    /**
     * Add a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     * @see #addGeneratedColumn(String, ColumnGenerator, int)
     */
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator);

    /**
     * Add a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     * @param index     index of a new generated column
     * @see #addGeneratedColumn(String, ColumnGenerator)
     */
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, ColumnGenerator<E, ?> generator, int index);

    /**
     * INTERNAL
     * <p>
     * Adds a generated column to the DataGrid.
     *
     * @param columnId  column identifier as defined in XML descriptor
     * @param generator column generator instance
     */
    @Internal
    io.jmix.ui.component.DataGrid.Column<E> addGeneratedColumn(String columnId, GenericColumnGenerator<E, ?> generator);

    /**
     * Creates renderer implementation by its type.
     *
     * @param type renderer type
     * @return renderer instance with given type
     */
    <T extends Renderer> T createRenderer(Class<T> type);

    /**
     * A callback interface for generating details for a particular row in Grid.
     *
     * @param <E> DataGrid data type
     * @deprecated Use {@link #setDetailsGenerator(Function)} instead
     */
    @Deprecated
    @FunctionalInterface
    interface DetailsGenerator<E> extends Function<E, Component> {
    }

    /**
     * Sets a new details generator for row details.
     * <p>
     * The currently opened row details will be re-rendered.
     *
     * @param detailsGenerator the details generator to set
     * @deprecated Use {@link #setDetailsGenerator(Function)} instead
     */
    @Deprecated
    void setDetailsGenerator(@Nullable DetailsGenerator<E> detailsGenerator);

    /**
     * A column in the DataGrid.
     */
    interface Column<E extends Entity> extends io.jmix.ui.component.DataGrid.Column<E>, HasFormatter {

        /**
         * @return the type of value represented by this column
         */
        Class getType();

        /**
         * INTERNAL
         * <p>
         * Sets a type of generated column.
         *
         * @param generatedType generated column type
         */
        @Internal
        void setGeneratedType(Class generatedType);

        /**
         * INTERNAL.
         *
         * @return generated column type
         */
        @Internal
        Class getGeneratedType();

        /**
         * @deprecated use {@link #setFormatter(Formatter)} instead
         */
        @Deprecated
        @SuppressWarnings({"rawtypes"})
        default void setFormatter(Function formatter) {
            setFormatter(value -> (String) formatter.apply(value));
        }

        /**
         * Returns the converter instance used by this column.
         *
         * @return the converter
         * @deprecated use {@link #getPresentationProvider()} instead
         */
        @Nullable
        @Deprecated
        Converter<?, ?> getConverter();

        /**
         * Sets the converter used to convert from the property value type to
         * the renderer presentation type. If given converter is null, then the
         * default converter will be used.
         * <p>
         * Takes precedence over {@link Function formatter}, but is inferior to the {@link Function presentation provider}.
         *
         * @param converter the converter to use, or {@code null} to not use any
         *                  converters
         * @deprecated use {@link #setRenderer(Renderer, Function)} instead
         */
        @Deprecated
        void setConverter(@Nullable Converter<?, ?> converter);

        /**
         * @deprecated use {@link #getPresentationProvider()} instead
         */
        @Deprecated
        @Nullable
        @Override
        Formatter getFormatter();

        /**
         * If {@link Function presentation provider} is set it takes precedence over {@link Formatter formatter}.
         *
         * @deprecated use {@link #getPresentationProvider()} instead
         */
        @Deprecated
        @Override
        void setFormatter(@Nullable Formatter formatter);

        /**
         * @return field generator that generates component for
         * this column in {@link io.jmix.ui.component.DataGrid} editor.
         */
        @Nullable
        @Deprecated
        ColumnEditorFieldGenerator getEditorFieldGenerator();

        /**
         * @param fieldFactory field generator that generates a component
         *                     for this column in {@link io.jmix.ui.component.DataGrid} editor.
         * @deprecated Use {{@link #setEditFieldGenerator(Function)}} instead
         */
        @Deprecated
        void setEditorFieldGenerator(@Nullable ColumnEditorFieldGenerator fieldFactory);
    }

    /**
     * @deprecated Use {@link EditorCloseEvent} instead
     */
    @Deprecated
    class CubaEditorCloseEvent<E extends Entity> extends EditorCloseEvent<E> {

        /**
         * Constructor for a DataGrid editor close event.
         *
         * @param component the DataGrid from which this event originates
         * @param item      the edited item
         * @param fields    the fields
         */
        public CubaEditorCloseEvent(io.jmix.ui.component.DataGrid component, E item, Map<String, io.jmix.ui.component.Field> fields) {
            super(component, item, fields);
        }

        /**
         * @return an item Id
         * @deprecated use {@link #getItem()} instead
         */
        @Nullable
        @Deprecated
        public Object getItemId() {
            return EntityValues.getId(item);
        }
    }
}
