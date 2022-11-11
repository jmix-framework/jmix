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

package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import com.vaadin.data.HasValue;
import com.vaadin.data.SelectionModel;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.GridSortOrder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DescriptionGenerator;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.components.grid.*;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.ValueSourceProvider;
import io.jmix.ui.component.data.aggregation.Aggregation;
import io.jmix.ui.component.data.aggregation.Aggregations;
import io.jmix.ui.component.data.aggregation.impl.AggregatableDelegate;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EmptyDataUnit;
import io.jmix.ui.component.data.meta.EntityDataGridItems;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.component.data.value.ContainerValueSourceProvider;
import io.jmix.ui.component.datagrid.DataGridDataProvider;
import io.jmix.ui.component.datagrid.DataGridItemsEventsDelegate;
import io.jmix.ui.component.datagrid.SortableDataGridDataProvider;
import io.jmix.ui.component.formatter.CollectionFormatter;
import io.jmix.ui.component.pagination.data.PaginationDataBinder;
import io.jmix.ui.component.pagination.data.PaginationDataUnitBinder;
import io.jmix.ui.component.pagination.data.PaginationEmptyBinder;
import io.jmix.ui.component.renderer.RendererWrapper;
import io.jmix.ui.component.valueprovider.EntityValueProvider;
import io.jmix.ui.component.valueprovider.FormatterBasedValueProvider;
import io.jmix.ui.component.valueprovider.StringPresentationValueProvider;
import io.jmix.ui.component.valueprovider.YesNoIconPresentationValueProvider;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.ScreenValidation;
import io.jmix.ui.sys.ShortcutsDelegate;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixCssActionsLayout;
import io.jmix.ui.widget.JmixEnhancedGrid;
import io.jmix.ui.widget.JmixGridEditorFieldFactory;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import io.jmix.ui.widget.data.SortableDataProvider;
import io.jmix.ui.widget.grid.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.ComponentsHelper.findActionById;
import static io.jmix.ui.screen.LookupScreen.LOOKUP_ENTER_PRESSED_ACTION_ID;
import static io.jmix.ui.screen.LookupScreen.LOOKUP_ITEM_CLICK_ACTION_ID;

public abstract class AbstractDataGrid<C extends Grid<E> & JmixEnhancedGrid<E>, E>
        extends AbstractComponent<C>
        implements DataGrid<E>, SecuredActionsHolder, LookupComponent.LookupSelectionChangeNotifier<E>,
        DataGridItemsEventsDelegate<E>, HasInnerComponents, InitializingBean {

    protected static final String HAS_TOP_PANEL_STYLE_NAME = "has-top-panel";
    protected static final String TEXT_SELECTION_ENABLED_STYLE = "text-selection-enabled";

    private static final Logger log = LoggerFactory.getLogger(AbstractDataGrid.class);

    /* Beans */
    protected MetadataTools metadataTools;
    protected AccessManager accessManager;
    protected Messages messages;
    protected MessageTools messageTools;
    protected ScreenValidation screenValidation;
    protected Actions actions;
    protected IconResolver iconResolver;
    protected Aggregations aggregations;
    protected AggregatableDelegate<Object> aggregatableDelegate;
    protected UiTestIdsSupport uiTestIdsSupport;

    // Style names used by grid itself
    protected final List<String> internalStyles = new ArrayList<>(2);

    protected final Map<String, Column<E>> columns = new HashMap<>();
    protected List<Column<E>> columnsOrder = new ArrayList<>();
    protected final Map<String, Function<ColumnGeneratorEvent<E>, ?>> columnGenerators = new HashMap<>();

    protected final List<Action> actionList = new ArrayList<>();
    protected final ShortcutsDelegate<ShortcutListener> shortcutsDelegate;
    protected final ActionsPermissions actionsPermissions = new ActionsPermissions(this);

    protected JmixGridContextMenu<E> contextMenu;
    protected final List<ActionMenuItemWrapper> contextMenuItems = new ArrayList<>();

    protected boolean sortable = true;
    protected boolean columnsCollapsingAllowed = true;
    protected boolean textSelectionEnabled = false;
    protected boolean editorCrossFieldValidate = true;

    protected Action itemClickAction;
    protected Action enterPressAction;

    protected SelectionMode selectionMode;

    protected GridComposition componentComposition;
    protected CssLayout topPanel;
    protected ButtonsPanel buttonsPanel;
    protected PaginationComponent pagination;

    protected List<Function<? super E, String>> rowStyleProviders;

    protected Function<? super E, String> rowDescriptionProvider;

    protected Function<E, io.jmix.ui.component.Component> detailsGenerator = null;

    protected Registration columnCollapsingChangeListenerRegistration;
    protected Registration columnResizeListenerRegistration;
    protected Registration contextClickListenerRegistration;

    protected Registration editorCancelListener;
    protected Registration editorOpenListener;
    protected Registration editorBeforeSaveListener;
    protected Registration editorSaveListener;

    protected final List<HeaderRow> headerRows = new ArrayList<>();
    protected final List<FooterRow> footerRows = new ArrayList<>();

    protected com.vaadin.ui.components.grid.HeaderRow headerAggregationRow;
    protected com.vaadin.ui.components.grid.FooterRow footerAggregationRow;

    protected boolean showIconsForPopupMenuActions;

    protected DataGridDataProvider<E> dataBinding;

    protected Map<E, Object> itemDatasources; // lazily initialized WeakHashMap;
    protected Consumer<EmptyStateClickEvent<E>> emptyStateClickEventHandler;

    public AbstractDataGrid() {
        component = createComponent();
        componentComposition = createComponentComposition();
        shortcutsDelegate = createShortcutsDelegate();
    }

    protected GridComposition createComponentComposition() {
        return new GridComposition();
    }

    protected abstract C createComponent();

    protected ShortcutsDelegate<ShortcutListener> createShortcutsDelegate() {
        return new ShortcutsDelegate<ShortcutListener>() {
            @Override
            protected ShortcutListener attachShortcut(String actionId, KeyCombination keyCombination) {
                ShortcutListener shortcut =
                        new ShortcutListenerDelegate(actionId, keyCombination.getKey().getCode(),
                                KeyCombination.Modifier.codes(keyCombination.getModifiers())
                        ).withHandler((sender, target) -> {
                            if (sender == componentComposition) {
                                Action action = getAction(actionId);
                                if (action != null && action.isEnabled() && action.isVisible()) {
                                    action.actionPerform(AbstractDataGrid.this);
                                }
                            }
                        });

                componentComposition.addShortcutListener(shortcut);
                return shortcut;
            }

            @Override
            protected void detachShortcut(Action action, ShortcutListener shortcutDescriptor) {
                componentComposition.removeShortcutListener(shortcutDescriptor);
            }

            @Override
            protected Collection<Action> getActions() {
                return AbstractDataGrid.this.getActions();
            }
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
        initComponentComposition(componentComposition);

        initHeaderRows(component);
        initFooterRows(component);
        initEditor(component);

        initContextMenu();

        initSidebarMenu();
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setThemeConstantsManager(ThemeConstantsManager themeConstantsManager) {
        ThemeConstants theme = themeConstantsManager.getConstants();
        this.showIconsForPopupMenuActions = theme.getBoolean("jmix.ui.showIconsForPopupMenuActions", false);
    }

    @Autowired
    protected void setScreenValidation(ScreenValidation screenValidation) {
        this.screenValidation = screenValidation;
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Autowired
    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }

    @Autowired
    public void setUiTestIdsSupport(UiTestIdsSupport uiTestIdsSupport) {
        this.uiTestIdsSupport = uiTestIdsSupport;
    }

    @SuppressWarnings("unchecked")
    protected void initComponent(Grid<E> component) {
        setSelectionMode(SelectionMode.SINGLE);

        component.setColumnReorderingAllowed(true);

        component.addItemClickListener(this::onItemClick);
        component.addColumnReorderListener(this::onColumnReorder);
        component.addSortListener(this::onSort);

        component.setSizeUndefined();
        component.setHeightMode(HeightMode.UNDEFINED);

        component.setStyleGenerator(this::getGeneratedRowStyle);

        ((JmixEnhancedGrid<E>) component).setJmixEditorFieldFactory(createEditorFieldFactory());
        ((JmixEnhancedGrid<E>) component).setBeforeRefreshHandler(this::onBeforeRefreshGridData);

        initEmptyState();
    }

    protected void onBeforeRefreshGridData(E item) {
        clearFieldDatasources(item);
    }

    protected JmixGridEditorFieldFactory<E> createEditorFieldFactory() {
        DataGridEditorFieldFactory fieldFactory = this.applicationContext.getBean(DataGridEditorFieldFactory.class);
        return new DataGridEditorFieldFactoryAdapter<>(this, fieldFactory);
    }

    protected void initComponentComposition(GridComposition componentComposition) {
        componentComposition.setPrimaryStyleName("jmix-data-grid-composition");
        componentComposition.setGrid(component);
        componentComposition.addComponent(component);
        componentComposition.setWidthUndefined();

        componentComposition.addShortcutListener(createEnterShortcutListener());
    }

    protected void onItemClick(Grid.ItemClick<E> e) {
        AppUI ui = (AppUI) component.getUI();
        if (!ui.isAccessibleForUser(component)) {
            LoggerFactory.getLogger(DataGridImpl.class)
                    .debug("Ignore click attempt because DataGrid is inaccessible for user");
            return;
        }

        com.vaadin.shared.MouseEventDetails vMouseEventDetails = e.getMouseEventDetails();
        if (vMouseEventDetails.isDoubleClick() && e.getItem() != null
                && !AbstractDataGrid.this.isEditorEnabled()) {
            // note: for now Grid doesn't send double click if editor is enabled,
            // but it's better to handle it manually
            handleDoubleClickAction();
        }

        if (hasSubscriptions(ItemClickEvent.class)) {
            MouseEventDetails mouseEventDetails = WrapperUtils.toMouseEventDetails(vMouseEventDetails);

            E item = e.getItem();
            if (item == null) {
                // this can happen if user clicked on an item which is removed from the
                // datasource, so we don't want to send such event because it's useless
                return;
            }

            Column<E> column = e.getColumn() != null
                    ? getColumnById(e.getColumn().getId())
                    : null;

            ItemClickEvent<E> event = new ItemClickEvent<>(AbstractDataGrid.this,
                    mouseEventDetails, item, EntityValues.getId(item), column != null ? column.getId() : null);
            publish(ItemClickEvent.class, event);
        }
    }

    protected void onColumnReorder(Grid.ColumnReorderEvent e) {
        // Grid doesn't know about columns hidden by security permissions,
        // so we need to return them back to they previous positions
        columnsOrder = restoreColumnsOrder(getColumnsOrderInternal());

        ColumnReorderEvent event = new ColumnReorderEvent(AbstractDataGrid.this, e.isUserOriginated());
        publish(ColumnReorderEvent.class, event);
    }

    protected void onSort(com.vaadin.event.SortEvent<GridSortOrder<E>> e) {
        if (component.getDataProvider() instanceof SortableDataProvider) {
            //noinspection unchecked
            SortableDataProvider<E> dataProvider = (SortableDataProvider<E>) component.getDataProvider();

            List<GridSortOrder<E>> sortOrders = e.getSortOrder();
            if (sortOrders.isEmpty()) {
                dataProvider.resetSortOrder();
            } else {
                GridSortOrder<E> sortOrder = sortOrders.get(0);

                Column<E> column = getColumnByGridColumn(sortOrder.getSorted());
                if (column != null) {
                    MetaPropertyPath propertyPath = column.getPropertyPath();
                    boolean ascending = com.vaadin.shared.data.sort.SortDirection.ASCENDING
                            .equals(sortOrder.getDirection());
                    dataProvider.sort(new Object[]{propertyPath}, new boolean[]{ascending});
                }
            }
        }

        List<SortOrder> sortOrders = convertToDataGridSortOrder(e.getSortOrder());

        SortEvent event = new SortEvent(AbstractDataGrid.this, sortOrders, e.isUserOriginated());
        publish(SortEvent.class, event);
    }

    protected void onSelectionChange(com.vaadin.event.selection.SelectionEvent<E> e) {
        DataGridItems<E> dataGridItems = getItems();

        if (dataGridItems == null
                || dataGridItems.getState() == BindingState.INACTIVE) {
            return;
        }

        Set<E> selected = getSelected();
        if (selected.isEmpty()) {
            dataGridItems.setSelectedItem(null);
        } else {
            // reset selection and select new item
            if (isMultiSelect()) {
                dataGridItems.setSelectedItem(null);
            }

            E newItem = selected.iterator().next();
            E dsItem = dataGridItems.getSelectedItem();
            // In some cases, the container may not contain
            // an item that we want to set as the selected
            if (dataGridItems.containsItem(newItem)) {
                dataGridItems.setSelectedItem(newItem);
            }

            if (Objects.equals(dsItem, newItem)) {
                // in this case item change event will not be generated
                refreshActionsState();
            }
        }

        fireSelectionEvent(e);

        LookupSelectionChangeEvent<E> selectionChangeEvent = new LookupSelectionChangeEvent<>(this);
        publish(LookupSelectionChangeEvent.class, selectionChangeEvent);
    }

    protected void fireSelectionEvent(com.vaadin.event.selection.SelectionEvent<E> e) {
        Set<E> oldSelection;
        if (e instanceof MultiSelectionEvent) {
            oldSelection = ((MultiSelectionEvent<E>) e).getOldSelection();
        } else {
            //noinspection unchecked
            E oldValue = ((HasValue.ValueChangeEvent<E>) e).getOldValue();
            oldSelection = oldValue != null ? Collections.singleton(oldValue) : Collections.emptySet();
        }

        SelectionEvent<E> event = new SelectionEvent<>(AbstractDataGrid.this, oldSelection, e.isUserOriginated());
        publish(SelectionEvent.class, event);
    }

    protected ShortcutListenerDelegate createEnterShortcutListener() {
        return new ShortcutListenerDelegate("dataGridEnter", KeyCode.ENTER, null)
                .withHandler((sender, target) -> {
                    if (sender == componentComposition) {
                        if (isEditorEnabled()) {
                            // Prevent custom actions on Enter if DataGrid editor is enabled
                            // since it's the default shortcut to open editor
                            return;
                        }

                        AppUI ui = (AppUI) componentComposition.getUI();
                        if (!ui.isAccessibleForUser(componentComposition)) {
                            LoggerFactory.getLogger(DataGridImpl.class)
                                    .debug("Ignore click attempt because DataGrid is inaccessible for user");
                            return;
                        }

                        if (enterPressAction != null) {
                            enterPressAction.actionPerform(this);
                        } else {
                            handleDoubleClickAction();
                        }
                    }
                });
    }

    @Override
    public Collection<io.jmix.ui.component.Component> getInnerComponents() {
        Collection<io.jmix.ui.component.Component> components = new ArrayList<>(2);
        if (buttonsPanel != null) {
            components.add(buttonsPanel);
        }
        if (pagination != null) {
            components.add(pagination);
        }
        return components;
    }

    protected void initEditor(Grid<E> component) {
        component.getEditor().setSaveCaption(messages.getMessage("actions.Ok"));
        component.getEditor().setCancelCaption(messages.getMessage("actions.Cancel"));
    }

    protected void initHeaderRows(Grid<E> component) {
        for (int i = 0; i < component.getHeaderRowCount(); i++) {
            com.vaadin.ui.components.grid.HeaderRow headerRow = component.getHeaderRow(i);
            addHeaderRowInternal(headerRow);
        }
    }

    protected void initFooterRows(Grid<E> component) {
        for (int i = 0; i < component.getFooterRowCount(); i++) {
            com.vaadin.ui.components.grid.FooterRow footerRow = component.getFooterRow(i);
            addFooterRowInternal(footerRow);
        }
    }

    protected List<Column<E>> getColumnsOrderInternal() {
        List<Grid.Column<E, ?>> columnsOrder = component.getColumns();
        return columnsOrder.stream()
                .map(this::getColumnByGridColumn)
                .collect(Collectors.toList());
    }

    /**
     * Inserts columns hidden by security permissions (or with visible = false,
     * which means that where is no Grid.Column associated with DatGrid.Column)
     * into a list of visible columns, passed as a parameter, in they original positions.
     *
     * @param visibleColumns the list of DataGrid columns,
     *                       not hidden by security permissions
     * @return a list of all columns in DataGrid
     */
    protected List<Column<E>> restoreColumnsOrder(List<Column<E>> visibleColumns) {
        List<Column<E>> newColumnsOrder = new ArrayList<>(visibleColumns);
        columnsOrder.stream()
                .filter(column -> !visibleColumns.contains(column))
                .forEach(column -> newColumnsOrder.add(columnsOrder.indexOf(column), column));
        return newColumnsOrder;
    }

    /*
     * Is used for DataGridSettingsWorker when applying column settings
     */
    protected void restoreColumnsOrderAndUpdate(List<Column<E>> visibleColumns) {
        columnsOrder = restoreColumnsOrder(visibleColumns);
    }

    protected void initContextMenu() {
        contextMenu = new JmixGridContextMenu<>(component);

        contextMenu.addGridBodyContextMenuListener(event -> {
            if (!component.getSelectedItems().contains(event.getItem())) {
                // In the multi select model 'setSelected' adds item to selected set,
                // but, in case of context click, we want to have a single selected item,
                // if it isn't in a set of already selected items
                if (isMultiSelect()) {
                    component.deselectAll();
                }
                setSelected(event.getItem());
            }
        });
    }

    protected void initSidebarMenu() {
        component.setSelectAllLabel(messages.getMessage("tableColumnSelector.selectAll"));
        component.setDeselectAllLabel(messages.getMessage("tableColumnSelector.deselectAll"));
    }

    protected void refreshActionsState() {
        getActions().forEach(Action::refreshState);
    }

    protected void handleDoubleClickAction() {
        Action action = getItemClickAction();
        if (action == null) {
            action = getEnterAction();
            if (action == null) {
                action = getAction("edit");
                if (action == null) {
                    action = getAction("view");
                }
            }
        }

        if (action != null && action.isEnabled()) {
            action.actionPerform(AbstractDataGrid.this);
        }
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<E>> selectHandler) {
        Consumer<Action.ActionPerformedEvent> actionHandler = event -> {
            Set<E> selected = getSelected();
            selectHandler.accept(selected);
        };

        setEnterPressAction(new BaseAction(LOOKUP_ENTER_PRESSED_ACTION_ID)
                .withHandler(actionHandler));

        setItemClickAction(new BaseAction(LOOKUP_ITEM_CLICK_ACTION_ID)
                .withHandler(actionHandler));

        if (buttonsPanel != null && !buttonsPanel.isAlwaysVisible()) {
            buttonsPanel.setVisible(false);
            setContextMenuEnabled(false);
        }

        setEditorEnabled(false);
    }

    @Override
    public Collection<E> getLookupSelectedItems() {
        return getSelected();
    }

    @Nullable
    protected Action getEnterAction() {
        for (Action action : getActions()) {
            KeyCombination kc = action.getShortcutCombination();
            if (kc != null) {
                if ((kc.getModifiers() == null || kc.getModifiers().length == 0)
                        && kc.getKey() == KeyCombination.Key.ENTER) {
                    return action;
                }
            }
        }
        return null;
    }

    @Override
    public List<Column<E>> getColumns() {
        return Collections.unmodifiableList(columnsOrder);
    }

    @Override
    public List<Column<E>> getVisibleColumns() {
        return columnsOrder.stream()
                .filter(Column::isVisible)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Column<E> getColumn(String id) {
        checkNotNullArgument(id);

        return columns.get(id);
    }

    @Override
    public Column<E> getColumnNN(String id) {
        Column<E> column = getColumn(id);
        if (column == null) {
            throw new IllegalStateException("Unable to find column with id " + id);
        }
        return column;
    }

    @Override
    public void addColumn(Column<E> column) {
        addColumn(column, columnsOrder.size());
    }

    @Override
    public void addColumn(Column<E> column, int index) {
        checkNotNullArgument(column, "Column must be non null");
        if (column.getOwner() != null && column.getOwner() != this) {
            throw new IllegalArgumentException("Can't add column owned by another DataGrid");
        }
        addColumnInternal((ColumnImpl<E>) column, index);
    }

    @Override
    public Column<E> addColumn(String id, @Nullable MetaPropertyPath propertyPath) {
        return addColumn(id, propertyPath, columnsOrder.size());
    }

    @Override
    public Column<E> addColumn(String id, @Nullable MetaPropertyPath propertyPath, int index) {
        ColumnImpl<E> column = createColumn(id, propertyPath, this);
        addColumnInternal(column, index);
        return column;
    }

    protected ColumnImpl<E> createColumn(String id, @Nullable MetaPropertyPath propertyPath, AbstractDataGrid<?, E> owner) {
        return new ColumnImpl<>(id, propertyPath, owner);
    }

    protected void addColumnInternal(ColumnImpl<E> column, int index) {
        Grid.Column<E, ?> gridColumn = component.addColumn(
                new EntityValueProvider<>(column.getPropertyPath()));

        columns.put(column.getId(), column);
        columnsOrder.add(index, column);

        final String caption = StringUtils.capitalize(column.getCaption() != null
                ? column.getCaption()
                : generateColumnCaption(column));
        column.setCaption(caption);

        if (column.getOwner() == null) {
            column.setOwner(this);
        }

        MetaPropertyPath propertyPath = column.getPropertyPath();
        if (propertyPath != null) {
            MetaProperty metaProperty = propertyPath.getMetaProperty();
            MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(propertyPath);
            if (metadataTools.isLob(metaProperty)
                    && !propertyMetaClass.getStore().supportsLobSortingAndFiltering()) {
                column.setSortable(false);
            }
        }

        setupGridColumnProperties(gridColumn, column);

        component.setColumnOrder(getColumnOrder());
    }

    protected String generateColumnCaption(Column<E> column) {
        return column.getPropertyPath() != null
                ? column.getPropertyPath().getMetaProperty().getName()
                : column.getId();
    }

    protected void setupGridColumnProperties(Grid.Column<E, ?> gridColumn, Column<E> column) {
        if (gridColumn.getId() == null) {
            gridColumn.setId(column.getId());
        } else if (!Objects.equals(gridColumn.getId(), column.getId())) {
            log.warn("Trying to copy column settings with mismatched ids. Grid.Column: " +
                    gridColumn.getId() + "; DataGrid.Column: " + column.getId());
        }
        gridColumn.setCaption(column.getCaption());
        gridColumn.setHidingToggleCaption(column.getCollapsingToggleCaption());
        if (column.isWidthAuto()) {
            gridColumn.setWidthUndefined();
        } else {
            gridColumn.setWidth(column.getWidth());
        }
        gridColumn.setExpandRatio(column.getExpandRatio());
        gridColumn.setMinimumWidth(column.getMinimumWidth());
        gridColumn.setMaximumWidth(column.getMaximumWidth());
        gridColumn.setHidden(column.isCollapsed());
        gridColumn.setHidable(column.isCollapsible() && column.getOwner().isColumnsCollapsingAllowed());
        gridColumn.setResizable(column.isResizable());
        gridColumn.setSortable(column.isSortable() && column.getOwner().isSortable());
        gridColumn.setEditable(column.isEditable());

        AppUI current = AppUI.getCurrent();
        if (current != null && current.isTestMode()) {
            addColumnId(gridColumn, column);
        }

        com.vaadin.ui.renderers.Renderer columnRenderer = getColumnRendererImplementation(column);
        if (!Objects.equals(gridColumn.getRenderer(), columnRenderer)) {
            gridColumn.setRenderer(getColumnPresentationValueProvider(column), columnRenderer);
        }

        gridColumn.setStyleGenerator(new CellStyleGeneratorAdapter<>(column));
        gridColumn.setDescriptionGenerator(new CellDescriptionGeneratorAdapter<>(column),
                WrapperUtils.toVaadinContentMode(((ColumnImpl) column).getDescriptionContentMode()));

        ((ColumnImpl<E>) column).setGridColumn(gridColumn);
    }

    @Nullable
    protected ValueProvider getColumnPresentationValueProvider(Column<E> column) {
        Function presentationProvider = column.getPresentationProvider();
        Renderer renderer = column.getRenderer();
        // The following priority is used to determine a value provider:
        // a presentation provider > a renderer's presentation provider > a value provider
        // that always returns its input argument > a default presentation provider
        return presentationProvider != null
                ? (ValueProvider) presentationProvider::apply
                : renderer != null && ((AbstractRenderer) renderer).getPresentationValueProvider() != null
                ? ((AbstractRenderer) renderer).getPresentationValueProvider()
                : renderer != null
                // In case renderer != null and there are no other user specified value providers
                // We use a value provider that always returns its input argument instead of a default
                // value provider as we want to keep the original value type.
                ? ValueProvider.identity()
                : getDefaultPresentationValueProvider(column);
    }

    protected ValueProvider getDefaultPresentationValueProvider(Column<E> column) {
        MetaProperty metaProperty = column.getPropertyPath() != null
                ? column.getPropertyPath().getMetaProperty()
                : null;

        if (metaProperty != null) {
            if (Collection.class.isAssignableFrom(metaProperty.getJavaType())) {
                return new FormatterBasedValueProvider<>(this.applicationContext.getBean(CollectionFormatter.class));
            }
            if (metaProperty.getJavaType() == Boolean.class) {
                return new YesNoIconPresentationValueProvider();
            }
        }

        return new StringPresentationValueProvider(metaProperty, metadataTools);
    }

    protected com.vaadin.ui.renderers.Renderer getColumnRendererImplementation(Column<E> column) {
        Renderer renderer = column.getRenderer();
        return renderer != null
                ? ((AbstractRenderer) renderer).getImplementation()
                : getDefaultRenderer(column);
    }

    protected com.vaadin.ui.renderers.Renderer getDefaultRenderer(Column<E> column) {
        MetaProperty metaProperty = column.getPropertyPath() != null
                ? column.getPropertyPath().getMetaProperty()
                : null;

        return metaProperty != null && metaProperty.getJavaType() == Boolean.class
                ? new com.vaadin.ui.renderers.HtmlRenderer()
                : new com.vaadin.ui.renderers.TextRenderer();
    }

    protected void addColumnId(Grid.Column<E, ?> gridColumn, Column<E> column) {
        component.addColumnId(gridColumn.getState().internalId, column.getId());
    }

    protected void removeColumnId(Grid.Column<E, ?> gridColumn) {
        component.removeColumnId(gridColumn.getId());
    }

    @Override
    public void removeColumn(@Nullable Column<E> column) {
        if (column == null) {
            return;
        }

        component.removeColumn(column.getId());

        columns.remove(column.getId());
        columnsOrder.remove(column);
        columnGenerators.remove(column.getId());

        if (column.getAggregation() != null) {
            component.removeAggregationPropertyId(column.getId());
        }

        ((ColumnImpl<E>) column).setGridColumn(null);
        column.setOwner(null);
    }

    @Override
    public void removeColumn(String id) {
        removeColumn(getColumn(id));
    }

    @Nullable
    @Override
    public DataGridItems<E> getItems() {
        return this.dataBinding != null ? this.dataBinding.getDataGridItems() : null;
    }

    protected DataGridItems<E> getDataGridItemsNN() {
        DataGridItems<E> dataGridItems = getItems();
        if (dataGridItems == null
                || dataGridItems.getState() == BindingState.INACTIVE) {
            throw new IllegalStateException("DataGridItems is not active");
        }
        return dataGridItems;
    }

    @Nullable
    protected EntityDataGridItems<E> getEntityDataGridItems() {
        return getItems() != null ? (EntityDataGridItems<E>) getItems() : null;
    }

    protected EntityDataGridItems<E> getEntityDataGridItemsNN() {
        return (EntityDataGridItems<E>) getDataGridItemsNN();
    }

    @Override
    public void setItems(@Nullable DataGridItems<E> dataGridItems) {
        if (dataGridItems != null && !(dataGridItems instanceof EntityDataGridItems)) {
            throw new IllegalArgumentException("DataGrid supports only EntityDataGridItems");
        }

        if (this.dataBinding != null) {
            this.dataBinding.unbind();
            this.dataBinding = null;

            clearFieldDatasources(null);

            this.component.setDataProvider(createEmptyDataProvider());
        }

        if (dataGridItems != null) {
            // DataGrid supports only EntityDataGridItems
            EntityDataGridItems<E> entityDataGridSource = (EntityDataGridItems<E>) dataGridItems;

            if (this.columns.isEmpty()) {
                setupAutowiredColumns(entityDataGridSource);
            }

            // Bind new datasource
            this.dataBinding = createDataGridDataProvider(dataGridItems);
            this.component.setDataProvider(this.dataBinding);
            updateAggregationRow();

            List<Column<E>> visibleColumnsOrder = getInitialVisibleColumns();

            setVisibleColumns(visibleColumnsOrder);

            for (Column<E> column : visibleColumnsOrder) {
                Grid.Column<E, ?> gridColumn = ((ColumnImpl<E>) column).getGridColumn();
                setupGridColumnProperties(gridColumn, column);
            }

            component.setColumnOrder(getColumnOrder());

            setupPaginationDataSourceProvider();

            if (!canBeSorted(dataGridItems)) {
                setSortable(false);
            }

            // resort data if dataGrid have been sorted before setting items
            if (isSortable()) {
                List<GridSortOrder<E>> sortOrders = component.getSortOrder();
                if (!sortOrders.isEmpty()) {
                    List<GridSortOrder<E>> copiedSortOrders = new ArrayList<>(sortOrders);
                    component.clearSortOrder();
                    component.setSortOrder(copiedSortOrders);
                }
            }

            refreshActionsState();

            setUiTestId(dataGridItems);
        }

        initEmptyState();
    }

    protected void setUiTestId(DataGridItems<E> items) {
        AppUI ui = AppUI.getCurrent();

        if (ui != null && ui.isTestMode()
                && getComponent().getJTestId() == null) {

            String testId = uiTestIdsSupport.getInferredTestId(items, "DataGrid");
            if (testId != null) {
                getComponent().setJTestId(testId);
                componentComposition.setJTestId(testId + "_composition");
            }
        }
    }

    protected DataProvider<E, ?> createEmptyDataProvider() {
        return new ListDataProvider<>(Collections.emptyList());
    }

    protected void setVisibleColumns(List<Column<E>> visibleColumnsOrder) {
        // mark columns hidden by security permissions as visible = false
        // and remove Grid.Column to prevent its property changing
        columnsOrder.stream()
                .filter(column -> !visibleColumnsOrder.contains(column))
                .forEach(column -> {
                    ColumnImpl<E> columnImpl = (ColumnImpl<E>) column;
                    columnImpl.setVisible(false);
                    columnImpl.setGridColumn(null);
                });
    }

    protected Collection<MetaPropertyPath> getAutowiredProperties(EntityDataGridItems<E> entityDataGridSource) {
        if (entityDataGridSource instanceof ContainerDataUnit) {
            CollectionContainer container = ((ContainerDataUnit) entityDataGridSource).getContainer();

            return container.getFetchPlan() != null ?
                    // if a view is specified - use view properties
                    metadataTools.getFetchPlanPropertyPaths(container.getFetchPlan(), container.getEntityMetaClass()) :
                    // otherwise use all properties from meta-class
                    metadataTools.getPropertyPaths(container.getEntityMetaClass());
        }

        if (entityDataGridSource instanceof EmptyDataUnit) {
            return metadataTools.getPropertyPaths(entityDataGridSource.getEntityMetaClass());
        }

        return Collections.emptyList();
    }


    protected void setupAutowiredColumns(EntityDataGridItems<E> entityDataGridSource) {
        Collection<MetaPropertyPath> paths = getAutowiredProperties(entityDataGridSource);

        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany()
                    && !metadataTools.isSystem(property)) {
                String propertyName = property.getName();
                ColumnImpl<E> column = createColumn(propertyName, metaPropertyPath, this);
                MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
                column.setCaption(messageTools.getPropertyCaption(propertyMetaClass, propertyName));

                addColumn(column);
            }
        }
    }

    protected DataGridDataProvider<E> createDataGridDataProvider(DataGridItems<E> dataGridItems) {
        return (dataGridItems instanceof DataGridItems.Sortable)
                ? new SortableDataGridDataProvider<>((DataGridItems.Sortable<E>) dataGridItems, this)
                : new DataGridDataProvider<>(dataGridItems, this);
    }

    @Override
    public void dataGridSourceItemSetChanged(DataGridItems.ItemSetChangeEvent<E> event) {
        clearFieldDatasources(null);

        // #PL-2035, reload selection from ds
        Set<E> selectedItems = getSelected();
        Set<E> newSelection = new HashSet<>();
        for (E item : selectedItems) {
            if (event.getSource().containsItem(item)) {
                newSelection.add(event.getSource().getItem(EntityValues.getId(item)));
            }
        }

        if (event.getSource().getState() == BindingState.ACTIVE
                && event.getSource().getSelectedItem() != null) {
            newSelection.add(event.getSource().getSelectedItem());
        }

        if (newSelection.isEmpty()) {
            setSelected((E) null);
        } else {
            // Workaround for the MultiSelect model.
            // Set the selected items only if the previous selection is different
            // Otherwise, the DataGrid rows will display the values before editing
            if (isMultiSelect() && !selectedItems.equals(newSelection)) {
                setSelectedItems(newSelection);
            }
        }

        updateAggregationRow();

        refreshActionsState();
    }

    @Override
    public void dataGridSourcePropertyValueChanged(DataGridItems.ValueChangeEvent<E> event) {
        updateAggregationRow();

        refreshActionsState();
    }

    @Override
    public void dataGridSourceStateChanged(DataGridItems.StateChangeEvent event) {
        refreshActionsState();
    }

    @Override
    public void dataGridSourceSelectedItemChanged(DataGridItems.SelectedItemChangeEvent<E> event) {
        refreshActionsState();
    }

    protected String[] getColumnOrder() {
        return columnsOrder.stream()
                .filter(Column::isVisible)
                .map(Column::getId)
                .toArray(String[]::new);
    }

    @Nullable
    @Override
    public String getCaption() {
        return getComposition().getCaption();
    }

    @Override
    public void setCaption(@Nullable String caption) {
        getComposition().setCaption(caption);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return ((com.vaadin.ui.AbstractComponent) getComposition()).isCaptionAsHtml();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        ((com.vaadin.ui.AbstractComponent) getComposition()).setCaptionAsHtml(captionAsHtml);
    }

    @Override
    public boolean isTextSelectionEnabled() {
        return textSelectionEnabled;
    }

    @Override
    public void setTextSelectionEnabled(boolean textSelectionEnabled) {
        if (this.textSelectionEnabled != textSelectionEnabled) {
            this.textSelectionEnabled = textSelectionEnabled;

            if (textSelectionEnabled) {
                if (!internalStyles.contains(TEXT_SELECTION_ENABLED_STYLE)) {
                    internalStyles.add(TEXT_SELECTION_ENABLED_STYLE);
                }

                componentComposition.addStyleName(TEXT_SELECTION_ENABLED_STYLE);
            } else {
                internalStyles.remove(TEXT_SELECTION_ENABLED_STYLE);
                componentComposition.removeStyleName(TEXT_SELECTION_ENABLED_STYLE);
            }
        }
    }

    @Override
    public boolean isColumnReorderingAllowed() {
        return component.isColumnReorderingAllowed();
    }

    @Override
    public void setColumnReorderingAllowed(boolean columnReorderingAllowed) {
        component.setColumnReorderingAllowed(columnReorderingAllowed);
    }

    @Override
    public boolean isSortable() {
        return sortable;
    }

    @Override
    public void setSortable(boolean sortable) {
        this.sortable = sortable
                && (getItems() == null
                || canBeSorted(getItems()));
        for (Column<E> column : getColumns()) {
            ((ColumnImpl<E>) column).updateSortable();
        }
    }

    @Override
    public boolean isColumnsCollapsingAllowed() {
        return columnsCollapsingAllowed;
    }

    @Override
    public void setColumnsCollapsingAllowed(boolean columnsCollapsingAllowed) {
        this.columnsCollapsingAllowed = columnsCollapsingAllowed;
        for (Column<E> column : getColumns()) {
            ((ColumnImpl<E>) column).updateCollapsible();
        }
    }

    @Override
    public boolean isEditorEnabled() {
        return component.getEditor().isEnabled();
    }

    @Override
    public void setEditorEnabled(boolean isEnabled) {
        component.getEditor().setEnabled(isEnabled);

        enableCrossFieldValidationHandling(editorCrossFieldValidate);
    }

    @Override
    public boolean isEditorBuffered() {
        return component.getEditor().isBuffered();
    }

    @Override
    public void setEditorBuffered(boolean editorBuffered) {
        component.getEditor().setBuffered(editorBuffered);
    }

    @Override
    public String getEditorSaveCaption() {
        return component.getEditor().getSaveCaption();
    }

    @Override
    public void setEditorSaveCaption(String saveCaption) {
        component.getEditor().setSaveCaption(saveCaption);
    }

    @Override
    public String getEditorCancelCaption() {
        return component.getEditor().getCancelCaption();
    }

    @Override
    public void setEditorCancelCaption(String cancelCaption) {
        component.getEditor().setCancelCaption(cancelCaption);
    }

    @Override
    public E getEditedItem() {
        return component.getEditor() instanceof JmixEditorImpl
                ? ((JmixEditorImpl<E>) component.getEditor()).getBean()
                : component.getEditor().getBinder().getBean();
    }

    @Override
    public boolean isEditorActive() {
        return component.getEditor().isOpen();
    }

    @Override
    public void closeEditor() {
        if (isEditorActive()) {
            component.getEditor().cancel();
        }
    }

    @Override
    public void edit(E item) {
        checkNotNullArgument(item, "Entity must be non null");

        DataGridItems<E> dataGridItems = getItems();
        if (dataGridItems == null
                || dataGridItems.getState() == BindingState.INACTIVE) {
            return;
        }

        if (!dataGridItems.containsItem(item)) {
            throw new IllegalArgumentException("Datasource doesn't contain item");
        }

        editItemInternal(item);
    }

    protected void editItemInternal(E item) {
        int rowIndex = getDataGridItemsNN().indexOfItem(item);
        component.getEditor().editRow(rowIndex);
    }

    @SuppressWarnings("ConstantConditions")
    protected Map<String, Field> convertToJmixFields(Map<Grid.Column<E, ?>, Component> columnFieldMap) {
        return columnFieldMap.entrySet().stream()
                .filter(entry ->
                        getColumnByGridColumn(entry.getKey()) != null)
                .collect(Collectors.toMap(
                        entry -> getColumnByGridColumn(entry.getKey()).getId(),
                        entry -> ((DataGridEditorCustomField) entry.getValue()).getField())
                );
    }

    @Override
    public Subscription addEditorOpenListener(Consumer<EditorOpenEvent<E>> listener) {
        if (editorOpenListener == null) {
            editorOpenListener = component.getEditor().addOpenListener(this::onEditorOpen);
        }

        getEventHub().subscribe(EditorOpenEvent.class, (Consumer) listener);

        return () -> internalRemoveEditorOpenListener(listener);
    }

    protected void onEditorOpen(com.vaadin.ui.components.grid.EditorOpenEvent<E> editorOpenEvent) {
        //noinspection unchecked
        JmixEditorOpenEvent<E> event = ((JmixEditorOpenEvent) editorOpenEvent);
        Map<String, Field> fields = convertToJmixFields(event.getColumnFieldMap());

        EditorOpenEvent<E> e = new EditorOpenEvent<>(this, event.getBean(), fields);
        publish(EditorOpenEvent.class, e);
    }

    protected void internalRemoveEditorOpenListener(Consumer<EditorOpenEvent<E>> listener) {
        unsubscribe(EditorOpenEvent.class, (Consumer) listener);

        if (!hasSubscriptions(EditorOpenEvent.class)) {
            editorOpenListener.remove();
            editorOpenListener = null;
        }
    }

    @Override
    public Subscription addEditorCloseListener(Consumer<EditorCloseEvent<E>> listener) {
        if (editorCancelListener == null) {
            editorCancelListener = component.getEditor().addCancelListener(this::onEditorCancel);
        }

        getEventHub().subscribe(EditorCloseEvent.class, (Consumer) listener);

        return () -> internalRemoveEditorCloseListener(listener);
    }

    protected void onEditorCancel(EditorCancelEvent<E> cancelEvent) {
        //noinspection unchecked
        JmixEditorCancelEvent<E> event = ((JmixEditorCancelEvent) cancelEvent);
        Map<String, Field> fields = convertToJmixFields(event.getColumnFieldMap());

        EditorCloseEvent<E> e = new EditorCloseEvent<>(this, event.getBean(), fields, event.isCancelled());
        publish(EditorCloseEvent.class, e);
    }

    protected void internalRemoveEditorCloseListener(Consumer<EditorCloseEvent<E>> listener) {
        unsubscribe(EditorCloseEvent.class, (Consumer) listener);

        if (!hasSubscriptions(EditorCloseEvent.class)) {
            editorCancelListener.remove();
            editorCancelListener = null;
        }
    }

    @Override
    public Subscription addEditorPreCommitListener(Consumer<EditorPreCommitEvent<E>> listener) {
        if (editorBeforeSaveListener == null) {
            //noinspection unchecked
            JmixEditorImpl<E> editor = (JmixEditorImpl) component.getEditor();
            editorBeforeSaveListener = editor.addBeforeSaveListener(this::onEditorBeforeSave);
        }

        getEventHub().subscribe(EditorPreCommitEvent.class, (Consumer) listener);

        return () -> internalRemoveEditorPreCommitListener(listener);
    }

    protected void onEditorBeforeSave(JmixEditorBeforeSaveEvent<E> event) {
        Map<String, Field> fields = convertToJmixFields(event.getColumnFieldMap());

        EditorPreCommitEvent<E> e = new EditorPreCommitEvent<>(this, event.getBean(), fields);
        publish(EditorPreCommitEvent.class, e);
    }

    protected void internalRemoveEditorPreCommitListener(Consumer<EditorPreCommitEvent<E>> listener) {
        unsubscribe(EditorPreCommitEvent.class, (Consumer) listener);

        if (!hasSubscriptions(EditorPreCommitEvent.class)) {
            editorBeforeSaveListener.remove();
            editorBeforeSaveListener = null;
        }
    }

    @Override
    public Subscription addEditorPostCommitListener(Consumer<EditorPostCommitEvent<E>> listener) {
        if (editorSaveListener == null) {
            editorSaveListener = component.getEditor().addSaveListener(this::onEditorSave);
        }

        getEventHub().subscribe(EditorPostCommitEvent.class, (Consumer) listener);

        return () -> internalRemoveEditorPostCommitListener(listener);
    }

    protected void onEditorSave(EditorSaveEvent<E> saveEvent) {
        //noinspection unchecked
        JmixEditorSaveEvent<E> event = ((JmixEditorSaveEvent) saveEvent);
        Map<String, Field> fields = convertToJmixFields(event.getColumnFieldMap());

        EditorPostCommitEvent<E> e = new EditorPostCommitEvent<>(this, event.getBean(), fields);
        publish(EditorPostCommitEvent.class, e);
    }

    protected void internalRemoveEditorPostCommitListener(Consumer<EditorPostCommitEvent<E>> listener) {
        unsubscribe(EditorPostCommitEvent.class, (Consumer) listener);

        if (!hasSubscriptions(EditorPostCommitEvent.class)) {
            editorSaveListener.remove();
            editorSaveListener = null;
        }
    }

    @Override
    public void setEditorCrossFieldValidate(boolean validate) {
        this.editorCrossFieldValidate = validate;

        enableCrossFieldValidationHandling(validate);
    }

    @Override
    public boolean isEditorCrossFieldValidate() {
        return editorCrossFieldValidate;
    }

    protected InstanceContainer<E> createInstanceContainer(E item) {
        if (itemDatasources == null) {
            itemDatasources = new WeakHashMap<>();
        }

        Object container = itemDatasources.get(item);
        if (container instanceof InstanceContainer) {
            //noinspection unchecked
            return (InstanceContainer<E>) container;
        }

        EntityDataGridItems<E> items = getEntityDataGridItemsNN();
        DataComponents factory = this.applicationContext.getBean(DataComponents.class);
        FetchPlanRepository viewRepository = this.applicationContext.getBean(FetchPlanRepository.class);
        MetaClass metaClass = items.getEntityMetaClass();

        InstanceContainer<E> instanceContainer;
        if (metaClass instanceof KeyValueMetaClass) {
            //noinspection unchecked
            instanceContainer = (InstanceContainer<E>) factory.createKeyValueContainer(metaClass);
        } else {
            instanceContainer = factory.createInstanceContainer(metaClass.getJavaClass());
        }
        instanceContainer.setFetchPlan(viewRepository.getFetchPlan(metaClass, FetchPlan.LOCAL));
        instanceContainer.setItem(item);

        itemDatasources.put(item, instanceContainer);

        return instanceContainer;
    }

    protected void clearFieldDatasources(@Nullable E item) {
        if (itemDatasources == null) {
            return;
        }

        if (item != null) {
            if (isEditorActive()
                    && !isEditorBuffered()
                    && item.equals(getEditedItem())) {
                return;
            }

            Object removed = itemDatasources.remove(item);
            if (removed != null) {
                detachItemContainer(removed);
            }
        } else {
            // detach instance containers from entities explicitly
            for (Map.Entry<E, Object> entry : itemDatasources.entrySet()) {
                detachItemContainer(entry.getValue());
            }

            itemDatasources.clear();
        }
    }

    @SuppressWarnings("unchecked")
    protected void detachItemContainer(Object container) {
        if (container instanceof InstanceContainer) {
            InstanceContainer<E> instanceContainer = (InstanceContainer<E>) container;
            instanceContainer.setItem(null);
        }
    }

    protected ValueSourceProvider createValueSourceProvider(E item) {
        InstanceContainer<E> instanceContainer = createInstanceContainer(item);
        return new ContainerValueSourceProvider<>(instanceContainer);
    }

    protected static class DataGridEditorFieldFactoryAdapter<E> implements JmixGridEditorFieldFactory<E> {

        protected AbstractDataGrid<?, E> dataGrid;
        protected DataGridEditorFieldFactory fieldFactory;

        public DataGridEditorFieldFactoryAdapter(AbstractDataGrid<?, E> dataGrid,
                                                 DataGridEditorFieldFactory fieldFactory) {
            this.dataGrid = dataGrid;
            this.fieldFactory = fieldFactory;
        }

        @Nullable
        @Override
        public JmixEditorField<?> createField(E bean, Grid.Column<E, ?> gridColumn) {
            ColumnImpl<E> column = dataGrid.getColumnByGridColumn(gridColumn);
            if (column == null || !column.isShouldBeEditable()) {
                return null;
            }

            Field<?> columnComponent;
            if (column.getEditFieldGenerator() != null) {
                ValueSourceProvider valueSourceProvider = dataGrid.createValueSourceProvider(bean);
                EditorFieldGenerationContext<E> context = new EditorFieldGenerationContext<>(bean, valueSourceProvider);
                columnComponent = column.getEditFieldGenerator().apply(context);
            } else {
                columnComponent = createField(column, bean);
            }

            columnComponent.setParent(dataGrid);
            columnComponent.setFrame(dataGrid.getFrame());

            return createCustomField(columnComponent);
        }

        protected Field<?> createField(ColumnImpl<E> column, E bean) {
            String fieldPropertyId = String.valueOf(column.getPropertyId());
            InstanceContainer<E> container = dataGrid.createInstanceContainer(bean);
            ContainerValueSource<E, Object> valueSource = new ContainerValueSource<>(container, fieldPropertyId);
            return fieldFactory.createField(valueSource, fieldPropertyId);
        }

        protected JmixEditorField<?> createCustomField(Field<?> columnComponent) {
            if (!(columnComponent instanceof Buffered)) {
                throw new IllegalArgumentException("Editor field must implement " +
                        "io.jmix.ui.component.Buffered");
            }

            Component content = ComponentsHelper.getComposition(columnComponent);

            JmixEditorField<?> wrapper = new DataGridEditorCustomField(columnComponent) {
                @Override
                protected Component initContent() {
                    return content;
                }
            };

            if (content instanceof Component.Focusable) {
                wrapper.setFocusDelegate((Component.Focusable) content);
            }

            wrapper.setReadOnly(!columnComponent.isEditable());
            wrapper.setRequiredIndicatorVisible(columnComponent.isRequired());

            columnComponent.addValueChangeListener(event -> wrapper.markAsDirty());

            return wrapper;
        }
    }

    protected static abstract class DataGridEditorCustomField<T> extends JmixEditorField<T> {

        protected Field<T> columnComponent;

        public DataGridEditorCustomField(Field<T> columnComponent) {
            this.columnComponent = columnComponent;
            initComponent(this.columnComponent);
        }

        protected void initComponent(Field<T> columnComponent) {
            columnComponent.addValueChangeListener(event ->
                    fireEvent(createValueChange(event.getPrevValue(), event.isUserOriginated())));
        }

        protected Field<T> getField() {
            return columnComponent;
        }

        @Override
        protected void doSetValue(T value) {
            columnComponent.setValue(value);
        }

        @Nullable
        @Override
        public T getValue() {
            return columnComponent.getValue();
        }

        @Override
        public boolean isBuffered() {
            return ((Buffered) columnComponent).isBuffered();
        }

        @Override
        public void setBuffered(boolean buffered) {
            ((Buffered) columnComponent).setBuffered(buffered);
        }

        @Override
        public void commit() {
            ((Buffered) columnComponent).commit();
        }

        @Override
        public ValidationResult validate() {
            try {
                columnComponent.validate();
                return ValidationResult.ok();
            } catch (ValidationException e) {
                return ValidationResult.error(e.getDetailsMessage());
            }
        }

        @Override
        public void discard() {
            ((Buffered) columnComponent).discard();
        }

        @Override
        public boolean isModified() {
            return ((Buffered) columnComponent).isModified();
        }

        @Override
        public void setWidth(float width, Sizeable.Unit unit) {
            super.setWidth(width, unit);

            if (getContent() != null) {
                if (width < 0) {
                    getContent().setWidthUndefined();
                } else {
                    getContent().setWidth(100, Sizeable.Unit.PERCENTAGE);
                }
            }
        }

        @Override
        public void setHeight(float height, Sizeable.Unit unit) {
            super.setHeight(height, unit);

            if (getContent() != null) {
                if (height < 0) {
                    getContent().setHeightUndefined();
                } else {
                    getContent().setHeight(100, Sizeable.Unit.PERCENTAGE);
                }
            }
        }
    }

    @Override
    public boolean isHeaderVisible() {
        return component.isHeaderVisible();
    }

    @Override
    public void setHeaderVisible(boolean headerVisible) {
        component.setHeaderVisible(headerVisible);
    }

    @Override
    public boolean isFooterVisible() {
        return component.isFooterVisible();
    }

    @Override
    public void setFooterVisible(boolean footerVisible) {
        component.setFooterVisible(footerVisible);
    }

    @Override
    public double getBodyRowHeight() {
        return component.getBodyRowHeight();
    }

    @Override
    public void setBodyRowHeight(double rowHeight) {
        component.setBodyRowHeight(rowHeight);
    }

    @Override
    public double getHeaderRowHeight() {
        return component.getHeaderRowHeight();
    }

    @Override
    public void setHeaderRowHeight(double rowHeight) {
        component.setHeaderRowHeight(rowHeight);
    }

    @Override
    public double getFooterRowHeight() {
        return component.getFooterRowHeight();
    }

    @Override
    public void setFooterRowHeight(double rowHeight) {
        component.setFooterRowHeight(rowHeight);
    }

    @Override
    public boolean isContextMenuEnabled() {
        return contextMenu.isEnabled();
    }

    @Override
    public void setContextMenuEnabled(boolean contextMenuEnabled) {
        contextMenu.setEnabled(contextMenuEnabled);
    }

    @Override
    public ColumnResizeMode getColumnResizeMode() {
        return WrapperUtils.convertToDataGridColumnResizeMode(component.getColumnResizeMode());
    }

    @Override
    public void setColumnResizeMode(ColumnResizeMode mode) {
        component.setColumnResizeMode(WrapperUtils.convertToGridColumnResizeMode(mode));
    }

    @Override
    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    @Override
    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        switch (selectionMode) {
            case SINGLE:
                component.setGridSelectionModel(new JmixSingleSelectionModel<>());
                break;
            case MULTI:
                component.setGridSelectionModel(new JmixMultiSelectionModel<>());
                break;
            case MULTI_CHECK:
                component.setGridSelectionModel(new JmixMultiCheckSelectionModel<>());
                break;
            case NONE:
                component.setSelectionMode(Grid.SelectionMode.NONE);
                return;
        }

        // Every time we change selection mode, the new selection model is set,
        // so we need to add selection listener again.
        component.getSelectionModel().addSelectionListener(this::onSelectionChange);
    }

    @Override
    public boolean isMultiSelect() {
        return SelectionMode.MULTI.equals(selectionMode)
                || SelectionMode.MULTI_CHECK.equals(selectionMode);
    }

    @Nullable
    @Override
    public E getSingleSelected() {
        return component.getSelectionModel()
                .getFirstSelectedItem()
                .orElse(null);
    }

    @Override
    public Set<E> getSelected() {
        return component.getSelectedItems();
    }

    @Override
    public void setSelected(@Nullable E item) {
        if (SelectionMode.NONE.equals(getSelectionMode())) {
            return;
        }

        if (item == null) {
            component.deselectAll();
        } else {
            setSelected(Collections.singletonList(item));
        }
    }

    @Override
    public void setSelected(Collection<E> items) {
        DataGridItems<E> dataGridItems = getDataGridItemsNN();

        for (E item : items) {
            if (!dataGridItems.containsItem(item)) {
                throw new IllegalStateException("Datasource doesn't contain items");
            }
        }

        setSelectedItems(items);
    }

    @SuppressWarnings("unchecked")
    protected void setSelectedItems(Collection<E> items) {
        switch (selectionMode) {
            case SINGLE:
                if (items.size() > 0) {
                    E item = items.iterator().next();
                    component.getSelectionModel().select(item);
                } else {
                    component.deselectAll();
                }
                break;
            case MULTI:
            case MULTI_CHECK:
                component.deselectAll();
                ((SelectionModel.Multi) component.getSelectionModel()).selectItems(items.toArray());
                break;

            default:
                throw new UnsupportedOperationException("Unsupported selection mode");
        }
    }

    @Override
    public void selectAll() {
        if (isMultiSelect()) {
            ((SelectionModel.Multi) component.getSelectionModel()).selectAll();
        }
    }

    @Override
    public void deselect(E item) {
        checkNotNullArgument(item);

        component.deselect(item);
    }

    @Override
    public void deselectAll() {
        component.deselectAll();
    }

    @Override
    public void sort(String columnId, SortDirection direction) {
        ColumnImpl<E> column = (ColumnImpl<E>) getColumnNN(columnId);
        component.sort(column.getGridColumn(), WrapperUtils.convertToGridSortDirection(direction));
    }

    @Override
    public List<SortOrder> getSortOrder() {
        return convertToDataGridSortOrder(component.getSortOrder());
    }

    protected List<SortOrder> convertToDataGridSortOrder(List<GridSortOrder<E>> gridSortOrder) {
        if (CollectionUtils.isEmpty(gridSortOrder)) {
            return Collections.emptyList();
        }

        return gridSortOrder.stream()
                .map(sortOrder -> {
                    Column column = getColumnByGridColumn(sortOrder.getSorted());
                    return new SortOrder(column != null ? column.getId() : null,
                            WrapperUtils.convertToDataGridSortDirection(sortOrder.getDirection()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addAction(Action action) {
        int index = findActionById(actionList, action.getId());
        if (index < 0) {
            index = actionList.size();
        }

        addAction(action, index);
    }

    @Override
    public void addAction(Action action, int index) {
        checkNotNullArgument(action, "Action must be non null");

        int oldIndex = findActionById(actionList, action.getId());
        if (oldIndex >= 0) {
            removeAction(actionList.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        if (StringUtils.isNotEmpty(action.getCaption())) {
            ActionMenuItemWrapper menuItemWrapper = createContextMenuItem(action);
            menuItemWrapper.setAction(action);
            contextMenuItems.add(menuItemWrapper);
        }

        actionList.add(index, action);
        shortcutsDelegate.addAction(null, action);
        attachAction(action);
        actionsPermissions.apply(action);
    }

    protected ActionMenuItemWrapper createContextMenuItem(Action action) {
        MenuItem menuItem = contextMenu.addItem(action.getCaption(), null);
        menuItem.setStyleName("jmix-cm-item");

        return new ActionMenuItemWrapper(menuItem, showIconsForPopupMenuActions, iconResolver) {
            @Override
            public void performAction(Action action) {
                action.actionPerform(AbstractDataGrid.this);
            }
        };
    }

    protected void attachAction(Action action) {
        if (action instanceof Action.HasTarget) {
            ((Action.HasTarget) action).setTarget(this);
        }

        action.refreshState();
    }

    @Override
    public void removeAction(Action action) {
        if (actionList.remove(action)) {
            ActionMenuItemWrapper menuItemWrapper = null;
            for (ActionMenuItemWrapper menuItem : contextMenuItems) {
                if (menuItem.getAction() == action) {
                    menuItemWrapper = menuItem;
                    break;
                }
            }

            if (menuItemWrapper != null) {
                menuItemWrapper.setAction(null);
                contextMenu.removeItem(menuItemWrapper.getMenuItem());
            }

            shortcutsDelegate.removeAction(action);
        }
    }

    @Override
    public void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    @Override
    public void removeAllActions() {
        for (Action action : actionList.toArray(new Action[0])) {
            removeAction(action);
        }
    }

    @Override
    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actionList);
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        for (Action action : getActions()) {
            if (Objects.equals(action.getId(), id)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public ActionsPermissions getActionsPermissions() {
        return actionsPermissions;
    }

    protected List<Column<E>> getInitialVisibleColumns() {
        return columnsOrder.stream()
                .filter(column -> {
                    MetaPropertyPath propertyPath = column.getPropertyPath();
                    if (propertyPath != null) {
                        UiEntityAttributeContext attributeContext =
                                new UiEntityAttributeContext(propertyPath);
                        accessManager.applyRegisteredConstraints(attributeContext);
                        return attributeContext.canView();
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Component getComposition() {
        return componentComposition;
    }

    @Override
    public int getFrozenColumnCount() {
        return component.getFrozenColumnCount();
    }

    @Override
    public void setFrozenColumnCount(int numberOfColumns) {
        component.setFrozenColumnCount(numberOfColumns);
    }

    @Override
    public void scrollTo(E item) {
        scrollTo(item, ScrollDestination.ANY);
    }

    @Override
    public void scrollTo(E item, ScrollDestination destination) {
        Preconditions.checkNotNullArgument(item);
        Preconditions.checkNotNullArgument(destination);

        DataGridItems<E> dataGridItems = getDataGridItemsNN();
        if (!dataGridItems.containsItem(item)) {
            throw new IllegalArgumentException("Unable to find item in DataGrid");
        }

        int rowIndex = dataGridItems.indexOfItem(item);
        component.scrollTo(rowIndex, WrapperUtils.convertToGridScrollDestination(destination));
    }

    @Override
    public void scrollToStart() {
        component.scrollToStart();
    }

    @Override
    public void scrollToEnd() {
        component.scrollToEnd();
    }

    @Override
    public void repaint() {
        component.repaint();
    }

    protected boolean canBeSorted(@Nullable DataGridItems<E> dataGridItems) {
        return dataGridItems instanceof DataGridItems.Sortable;
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        AppUI ui = AppUI.getCurrent();
        if (id != null && ui != null) {
            componentComposition.setId(ui.getTestIdManager().getTestId(id + "_composition"));
        }
    }

    @Override
    public void setId(@Nullable String id) {
        super.setId(id);

        AppUI ui = AppUI.getCurrent();
        if (id != null
                && ui != null
                && ui.isTestMode()) {
            componentComposition.setJTestId(id + "_composition");
        }
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        internalStyles.forEach(internalStyle ->
                componentComposition.addStyleName(internalStyle));
    }

    @Nullable
    @Override
    public ButtonsPanel getButtonsPanel() {
        return buttonsPanel;
    }

    @Override
    public void setButtonsPanel(@Nullable ButtonsPanel panel) {
        if (buttonsPanel != null && topPanel != null) {
            topPanel.removeComponent(buttonsPanel.unwrap(Component.class));
            buttonsPanel.setParent(null);
        }
        buttonsPanel = panel;
        if (panel != null) {
            if (panel.getParent() != null && panel.getParent() != this) {
                throw new IllegalStateException("Component already has parent");
            }

            if (topPanel == null) {
                topPanel = createTopPanel();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel);
            }
            Component bp = panel.unwrap(Component.class);
            topPanel.addComponent(bp);

            if (panel instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) panel).addVisibilityChangeListener(event ->
                        updateCompositionStylesTopPanelVisible()
                );
            }
            panel.setParent(this);
        }

        updateCompositionStylesTopPanelVisible();
    }

    @Nullable
    @Override
    public PaginationComponent getPagination() {
        return pagination;
    }

    @Override
    public void setPagination(@Nullable PaginationComponent pagination) {
        if (pagination != null && !(pagination instanceof SimplePagination)) {
            throw new IllegalStateException(String.format("DataGrid does not support pagination type `%s`",
                    pagination.getClass().getCanonicalName()));
        }

        if (this.pagination != null && topPanel != null) {
            topPanel.removeComponent(this.pagination.unwrap(Component.class));
            this.pagination.setParent(null);
        }
        this.pagination = pagination;
        if (pagination != null) {
            if (pagination.getParent() != null && pagination.getParent() != this) {
                throw new IllegalStateException("Component already has parent");
            }

            if (topPanel == null) {
                topPanel = createTopPanel();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel);
            }
            pagination.setWidthAuto();
            Component rc = pagination.unwrap(Component.class);
            topPanel.addComponent(rc);

            if (pagination instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) pagination).addVisibilityChangeListener(event ->
                        updateCompositionStylesTopPanelVisible()
                );
            }

            pagination.addAfterRefreshListener(this::onPaginationAfterRefresh);
        }

        updateCompositionStylesTopPanelVisible();
    }

    protected void onPaginationAfterRefresh(PaginationComponent.AfterRefreshEvent event) {
        scrollToStart();
    }

    public void setupPaginationDataSourceProvider() {
        if (pagination == null) {
            return;
        }

        DataUnit items = getItems();

        PaginationDataBinder provider;
        if (items instanceof ContainerDataUnit) {
            provider = applicationContext.getBean(PaginationDataUnitBinder.class, items);
        } else if (items instanceof EmptyDataUnit
                && items instanceof EntityDataUnit) {
            provider = new PaginationEmptyBinder(((EntityDataUnit) items).getEntityMetaClass());
        } else {
            throw new IllegalStateException("Unsupported data unit type: " + items);
        }

        pagination.setDataBinder(provider);
    }

    protected CssLayout createTopPanel() {
        CssLayout topPanel = new CssLayout();
        topPanel.setStyleName("jmix-data-grid-top");
        return topPanel;
    }

    // if buttons panel becomes hidden we need to set top panel height to 0
    protected void updateCompositionStylesTopPanelVisible() {
        if (topPanel != null) {
            boolean hasChildren = topPanel.getComponentCount() > 0;
            boolean anyChildVisible = false;
            for (Component childComponent : topPanel) {
                if (childComponent.isVisible()) {
                    anyChildVisible = true;
                    break;
                }
            }
            boolean topPanelVisible = hasChildren && anyChildVisible;

            if (!topPanelVisible) {
                componentComposition.removeStyleName(HAS_TOP_PANEL_STYLE_NAME);

                internalStyles.remove(HAS_TOP_PANEL_STYLE_NAME);
            } else {
                componentComposition.addStyleName(HAS_TOP_PANEL_STYLE_NAME);

                if (!internalStyles.contains(HAS_TOP_PANEL_STYLE_NAME)) {
                    internalStyles.add(HAS_TOP_PANEL_STYLE_NAME);
                }
            }
        }
    }

    @Nullable
    @Override
    public Action getEnterPressAction() {
        return enterPressAction;
    }

    @Override
    public void setEnterPressAction(@Nullable Action action) {
        enterPressAction = action;
    }

    @Nullable
    @Override
    public Action getItemClickAction() {
        return itemClickAction;
    }

    @Override
    public void setItemClickAction(@Nullable Action action) {
        if (itemClickAction != null) {
            removeAction(itemClickAction);
        }
        itemClickAction = action;
        if (!getActions().contains(action)) {
            addAction(action);
        }
    }

    @Nullable
    protected ColumnImpl<E> getColumnByGridColumn(Grid.Column<E, ?> gridColumn) {
        for (Column<E> column : getColumns()) {
            ColumnImpl<E> columnImpl = (ColumnImpl<E>) column;
            if (columnImpl.getGridColumn() == gridColumn) {
                return columnImpl;
            }
        }
        return null;
    }

    @Nullable
    protected Column<E> getColumnById(Object id) {
        for (Column<E> column : getColumns()) {
            String columnId = column.getId();
            if (Objects.equals(columnId, id)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public void addRowStyleProvider(Function<? super E, String> styleProvider) {
        if (this.rowStyleProviders == null) {
            this.rowStyleProviders = new LinkedList<>();
        }

        if (!this.rowStyleProviders.contains(styleProvider)) {
            this.rowStyleProviders.add(styleProvider);

            repaint();
        }
    }

    @Override
    public void removeRowStyleProvider(Function<? super E, String> styleProvider) {
        if (this.rowStyleProviders != null) {
            if (this.rowStyleProviders.remove(styleProvider)) {
                repaint();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public Function<E, String> getRowDescriptionProvider() {
        return (Function<E, String>) rowDescriptionProvider;
    }

    @Override
    public void setRowDescriptionProvider(@Nullable Function<? super E, String> provider) {
        setRowDescriptionProvider(provider, ContentMode.PREFORMATTED);
    }

    @Override
    public void setRowDescriptionProvider(@Nullable Function<? super E, String> provider, ContentMode contentMode) {
        this.rowDescriptionProvider = provider;

        if (provider != null) {
            component.setDescriptionGenerator(this::getRowDescription,
                    WrapperUtils.toVaadinContentMode(contentMode));
        } else {
            component.setDescriptionGenerator(null);
        }
    }

    @Nullable
    protected String getRowDescription(E item) {
        String rowDescription = rowDescriptionProvider.apply(item);
        return WrapperUtils.toContentMode(component.getRowDescriptionContentMode()) == ContentMode.HTML
                ? sanitize(rowDescription)
                : rowDescription;
    }

    @Override
    public Column<E> addGeneratedColumn(String columnId, Function<ColumnGeneratorEvent<E>, ?> generator) {
        return addGeneratedColumn(columnId, generator, columnsOrder.size());
    }

    @Override
    public Column<E> addGeneratedColumn(String columnId, Function<ColumnGeneratorEvent<E>, ?> generator, int index) {
        checkNotNullArgument(columnId, "columnId is null");
        checkNotNullArgument(generator, "generator is null for column id '%s'", columnId);

        Column<E> existingColumn = getColumn(columnId);
        if (existingColumn != null) {
            index = columnsOrder.indexOf(existingColumn);
            removeColumn(existingColumn);
        }

        Grid.Column<E, Object> generatedColumn =
                component.addColumn(createGeneratedColumnValueProvider(columnId, generator));

        // Pass propertyPath from the existing column to support sorting
        ColumnImpl<E> column = createColumn(columnId,
                existingColumn != null ? existingColumn.getPropertyPath() : null,
                this);
        if (existingColumn != null) {
            copyColumnProperties(column, existingColumn);
        } else {
            column.setCaption(columnId);
        }
        column.setGenerated(true);

        columns.put(column.getId(), column);
        columnsOrder.add(index, column);
        columnGenerators.put(column.getId(), generator);

        setupGridColumnProperties(generatedColumn, column);

        component.setColumnOrder(getColumnOrder());

        return column;
    }

    protected ValueProvider<E, Object> createGeneratedColumnValueProvider(String columnId,
                                                                          Function<ColumnGeneratorEvent<E>, ?> generator) {
        return (ValueProvider<E, Object>) item -> {
            ColumnGeneratorEvent<E> event = new ColumnGeneratorEvent<>(AbstractDataGrid.this,
                    item, columnId, this::createInstanceContainer);
            return generator.apply(event);
        };
    }

    @Nullable
    @Override
    public Function<ColumnGeneratorEvent<E>, ?> getColumnGenerator(String columnId) {
        return columnGenerators.get(columnId);
    }

    protected void copyColumnProperties(Column<E> column, Column<E> existingColumn) {
        column.setCaption(existingColumn.getCaption());
        column.setVisible(existingColumn.isVisible());
        column.setCollapsed(existingColumn.isCollapsed());
        column.setCollapsible(existingColumn.isCollapsible());
        column.setCollapsingToggleCaption(existingColumn.getCollapsingToggleCaption());
        column.setMinimumWidth(existingColumn.getMinimumWidth());
        column.setMaximumWidth(existingColumn.getMaximumWidth());
        column.setWidth(existingColumn.getWidth());
        column.setExpandRatio(existingColumn.getExpandRatio());
        column.setResizable(existingColumn.isResizable());
        column.setStyleProvider(existingColumn.getStyleProvider());
        column.setDescriptionProvider(existingColumn.getDescriptionProvider(),
                ((ColumnImpl) existingColumn).getDescriptionContentMode());
        column.setEditFieldGenerator(existingColumn.getEditFieldGenerator());

        // If the new column has propertyPath and it equals to propertyPath
        // of the existing column, then coping the Sortable state, as it seems
        // that the new column corresponds to the generated column that is
        // related to the existing Entity attribute, so that this column can be sortable
        if (column.getPropertyPath() != null
                && column.getPropertyPath().equals(existingColumn.getPropertyPath())) {
            column.setSortable(existingColumn.isSortable());
        }

        if (existingColumn.getRenderer() != null) {
            Renderer existingRenderer = existingColumn.getRenderer();
            Class<?>[] rendererInterfaces = existingRenderer.getClass().getInterfaces();

            Class<? extends Renderer> rendererType = (Class<? extends Renderer>) Arrays.stream(rendererInterfaces)
                    .filter(Renderer.class::isAssignableFrom)
                    .findFirst()
                    .orElseThrow(() ->
                            new DevelopmentException(
                                    "Renderer should be specified explicitly for generated column: " + column.getId()));

            Renderer renderer = applicationContext.getBean(rendererType);
            ((AbstractRenderer) renderer).copy(existingRenderer);
            column.setRenderer(renderer);
        }
    }

    @Override
    public Subscription addColumnCollapsingChangeListener(Consumer<ColumnCollapsingChangeEvent> listener) {
        if (columnCollapsingChangeListenerRegistration == null) {
            columnCollapsingChangeListenerRegistration =
                    component.addColumnVisibilityChangeListener(this::onColumnVisibilityChanged);
        }

        getEventHub().subscribe(ColumnCollapsingChangeEvent.class, listener);

        return () -> internalRemoveColumnCollapsingChangeListener(listener);
    }

    protected void onColumnVisibilityChanged(Grid.ColumnVisibilityChangeEvent e) {
        // Due to vaadin/framework#11419,
        // we discard events with UserOriginated == false.
        if (!e.isUserOriginated()) {
            return;
        }

        //noinspection unchecked
        ColumnCollapsingChangeEvent event = new ColumnCollapsingChangeEvent(AbstractDataGrid.this,
                getColumnByGridColumn((Grid.Column<E, ?>) e.getColumn()), e.isHidden(), e.isUserOriginated());
        publish(ColumnCollapsingChangeEvent.class, event);
    }

    protected void internalRemoveColumnCollapsingChangeListener(Consumer<ColumnCollapsingChangeEvent> listener) {
        unsubscribe(ColumnCollapsingChangeEvent.class, listener);

        if (!hasSubscriptions(ColumnCollapsingChangeEvent.class)
                && columnCollapsingChangeListenerRegistration != null) {
            columnCollapsingChangeListenerRegistration.remove();
            columnCollapsingChangeListenerRegistration = null;
        }
    }

    @Override
    public Subscription addColumnResizeListener(Consumer<ColumnResizeEvent> listener) {
        if (columnResizeListenerRegistration == null) {
            columnResizeListenerRegistration =
                    component.addColumnResizeListener(this::onColumnResize);
        }

        getEventHub().subscribe(ColumnResizeEvent.class, listener);

        return () -> internalRemoveColumnResizeListener(listener);
    }

    protected void onColumnResize(Grid.ColumnResizeEvent e) {
        //noinspection unchecked
        ColumnResizeEvent event = new ColumnResizeEvent(AbstractDataGrid.this,
                getColumnByGridColumn((Grid.Column<E, ?>) e.getColumn()), e.isUserOriginated());
        publish(ColumnResizeEvent.class, event);
    }

    protected void internalRemoveColumnResizeListener(Consumer<ColumnResizeEvent> listener) {
        unsubscribe(ColumnResizeEvent.class, listener);

        if (!hasSubscriptions(ColumnResizeEvent.class)
                && columnResizeListenerRegistration != null) {
            columnResizeListenerRegistration.remove();
            columnResizeListenerRegistration = null;
        }
    }

    @Override
    public Subscription addSortListener(Consumer<SortEvent> listener) {
        return getEventHub().subscribe(SortEvent.class, listener);
    }

    @Override
    public Subscription addColumnReorderListener(Consumer<ColumnReorderEvent> listener) {
        return getEventHub().subscribe(ColumnReorderEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener) {
        return getEventHub().subscribe(SelectionEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemClickListener(Consumer<ItemClickEvent<E>> listener) {
        return getEventHub().subscribe(ItemClickEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addContextClickListener(Consumer<ContextClickEvent> listener) {
        if (contextClickListenerRegistration == null) {
            contextClickListenerRegistration =
                    component.addContextClickListener(this::onContextClick);
        }

        getEventHub().subscribe(ContextClickEvent.class, listener);

        return () -> internalRemoveContextClickListener(listener);
    }

    protected void onContextClick(com.vaadin.event.ContextClickEvent e) {
        MouseEventDetails mouseEventDetails = WrapperUtils.toMouseEventDetails(e);

        ContextClickEvent event = new ContextClickEvent(AbstractDataGrid.this, mouseEventDetails);
        publish(ContextClickEvent.class, event);
    }

    protected void internalRemoveContextClickListener(Consumer<ContextClickEvent> listener) {
        unsubscribe(ContextClickEvent.class, listener);

        if (!hasSubscriptions(ContextClickEvent.class)
                && contextClickListenerRegistration != null) {
            contextClickListenerRegistration.remove();
            contextClickListenerRegistration = null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Subscription addLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        return getEventHub().subscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @Nullable
    @Override
    public HeaderRow getHeaderRow(int index) {
        checkHeaderIndexInRange(index, true);
        return getHeaderRowByGridRow(component.getHeaderRow(index));
    }

    @Nullable
    protected HeaderRow getHeaderRowByGridRow(com.vaadin.ui.components.grid.HeaderRow gridRow) {
        for (HeaderRow headerRow : headerRows) {
            if (((HeaderRowImpl) headerRow).getGridRow() == gridRow) {
                return headerRow;
            }
        }
        return null;
    }

    @Override
    public HeaderRow appendHeaderRow() {
        boolean isAggregated = isAggregatable() && headerAggregationRow != null;
        return addHeaderRowInternal(isAggregated ?
                component.addHeaderRowAt(headerRows.size() - 1) :
                component.appendHeaderRow());
    }

    @Override
    public HeaderRow prependHeaderRow() {
        com.vaadin.ui.components.grid.HeaderRow headerRow = component.prependHeaderRow();
        return addHeaderRowInternal(headerRow);
    }

    @Override
    public HeaderRow addHeaderRowAt(int index) {
        checkHeaderIndexInRange(index, false);
        com.vaadin.ui.components.grid.HeaderRow headerRow = component.addHeaderRowAt(index);
        return addHeaderRowInternal(headerRow);
    }

    protected void checkHeaderIndexInRange(int index, boolean includeBounds) {
        if (isAggregatable() && headerAggregationRow != null) {
            boolean outOfBounds = includeBounds ?
                    index >= (headerRows.size() - 1) : // for getting row
                    index > (headerRows.size() - 1); // for adding row
            if (outOfBounds) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + (headerRows.size() - 1));
            }
        }
    }

    protected HeaderRow addHeaderRowInternal(com.vaadin.ui.components.grid.HeaderRow headerRow) {
        HeaderRowImpl rowImpl = new HeaderRowImpl(this, (Header.Row) headerRow);
        headerRows.add(rowImpl);
        return rowImpl;
    }

    @Override
    public void removeHeaderRow(@Nullable HeaderRow headerRow) {
        if (headerRow == null || !headerRows.contains(headerRow)) {
            return;
        }

        component.removeHeaderRow(((HeaderRowImpl) headerRow).getGridRow());
        headerRows.remove(headerRow);
    }

    @Override
    public void removeHeaderRow(int index) {
        HeaderRow headerRow = getHeaderRow(index);
        removeHeaderRow(headerRow);
    }

    @Nullable
    @Override
    public HeaderRow getDefaultHeaderRow() {
        return getHeaderRowByGridRow(component.getDefaultHeaderRow());
    }

    @Override
    public void setDefaultHeaderRow(HeaderRow headerRow) {
        component.setDefaultHeaderRow(((HeaderRowImpl) headerRow).getGridRow());
    }

    @Override
    public int getHeaderRowCount() {
        boolean isAggregated = isAggregatable() && headerAggregationRow != null;
        return isAggregated ? headerRows.size() - 1 : headerRows.size();
    }

    @Nullable
    @Override
    public FooterRow getFooterRow(int index) {
        index = calculateFooterIndex(index);
        return getFooterRowByGridRow(component.getFooterRow(index));
    }

    @Nullable
    protected FooterRow getFooterRowByGridRow(com.vaadin.ui.components.grid.FooterRow gridRow) {
        for (FooterRow footerRow : footerRows) {
            if (((FooterRowImpl) footerRow).getGridRow() == gridRow) {
                return footerRow;
            }
        }
        return null;
    }

    @Override
    public FooterRow appendFooterRow() {
        com.vaadin.ui.components.grid.FooterRow footerRow = component.appendFooterRow();
        return addFooterRowInternal(footerRow);
    }

    @Override
    public FooterRow prependFooterRow() {
        boolean isAggregated = isAggregatable() && footerAggregationRow != null;
        return addFooterRowInternal(isAggregated ?
                component.addFooterRowAt(1) :
                component.prependFooterRow());
    }

    @Override
    public FooterRow addFooterRowAt(int index) {
        index = calculateFooterIndex(index);
        com.vaadin.ui.components.grid.FooterRow footerRow = component.addFooterRowAt(index);
        return addFooterRowInternal(footerRow);
    }

    protected int calculateFooterIndex(int index) {
        if (isAggregatable() && footerAggregationRow != null) {
            int calculated = index + 1;
            if (calculated <= 0 || calculated > footerRows.size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + (footerRows.size() - 1));
            }
            return calculated;
        }
        return index;
    }

    protected FooterRow addFooterRowInternal(com.vaadin.ui.components.grid.FooterRow footerRow) {
        FooterRowImpl rowImpl = new FooterRowImpl(this, (Footer.Row) footerRow);
        footerRows.add(rowImpl);
        return rowImpl;
    }

    @Override
    public void removeFooterRow(@Nullable FooterRow footerRow) {
        if (footerRow == null || !footerRows.contains(footerRow)) {
            return;
        }

        component.removeFooterRow(((FooterRowImpl) footerRow).getGridRow());
        footerRows.remove(footerRow);
    }

    @Override
    public void removeFooterRow(int index) {
        FooterRow footerRow = getFooterRow(index);
        removeFooterRow(footerRow);
    }

    @Override
    public int getFooterRowCount() {
        boolean isAggregated = isAggregatable() && footerAggregationRow != null;
        return isAggregated ? footerRows.size() - 1 : footerRows.size();
    }

    @Nullable
    @Override
    public Function<E, io.jmix.ui.component.Component> getDetailsGenerator() {
        return detailsGenerator;
    }

    @Override
    public void setDetailsGenerator(@Nullable Function<E, io.jmix.ui.component.Component> detailsGenerator) {
        this.detailsGenerator = detailsGenerator;
        component.setDetailsGenerator(detailsGenerator != null ? this::getRowDetails : null);
    }

    @Nullable
    protected Component getRowDetails(E item) {
        io.jmix.ui.component.Component component = detailsGenerator.apply(item);
        return component != null ? component.unwrapComposition(Component.class) : null;
    }

    @Override
    public boolean isDetailsVisible(E entity) {
        return component.isDetailsVisible(entity);
    }

    @Override
    public void setDetailsVisible(E entity, boolean visible) {
        component.setDetailsVisible(entity, visible);
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public boolean isAggregatable() {
        return component.isAggregatable();
    }

    @Override
    public void setAggregatable(boolean aggregatable) {
        component.setAggregatable(aggregatable);

        updateAggregationRow();
    }

    @Override
    public AggregationPosition getAggregationPosition() {
        return AggregationPosition.valueOf(component.getAggregationPosition().name());
    }

    @Override
    public void setAggregationPosition(AggregationPosition position) {
        component.setAggregationPosition(JmixEnhancedGrid.AggregationPosition.valueOf(position.name()));
    }

    @Override
    public Map<String, Object> getAggregationResults() {
        return __aggregateValues();
    }

    @Override
    public void setEmptyStateMessage(@Nullable String message) {
        component.setEmptyStateMessage(message);

        showEmptyStateIfPossible();
    }

    @Nullable
    @Override
    public String getEmptyStateMessage() {
        return component.getEmptyStateMessage();
    }

    @Override
    public void setEmptyStateLinkMessage(@Nullable String linkMessage) {
        component.setEmptyStateLinkMessage(linkMessage);

        showEmptyStateIfPossible();
    }

    @Nullable
    @Override
    public String getEmptyStateLinkMessage() {
        return component.getEmptyStateLinkMessage();
    }

    @Override
    public void setEmptyStateLinkClickHandler(@Nullable Consumer<EmptyStateClickEvent<E>> handler) {
        this.emptyStateClickEventHandler = handler;
    }

    @Nullable
    @Override
    public Float getMinHeight() {
        return component.getMinHeight();
    }

    @Nullable
    @Override
    public SizeUnit getMinHeightSizeUnit() {
        return component.getMinHeightSizeUnit() != null
                ? WrapperUtils.toSizeUnit(component.getMinHeightSizeUnit())
                : null;
    }

    @Override
    public void setMinHeight(@Nullable String minHeight) {
        if (minHeight != null) {
            // validate size unit
            SizeWithUnit.parseStringSize(minHeight);
        }
        component.setMinHeight(minHeight);
    }

    @Nullable
    @Override
    public Float getMinWidth() {
        return component.getMinWidth();
    }

    @Nullable
    @Override
    public SizeUnit getMinWidthSizeUnit() {
        return component.getMinWidthSizeUnit() != null
                ? WrapperUtils.toSizeUnit(component.getMinWidthSizeUnit())
                : null;
    }

    @Override
    public void setMinWidth(@Nullable String minWidth) {
        if (minWidth != null) {
            // validate size unit
            SizeWithUnit.parseStringSize(minWidth);
        }
        component.setMinWidth(minWidth);
    }

    @Nullable
    @Override
    public Consumer<EmptyStateClickEvent<E>> getEmptyStateLinkClickHandler() {
        return emptyStateClickEventHandler;
    }

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

    protected Map<String, String> __aggregate() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException("DataGrid must be aggregatable and items must not be null in order to " +
                    "use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, String> aggregationInfoMap = getAggregatableDelegate().aggregate(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().map(EntityValues::getId).collect(Collectors.toList())
        );

        return convertAggregationKeyMapToColumnIdKeyMap(aggregationInfoMap);
    }

    protected Map<String, Object> __aggregateValues() {
        if (!isAggregatable() || getItems() == null) {
            throw new IllegalStateException("DataGrid must be aggregatable and items must not be null in order to " +
                    "use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos();

        Map<AggregationInfo, Object> aggregationInfoMap = getAggregatableDelegate().aggregateValues(
                aggregationInfos.toArray(new AggregationInfo[0]),
                getItems().getItems().map(EntityValues::getId).collect(Collectors.toList())
        );

        return convertAggregationKeyMapToColumnIdKeyMap(aggregationInfoMap);
    }

    protected <V> Map<String, V> convertAggregationKeyMapToColumnIdKeyMap(Map<AggregationInfo, V> aggregationInfoMap) {
        Map<String, V> resultsByColumns = new LinkedHashMap<>();
        for (String propertyId : component.getAggregationPropertyIds()) {
            DataGrid.Column column = columns.get(propertyId);
            if (column.getAggregation() != null) {
                resultsByColumns.put(column.getId(), aggregationInfoMap.get(column.getAggregation()));
            }
        }
        return resultsByColumns;
    }

    protected List<AggregationInfo> getAggregationInfos() {
        List<AggregationInfo> aggregationInfos = new ArrayList<>();
        for (String propertyId : component.getAggregationPropertyIds()) {
            DataGrid.Column column = columns.get(propertyId);
            AggregationInfo aggregation = column.getAggregation();
            if (aggregation != null) {
                checkAggregation(aggregation);
                aggregationInfos.add(aggregation);
            }
        }
        return aggregationInfos;
    }

    protected void checkAggregation(AggregationInfo aggregationInfo) {
        AggregationInfo.Type aggregationType = aggregationInfo.getType();

        if (aggregationType == AggregationInfo.Type.CUSTOM) {
            return;
        }

        MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();
        Class<?> javaType = propertyPath.getMetaProperty().getJavaType();
        Aggregation<?> aggregation = aggregations.get(javaType);

        if (aggregation != null && aggregation.getSupportedAggregationTypes().contains(aggregationType)) {
            return;
        }

        String msg = String.format("Unable to aggregate column \"%s\" with data type %s with default aggregation strategy: %s",
                propertyPath, propertyPath.getRange(), aggregationInfo.getType());

        throw new IllegalArgumentException(msg);
    }

    protected void fillAggregationRow(Map<String, String> values) {
        if (getAggregationPosition() == AggregationPosition.TOP) {
            if (headerAggregationRow == null) {
                initAggregationRow();
            }
            for (Map.Entry<String, String> value : values.entrySet()) {
                com.vaadin.ui.components.grid.HeaderCell headerCell = headerAggregationRow.getCell(value.getKey());
                headerCell.setText(value.getValue());
            }
        } else {
            if (footerAggregationRow == null) {
                initAggregationRow();
            }
            for (Map.Entry<String, String> value : values.entrySet()) {
                com.vaadin.ui.components.grid.FooterCell footerCell = footerAggregationRow.getCell(value.getKey());
                footerCell.setText(value.getValue());
            }
        }
    }

    protected void initAggregationRow() {
        if (isAggregatable()) {
            if (getAggregationPosition() == AggregationPosition.TOP) {
                headerAggregationRow = component.appendHeaderRow();
                headerAggregationRow.setStyleName("jmix-aggregation-row");

                for (Column<E> column : columns.values()) {
                    if (column.getAggregation() != null) {
                        com.vaadin.ui.components.grid.HeaderCell headerCell = headerAggregationRow.getCell(column.getId());
                        headerCell.setDescription(getColumnAggregationDescription(column));
                    }
                }
                addHeaderRowInternal(headerAggregationRow);
            } else if (getAggregationPosition() == AggregationPosition.BOTTOM) {
                footerAggregationRow = component.prependFooterRow();
                footerAggregationRow.setStyleName("jmix-aggregation-row");

                for (Column<E> column : columns.values()) {
                    if (column.getAggregation() != null) {
                        com.vaadin.ui.components.grid.FooterCell footerCell = footerAggregationRow.getCell(column.getId());
                        footerCell.setDescription(getColumnAggregationDescription(column));
                    }
                }
                addFooterRowInternal(footerAggregationRow);
            }
        }
    }

    protected void updateAggregationRow() {
        boolean isAggregatable = isAggregatable()
                && getItems() != null
                && !component.getAggregationPropertyIds().isEmpty();
        if (isAggregatable) {
            Map<String, String> results = __aggregate();
            fillAggregationRow(results);
        } else {
            removeAggregationRow();
        }
    }

    protected void removeAggregationRow() {
        if (getAggregationPosition() == AggregationPosition.TOP) {
            if (headerAggregationRow != null) {
                removeHeaderRow(getHeaderRowByGridRow(headerAggregationRow));
                headerAggregationRow = null;
            }
        } else if (getAggregationPosition() == AggregationPosition.BOTTOM) {
            if (footerAggregationRow != null) {
                removeFooterRow(getFooterRowByGridRow(footerAggregationRow));
                footerAggregationRow = null;
            }
        }
    }

    protected String getColumnAggregationDescription(Column<E> column) {
        String descriptionByType = null;
        if (column.getAggregation().getType() != AggregationInfo.Type.CUSTOM) {
            descriptionByType = getColumnAggregationDescriptionByType(column);
        }

        return column.getValueDescription() != null ? column.getValueDescription() : descriptionByType;
    }

    protected String getColumnAggregationDescriptionByType(Column<E> column) {
        String aggregationTypeLabel;
        switch (column.getAggregation().getType()) {
            case AVG:
                aggregationTypeLabel = "aggregation.avg";
                break;
            case COUNT:
                aggregationTypeLabel = "aggregation.count";
                break;
            case SUM:
                aggregationTypeLabel = "aggregation.sum";
                break;
            case MIN:
                aggregationTypeLabel = "aggregation.min";
                break;
            case MAX:
                aggregationTypeLabel = "aggregation.max";
                break;
            default:
                throw new IllegalArgumentException(String.format("AggregationType %s is not supported",
                        column.getAggregation().getType().toString()));
        }
        return messages.getMessage(aggregationTypeLabel);
    }

    protected void enableCrossFieldValidationHandling(boolean enable) {
        if (isEditorEnabled()) {
            ((JmixEditorImpl<E>) component.getEditor()).setCrossFieldValidationHandler(
                    enable ? this::validateCrossFieldRules : null);
        }
    }

    @Nullable
    protected String validateCrossFieldRules(Map<String, Object> properties) {
        E item = getEditedItem();
        if (item == null) {
            return null;
        }

        // set changed values from editor to copied entity
        E copiedItem = metadataTools.deepCopy(item);
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            EntityValues.setValueEx(copiedItem, property.getKey(), property.getValue());
        }

        // validate copy
        ValidationErrors errors = screenValidation.validateCrossFieldRules(
                getFrame() != null ? getFrame().getFrameOwner() : null,
                copiedItem);
        if (errors.isEmpty()) {
            return null;
        }

        StringBuilder errorMessage = new StringBuilder();
        for (ValidationErrors.Item error : errors.getAll()) {
            errorMessage.append(error.description).append("\n");
        }
        return errorMessage.toString();
    }

    @Nullable
    protected String getGeneratedRowStyle(E item) {
        if (rowStyleProviders == null) {
            return null;
        }

        StringBuilder joinedStyle = null;
        for (Function<? super E, String> styleProvider : rowStyleProviders) {
            String styleName = styleProvider.apply(item);
            if (styleName != null) {
                if (joinedStyle == null) {
                    joinedStyle = new StringBuilder(styleName);
                } else {
                    joinedStyle.append(" ").append(styleName);
                }
            }
        }

        return joinedStyle != null ? joinedStyle.toString() : null;
    }

    protected void initEmptyState() {
        component.setEmptyStateLinkClickHandler(() -> {
            if (emptyStateClickEventHandler != null) {
                emptyStateClickEventHandler.accept(new EmptyStateClickEvent<>(this));
            }
        });

        if (dataBinding != null) {
            dataBinding.addDataProviderListener(event -> showEmptyStateIfPossible());
        }

        showEmptyStateIfPossible();
    }

    protected void showEmptyStateIfPossible() {
        boolean emptyItems = (dataBinding != null && dataBinding.getDataGridItems().size() == 0) || getItems() == null;
        boolean notEmptyMessages = !Strings.isNullOrEmpty(component.getEmptyStateMessage())
                || !Strings.isNullOrEmpty(component.getEmptyStateLinkMessage());

        component.setShowEmptyState(emptyItems && notEmptyMessages);
    }

    protected class CellStyleGeneratorAdapter<T extends E> implements StyleGenerator<T> {

        protected Column<T> column;

        public CellStyleGeneratorAdapter(Column<T> column) {
            this.column = column;
        }

        @Override
        public String apply(T item) {
            //noinspection unchecked
            return getGeneratedCellStyle(item, (Column<E>) column);
        }
    }

    @Nullable
    protected String getGeneratedCellStyle(E item, Column<E> column) {
        StringBuilder joinedStyle = null;

        if (column.getStyleProvider() != null) {
            String styleName = column.getStyleProvider().apply(item);
            if (styleName != null) {
                joinedStyle = new StringBuilder(styleName);
            }
        }

        return joinedStyle != null ? joinedStyle.toString() : null;
    }

    protected class CellDescriptionGeneratorAdapter<T extends E> implements DescriptionGenerator<T> {
        protected Column<T> column;

        public CellDescriptionGeneratorAdapter(Column<T> column) {
            this.column = column;
        }

        @Nullable
        @Override
        public String apply(T item) {
            //noinspection unchecked
            return getGeneratedCellDescription(item, (Column<E>) column);
        }
    }

    @Nullable
    protected String getGeneratedCellDescription(E item, Column<E> column) {
        if (column.getDescriptionProvider() != null) {
            String cellDescription = column.getDescriptionProvider().apply(item);
            return ((ColumnImpl) column).getDescriptionContentMode() == ContentMode.HTML
                    ? sanitize(cellDescription)
                    : cellDescription;
        }

        return null;
    }

    @Override
    public void attached() {
        super.attached();

        for (io.jmix.ui.component.Component component : getInnerComponents()) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (io.jmix.ui.component.Component component : getInnerComponents()) {
            ((AttachNotifier) component).detached();
        }
    }

    public static abstract class AbstractRenderer<T, V> implements RendererWrapper<V> {
        protected com.vaadin.ui.renderers.Renderer<V> renderer;
        protected AbstractDataGrid<?, T> dataGrid;
        protected String nullRepresentation;

        protected AbstractRenderer() {
        }

        protected AbstractRenderer(String nullRepresentation) {
            this.nullRepresentation = nullRepresentation;
        }

        @Override
        public com.vaadin.ui.renderers.Renderer<V> getImplementation() {
            if (renderer == null) {
                renderer = createImplementation();
            }
            return renderer;
        }

        protected abstract com.vaadin.ui.renderers.Renderer<V> createImplementation();

        protected abstract void copy(Renderer existingRenderer);

        @Nullable
        public ValueProvider<?, V> getPresentationValueProvider() {
            // Some renderers need specific presentation ValueProvider to be set at the same time
            // (see com.vaadin.ui.Grid.Column.setRenderer(com.vaadin.data.ValueProvider<V,P>,
            //          com.vaadin.ui.renderers.Renderer<? super P>)).
            // Default `null` means do not use any presentation ValueProvider
            return null;
        }

        @Override
        public void resetImplementation() {
            renderer = null;
        }

        @Nullable
        protected AbstractDataGrid<?, T> getDataGrid() {
            return dataGrid;
        }

        protected void setDataGrid(@Nullable AbstractDataGrid<?, T> dataGrid) {
            this.dataGrid = dataGrid;
        }

        protected String getNullRepresentation() {
            return nullRepresentation;
        }

        protected void setNullRepresentation(String nullRepresentation) {
            checkRendererNotSet();
            this.nullRepresentation = nullRepresentation;
        }

        protected Column<T> getColumnByGridColumn(Grid.Column<T, ?> column) {
            return dataGrid.getColumnByGridColumn(column);
        }

        protected void checkRendererNotSet() {
            if (renderer != null) {
                throw new IllegalStateException("Renderer parameters cannot be changed after it is set to a column");
            }
        }
    }

    protected static class ColumnImpl<E> implements Column<E>, HasXmlDescriptor {

        protected final String id;
        protected final MetaPropertyPath propertyPath;

        protected String caption;
        protected String collapsingToggleCaption;
        protected double width;
        protected double minWidth;
        protected double maxWidth;
        protected int expandRatio;
        protected boolean collapsed;
        protected boolean visible;
        protected boolean collapsible;
        protected boolean sortable;
        protected boolean resizable;
        protected boolean editable;
        protected boolean generated;
        protected AggregationInfo aggregation;
        protected String valueDescription;

        protected AbstractRenderer<E, ?> renderer;
        protected Function presentationProvider;

        protected Function<? super E, String> styleProvider;
        protected Function<? super E, String> descriptionProvider;
        protected ContentMode descriptionContentMode = ContentMode.PREFORMATTED;

        protected Element element;

        protected AbstractDataGrid<?, E> owner;
        protected Grid.Column<E, ?> gridColumn;

        protected Function<EditorFieldGenerationContext<E>, Field<?>> generator;

        protected ColumnImpl(String id, @Nullable MetaPropertyPath propertyPath, AbstractDataGrid<?, E> owner) {
            this.id = id;
            this.propertyPath = propertyPath;
            this.owner = owner;

            setupDefaults();
        }

        protected void setupDefaults() {
            resizable = true;
            editable = true;
            generated = false;
            // the generated properties are not normally sortable,
            // but require special handling to enable sorting.
            // for now sorting for generated properties is disabled
            sortable = propertyPath != null && owner.isSortable();
            collapsible = owner.isColumnsCollapsingAllowed();
            collapsed = false;

            visible = true;

            ThemeConstants theme = App.getInstance().getThemeConstants();
            width = theme.getInt("jmix.ui.DataGrid.defaultColumnWidth", -1);
            maxWidth = theme.getInt("jmix.ui.DataGrid.defaultColumnMaxWidth", -1);
            minWidth = theme.getInt("jmix.ui.DataGrid.defaultColumnMinWidth", 10);
            expandRatio = theme.getInt("jmix.ui.DataGrid.defaultColumnExpandRatio", -1);
        }

        @Override
        public String getId() {
            if (gridColumn != null) {
                return gridColumn.getId();
            }
            return id;
        }

        @Nullable
        @Override
        public MetaPropertyPath getPropertyPath() {
            return propertyPath;
        }

        public Object getPropertyId() {
            return propertyPath != null ? propertyPath : id;
        }

        @Nullable
        @Override
        public String getCaption() {
            if (gridColumn != null) {
                return gridColumn.getCaption();
            }
            return caption;
        }

        @Override
        public void setCaption(String caption) {
            this.caption = caption;
            if (gridColumn != null) {
                gridColumn.setCaption(caption);
            }
        }

        @Override
        public String getCollapsingToggleCaption() {
            if (gridColumn != null) {
                return gridColumn.getHidingToggleCaption();
            }
            return collapsingToggleCaption;
        }

        @Override
        public void setCollapsingToggleCaption(@Nullable String collapsingToggleCaption) {
            this.collapsingToggleCaption = collapsingToggleCaption;
            if (gridColumn != null) {
                gridColumn.setHidingToggleCaption(collapsingToggleCaption);
            }
        }

        @Override
        public double getWidth() {
            if (gridColumn != null) {
                return gridColumn.getWidth();
            }
            return width;
        }

        @Override
        public void setWidth(double width) {
            this.width = width;
            if (gridColumn != null) {
                gridColumn.setWidth(width);
            }
        }

        @Override
        public boolean isWidthAuto() {
            if (gridColumn != null) {
                return gridColumn.isWidthUndefined();
            }
            return width < 0;
        }

        @Override
        public void setWidthAuto() {
            if (!isWidthAuto()) {
                width = -1;
                if (gridColumn != null) {
                    gridColumn.setWidthUndefined();
                }
            }
        }

        @Override
        public int getExpandRatio() {
            if (gridColumn != null) {
                return gridColumn.getExpandRatio();
            }
            return expandRatio;
        }

        @Override
        public void setExpandRatio(int expandRatio) {
            this.expandRatio = expandRatio;
            if (gridColumn != null) {
                gridColumn.setExpandRatio(expandRatio);
            }
        }

        @Override
        public void clearExpandRatio() {
            setExpandRatio(-1);
        }

        @Override
        public double getMinimumWidth() {
            if (gridColumn != null) {
                return gridColumn.getMinimumWidth();
            }
            return minWidth;
        }

        @Override
        public void setMinimumWidth(double pixels) {
            this.minWidth = pixels;
            if (gridColumn != null) {
                gridColumn.setMinimumWidth(pixels);
            }
        }

        @Override
        public double getMaximumWidth() {
            if (gridColumn != null) {
                return gridColumn.getMaximumWidth();
            }
            return maxWidth;
        }

        @Override
        public void setMaximumWidth(double pixels) {
            this.maxWidth = pixels;
            if (gridColumn != null) {
                gridColumn.setMaximumWidth(pixels);
            }
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public void setVisible(boolean visible) {
            if (this.visible != visible) {
                this.visible = visible;

                Grid<E> grid = owner.getComponent();

                if (visible) {
                    Grid.Column<E, ?> gridColumn =
                            grid.addColumn(new EntityValueProvider<>(getPropertyPath()));
                    owner.setupGridColumnProperties(gridColumn, this);

                    grid.setColumnOrder(owner.getColumnOrder());
                } else {
                    grid.removeColumn(getId());
                    setGridColumn(null);
                }
            }
        }

        @Override
        public boolean isCollapsed() {
            if (gridColumn != null) {
                return gridColumn.isHidden();
            }
            return collapsed;
        }

        @Override
        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            if (gridColumn != null) {
                gridColumn.setHidden(collapsed);

                // Due to vaadin/framework#11419,
                // we explicitly send ColumnCollapsingChangeEvent with UserOriginated == false.
                ColumnCollapsingChangeEvent event = new ColumnCollapsingChangeEvent(owner, this, collapsed, false);
                owner.publish(ColumnCollapsingChangeEvent.class, event);
            }
        }

        @Override
        public boolean isCollapsible() {
            if (gridColumn != null) {
                return gridColumn.isHidable();
            }
            return collapsible;
        }

        @Override
        public void setCollapsible(boolean collapsible) {
            this.collapsible = collapsible;
            updateCollapsible();
        }

        public void updateCollapsible() {
            if (gridColumn != null) {
                gridColumn.setHidable(collapsible && owner.isColumnsCollapsingAllowed());
            }
        }

        @Override
        public boolean isSortable() {
            return sortable;
        }

        @Override
        public void setSortable(boolean sortable) {
            this.sortable = sortable;
            updateSortable();
        }

        public void updateSortable() {
            if (gridColumn != null) {
                gridColumn.setSortable(this.sortable && owner.isSortable());
            }
        }

        @Override
        public boolean isResizable() {
            if (gridColumn != null) {
                return gridColumn.isResizable();
            }
            return resizable;
        }

        @Override
        public void setResizable(boolean resizable) {
            this.resizable = resizable;
            if (gridColumn != null) {
                gridColumn.setResizable(resizable);
            }
        }

        @Nullable
        protected MetaProperty getMetaProperty() {
            return getPropertyPath() != null
                    ? getPropertyPath().getMetaProperty()
                    : null;
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

        @Nullable
        @Override
        public Renderer getRenderer() {
            return renderer;
        }

        @Override
        public void setRenderer(@Nullable Renderer renderer) {
            setRenderer(renderer, null);
        }

        @Override
        public void setRenderer(@Nullable Renderer renderer, @Nullable Function presentationProvider) {
            if (renderer == null && this.renderer != null) {
                this.renderer.resetImplementation();
                this.renderer.setDataGrid(null);
            }

            //noinspection unchecked
            this.renderer = (AbstractRenderer) renderer;
            this.presentationProvider = presentationProvider;

            if (this.renderer != null) {
                this.renderer.setDataGrid(owner);
            }

            updateRendererInternal();
        }

        @SuppressWarnings("unchecked")
        protected void updateRendererInternal() {
            if (gridColumn != null) {
                com.vaadin.ui.renderers.Renderer vRenderer = owner.getColumnRendererImplementation(this);
                ValueProvider vPresentationProvider = owner.getColumnPresentationValueProvider(this);
                gridColumn.setRenderer(vPresentationProvider, vRenderer);
                owner.repaint();
            }
        }

        @Nullable
        @Override
        public Function getPresentationProvider() {
            return presentationProvider;
        }

        public boolean isGenerated() {
            return generated;
        }

        public void setGenerated(boolean generated) {
            this.generated = generated;
        }

        @Override
        public boolean isEditable() {
            if (gridColumn != null) {
                return gridColumn.isEditable();
            }
            return isShouldBeEditable();
        }

        public boolean isShouldBeEditable() {
            return editable
                    && propertyPath != null  // We can't generate field for editor in case we don't have propertyPath
                    && (!generated && !isRepresentsCollection()
                    || generator != null)
                    && isEditingPermitted();
        }

        protected boolean isRepresentsCollection() {
            if (propertyPath != null) {
                MetaProperty metaProperty = propertyPath.getMetaProperty();
                Class<?> javaType = metaProperty.getJavaType();
                return Collection.class.isAssignableFrom(javaType);
            }
            return false;
        }

        protected boolean isEditingPermitted() {
            if (propertyPath != null) {
                UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(propertyPath);
                owner.accessManager.applyRegisteredConstraints(attributeContext);
                return attributeContext.canModify();
            }
            return true;
        }

        @Override
        public void setEditable(boolean editable) {
            this.editable = editable;
            updateEditable();
        }

        protected void updateEditable() {
            if (gridColumn != null) {
                gridColumn.setEditable(isShouldBeEditable());
            }
        }

        @Nullable
        @Override
        public Function<EditorFieldGenerationContext<E>, Field<?>> getEditFieldGenerator() {
            return generator;
        }

        @Override
        public void setEditFieldGenerator(@Nullable Function<EditorFieldGenerationContext<E>, Field<?>> generator) {
            this.generator = generator;
            updateEditable();
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public Function<E, String> getStyleProvider() {
            return (Function<E, String>) styleProvider;
        }

        @Override
        public void setStyleProvider(@Nullable Function<? super E, String> styleProvider) {
            this.styleProvider = styleProvider;
            owner.repaint();
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public Function<E, String> getDescriptionProvider() {
            return (Function<E, String>) descriptionProvider;
        }

        @Override
        public void setDescriptionProvider(@Nullable Function<? super E, String> descriptionProvider) {
            setDescriptionProvider(descriptionProvider, ContentMode.PREFORMATTED);
        }

        @Override
        public void setDescriptionProvider(@Nullable Function<? super E, String> descriptionProvider,
                                           ContentMode contentMode) {
            this.descriptionProvider = descriptionProvider;
            this.descriptionContentMode = contentMode;

            if (gridColumn != null) {
                gridColumn.getState().tooltipContentMode = WrapperUtils.toVaadinContentMode(contentMode);
            }

            owner.repaint();
        }

        public ContentMode getDescriptionContentMode() {
            return descriptionContentMode;
        }

        public Grid.Column<E, ?> getGridColumn() {
            return gridColumn;
        }

        public void setGridColumn(@Nullable Grid.Column<E, ?> gridColumn) {
            AppUI current = AppUI.getCurrent();
            if (gridColumn == null
                    && this.gridColumn != null
                    && current != null
                    && current.isTestMode()) {
                owner.removeColumnId(this.gridColumn);
            }

            this.gridColumn = gridColumn;
        }

        @Nullable
        @Override
        public DataGrid<E> getOwner() {
            return owner;
        }

        @Override
        public void setOwner(@Nullable DataGrid<E> owner) {
            if (owner != null) {
                this.owner = (AbstractDataGrid<?, E>) owner;
            } else {
                this.owner = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ColumnImpl column = (ColumnImpl) o;

            return id.equals(column.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id == null ? super.toString() : id;
        }

        @Nullable
        @Override
        public AggregationInfo getAggregation() {
            return aggregation;
        }

        @Override
        public void setAggregation(@Nullable AggregationInfo info) {
            this.aggregation = info;

            if (owner != null && id != null) {
                owner.getComponent().addAggregationPropertyId(id);
            }
        }

        @Nullable
        @Override
        public String getValueDescription() {
            return valueDescription;
        }

        @Override
        public void setValueDescription(@Nullable String valueDescription) {
            this.valueDescription = valueDescription;
        }
    }

    protected abstract static class AbstractStaticRowImp<T extends StaticCell> implements StaticRow<T> {

        protected AbstractDataGrid dataGrid;
        protected StaticSection.StaticRow<?> gridRow;

        public AbstractStaticRowImp(AbstractDataGrid dataGrid, StaticSection.StaticRow<?> gridRow) {
            this.dataGrid = dataGrid;
            this.gridRow = gridRow;
        }

        @Nullable
        @Override
        public String getStyleName() {
            return gridRow.getStyleName();
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            gridRow.setStyleName(styleName);
        }

        @Override
        public T join(String... columnIds) {
            return joinInternal(columnIds);
        }

        protected abstract T joinInternal(String... columnIds);

        @Nullable
        @Override
        public T getCell(String columnId) {
            ColumnImpl column = (ColumnImpl) dataGrid.getColumnNN(columnId);
            return getCellInternal(column.getId());
        }

        protected abstract T getCellInternal(String columnId);

        public StaticSection.StaticRow<?> getGridRow() {
            return gridRow;
        }
    }

    protected static class HeaderRowImpl extends AbstractStaticRowImp<HeaderCell> implements HeaderRow {

        public HeaderRowImpl(AbstractDataGrid dataGrid, Header.Row headerRow) {
            super(dataGrid, headerRow);
        }

        @Override
        public Header.Row getGridRow() {
            return (Header.Row) super.getGridRow();
        }

        @Override
        protected HeaderCell getCellInternal(String columnId) {
            Header.Row.Cell gridCell = getGridRow().getCell(columnId);
            return new HeaderCellImpl(this, gridCell);
        }

        @Override
        protected HeaderCell joinInternal(String... columnIds) {
            Header.Row.Cell gridCell = (Header.Row.Cell) getGridRow().join(columnIds);
            return new HeaderCellImpl(this, gridCell);
        }
    }

    protected static class FooterRowImpl extends AbstractStaticRowImp<FooterCell> implements FooterRow {

        public FooterRowImpl(AbstractDataGrid dataGrid, Footer.Row footerRow) {
            super(dataGrid, footerRow);
        }

        @Override
        public Footer.Row getGridRow() {
            return (Footer.Row) super.getGridRow();
        }

        @Override
        protected FooterCell getCellInternal(String columnId) {
            Footer.Row.Cell gridCell = getGridRow().getCell(columnId);
            return new FooterCellImpl(this, gridCell);
        }

        @Override
        protected FooterCell joinInternal(String... columnIds) {
            Footer.Row.Cell gridCell = (Footer.Row.Cell) getGridRow().join(columnIds);
            return new FooterCellImpl(this, gridCell);
        }
    }

    protected abstract static class AbstractStaticCellImpl implements StaticCell {

        protected StaticRow<?> row;
        protected io.jmix.ui.component.Component component;

        public AbstractStaticCellImpl(StaticRow<?> row) {
            this.row = row;
        }

        @Override
        public StaticRow<?> getRow() {
            return row;
        }

        @Override
        public io.jmix.ui.component.Component getComponent() {
            return component;
        }

        @Override
        public void setComponent(io.jmix.ui.component.Component component) {
            this.component = component;
        }
    }

    protected static class HeaderCellImpl extends AbstractStaticCellImpl implements HeaderCell {

        protected Header.Row.Cell gridCell;

        public HeaderCellImpl(HeaderRow row, Header.Row.Cell gridCell) {
            super(row);
            this.gridCell = gridCell;
        }

        @Override
        public HeaderRow getRow() {
            return (HeaderRow) super.getRow();
        }

        @Nullable
        @Override
        public String getStyleName() {
            return gridCell.getStyleName();
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            gridCell.setStyleName(styleName);
        }

        @Override
        public DataGridStaticCellType getCellType() {
            return WrapperUtils.toDataGridStaticCellType(gridCell.getCellType());
        }

        @Override
        public void setComponent(io.jmix.ui.component.Component component) {
            super.setComponent(component);
            gridCell.setComponent(component.unwrap(Component.class));
        }

        @Nullable
        @Override
        public String getHtml() {
            return gridCell.getHtml();
        }

        @Override
        public void setHtml(String html) {
            gridCell.setHtml(html);
        }

        @Nullable
        @Override
        public String getText() {
            return gridCell.getText();
        }

        @Override
        public void setText(String text) {
            gridCell.setText(text);
        }
    }

    protected static class FooterCellImpl extends AbstractStaticCellImpl implements FooterCell {

        protected Footer.Row.Cell gridCell;

        public FooterCellImpl(FooterRow row, Footer.Row.Cell gridCell) {
            super(row);
            this.gridCell = gridCell;
        }

        @Override
        public FooterRow getRow() {
            return (FooterRow) super.getRow();
        }

        @Nullable
        @Override
        public String getStyleName() {
            return gridCell.getStyleName();
        }

        @Override
        public void setStyleName(@Nullable String styleName) {
            gridCell.setStyleName(styleName);
        }

        @Override
        public DataGridStaticCellType getCellType() {
            return WrapperUtils.toDataGridStaticCellType(gridCell.getCellType());
        }

        @Override
        public void setComponent(io.jmix.ui.component.Component component) {
            super.setComponent(component);
            gridCell.setComponent(component.unwrap(Component.class));
        }

        @Nullable
        @Override
        public String getHtml() {
            return gridCell.getHtml();
        }

        @Override
        public void setHtml(String html) {
            gridCell.setHtml(html);
        }

        @Nullable
        @Override
        public String getText() {
            return gridCell.getText();
        }

        @Override
        public void setText(String text) {
            gridCell.setText(text);
        }
    }

    public static class ActionMenuItemWrapper {
        protected MenuItem menuItem;
        protected Action action;

        protected boolean showIconsForPopupMenuActions;

        protected IconResolver iconResolver;

        protected Consumer<PropertyChangeEvent> actionPropertyChangeListener;

        public ActionMenuItemWrapper(MenuItem menuItem, boolean showIconsForPopupMenuActions, IconResolver iconResolver) {
            this.menuItem = menuItem;
            this.showIconsForPopupMenuActions = showIconsForPopupMenuActions;
            this.iconResolver = iconResolver;

            this.menuItem.setCommand((MenuBar.Command) selectedItem -> {
                if (action != null) {
                    performAction(action);
                }
            });
        }

        public void performAction(Action action) {
            action.actionPerform(null);
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(@Nullable Action action) {
            if (this.action != action) {
                if (this.action != null) {
                    this.action.removePropertyChangeListener(actionPropertyChangeListener);
                }

                this.action = action;

                if (action != null) {
                    String caption = action.getCaption();
                    if (!StringUtils.isEmpty(caption)) {
                        setCaption(caption);
                    }

                    menuItem.setEnabled(action.isEnabled());
                    menuItem.setVisible(action.isVisible());

                    if (action.getIcon() != null) {
                        setIcon(action.getIcon());
                    }

                    actionPropertyChangeListener = evt -> {
                        if (Action.PROP_ICON.equals(evt.getPropertyName())) {
                            setIcon(this.action.getIcon());
                        } else if (Action.PROP_CAPTION.equals(evt.getPropertyName())) {
                            setCaption(this.action.getCaption());
                        } else if (Action.PROP_ENABLED.equals(evt.getPropertyName())) {
                            setEnabled(this.action.isEnabled());
                        } else if (Action.PROP_VISIBLE.equals(evt.getPropertyName())) {
                            setVisible(this.action.isVisible());
                        }
                    };
                    action.addPropertyChangeListener(actionPropertyChangeListener);
                }
            }
        }

        public void setEnabled(boolean enabled) {
            menuItem.setEnabled(enabled);
        }

        public void setVisible(boolean visible) {
            menuItem.setVisible(visible);
        }

        public void setIcon(@Nullable String icon) {
            if (showIconsForPopupMenuActions) {
                if (!StringUtils.isEmpty(icon)) {
                    menuItem.setIcon(iconResolver.getIconResource(icon));
                } else {
                    menuItem.setIcon(null);
                }
            }
        }

        public void setCaption(@Nullable String caption) {
            if (action.getShortcutCombination() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(caption);
                if (action.getShortcutCombination() != null) {
                    sb.append(" (").append(action.getShortcutCombination().format()).append(")");
                }
                caption = sb.toString();
            }

            menuItem.setText(caption);
        }
    }

    protected static class GridComposition extends JmixCssActionsLayout {
        protected Grid<?> grid;

        public static final String BORDERLESS_STYLE = "borderless";
        public static final String NO_HORIZONTAL_LINES_STYLE = "no-horizontal-lines";
        public static final String NO_VERTICAL_LINES_STYLE = "no-vertical-lines";

        public Grid<?> getGrid() {
            return grid;
        }

        public void setGrid(Grid<?> grid) {
            this.grid = grid;
        }

        @Override
        public void setHeight(float height, Sizeable.Unit unit) {
            super.setHeight(height, unit);

            if (getHeight() < 0) {
                grid.setHeightUndefined();
                grid.setHeightMode(HeightMode.UNDEFINED);
            } else {
                grid.setHeight(100, Sizeable.Unit.PERCENTAGE);
                grid.setHeightMode(HeightMode.CSS);
            }
        }

        @Override
        public void setWidth(float width, Sizeable.Unit unit) {
            super.setWidth(width, unit);

            if (getWidth() < 0) {
                grid.setWidthUndefined();
            } else {
                grid.setWidth(100, Sizeable.Unit.PERCENTAGE);
            }
        }

        @Override
        public void addStyleName(String style) {
            super.addStyleName(style);

            // Safari hides footer while changing predefined styles at runtime
            updateFooterVisibility(style);
        }

        @Override
        public void removeStyleName(String style) {
            super.removeStyleName(style);

            // Safari hides footer while changing predefined styles at runtime
            updateFooterVisibility(style);
        }

        protected void updateFooterVisibility(String style) {
            Page page = Page.getCurrent();
            if (page == null) {
                return;
            }
            WebBrowser webBrowser = page.getWebBrowser();
            boolean isPredefinedStyle = style.equals(BORDERLESS_STYLE)
                    || style.equals(NO_HORIZONTAL_LINES_STYLE)
                    || style.equals(NO_VERTICAL_LINES_STYLE);

            if (webBrowser.isSafari()
                    && isPredefinedStyle
                    && grid.getFooterRowCount() > 0
                    && grid.isFooterVisible()) {
                ((JmixEnhancedGrid) grid).updateFooterVisibility();
            }
        }
    }
}
