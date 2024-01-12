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

package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.LookupComponent.MultiSelectLookupComponent;
import io.jmix.flowui.component.SupportsEnterPress;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.delegate.AbstractGridDelegate;
import io.jmix.flowui.component.delegate.GridDelegate;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.component.grid.editor.DataGridEditorImpl;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.grid.GridActionsSupport;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixGridContextMenu;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DataGrid<E> extends JmixGrid<E> implements ListDataComponent<E>, MultiSelectLookupComponent<E>,
        EnhancedDataGrid<E>, SupportsEnterPress<DataGrid<E>>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected GridDelegate<E, DataGridItems<E>> gridDelegate;
    protected JmixGridContextMenu<E> contextMenu;

    protected boolean editorCreated = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        gridDelegate = createDelegate();
        gridDelegate.setAfterColumnSecurityApplyHandler(this::onAfterApplyColumnSecurity);
    }

    @SuppressWarnings("unchecked")
    protected GridDelegate<E, DataGridItems<E>> createDelegate() {
        return applicationContext.getBean(GridDelegate.class, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridDataView<E> setItems(DataProvider<E, Void> dataProvider) {
        if (dataProvider instanceof DataGridItems) {
            gridDelegate.setItems((DataGridItems<E>) dataProvider);
        }

        return super.setItems(dataProvider);
    }

    @Nullable
    @Override
    public E getSingleSelectedItem() {
        return gridDelegate.getSingleSelectedItem();
    }

    @Override
    public Set<E> getSelectedItems() {
        return gridDelegate.getSelectedItems();
    }

    @Override
    public void select(E item) {
        gridDelegate.select(item);
    }

    @Override
    public void select(Collection<E> items) {
        gridDelegate.select(items);
    }

    @Override
    public void deselect(E item) {
        gridDelegate.deselect(item);
    }

    @Override
    public void deselectAll() {
        gridDelegate.deselectAll();
    }

    @Nullable
    @Override
    public DataGridItems<E> getItems() {
        return gridDelegate.getItems();
    }

    @Override
    public boolean isMultiSelect() {
        return gridDelegate.isMultiSelect();
    }

    @Override
    public Registration addSelectionListener(SelectionListener<Grid<E>, E> listener) {
        return gridDelegate.addSelectionListener(listener);
    }

    @Override
    public Registration addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<E>> listener) {
        return gridDelegate.addItemDoubleClickListener(listener);
    }

    /**
     * Sets code to execute when Enter key is pressed.
     * <p>
     * If such code is not set, this component responds to Enter press
     * by attempting to find and execute the following actions:
     * <ul>
     *     <li>Action assigned to Enter key by setting its {@link KeyCombination}</li>
     *     <li>{@link io.jmix.flowui.action.list.EditAction}</li>
     *     <li>{@link io.jmix.flowui.action.list.ReadAction}</li>
     * </ul>
     * <p>
     * If one of these actions is found and enabled, it is executed.
     * <p>
     * Note: if no explicit double click listeners are added, then the
     * above rule is used to handle double clicks on this component.
     *
     * @param handler code to execute when Enter key is pressed
     *                or {@code null} to remove previously set.
     * @see com.vaadin.flow.component.grid.Grid#addItemDoubleClickListener(ComponentEventListener)
     */
    @Override
    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent<DataGrid<E>>> handler) {
        gridDelegate.setEnterPressHandler(handler);
    }

    @Override
    public void enableMultiSelect() {
        gridDelegate.enableMultiSelect();
    }

    @Override
    public void setMultiSelect(boolean multiSelect) {
        gridDelegate.setMultiSelect(multiSelect);
    }

    @Override
    public GridSelectionModel<E> setSelectionMode(SelectionMode selectionMode) {
        GridSelectionModel<E> selectionModel = super.setSelectionMode(selectionMode);

        gridDelegate.onSelectionModelChange(selectionModel);

        return selectionModel;
    }

    @Override
    protected BiFunction<Renderer<E>, String, Column<E>> getDefaultColumnFactory() {
        return gridDelegate.getDefaultColumnFactory();
    }

    @Nullable
    @Override
    public MetaPropertyPath getColumnMetaPropertyPath(Column<E> column) {
        return gridDelegate.getColumnMetaPropertyPath(column);
    }

    /**
     * Adds column by the meta property path.
     *
     * @param metaPropertyPath meta property path to add column
     * @return added column
     */
    @Override
    public DataGridColumn<E> addColumn(MetaPropertyPath metaPropertyPath) {
        Preconditions.checkNotNullArgument(metaPropertyPath);

        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
        return addColumn(metaProperty.getName(), metaPropertyPath);
    }

    /**
     * Adds column by the meta property path and specified key. The key is used to identify the column, see
     * {@link #getColumnByKey(String)}.
     *
     * @param key              column key
     * @param metaPropertyPath meta property path to add column
     * @return added column
     */
    @Override
    public DataGridColumn<E> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        Preconditions.checkNotNullArgument(metaPropertyPath);
        Preconditions.checkNotNullArgument(key);

        return gridDelegate.addColumn(key, metaPropertyPath);
    }

    @Override
    public DataGridColumn<E> addColumn(ValueProvider<E, ?> valueProvider) {
        Column<E> column = super.addColumn(valueProvider);
        return gridDelegate.addColumn(column);
    }

    @Override
    public DataGridColumn<E> addColumn(Renderer<E> renderer) {
        Column<E> column = super.addColumn(renderer);
        return gridDelegate.addColumn(column);
    }

    @Override
    public <V extends Component> DataGridColumn<E> addComponentColumn(ValueProvider<E, V> componentProvider) {
        return (DataGridColumn<E>) super.addComponentColumn(componentProvider);
    }

    @Override
    public <V extends Comparable<? super V>> DataGridColumn<E> addColumn(ValueProvider<E, V> valueProvider,
                                                                         String... sortingProperties) {
        return (DataGridColumn<E>) super.addColumn(valueProvider, sortingProperties);
    }

    @Override
    public DataGridColumn<E> addColumn(String propertyName) {
        return (DataGridColumn<E>) super.addColumn(propertyName);
    }

    @Override
    public boolean isAggregatable() {
        return gridDelegate.isAggregatable();
    }

    @Override
    public void setAggregatable(boolean aggregatable) {
        gridDelegate.setAggregatable(aggregatable);
    }

    @Override
    public AggregationPosition getAggregationPosition() {
        return gridDelegate.getAggregationPosition();
    }

    @Override
    public void setAggregationPosition(AggregationPosition aggregationPosition) {
        gridDelegate.setAggregationPosition(aggregationPosition);
    }

    @Override
    public void addAggregation(Column<E> column, AggregationInfo info) {
        gridDelegate.addAggregationInfo(column, info);
    }

    @Override
    public Map<Grid.Column<E>, Object> getAggregationResults() {
        return gridDelegate.getAggregationResults();
    }

    /**
     * <strong>Note:</strong> If column reordering is enabled with
     * {@link #setColumnReorderingAllowed(boolean)} and the user has reordered
     * the columns, the order of the list returned by this method might be
     * incorrect.
     *
     * @return an unmodifiable list of {@link Column}s that are not hidden by security
     */
    @Override
    public List<Column<E>> getColumns() {
        return super.getColumns();
    }

    /**
     * Gets an unmodifiable list of all currently added {@link Column}s.
     * <p>
     * If column reordering is enabled with {@link #setColumnReorderingAllowed(boolean)}
     * and the user has reordered the columns, the order of the returned list will be correct.
     *
     * @return a copy of all currently added {@link Column}s including hidden by security
     */
    public List<Column<E>> getAllColumns() {
        return gridDelegate.getColumns();
    }

    @Nullable
    @Override
    public DataGridColumn<E> getColumnByKey(String columnKey) {
        return gridDelegate.getColumnByKey(columnKey);
    }

    @Override
    public void removeColumn(Column<E> column) {
        gridDelegate.removeColumn(column);

        // Due to columns hidden by security are not added to Grid, removing
        // them can lead to an exception. So we check it silently before.
        if (gridDelegate.isDataGridOwner(column)) {
            super.removeColumn(column);
        }
    }

    /**
     * Moves column to the provided position in the {@link DataGrid}.
     *
     * @param column column to move
     * @param index  new index of column in {@link #getAllColumns()} list
     */
    public void setColumnPosition(Column<E> column, int index) {
        gridDelegate.setColumnPosition(column, index);
    }

    @Override
    public boolean isEditorCreated() {
        return editorCreated;
    }

    @Override
    protected void onDataProviderChange() {
        super.onDataProviderChange();

        if (isEditorCreated()) {
            DataGridEditor<E> editor = getEditor();
            if (editor instanceof DataGridDataProviderChangeObserver) {
                ((DataGridDataProviderChangeObserver) editor).dataProviderChanged();
            }
        }
    }

    @Override
    public DataGridEditor<E> getEditor() {
        return ((DataGridEditor<E>) super.getEditor());
    }

    @Override
    protected DataGridEditor<E> createEditor() {
        editorCreated = true;
        return new DataGridEditorImpl<>(this, applicationContext);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected GridActionsSupport<JmixGrid<E>, E> createActionsSupport() {
        return new DataGridActionsSupport(this);
    }

    protected void onAfterApplyColumnSecurity(AbstractGridDelegate.ColumnSecurityContext<E> context) {
        if (!context.isPropertyEnabled()) {
            // Remove column from component while GridDelegate stores this column
            super.removeColumn(context.getColumn());
        }
    }

    @Override
    public JmixGridContextMenu<E> getContextMenu() {
        if (contextMenu == null) {
            contextMenu = new JmixGridContextMenu<>(this);
        }
        return contextMenu;
    }

    @Override
    public GridContextMenu<E> addContextMenu() {
        throw new UnsupportedOperationException("DataGrid can have only one context menu attached, " +
                "use getContextMenu() to retrieve it");
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        Object column = super.getSubPart(name);
        if (column != null) {
            return column;
        }
        if (contextMenu != null) {
            if (UiComponentUtils.sameId(contextMenu, name)) {
                return contextMenu;
            } else {
                return contextMenu.getSubPart(name);
            }
        }
        return null;
    }
}
