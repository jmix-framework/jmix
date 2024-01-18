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
import com.google.common.primitives.Booleans;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.ReadAction;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.SupportsEnterPress.EnterPressEvent;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.DataGridDataProviderChangeObserver;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.data.aggregation.Aggregation;
import io.jmix.flowui.data.aggregation.Aggregations;
import io.jmix.flowui.data.aggregation.impl.AggregatableDelegate;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.data.provider.StringPresentationValueProvider;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.BeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiFunction;
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
    protected Aggregations aggregations;
    protected AggregatableDelegate<Object> aggregatableDelegate;

    protected ITEMS dataGridItems;

    protected Registration selectionListenerRegistration;
    protected Registration itemSetChangeRegistration;
    protected Registration valueChangeRegistration;

    // own selection listeners registration is needed to keep listeners if selection model is changed
    protected Set<SelectionListener<Grid<E>, E>> selectionListeners = new HashSet<>();

    protected Set<ComponentEventListener<ItemDoubleClickEvent<E>>> itemDoubleClickListeners = new HashSet<>();
    protected Consumer<EnterPressEvent<C>> enterPressHandler;

    protected Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler;

    protected boolean aggregatable;
    protected EnhancedDataGrid.AggregationPosition aggregationPosition = EnhancedDataGrid.AggregationPosition.BOTTOM;
    protected Map<Grid.Column<E>, AggregationInfo> aggregationMap = new LinkedHashMap<>();

    protected HeaderRow aggregationHeader;
    protected FooterRow aggregationFooter;

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
        aggregations = applicationContext.getBean(Aggregations.class);
    }

    protected void initComponent() {
        component.addSortListener(this::onSort);
        component.addColumnReorderListener(this::onColumnReorderChange);
        addSelectionListener(this::notifyDataProviderSelectionChanged);

        // Can't use method reference, because of compilation error
        //noinspection Convert2Lambda,Anonymous2MethodRef
        ComponentUtil.addListener(component, ItemDoubleClickEvent.class, new ComponentEventListener<>() {
            @Override
            public void onComponentEvent(ItemDoubleClickEvent event) {
                //noinspection unchecked
                onItemDoubleClick(event);
            }
        });

        Shortcuts.addShortcutListener(component, this::handleEnterPress, Key.ENTER)
                .listenOn(component);
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
            updateAggregationRow();

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
        updateAggregationRow();
        //refresh selection because it contains old item instances which may not exist in the container anymore
        refreshSelection(event.getSource().getItems());
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

    /**
     * Refreshes current selection using provided items.
     */
    protected void refreshSelection(Collection<E> items) {
        Set<E> prevSelectedItemsToRefresh = new HashSet<>(getSelectedItems());

        List<E> itemsToSelect = new ArrayList<>(prevSelectedItemsToRefresh.size());
        for (E item : items) {
            //select the item if it was selected before refresh
            if (prevSelectedItemsToRefresh.remove(item)) {
                itemsToSelect.add(item);
            }

            //skip further checks if no more items were selected
            if (prevSelectedItemsToRefresh.isEmpty()) {
                break;
            }
        }
        //selection model doesn't provide direct access to selected items,
        //so to update the model we are forced to deselect all items and select new items again
        //to handle any changes in item collection or items themselves
        deselectAll();
        select(itemsToSelect);
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
        updateAggregationRow();
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

    public Registration addItemDoubleClickListener(ComponentEventListener<ItemDoubleClickEvent<E>> listener) {
        itemDoubleClickListeners.add(listener);
        return () -> itemDoubleClickListeners.remove(listener);
    }


    public void setEnterPressHandler(@Nullable Consumer<EnterPressEvent<C>> handler) {
        this.enterPressHandler = handler;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public void setAggregatable(boolean aggregatable) {
        this.aggregatable = aggregatable;

        updateAggregationRow();
    }

    public EnhancedDataGrid.AggregationPosition getAggregationPosition() {
        return aggregationPosition;
    }

    public void setAggregationPosition(EnhancedDataGrid.AggregationPosition position) {
        this.aggregationPosition = position;
    }

    public void addAggregationInfo(Grid.Column<E> column, AggregationInfo info) {
        if (aggregationMap.containsKey(column)) {
            throw new IllegalStateException(String.format("Aggregation property %s already exists", column.getKey()));
        }

        aggregationMap.put(column, info);
    }

    public void removeAggregationInfo(Grid.Column<E> column) {
        aggregationMap.remove(column);
    }

    public Map<Grid.Column<E>, Object> getAggregationResults() {
        return aggregateValues();
    }

    protected Map<Grid.Column<E>, String> aggregate() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException(String.format("%s must be aggregatable and items must not be null in " +
                    "order to use aggregation", component.getClass().getSimpleName()));
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, String> aggregationInfoMap = getAggregatableDelegate().aggregate(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().stream()
                        .map(EntityValues::getId)
                        .toList()
        );

        return convertAggregationKeyMapToColumnMap(aggregationInfoMap);
    }

    protected Map<Grid.Column<E>, Object> aggregateValues() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException("DataGrid must be aggregatable and items must not be null in order to " +
                    "use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, Object> aggregationInfoMap = getAggregatableDelegate().aggregateValues(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().stream()
                        .map(EntityValues::getId)
                        .toList()
        );

        return convertAggregationKeyMapToColumnMap(aggregationInfoMap);
    }

    protected <V> Map<Grid.Column<E>, V> convertAggregationKeyMapToColumnMap(Map<AggregationInfo, V> aggregationInfos) {
        return aggregationMap.entrySet()
                .stream()
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), aggregationInfos.get(entry.getValue())),
                        LinkedHashMap::putAll);
    }

    protected List<AggregationInfo> getAggregationInfos() {
        return aggregationMap.values()
                .stream()
                .filter(this::checkAggregation)
                .toList();
    }

    protected boolean checkAggregation(AggregationInfo aggregationInfo) {
        AggregationInfo.Type aggregationType = aggregationInfo.getType();
        if (aggregationType == AggregationInfo.Type.CUSTOM) {
            return true;
        }

        MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();
        if (propertyPath == null) {
            throw new IllegalArgumentException("Unable to aggregate column without property");
        }

        Class<?> javaType = propertyPath.getMetaProperty().getJavaType();
        Aggregation<?> aggregation = aggregations.get(javaType);

        if (aggregation != null && aggregation.getSupportedAggregationTypes().contains(aggregationType)) {
            return true;
        }

        String message = String.format("Unable to aggregate column \"%s\" with data type %s " +
                        "with default aggregation strategy: %s",
                propertyPath, propertyPath.getRange(), aggregationInfo.getType());

        throw new IllegalArgumentException(message);
    }

    @SuppressWarnings("DuplicatedCode")
    protected void fillAggregationRow(Map<Grid.Column<E>, String> values) {
        switch (getAggregationPosition()) {
            case TOP -> {
                if (aggregationHeader == null) {
                    aggregationHeader = component.appendHeaderRow();
                }

                fillHeaderRow(values);
            }
            case BOTTOM -> {
                if (aggregationFooter == null) {
                    aggregationFooter = component.prependFooterRow();
                }

                fillFooterRow(values);
            }
        }
    }

    protected void fillHeaderRow(Map<Grid.Column<E>, String> values) {
        for (Map.Entry<Grid.Column<E>, String> entry : values.entrySet()) {
            Grid.Column<E> column = entry.getKey();
            HeaderRow.HeaderCell cell = aggregationHeader.getCell(column);
            String cellTitle = aggregationMap.get(column).getCellTitle();

            if (cellTitle != null) {
                Span headerSpan = new Span(entry.getValue());
                headerSpan.setTitle(cellTitle);

                cell.setComponent(headerSpan);
            } else {
                cell.setText(entry.getValue());
            }
        }
    }

    protected void fillFooterRow(Map<Grid.Column<E>, String> values) {
        for (Map.Entry<Grid.Column<E>, String> entry : values.entrySet()) {
            Grid.Column<E> column = entry.getKey();
            FooterRow.FooterCell cell = aggregationFooter.getCell(column);
            String cellTitle = aggregationMap.get(column).getCellTitle();

            if (cellTitle != null) {
                Span footerSpan = new Span(entry.getValue());
                footerSpan.setTitle(cellTitle);

                cell.setComponent(footerSpan);
            } else {
                cell.setText(entry.getValue());
            }
        }
    }

    protected void updateAggregationRow() {
        if (isAggregatable()
                && getItems() != null
                && MapUtils.isNotEmpty(aggregationMap)) {
            Map<Grid.Column<E>, String> results = aggregate();
            fillAggregationRow(results);
        }
    }

    public BiFunction<Renderer<E>, String, Grid.Column<E>> getDefaultColumnFactory() {
        return (Renderer<E> renderer, String columnId) -> {
            DataGridColumn<E> dataGridColumn =
                    new DataGridColumn<>(component, columnId, renderer);
            BeanUtil.autowireContext(applicationContext, dataGridColumn);
            return dataGridColumn;
        };
    }

    @Nullable
    public MetaPropertyPath getColumnMetaPropertyPath(Grid.Column<E> column) {
        return propertyColumns.get(column);
    }

    public DataGridColumn<E> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        Grid.Column<E> column = addColumnInternal(key, metaPropertyPath);
        propertyColumns.put(column, metaPropertyPath);
        return (DataGridColumn<E>) column;
    }

    public DataGridColumn<E> addColumn(Grid.Column<E> column) {
        columns.add(column);
        return (DataGridColumn<E>) column;
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
            Map<Object, Boolean> sortedColumnMap = new LinkedHashMap<>();

            for (GridSortOrder<E> sortOrder : sortOrders) {
                Grid.Column<E> column = sortOrder.getSorted();

                if (column != null) {
                    MetaPropertyPath mpp = propertyColumns.get(column);

                    if (mpp != null) {
                        boolean ascending = SortDirection.ASCENDING.equals(sortOrder.getDirection());
                        sortedColumnMap.put(mpp, ascending);
                    }
                }
            }

            dataProvider.sort(sortedColumnMap.keySet().toArray(), Booleans.toArray(sortedColumnMap.values()));
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

    @Nullable
    public DataGridColumn<E> getColumnByKey(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }
        return (DataGridColumn<E>) columns.stream()
                .filter(c -> key.equals(c.getKey()))
                .findFirst()
                .orElse(null);
    }

    public void removeColumn(Grid.Column<E> column) {
        columns.remove(column);

        propertyColumns.keySet().remove(column);
        removeAggregationInfo(column);
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

    @SuppressWarnings("unchecked")
    protected AggregatableDelegate<Object> getAggregatableDelegate() {
        if (aggregatableDelegate == null) {
            aggregatableDelegate = applicationContext.getBean(AggregatableDelegate.class);
        }

        if (getItems() != null) {
            aggregatableDelegate.setItemProvider(getItems()::getItem);
            aggregatableDelegate.setItemValueProvider(getItems()::getItemValue);
        }
        return aggregatableDelegate;
    }

    @Nullable
    public Consumer<ColumnSecurityContext<E>> getAfterColumnSecurityApplyHandler() {
        return afterColumnSecurityApplyHandler;
    }

    public void setAfterColumnSecurityApplyHandler(
            @Nullable Consumer<ColumnSecurityContext<E>> afterColumnSecurityApplyHandler) {
        this.afterColumnSecurityApplyHandler = afterColumnSecurityApplyHandler;
    }

    protected void onItemDoubleClick(ItemDoubleClickEvent<E> itemDoubleClickEvent) {
        if (itemDoubleClickListeners.isEmpty()) {
            handleDoubleClickAction(itemDoubleClickEvent.getItem());
        } else {
            fireItemDoubleClick(itemDoubleClickEvent);
        }
    }

    protected void fireItemDoubleClick(ItemDoubleClickEvent<E> itemDoubleClickEvent) {
        for (ComponentEventListener<ItemDoubleClickEvent<E>> listener : itemDoubleClickListeners) {
            listener.onComponentEvent(itemDoubleClickEvent);
        }
    }

    protected void handleEnterPress() {
        handleDoubleClickAction(null);
    }

    protected void handleDoubleClickAction(@Nullable E item) {
        if (item != null) {
            // have to select clicked item to make action work, otherwise
            // consecutive clicks on the same item deselect it
            component.select(item);
        }

        if (enterPressHandler != null) {
            enterPressHandler.accept(new EnterPressEvent<>(component));
            return;
        }

        Action action = findEnterAction();
        if (action == null) {
            action = component.getAction(EditAction.ID);
            if (action == null) {
                action = component.getAction(ReadAction.ID);
            }
        }

        if (action != null && action.isEnabled()) {
            action.actionPerform(component);
        }
    }

    @Nullable
    protected Action findEnterAction() {
        for (Action action : component.getActions()) {
            KeyCombination keyCombination = action.getShortcutCombination();
            if (keyCombination != null) {
                if ((keyCombination.getKeyModifiers() == null || keyCombination.getKeyModifiers().length == 0)
                        && keyCombination.getKey() == Key.ENTER) {
                    return action;
                }
            }
        }

        return null;
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

        public DataGridColumn<E> getColumn() {
            return (DataGridColumn<E>) column;
        }

        public MetaPropertyPath getMetaPropertyPath() {
            return metaPropertyPath;
        }

        public Boolean isPropertyEnabled() {
            return propertyEnabled;
        }
    }
}
