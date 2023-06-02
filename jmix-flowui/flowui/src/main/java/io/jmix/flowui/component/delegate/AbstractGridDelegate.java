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

package io.jmix.flowui.component.delegate;

import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGridDataProviderChangeObserver;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.data.provider.StringPresentationValueProvider;
import io.jmix.flowui.kit.component.HasActions;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractGridDelegate<C extends Grid<E> & ListDataComponent<E> & EnhancedDataGrid<E> & HasActions,
        E, ITEMS extends DataGridItems<E>>
        extends AbstractComponentDelegate<C>
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected MetadataTools metadataTools;
    protected MessageTools messageTools;
    protected UiComponents uiComponents;
    protected AccessManager accessManager;

    protected ITEMS dataGridItems;

    protected Registration selectionListenerRegistration;
    protected Registration itemSetChangeRegistration;
    protected Registration valueChangeRegistration;

    protected Set<SelectionListener<Grid<E>, E>> selectionListeners = new HashSet<>();
    protected Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler;

    /**
     * Columns that are bounded with data container (loaded from descriptor or
     * added using {@link #addColumn(String, MetaPropertyPath)}).
     */
    protected Map<Grid.Column<E>, MetaPropertyPath> propertyColumns = new HashMap<>();

    /**
     * Contains all columns like a Grid and additionally hidden columns by security.
     * The order of columns corresponds to the client side column order.
     */
    protected List<Grid.Column<E>> columns = new ArrayList<>();

    public AbstractGridDelegate(C component) {
        super(component);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        metadataTools = applicationContext.getBean(MetadataTools.class);
        messageTools = applicationContext.getBean(MessageTools.class);
        uiComponents = applicationContext.getBean(UiComponents.class);
        accessManager = applicationContext.getBean(AccessManager.class);
    }

    protected void initComponent() {
        component.addSortListener(this::onSort);
        component.addColumnReorderListener(this::onColumnReorderChange);
        addSelectionListener(this::notifyDataProviderSelectionChanged);
    }

    @Nullable
    public ITEMS getItems() {
        return dataGridItems;
    }

    public void setItems(@Nullable ITEMS dataGridItems) {
        unbind();

        if (dataGridItems != null) {
            this.dataGridItems = dataGridItems;

            bind(dataGridItems);

            applySecurityToPropertyColumns();
        }
    }

    protected void bind(DataGridItems<E> dataGridItems) {
        itemSetChangeRegistration = dataGridItems.addItemSetChangeListener(this::itemsItemSetChanged);
        valueChangeRegistration = dataGridItems.addValueChangeListener(this::itemsValueChanged);
    }

    protected void unbind() {
        if (dataGridItems != null) {
            dataGridItems = null;
            setupEmptyDataProvider();
        }

        if (itemSetChangeRegistration != null) {
            itemSetChangeRegistration.remove();
            itemSetChangeRegistration = null;
        }

        if (valueChangeRegistration != null) {
            valueChangeRegistration.remove();
            valueChangeRegistration = null;
        }
    }

    protected void itemsItemSetChanged(DataGridItems.ItemSetChangeEvent<E> event) {
        closeEditorIfOpened();
        component.getDataCommunicator().reset();
    }

    protected void closeEditorIfOpened() {
        if (getComponent().isEditorCreated()
                && getComponent().getEditor().isOpen()) {
            Editor<E> editor = getComponent().getEditor();
            if (editor.isBuffered()) {
                editor.cancel();
            } else {
                editor.closeEditor();
            }

            if (editor instanceof DataGridDataProviderChangeObserver) {
                ((DataGridDataProviderChangeObserver) editor).dataProviderChanged();
            }
        }
    }

    protected void itemsValueChanged(DataGridItems.ValueChangeEvent<E> event) {
        if (itemIsBeingEdited(event.getItem())) {
            DataGridEditor<E> editor = ((DataGridEditor<E>) getComponent().getEditor());
            // Do not interrupt the save process
            if (editor.isBuffered() && !editor.isSaving()) {
                editor.cancel();
            } else {
                // In case of unbuffered editor, we don't need to refresh an item,
                // because it results in row repainting, i.e. all editor components
                // are recreated and focus lost. In case of buffered editor in a
                // save process, an item will be refreshed after editor is closed.
                return;
            }
        }

        component.getDataCommunicator().refresh(event.getItem());
    }

    protected boolean itemIsBeingEdited(E item) {
        if (getComponent().isEditorCreated()) {
            Editor<E> editor = getComponent().getEditor();
            return editor.isOpen()
                    && Objects.equals(item, editor.getItem());
        }

        return false;
    }

    @Nullable
    public E getSingleSelectedItem() {
        return getSelectionModel()
                .getFirstSelectedItem()
                .orElse(null);
    }

    public Set<E> getSelectedItems() {
        return getSelectionModel().getSelectedItems();
    }

    public void select(E item) {
        Preconditions.checkNotNullArgument(item);

        select(Collections.singletonList(item));
    }

    public void select(Collection<E> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            if (isMultiSelect()) {
                //noinspection unchecked
                ((SelectionModel.Multi<Grid<E>, E>) getSelectionModel())
                        .updateSelection(new LinkedHashSet<>(items), Collections.emptySet());
            } else {
                getSelectionModel().select(items.iterator().next());
            }
        } else {
            deselectAll();
        }
    }

    public void deselect(E item) {
        getSelectionModel().deselect(item);
    }

    public void deselectAll() {
        getSelectionModel().deselectAll();
    }

    public boolean isMultiSelect() {
        return getSelectionModel() instanceof SelectionModel.Multi;
    }

    public void enableMultiSelect() {
        setMultiSelect(true);
    }

    public void setMultiSelect(boolean multiSelect) {
        component.setSelectionMode(multiSelect
                ? Grid.SelectionMode.MULTI
                : Grid.SelectionMode.SINGLE);
    }

    public Registration addSelectionListener(SelectionListener<Grid<E>, E> listener) {
        if (selectionListenerRegistration == null) {
            attachSelectionListener();
        }

        selectionListeners.add(listener);

        return () -> {
            selectionListeners.remove(listener);
            if (selectionListeners.isEmpty()) {
                detachSelectionListener();
            }
        };
    }

    @Nullable
    public MetaPropertyPath getColumnMetaPropertyPath(Grid.Column<E> column) {
        return propertyColumns.get(column);
    }

    public Grid.Column<E> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        Grid.Column<E> column = addColumnInternal(key, metaPropertyPath);
        propertyColumns.put(column, metaPropertyPath);
        return column;
    }

    public Grid.Column<E> addColumn(Grid.Column<E> column) {
        columns.add(column);
        return column;
    }

    protected void setupEmptyDataProvider() {
        component.setItems(new ListDataProvider<>(Collections.emptyList()));
    }

    protected Grid.Column<E> addColumnInternal(String key, MetaPropertyPath metaPropertyPath) {
        ValueProvider<E, ?> valueProvider = getValueProvider(metaPropertyPath);

        // Also it leads to adding column to {@link #columns} list
        Grid.Column<E> column = component.addColumn(valueProvider);
        column.setKey(key);

        initColumn(column, metaPropertyPath);

        return column;
    }

    protected ValueProvider<E, ?> getValueProvider(MetaPropertyPath metaPropertyPath) {
        return new StringPresentationValueProvider<>(metaPropertyPath, metadataTools);
    }

    protected void initColumn(Grid.Column<E> column, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
        column.setSortable(true);

        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        column.setHeader(messageTools.getPropertyCaption(propertyMetaClass, metaProperty.getName()));
    }

    protected void onSelectionChange(SelectionEvent<Grid<E>, E> event) {
        for (SelectionListener<Grid<E>, E> listener : selectionListeners) {
            listener.selectionChange(event);
        }
    }

    public void onSelectionModelChange(SelectionModel<Grid<E>, E> selectionModel) {
        if (selectionModel instanceof GridNoneSelectionModel) {
            detachSelectionListener();
            return;
        }

        if (!selectionListeners.isEmpty()) {
            attachSelectionListener();
        }
    }

    protected void attachSelectionListener() {
        detachSelectionListener();

        if (!(getSelectionModel() instanceof GridNoneSelectionModel)) {
            selectionListenerRegistration = getSelectionModel().addSelectionListener(this::onSelectionChange);
        }
    }

    protected void detachSelectionListener() {
        if (selectionListenerRegistration != null) {
            selectionListenerRegistration.remove();
            selectionListenerRegistration = null;
        }
    }

    protected GridSelectionModel<E> getSelectionModel() {
        return component.getSelectionModel();
    }

    protected void onColumnReorderChange(ColumnReorderEvent<E> event) {
        // Grid doesn't know about columns hidden by security permissions,
        // so we need to return them back to their previous positions
        columns = restoreColumnsOrder(event.getColumns());
    }

    /**
     * Inserts columns hidden by security permissions into a list of visible columns in their original positions.
     *
     * @param visibleColumns the list of DataGrid columns, not hidden by security permissions
     * @return a list of all columns in DataGrid
     */
    protected List<Grid.Column<E>> restoreColumnsOrder(List<Grid.Column<E>> visibleColumns) {
        List<Grid.Column<E>> newOrderColumns = new ArrayList<>(visibleColumns);
        for (Grid.Column<E> column : columns) {
            if (!newOrderColumns.contains(column)) {
                newOrderColumns.add(columns.indexOf(column), column);
            }
        }
        return newOrderColumns;
    }

    protected List<Grid.Column<E>> deleteHiddenColumns(List<Grid.Column<E>> allColumns) {
        return allColumns.stream()
                .filter(c -> !propertyColumns.containsKey(c)
                        || isPropertyEnabledBySecurity(propertyColumns.get(c)))
                .collect(Collectors.toList());
    }

    protected void onSort(SortEvent<Grid<E>, GridSortOrder<E>> event) {
        if (!(dataGridItems instanceof DataGridItems.Sortable)
                || !(dataGridItems instanceof EntityDataUnit)) {
            return;
        }

        //noinspection unchecked
        DataGridItems.Sortable<E> dataProvider = (DataGridItems.Sortable<E>) dataGridItems;

        List<GridSortOrder<E>> sortOrders = event.getSortOrder();
        if (sortOrders.isEmpty()) {
            dataProvider.resetSortOrder();
        } else {
            GridSortOrder<E> sortOrder = sortOrders.get(0);

            Grid.Column<E> column = sortOrder.getSorted();
            if (column != null
                    && !Strings.isNullOrEmpty(column.getKey())) {
                MetaClass metaClass = ((EntityDataUnit) dataGridItems).getEntityMetaClass();
                MetaPropertyPath mpp = metaClass.getPropertyPath(column.getKey());
                if (mpp != null) {
                    boolean ascending = SortDirection.ASCENDING.equals(sortOrder.getDirection());
                    dataProvider.sort(new Object[]{mpp}, new boolean[]{ascending});
                }
            }
        }
    }

    protected void notifyDataProviderSelectionChanged(SelectionEvent<Grid<E>, E> ignore) {
        ITEMS items = getItems();

        if (items == null
                || items.getState() == BindingState.INACTIVE) {
            return;
        }

        Set<E> selected = getSelectedItems();
        if (selected.isEmpty()) {
            items.setSelectedItem(null);
        } else {
            E newItem = selected.iterator().next();
            // In some cases, the container may not contain
            // an item that we want to set as the selected
            if (items.containsItem(newItem)) {
                items.setSelectedItem(newItem);
            }
        }
    }

    protected void applySecurityToPropertyColumns() {
        for (Map.Entry<Grid.Column<E>, MetaPropertyPath> e : propertyColumns.entrySet()) {
            if (afterColumnSecurityApplyHandler != null) {
                afterColumnSecurityApplyHandler.accept(
                        new ColumnSecurityContext<>(e.getKey(), e.getValue(),
                                isPropertyEnabledBySecurity(e.getValue())));
            }
        }
    }

    public boolean isPropertyEnabledBySecurity(MetaPropertyPath mpp) {
        EntityAttributeContext context = new EntityAttributeContext(mpp);
        accessManager.applyRegisteredConstraints(context);
        return context.canView();
    }

    public List<Grid.Column<E>> getColumns() {
        return List.copyOf(columns);
    }

    /**
     * @return a copy of columns that are visible and not hidden by security
     * @deprecated use {@link Grid#getColumns()} and filter by visibility
     */
    @Deprecated
    public List<Grid.Column<E>> getVisibleColumns() {
        return columns.stream()
                .filter(c -> c.isVisible()
                        && (!propertyColumns.containsKey(c) || isPropertyEnabledBySecurity(propertyColumns.get(c))))
                .collect(Collectors.toList());
    }

    @Nullable
    public Grid.Column<E> getColumnByKey(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }
        return columns.stream()
                .filter(c -> key.equals(c.getKey()))
                .findFirst()
                .orElse(null);
    }

    public void removeColumn(Grid.Column<E> column) {
        columns.remove(column);

        propertyColumns.keySet().remove(column);
    }

    public boolean isDataGridOwner(Grid.Column<E> column) {
        return column.getGrid().equals(component)
                && column.getElement().getParent() != null;
    }

    public void setColumnPosition(Grid.Column<E> column, int index) {
        Preconditions.checkNotNullArgument(column);
        if (index >= columns.size() || index < 0) {
            throw new IndexOutOfBoundsException(String.format("Index '%s' is out of range. Available indexes " +
                    "to move column: from 0 to %s including bounds", index, columns.size() - 1));
        }

        columns.remove(column);
        columns.add(index, column);

        // remove hidden columns by security
        List<Grid.Column<E>> newColumnOrder = deleteHiddenColumns(columns);

        component.setColumnOrder(newColumnOrder);
    }

    @Nullable
    public Consumer<ColumnSecurityContext<E>> getAfterColumnSecurityApplyHandler() {
        return afterColumnSecurityApplyHandler;
    }

    public void setAfterColumnSecurityApplyHandler(
            @Nullable Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler) {
        this.afterColumnSecurityApplyHandler = afterColumnSecurityApplyHandler;
    }

    public static class ColumnSecurityContext<E> {

        protected Grid.Column<E> column;
        protected MetaPropertyPath metaPropertyPath;
        protected Boolean propertyEnabled;

        public ColumnSecurityContext(Grid.Column<E> column,
                                     MetaPropertyPath metaPropertyPath,
                                     Boolean propertyEnabled) {
            this.column = column;
            this.metaPropertyPath = metaPropertyPath;
            this.propertyEnabled = propertyEnabled;
        }

        public Grid.Column<E> getColumn() {
            return column;
        }

        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }

        public Boolean isPropertyEnabled() {
            return propertyEnabled;
        }
    }
}
