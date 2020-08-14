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
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.*;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.data.util.converter.ConverterUtil;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.Table.ColumnHeaderMode;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.Actions;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ShowInfoAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.LookupComponent.LookupSelectionChangeNotifier;
import io.jmix.ui.component.columnmanager.ColumnManager;
import io.jmix.ui.component.data.*;
import io.jmix.ui.component.data.aggregation.Aggregation;
import io.jmix.ui.component.data.aggregation.Aggregations;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EmptyDataUnit;
import io.jmix.ui.component.data.meta.EntityTableItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.component.table.*;
import io.jmix.ui.context.UiEntityAttributeContext;
import io.jmix.ui.context.UiEntityContext;
import io.jmix.ui.context.UiShowEntityInfoContext;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.InstallTargetHandler;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.SettingsHelper;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import io.jmix.ui.settings.component.binder.TableSettingsBinder;
import io.jmix.ui.sys.PersistenceManagerClient;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.JmixEnhancedTable;
import io.jmix.ui.widget.JmixEnhancedTable.AggregationInputValueChangeContext;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import io.jmix.ui.widget.compatibility.JmixValueChangeEvent;
import io.jmix.ui.widget.data.AggregationContainer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("deprecation")
public abstract class WebAbstractTable<T extends com.vaadin.v7.ui.Table & JmixEnhancedTable, E extends JmixEntity>
        extends WebAbstractActionsHolderComponent<T>
        implements Table<E>, TableItemsEventsDelegate<E>, LookupSelectionChangeNotifier<E>,
        HasInnerComponents, InstallTargetHandler, InitializingBean, ColumnManager {

    private static final Logger log = LoggerFactory.getLogger(WebAbstractTable.class);

    public static final int MAX_TEXT_LENGTH_GAP = 10;

    public static final String BOOLEAN_CELL_STYLE_TRUE = "boolean-cell boolean-cell-true";
    public static final String BOOLEAN_CELL_STYLE_FALSE = "boolean-cell boolean-cell-false";

    protected static final com.vaadin.v7.ui.Table.ColumnGenerator VOID_COLUMN_GENERATOR =
            (source, itemId, columnId) -> null;

    protected static final String HAS_TOP_PANEL_STYLENAME = "has-top-panel";
    protected static final String CUSTOM_STYLE_NAME_PREFIX = "cs ";

    protected static final String EDIT_ACTION_ID = "edit";
    protected static final String VIEW_ACTION_ID = "view";

    // Vaadin considers null as row header property id
    protected static final Object ROW_HEADER_PROPERTY_ID = null;

    // Beans

    protected UiProperties uiProperties;
    protected IconResolver iconResolver;
    protected MetadataTools metadataTools;
    protected Metadata metadata;
    protected AccessManager accessManager;
    protected Messages messages;
    protected MessageTools messageTools;
    protected PersistenceManagerClient persistenceManagerClient;
    protected DatatypeRegistry datatypeRegistry;
    protected DataComponents dataComponents;
    protected FetchPlanRepository viewRepository;
    protected UserSettingsTools userSettingsTools;
    protected EntityStates entityStates;
    protected Actions actions;
    protected UiComponentsGenerator uiComponentsGenerator;
    protected Aggregations aggregations;

    protected Locale locale;

    // Style names used by table itself
    protected List<String> internalStyles = new ArrayList<>(2);

    protected Map<Object, Table.Column<E>> columns = new HashMap<>();
    protected List<Table.Column<E>> columnsOrder = new ArrayList<>();

    protected boolean sortable = true;
    protected boolean editable;
    protected Action itemClickAction;
    protected Action enterPressAction;

    @Nullable
    protected BiFunction<? super E, String, String> itemDescriptionProvider;
    protected Function<? super E, String> iconProvider;
    @Nullable
    protected List<Table.StyleProvider> styleProviders; // lazily initialized List
    @Nullable
    protected Map<Table.Column, String> requiredColumns; // lazily initialized Map
    @Nullable
    protected Map<JmixEntity, Object> fieldDatasources; // lazily initialized WeakHashMap;

    protected TableComposition componentComposition;

    protected HorizontalLayout topPanel;

    protected ButtonsPanel buttonsPanel;
    protected TablePagination pagination;

    protected Map<Table.Column, String> aggregationCells = null;

    protected boolean usePresentations;
    protected TablePresentations presentations;

    protected TableSettings defaultTableSettings;

    protected com.vaadin.v7.ui.Table.ColumnCollapseListener columnCollapseListener;

    protected AggregationDistributionProvider<E> distributionProvider;

    // Map column id to Printable representation
    // todo this functionality should be moved to Excel action
    protected Map<String, Printable> printables; // lazily initialized Map

    protected TableDataContainer<E> dataBinding;

    protected boolean ignoreUnfetchedAttributes;

    protected com.vaadin.v7.ui.Table.ColumnGenerator VALUE_PROVIDER_GENERATOR =
            (source, itemId, columnId) -> formatCellValue(itemId, columnId, null);

    protected Consumer<EmptyStateClickEvent<E>> emptyStateClickLinkHandler;

    protected WebAbstractTable() {
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setPersistenceManagerClient(PersistenceManagerClient persistenceManagerClient) {
        this.persistenceManagerClient = persistenceManagerClient;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.locale = currentAuthentication.getLocale();
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setDataComponents(DataComponents dataComponents) {
        this.dataComponents = dataComponents;
    }

    @Autowired
    public void setViewRepository(FetchPlanRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    @Autowired(required = false)
    public void setUserSettingsTools(UserSettingsTools userSettingsTools) {
        this.userSettingsTools = userSettingsTools;
    }

    @Autowired
    public void setEntityStates(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setUiComponentsGenerator(UiComponentsGenerator uiComponentsGenerator) {
        this.uiComponentsGenerator = uiComponentsGenerator;
    }

    @Autowired
    public void setAggregations(Aggregations aggregations) {
        this.aggregations = aggregations;
    }

    @Override
    public Collection<io.jmix.ui.component.Component> getInnerComponents() {
        if (buttonsPanel != null) {
            return Collections.singletonList(buttonsPanel);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Set<Object> getSelectedItemIds() {
        Object value = component.getValue();
        if (value == null) {
            return null;
        } else if (value instanceof Set) {
            return (Set) value;
        } else if (value instanceof Collection) {
            return new LinkedHashSet((Collection) value);
        } else {
            return Collections.singleton(value);
        }
    }

    @Override
    public E getSingleSelected() {
        TableItems<E> tableItems = getItems();
        if (tableItems == null
                || tableItems.getState() == BindingState.INACTIVE) {
            return null;
        }

        Set selected = getSelectedItemIds();
        return selected == null || selected.isEmpty() ?
                null : tableItems.getItem(selected.iterator().next());
    }

    @Override
    public Set<E> getSelected() {
        TableItems<E> tableItems = getItems();
        if (tableItems == null
                || tableItems.getState() == BindingState.INACTIVE) {
            return Collections.emptySet();
        }

        Set<Object> itemIds = getSelectedItemIds();

        if (itemIds != null) {
            if (itemIds.size() == 1) {
                E item = tableItems.getItem(itemIds.iterator().next());
                return item != null
                        ? Collections.singleton(item)
                        : Collections.emptySet();
            }

            Set<E> res = new LinkedHashSet<>();
            for (Object id : itemIds) {
                E item = tableItems.getItem(id);
                if (item != null) {
                    res.add(item);
                }
            }
            return Collections.unmodifiableSet(res);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void setSelected(@Nullable E item) {
        if (item == null) {
            component.setValue(null);
        } else {
            setSelected(Collections.singletonList(item));
        }
    }

    @Override
    public void setSelected(Collection<E> items) {
        TableItems<E> tableItems = getItems();
        if (tableItems == null
                || tableItems.getState() == BindingState.INACTIVE) {
            throw new IllegalStateException("TableItems is not active");
        }

        if (items.isEmpty()) {
            setSelectedIds(Collections.emptyList());
        } else if (items.size() == 1) {
            E item = items.iterator().next();
            if (tableItems.getItem(EntityValues.getId(item)) == null) {
                throw new IllegalArgumentException("Datasource doesn't contain item to select: " + item);
            }
            setSelectedIds(Collections.singletonList(EntityValues.getId(item)));
        } else {
            Set<Object> itemIds = new LinkedHashSet<>();
            for (JmixEntity item : items) {
                if (tableItems.getItem(EntityValues.getId(item)) == null) {
                    throw new IllegalArgumentException("Datasource doesn't contain item to select: " + item);
                }
                itemIds.add(EntityValues.getId(item));
            }
            setSelectedIds(itemIds);
        }
    }

    protected void setSelectedIds(Collection<Object> itemIds) {
        if (component.isMultiSelect()) {
            component.setValue(itemIds);
        } else {
            component.setValue(itemIds.size() > 0 ? itemIds.iterator().next() : null);
        }
    }

    @Override
    protected void attachAction(Action action) {
        if (action instanceof Action.HasTarget) {
            ((Action.HasTarget) action).setTarget(this);
        }

        super.attachAction(action);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public boolean isMultiSelect() {
        return component.isMultiSelect();
    }

    @Override
    public void setMultiSelect(boolean multiselect) {
        component.setMultiSelect(multiselect);
    }

    @Override
    public List<Table.Column<E>> getColumns() {
        return Collections.unmodifiableList(columnsOrder);
    }

    public Map<Object, Column<E>> getColumnsInternal() {
        return columns;
    }

    @Nullable
    public Map<Column, String> getRequiredColumnsInternal() {
        return requiredColumns;
    }

    @Nullable
    @Override
    public Table.Column<E> getColumn(String id) {
        for (Table.Column<E> column : columnsOrder) {
            if (column.getStringId().equals(id))
                return column;
        }
        return null;
    }

    @Override
    public void addColumn(Table.Column<E> column) {
        addColumnInternal(column, columnsOrder.size());
    }

    @Override
    public void addColumn(Column<E> column, int index) {
        addColumnInternal(column, index);

        // Update column order only if we add a column to an arbitrary position.
        component.setVisibleColumns(columnsOrder.stream()
                .map(Table.Column::getId)
                .toArray());
    }

    protected void addColumnInternal(Column<E> column, int index) {
        checkNotNullArgument(column, "Column must be non null");

        Object columnId = column.getId();
        component.addContainerProperty(columnId, column.getType(), null);

        if (StringUtils.isNotBlank(column.getDescription())) {
            component.setColumnDescription(columnId, column.getDescription());
        }

        if (StringUtils.isNotBlank(column.getValueDescription())) {
            component.setAggregationDescription(columnId, column.getValueDescription());
        } else if (column.getAggregation() != null
                && column.getAggregation().getType() != AggregationInfo.Type.CUSTOM) {
            setColumnAggregationDescriptionByType(column, columnId);
        }

        if (!column.isSortable()) {
            component.setColumnSortable(columnId, column.isSortable());
        }

        columns.put(columnId, column);
        columnsOrder.add(index, column);
        if (column.getWidth() != null) {
            component.setColumnWidth(columnId, column.getWidth());
        }
        if (column.getAlignment() != null) {
            component.setColumnAlignment(columnId,
                    WebWrapperUtils.convertColumnAlignment(column.getAlignment()));
        }

        setColumnHeader(columnId, getColumnCaption(columnId, column));

        component.setColumnCaptionAsHtml(columnId, column.getCaptionAsHtml());

        if (column.getExpandRatio() != null) {
            component.setColumnExpandRatio(columnId, column.getExpandRatio());
        }

        column.setOwner(this);

        MetaPropertyPath propertyPath = column.getBoundProperty();
        if (propertyPath != null) {
            MetaProperty metaProperty = propertyPath.getMetaProperty();
            MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(propertyPath);
            String storeName = metadataTools.getStoreName(propertyMetaClass);
            if (metadataTools.isLob(metaProperty)
                    && !persistenceManagerClient.supportsLobSortingAndFiltering(storeName)) {
                component.setColumnSortable(columnId, false);
            }
        }

        if (column.isAggregationEditable()) {
            component.addAggregationEditableColumn(columnId);
        }
    }

    protected void setColumnAggregationDescriptionByType(Column<E> column, Object columnId) {
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
                throw new IllegalArgumentException(
                        String.format("AggregationType %s is not supported",
                                column.getAggregation().getType().toString()));
        }

        component.setAggregationDescription(columnId, messages.getMessage(aggregationTypeLabel));
    }

    @Override
    public void removeColumn(Table.Column column) {
        Preconditions.checkNotNullArgument(column);

        component.removeContainerProperty(column.getId());
        columns.remove(column.getId());
        columnsOrder.remove(column);

        column.setOwner(null);
    }

    /*
    TODO: legacy-ui
    @SuppressWarnings("unchecked")
    @Override
    public Datasource getItemDatasource(Entity item) {
        if (fieldDatasources == null) {
            fieldDatasources = new WeakHashMap<>();
        }

        Object fieldDatasource = fieldDatasources.get(item);
        if (fieldDatasource instanceof Datasource) {
            return (Datasource) fieldDatasource;
        }

        EntityTableItems containerTableItems = (EntityTableItems) getItems();
        Datasource datasource = DsBuilder.create()
                .setAllowCommit(false)
                .setMetaClass(containerTableItems.getEntityMetaClass())
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .setViewName(View.LOCAL)
                .buildDatasource();

        ((DatasourceImplementation) datasource).valid();

        datasource.setItem(item);
        fieldDatasources.put(item, datasource);

        return datasource;
    }*/

    @SuppressWarnings("unchecked")
    @Override
    public InstanceContainer<E> getInstanceContainer(E item) {
        if (fieldDatasources == null) {
            fieldDatasources = new WeakHashMap<>();
        }

        Object fieldDatasource = fieldDatasources.get(item);
        if (fieldDatasource instanceof InstanceContainer) {
            return (InstanceContainer<E>) fieldDatasource;
        }

        EntityTableItems containerTableItems = (EntityTableItems) getItems();
        if (containerTableItems == null) {
            throw new IllegalStateException("Table is not bound to items");
        }

        InstanceContainer<E> instanceContainer;
        MetaClass metaClass = containerTableItems.getEntityMetaClass();
        if (metaClass instanceof KeyValueMetaClass) {
            instanceContainer = (InstanceContainer<E>) dataComponents.createKeyValueContainer(metaClass);
        } else {
            instanceContainer = dataComponents.createInstanceContainer(metaClass.getJavaClass());
        }
        FetchPlan view = viewRepository.getFetchPlan(metaClass, FetchPlan.LOCAL);
        instanceContainer.setFetchPlan(view);
        instanceContainer.setItem(item);

        fieldDatasources.put(item, instanceContainer);

        return instanceContainer;
    }

    protected void addGeneratedColumnInternal(Object id, com.vaadin.v7.ui.Table.ColumnGenerator generator) {
        component.addGeneratedColumn(id, generator);
    }

    protected void removeGeneratedColumnInternal(Object id) {
        boolean wasEnabled = component.disableContentBufferRefreshing();

        com.vaadin.v7.ui.Table.ColumnGenerator columnGenerator = component.getColumnGenerator(id);
        if (columnGenerator instanceof CustomColumnGenerator) {
            CustomColumnGenerator tableGenerator = (CustomColumnGenerator) columnGenerator;
            if (tableGenerator.getAssociatedRuntimeColumn() != null) {
                removeColumn(tableGenerator.getAssociatedRuntimeColumn());
            }
        }
        component.removeGeneratedColumn(id);

        component.enableContentBufferRefreshing(wasEnabled);
    }

    @Override
    public void addPrintable(String columnId, Printable<? super E, ?> printable) {
        if (printables == null) {
            printables = new HashMap<>();
        }
        printables.put(columnId, printable);
    }

    @Override
    public void removePrintable(String columnId) {
        if (printables != null) {
            printables.remove(columnId);
        }
    }

    @Override
    @Nullable
    public Printable getPrintable(Table.Column column) {
        return getPrintable(column.getStringId());
    }

    @Nullable
    @Override
    public Printable getPrintable(String columnId) {
        Printable printable = printables != null ? printables.get(columnId) : null;
        if (printable != null) {
            return printable;
        } else {
            com.vaadin.v7.ui.Table.ColumnGenerator vColumnGenerator = component.getColumnGenerator(getColumn(columnId).getId());
            if (vColumnGenerator instanceof CustomColumnGenerator) {
                ColumnGenerator columnGenerator = ((CustomColumnGenerator) vColumnGenerator).getColumnGenerator();
                if (columnGenerator instanceof Printable) {
                    return (Printable) columnGenerator;
                }
            }
            return null;
        }
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            this.editable = editable;

            component.disableContentBufferRefreshing();

            EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();

            if (entityTableSource != null) {
                com.vaadin.v7.data.Container ds = component.getContainerDataSource();

                @SuppressWarnings("unchecked")
                Collection<MetaPropertyPath> propertyIds = (Collection<MetaPropertyPath>) ds.getContainerPropertyIds();

                if (editable) {
                    enableEditableColumns(entityTableSource, propertyIds);
                } else {
                    disableEditableColumns(entityTableSource, propertyIds);
                }
            }

            component.setEditable(editable);

            component.enableContentBufferRefreshing(true);
        }
    }

    protected void enableEditableColumns(EntityTableItems<E> entityTableSource,
                                         Collection<MetaPropertyPath> propertyIds) {
        MetaClass metaClass = entityTableSource.getEntityMetaClass();

        List<MetaPropertyPath> editableColumns = new ArrayList<>(propertyIds.size());
        for (MetaPropertyPath propertyId : propertyIds) {
            UiEntityAttributeContext attributeContext =
                    new UiEntityAttributeContext(metaClass, propertyId.toString());
            accessManager.applyRegisteredConstraints(attributeContext);

            if (!attributeContext.isModifyPermitted()) {
                continue;
            }

            Column column = getColumn(propertyId.toString());
            if (column != null && BooleanUtils.isTrue(column.isEditable())) {
                com.vaadin.v7.ui.Table.ColumnGenerator generator = component.getColumnGenerator(column.getId());
                if (generator != null) {
                    if (generator instanceof SystemTableColumnGenerator) {
                        // remove default generator
                        component.removeGeneratedColumn(propertyId);
                    } else {
                        // do not edit generated columns
                        continue;
                    }
                }

                editableColumns.add(propertyId);
            }
        }
        setEditableColumns(editableColumns);
    }

    @SuppressWarnings("unchecked")
    protected void disableEditableColumns(@SuppressWarnings("unused") EntityTableItems<E> entityTableSource,
                                          Collection<MetaPropertyPath> propertyIds) {
        setEditableColumns(Collections.emptyList());

        // restore generators for some type of attributes
        for (MetaPropertyPath propertyId : propertyIds) {
            Column column = columns.get(propertyId);
            if (column != null) {
                String isLink = column.getXmlDescriptor() == null ?
                        null : column.getXmlDescriptor().attributeValue("link");

                if (component.getColumnGenerator(column.getId()) == null) {
                    if (propertyId.getRange().isClass()) {
                        if (StringUtils.isNotEmpty(isLink)) {
                            setClickListener(propertyId.toString(), new LinkCellClickListener(this, applicationContext));
                        }
                    } else if (propertyId.getRange().isDatatype()) {
                        if (StringUtils.isNotEmpty(isLink)) {
                            setClickListener(propertyId.toString(), new LinkCellClickListener(this, applicationContext));
                        } else {
                            if (column.getMaxTextLength() != null) {
                                addGeneratedColumnInternal(propertyId, new AbbreviatedColumnGenerator(column, metadataTools));
                            }
                        }
                    }
                }
            }
        }
    }

    protected void setEditableColumns(List<MetaPropertyPath> editableColumns) {
        component.setEditableColumns(editableColumns.toArray());
    }

    @Override
    public boolean isSortable() {
        return component.isSortEnabled();
    }

    @Override
    public void setSortable(boolean sortable) {
        this.sortable = sortable;

        component.setSortEnabled(sortable && canBeSorted(getItems()));
    }

    @Override
    public void setAutoScrolling(boolean autoScroll) {
    }

    @Override
    public boolean isAutoScrolling() {
        return true;
    }

    @Override
    public boolean getColumnReorderingAllowed() {
        return component.isColumnReorderingAllowed();
    }

    @Override
    public void setColumnReorderingAllowed(boolean columnReorderingAllowed) {
        component.setColumnReorderingAllowed(columnReorderingAllowed);
    }

    @Override
    public boolean getColumnControlVisible() {
        return component.isColumnCollapsingAllowed();
    }

    @Override
    public void setColumnControlVisible(boolean columnCollapsingAllowed) {
        component.setColumnCollapsingAllowed(columnCollapsingAllowed);
    }

    @Override
    public void sortBy(Object propertyId, boolean ascending) {
        if (isSortable()) {
            component.setSortOptions(propertyId, ascending);
            component.sort();
        }
    }

    @Override
    public void sort(String columnId, SortDirection direction) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalArgumentException("Unable to find column " + columnId);
        }

        if (isSortable()) {
            component.setSortOptions(column.getId(), direction == SortDirection.ASCENDING);
            component.sort();
        }
    }

    @Nullable
    @Override
    public SortInfo getSortInfo() {
        Object sortContainerPropertyId = component.getSortContainerPropertyId();
        return sortContainerPropertyId != null
                ? new SortInfo(sortContainerPropertyId, component.isSortAscending())
                : null;
    }

    @Nullable
    @Override
    public TablePagination getPagination() {
        return pagination;
    }

    @Override
    public void setPagination(@Nullable TablePagination pagination) {
        if (this.pagination != null && topPanel != null) {
            topPanel.removeComponent(this.pagination.unwrap(com.vaadin.ui.Component.class));
        }
        this.pagination = pagination;
        if (pagination != null) {
            if (topPanel == null) {
                topPanel = createTopPanel();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel);
            }
            pagination.setWidthAuto();
            com.vaadin.ui.Component rc = pagination.unwrap(com.vaadin.ui.Component.class);
            topPanel.addComponent(rc);

            if (pagination instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) pagination).addVisibilityChangeListener(event ->
                        updateCompositionStylesTopPanelVisible()
                );
            }
        }

        updateCompositionStylesTopPanelVisible();
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
                componentComposition.removeStyleName(HAS_TOP_PANEL_STYLENAME);

                internalStyles.remove(HAS_TOP_PANEL_STYLENAME);
            } else {
                componentComposition.addStyleName(HAS_TOP_PANEL_STYLENAME);

                if (!internalStyles.contains(HAS_TOP_PANEL_STYLENAME)) {
                    internalStyles.add(HAS_TOP_PANEL_STYLENAME);
                }
            }
        }
    }

    @Override
    public boolean isMultiLineCells() {
        return component.isMultiLineCells();
    }

    @Override
    public void setMultiLineCells(boolean multiLineCells) {
        component.setMultiLineCells(multiLineCells);
    }

    @Override
    public boolean isAggregatable() {
        return component.isAggregatable();
    }

    @Override
    public void setAggregatable(boolean aggregatable) {
        component.setAggregatable(aggregatable);
    }

    @Override
    public Map<Object, Object> getAggregationResults() {
        Collection<?> itemIds = WebAbstractTable.this.getItems().getItemIds();
        return component.aggregateValues(new AggregationContainer.Context(itemIds));
    }

    @Override
    public AggregationStyle getAggregationStyle() {
        return AggregationStyle.valueOf(component.getAggregationStyle().name());
    }

    @Override
    public void setAggregationStyle(AggregationStyle aggregationStyle) {
        component.setAggregationStyle(JmixEnhancedTable.AggregationStyle.valueOf(aggregationStyle.name()));
    }

    @Override
    public boolean isShowTotalAggregation() {
        return component.isShowTotalAggregation();
    }

    @Override
    public void setShowTotalAggregation(boolean showAggregation) {
        component.setShowTotalAggregation(showAggregation);
    }

    @Override
    public com.vaadin.ui.Component getComposition() {
        return componentComposition;
    }

    @Override
    public boolean isContextMenuEnabled() {
        return component.isContextMenuEnabled();
    }

    @Override
    public void setContextMenuEnabled(boolean contextMenuEnabled) {
        component.setContextMenuEnabled(contextMenuEnabled);
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    protected void setTablePresentationsLayout(TablePresentationsLayout tablePresentations) {
        component.setPresentationsLayout(tablePresentations);
    }

    protected void initComponent(T component) {
        component.setMultiSelect(false);
        component.setValidationVisible(false);
        component.setShowBufferedSourceException(false);

        component.setCustomCellValueFormatter(this::formatCellValue);

        component.addValueChangeListener(this::tableSelectionChanged);
        component.setSpecificVariablesHandler(this::handleSpecificVariables);
        component.setIconProvider(this::getItemIcon);
        component.setBeforePaintListener(this::beforeComponentPaint);

        component.setSortAscendingLabel(messages.getMessage("tableSort.ascending"));
        component.setSortResetLabel(messages.getMessage("tableSort.reset"));
        component.setSortDescendingLabel(messages.getMessage("tableSort.descending"));

        component.setSelectAllLabel(messages.getMessage("tableColumnSelector.selectAll"));
        component.setDeselectAllLabel(messages.getMessage("tableColumnSelector.deselectAll"));

        int defaultRowHeaderWidth = 16;
        ThemeConstantsManager themeConstantsManager =
                applicationContext.getBean(ThemeConstantsManager.NAME, ThemeConstantsManager.class);
        ThemeConstants theme = themeConstantsManager.getConstants();
        if (theme != null) {
            defaultRowHeaderWidth = theme.getInt("cuba.web.Table.defaultRowHeaderWidth", 16);
        }

        component.setColumnWidth(ROW_HEADER_PROPERTY_ID, defaultRowHeaderWidth);

        contextMenuPopup.setParent(component);
        component.setContextMenuPopup(contextMenuPopup);

        shortcutsDelegate.setAllowEnterShortcut(false);

        component.addShortcutListener(
                new ShortcutListenerDelegate("tableEnter", KeyCode.ENTER, null)
                        .withHandler((sender, target) -> {
                            T tableImpl = WebAbstractTable.this.component;

                            AppUI ui = (AppUI) tableImpl.getUI();
                            if (!ui.isAccessibleForUser(tableImpl)) {
                                LoggerFactory.getLogger(WebAbstractTable.class)
                                        .debug("Ignore click attempt because Table is inaccessible for user");
                                return;
                            }

                            if (target == this.component) {
                                if (enterPressAction != null) {
                                    enterPressAction.actionPerform(this);
                                } else {
                                    handleClickAction();
                                }
                            }
                        }));

        component.addShortcutListener(
                new ShortcutListenerDelegate("tableSelectAll", KeyCode.A,
                        new int[]{com.vaadin.event.ShortcutAction.ModifierKey.CTRL})
                        .withHandler((sender, target) -> {
                            if (target == this.component) {
                                selectAll();
                            }
                        }));

        component.addItemClickListener(event -> {
            if (event.isDoubleClick() && event.getItem() != null) {
                T tableImpl = WebAbstractTable.this.component;

                AppUI ui = (AppUI) tableImpl.getUI();
                if (!ui.isAccessibleForUser(tableImpl)) {
                    LoggerFactory.getLogger(WebAbstractTable.class)
                            .debug("Ignore click attempt because Table is inaccessible for user");
                    return;
                }

                handleClickAction();
            }
        });

        component.setAfterUnregisterComponentHandler(this::onAfterUnregisterComponent);
        component.setBeforeRefreshRowCacheHandler(this::onBeforeRefreshRowCache);

        component.setSelectable(true);
        component.setTableFieldFactory(createFieldFactory());
        component.setColumnCollapsingAllowed(true);
        component.setColumnReorderingAllowed(true);

        setEditable(false);

        componentComposition = new TableComposition();
        componentComposition.setTable(component);
        componentComposition.setPrimaryStyleName("c-table-composition");
        componentComposition.addComponent(component);

        component.setCellStyleGenerator(createStyleGenerator());
        component.addColumnCollapseListener(this::handleColumnCollapsed);

        // force default sizes
        componentComposition.setHeightUndefined();
        componentComposition.setWidthUndefined();

        setClientCaching();
        initEmptyState();
    }

    protected void onAfterUnregisterComponent(Component component) {
        Object data = ((AbstractComponent) component).getData();
        if (data instanceof HasValueSource) {
            HasValueSource<?> hasValueSource = (HasValueSource) data;

            // if it supports value binding and bound to ValueSource, we need to unsubscribe it
            if (hasValueSource.getValueSource() != null) {
                hasValueSource.setValueSource(null);
            }
        }
    }

    protected void onBeforeRefreshRowCache() {
        clearFieldDatasources();
    }

    protected void tableSelectionChanged(@SuppressWarnings("unused") Property.ValueChangeEvent event) {
        EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();

        if (entityTableSource == null
                || entityTableSource.getState() == BindingState.INACTIVE) {
            return;
        }

        Set<E> selected = getSelected();
        if (selected.isEmpty()) {
            entityTableSource.setSelectedItem(null);
        } else {
            // reset selection and select new item
            if (isMultiSelect()) {
                entityTableSource.setSelectedItem(null);
            }

            E newItem = selected.iterator().next();
            E dsItem = entityTableSource.getSelectedItem();
            entityTableSource.setSelectedItem(newItem);

            if (Objects.equals(dsItem, newItem)) {
                // in this case item change event will not be generated
                refreshActionsState();
            }
        }

        fireSelectionEvent(event);

        LookupSelectionChangeEvent<E> selectionChangeEvent = new LookupSelectionChangeEvent<>(this);
        publish(LookupSelectionChangeEvent.class, selectionChangeEvent);
    }

    protected void fireSelectionEvent(Property.ValueChangeEvent e) {
        boolean userOriginated = e instanceof JmixValueChangeEvent
                && ((JmixValueChangeEvent) e).isUserOriginated();

        SelectionEvent<E> event =
                new SelectionEvent<>(this, getSelected(), userOriginated);
        publish(SelectionEvent.class, event);
    }

    @Nullable
    protected String formatCellValue(Object rowId, Object colId, @Nullable Property<?> property) {
        TableItems<E> tableItems = getItems();
        if (tableItems == null
                || tableItems.getState() == BindingState.INACTIVE) {
            return null;
        }

        Column<E> column = columns.get(colId);
        if (column != null && column.getValueProvider() != null) {
            E item = tableItems.getItem(rowId);
            Object generatedValue = column.getValueProvider().apply(item);
            Formatter formatter = column.getFormatter();

            if (formatter != null) {
                return (String) formatter.apply(generatedValue);
            }

            return metadataTools.format(generatedValue);
        }

        Object cellValue;
        if (ignoreUnfetchedAttributes
                && colId instanceof MetaPropertyPath) {
            E item = tableItems.getItem(rowId);
            cellValue = getValueExIgnoreUnfetched(item, ((MetaPropertyPath) colId).getPath());
        } else if (property != null) {
            cellValue = property.getValue();
        } else {
            cellValue = null;
        }

        if (colId instanceof MetaPropertyPath) {
            MetaPropertyPath propertyPath = (MetaPropertyPath) colId;

            if (column != null) {
                if (column.getFormatter() != null) {
                    return (String) column.getFormatter().apply(cellValue);
                } else if (column.getXmlDescriptor() != null) {
                    // vaadin8 move to Column
                    String captionProperty = column.getXmlDescriptor().attributeValue("captionProperty");
                    if (StringUtils.isNotEmpty(captionProperty)) {
                        E item = getItems().getItemNN(rowId);
                        Object captionValue = EntityValues.getValueEx(item, captionProperty);
                        return captionValue != null ? String.valueOf(captionValue) : null;
                    }
                }
            }

            return metadataTools.format(cellValue, propertyPath.getMetaProperty());
        }

        if (cellValue == null) {
            return "";
        }

        if (!(cellValue instanceof Component)) {
            return metadataTools.format(cellValue);
        }

        // fallback to Vaadin formatting
        UI ui = component.getUI();
        VaadinSession session = ui != null ? ui.getSession() : null;
        Converter converter = ConverterUtil.getConverter(String.class, property.getType(), session);
        if (converter != null) {
            return (String) converter.convertToPresentation(cellValue, String.class, locale);
        }

        return cellValue.toString();
    }

    protected WebTableFieldFactory createFieldFactory() {
        return new WebTableFieldFactory<>(this, accessManager, metadataTools, uiComponentsGenerator);
    }

    protected void setClientCaching() {
        double cacheRate = uiProperties.getTableCacheRate();
        cacheRate = cacheRate >= 0 ? cacheRate : 2;

        int pageLength = uiProperties.getTablePageLength();
        pageLength = pageLength >= 0 ? pageLength : 15;

        componentComposition.setClientCaching(cacheRate, pageLength);
    }

    protected void refreshActionsState() {
        for (Action action : getActions()) {
            action.refreshState();
        }
    }

    protected StyleGeneratorAdapter createStyleGenerator() {
        return new StyleGeneratorAdapter();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected String getGeneratedCellStyle(Object itemId, @Nullable Object propertyId) {
        if (styleProviders == null) {
            return null;
        }
        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return null;
        }

        E item = tableItems.getItem(itemId);

        String propertyStringId = propertyId == null ? null : propertyId.toString();

        String joinedStyle = styleProviders.stream()
                .map(sp -> sp.getStyleName(item, propertyStringId))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        return Strings.emptyToNull(joinedStyle);
    }

    @Nullable
    protected String generateCellDescription(Object itemId, @Nullable Object propertyId) {
        if (itemDescriptionProvider == null) {
            return null;
        }

        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return null;
        }

        E item = tableItems.getItem(itemId);
        String property = propertyId == null
                ? null
                : propertyId.toString();

        return itemDescriptionProvider.apply(item, property);
    }

    @Override
    protected JmixButton createContextMenuButton() {
        return new JmixButton();
    }

    @Override
    protected void beforeContextMenuButtonHandlerPerformed() {
        this.component.hideContextMenuPopup();
    }

    protected void handleClickAction() {
        Action action = getItemClickAction();
        if (action == null) {
            action = getEnterAction();
            if (action == null) {
                action = getAction(EDIT_ACTION_ID);
                if (action == null) {
                    action = getAction(VIEW_ACTION_ID);
                }
            }
        }

        if (action != null && action.isEnabled()) {
            action.actionPerform(this);
        }
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<E>> selectHandler) {
        Consumer<Action.ActionPerformedEvent> actionHandler = event -> {
            Set<E> selected = getSelected();
            selectHandler.accept(selected);
        };

        setEnterPressAction(
                new BaseAction(Window.Lookup.LOOKUP_ENTER_PRESSED_ACTION_ID)
                        .withHandler(actionHandler)
        );

        setItemClickAction(
                new BaseAction(Window.Lookup.LOOKUP_ITEM_CLICK_ACTION_ID)
                        .withHandler(actionHandler)
        );

        removeAllClickListeners();

        if (isEditable()) {
            EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();
            com.vaadin.v7.data.Container ds = component.getContainerDataSource();
            @SuppressWarnings("unchecked")
            Collection<MetaPropertyPath> propertyIds = (Collection<MetaPropertyPath>) ds.getContainerPropertyIds();

            disableEditableColumns(entityTableSource, propertyIds);
        }

        if (buttonsPanel != null && !buttonsPanel.isAlwaysVisible()) {
            buttonsPanel.setVisible(false);
            setContextMenuEnabled(false);
        }
    }

    protected void removeAllClickListeners() {
        for (Column column : columnsOrder) {
            component.removeClickListener(column.getId());
            component.removeTableCellClickListener(column.getId());
        }
    }

    @Override
    public Collection<E> getLookupSelectedItems() {
        return getSelected();
    }

    @Nullable
    protected Action getEnterAction() {
        for (Action action : getActions()) {
            KeyCombination kc = action.getShortcutCombination();
            if (kc != null
                    && (kc.getModifiers() == null || kc.getModifiers().length == 0)
                    && kc.getKey() == KeyCombination.Key.ENTER) {
                return action;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void createColumns(com.vaadin.v7.data.Container ds) {
        Collection<MetaPropertyPath> properties = (Collection<MetaPropertyPath>) ds.getContainerPropertyIds();

        for (MetaPropertyPath propertyPath : properties) {
            Table.Column column = columns.get(propertyPath);

            if (column != null && !(editable && column.isEditable())) {
                String isLink = column.getXmlDescriptor() == null ?
                        null : column.getXmlDescriptor().attributeValue("link");

                if (propertyPath.getRange().isClass()) {
                    if (StringUtils.isNotEmpty(isLink)) {
                        setClickListener(propertyPath.toString(), new LinkCellClickListener(this, applicationContext));
                    }
                } else if (propertyPath.getRange().isDatatype()) {
                    if (StringUtils.isNotEmpty(isLink)) {
                        setClickListener(propertyPath.toString(), new LinkCellClickListener(this, applicationContext));
                    } else {
                        if (column.getMaxTextLength() != null) {
                            addGeneratedColumnInternal(propertyPath, new AbbreviatedColumnGenerator(column, metadataTools));
                            setClickListener(propertyPath.toString(), new AbbreviatedCellClickListener(this, metadata, metadataTools));
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public TableItems<E> getItems() {
        return this.dataBinding != null ? this.dataBinding.getTableItems() : null;
    }

    @Override
    public void setItems(@Nullable TableItems<E> tableItems) {
        if (this.dataBinding != null) {
            this.dataBinding.unbind();
            this.dataBinding = null;

            clearFieldDatasources();

            this.component.setContainerDataSource(null);
        }

        if (tableItems != null) {
            // Table supports only EntityTableItems
            EntityTableItems<E> entityTableSource = (EntityTableItems<E>) tableItems;

            if (this.columns.isEmpty()) {
                setupAutowiredColumns(entityTableSource);
            }

            // bind new datasource
            this.dataBinding = createTableDataContainer(tableItems);
            this.dataBinding.setProperties(getPropertyColumns(entityTableSource, columnsOrder));
            this.component.setContainerDataSource(this.dataBinding);

            setupColumnSettings(entityTableSource);

            createColumns(component.getContainerDataSource());

            for (Table.Column column : this.columnsOrder) {
                if (editable && column.getAggregation() != null
                        && (BooleanUtils.isTrue(column.isEditable()))) {
                    addAggregationCell(column);
                }
            }

            createStubsForGeneratedColumns();

            setVisibleColumns(getInitialVisibleColumnIds(entityTableSource));

            UiShowEntityInfoContext showInfoContext = new UiShowEntityInfoContext();
            accessManager.applyRegisteredConstraints(showInfoContext);

            if (showInfoContext.isPermitted()) {
                if (getAction(ShowInfoAction.ACTION_ID) == null) {
                    addAction(actions.create(ShowInfoAction.ACTION_ID));
                }
            }

            if (pagination != null) {
                pagination.setTablePaginationTarget(this);
            }

            if (!canBeSorted(tableItems)) {
                setSortable(false);
            } else { // restore sortable
                setSortable(sortable);
            }

            // resort data if table have been sorted before setting items
            if (isSortable()) {
                if (getSortInfo() != null) {
                    SortDirection sortDirection = getSortInfo().getAscending()
                            ? SortDirection.ASCENDING : SortDirection.DESCENDING;

                    Object columnId = getSortInfo().getPropertyId();
                    String id = columnId instanceof MetaPropertyPath
                            ? ((MetaPropertyPath) columnId).toPathString() : String.valueOf(columnId);

                    sort(id, sortDirection);
                }
            }

            refreshActionsState();

            setUiTestId(tableItems);
        }

        initEmptyState();
    }

    protected void setUiTestId(TableItems<E> items) {
        AppUI ui = AppUI.getCurrent();

        if (ui != null && ui.isTestMode()
                && getComponent().getJTestId() == null) {

            String testId = UiTestIds.getInferredTestId(items, "Table");
            if (testId != null) {
                getComponent().setJTestId(testId);
                componentComposition.setJTestId(testId + "_composition");
            }
        }
    }

    protected List<Object> getPropertyColumns(EntityTableItems<E> entityTableSource, List<Column<E>> columnsOrder) {
        MetaClass entityMetaClass = entityTableSource.getEntityMetaClass();
        return columnsOrder.stream()
                .filter(c -> {
                    MetaPropertyPath propertyPath = c.getBoundProperty();
                    if (propertyPath != null) {
                        UiEntityAttributeContext attributeContext =
                                new UiEntityAttributeContext(entityMetaClass, propertyPath.toString());
                        accessManager.applyRegisteredConstraints(attributeContext);

                        return attributeContext.isViewPermitted();
                    }
                    return false;
                })
                .map(Column::getBoundProperty)
                .collect(Collectors.toList());
    }

    protected void setupColumnSettings(EntityTableItems<E> entityTableSource) {
        MetaClass metaClass = entityTableSource.getEntityMetaClass();

        List<MetaPropertyPath> editableColumns = Collections.emptyList();

        for (Map.Entry<Object, Table.Column<E>> entry : this.columns.entrySet()) {
            Object columnId = entry.getKey();
            Column<E> column = entry.getValue();

            String caption;
            if (column != null) {
                caption = getColumnCaption(columnId, column);
            } else {
                caption = StringUtils.capitalize(getColumnCaption(columnId));
            }

            setColumnHeader(columnId, caption);

            if (column != null) {
                if (column.isEditable() && (columnId instanceof MetaPropertyPath)) {
                    MetaPropertyPath propertyPath = ((MetaPropertyPath) columnId);

                    UiEntityAttributeContext attributeContext =
                            new UiEntityAttributeContext(metaClass, propertyPath.toString());
                    accessManager.applyRegisteredConstraints(attributeContext);

                    if (attributeContext.isModifyPermitted() && attributeContext.isViewPermitted()) {
                        if (editableColumns.isEmpty()) {
                            editableColumns = new ArrayList<>();
                        }
                        editableColumns.add(propertyPath);
                    } else {
                        log.info("Editable column '{}' is not permitted to read or update",
                                propertyPath.toString());
                    }
                }

                if (column.isCollapsed() && component.isColumnCollapsingAllowed()) {
                    UiEntityAttributeContext attributeContext =
                            new UiEntityAttributeContext(metaClass, columnId.toString());
                    accessManager.applyRegisteredConstraints(attributeContext);

                    if (!(columnId instanceof MetaPropertyPath) || attributeContext.isViewPermitted()) {
                        component.setColumnCollapsed(column.getId(), true);
                    }
                }

                if (column.getAggregation() != null) {
                    checkAggregation(column.getAggregation());

                    component.addContainerPropertyAggregation(column.getId(),
                            WebWrapperUtils.convertAggregationType(column.getAggregation().getType()));
                }
            }
        }

        if (isEditable() && !editableColumns.isEmpty()) {

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            if (entityContext.isViewPermitted() && entityContext.isEditPermitted()) {
                setEditableColumns(editableColumns);
            } else {
                log.info("Entity '{}' is not permitted to read or update",
                        metaClass.getName());
            }
        }
    }

    protected void setupAutowiredColumns(EntityTableItems<E> entityTableSource) {
        Collection<MetaPropertyPath> paths = getAutowiredProperties(entityTableSource);

        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany()
                    && !metadataTools.isSystem(property)) {
                Table.Column<E> column = new Table.Column<>(metaPropertyPath);

                String propertyName = property.getName();
                MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);

                column.setCaption(messageTools.getPropertyCaption(propertyMetaClass, propertyName));
                column.setType(metaPropertyPath.getRangeJavaClass());

                addColumn(column);
            }
        }
    }

    protected Collection<MetaPropertyPath> getAutowiredProperties(EntityTableItems<E> entityTableSource) {
        if (entityTableSource instanceof ContainerDataUnit) {
            CollectionContainer container = ((ContainerDataUnit) entityTableSource).getContainer();

            return container.getFetchPlan() != null ?
                    // if a view is specified - use view properties
                    metadataTools.getViewPropertyPaths(container.getFetchPlan(), container.getEntityMetaClass()) :
                    // otherwise use all properties from meta-class
                    metadataTools.getPropertyPaths(container.getEntityMetaClass());
        }

        /*if (entityTableSource instanceof DatasourceDataUnit) {

            TODO: legacy-ui
            CollectionDatasource datasource = ((DatasourceDataUnit) entityTableSource).getDatasource();

            return datasource.getView() != null ?
                    // if a view is specified - use view properties
                    metadataTools.getViewPropertyPaths(datasource.getView(), datasource.getMetaClass()) :
                    // otherwise use all properties from meta-class
                    metadataTools.getPropertyPaths(datasource.getMetaClass());
        }*/

        if (entityTableSource instanceof EmptyDataUnit) {
            return metadataTools.getPropertyPaths(entityTableSource.getEntityMetaClass());
        }

        return Collections.emptyList();
    }

    @Override
    public void tableSourceItemSetChanged(TableItems.ItemSetChangeEvent<E> event) {
        // replacement for collectionChangeSelectionListener
        // #PL-2035, reload selection from ds
        Set<Object> selectedItemIds = getSelectedItemIds();
        if (selectedItemIds == null) {
            selectedItemIds = Collections.emptySet();
        }

        Set<Object> newSelection = new LinkedHashSet<>();

        TableItems<E> tableItems = event.getSource();
        for (Object entityId : selectedItemIds) {
            if (tableItems.getItem(entityId) != null) {
                newSelection.add(entityId);
            }
        }

        if (tableItems.getState() == BindingState.ACTIVE
                && tableItems instanceof EntityTableItems) {

            EntityTableItems entityTableSource = (EntityTableItems) tableItems;

            if (entityTableSource.getSelectedItem() != null) {
                newSelection.add(EntityValues.getId(entityTableSource.getSelectedItem()));
            }
        }

        if (newSelection.isEmpty()) {
            setSelected((E) null);
        } else {
            setSelectedIds(newSelection);
        }

        refreshActionsState();
    }

    @SuppressWarnings("unchecked")
    protected void clearFieldDatasources() {
        if (fieldDatasources == null) {
            return;
        }

        // detach instance containers from entities explicitly
        for (Map.Entry<JmixEntity, Object> entry : fieldDatasources.entrySet()) {
            if (entry.getValue() instanceof InstanceContainer) {
                InstanceContainer container = (InstanceContainer) entry.getValue();
                container.setItem(null);
            }/*
             TODO: legacy-ui
             else if (entry.getValue() instanceof Datasource) {
                Datasource datasource = (Datasource) entry.getValue();
                datasource.setItem(null);
            }*/
        }

        fieldDatasources.clear();
    }

    @Override
    public void tableSourcePropertyValueChanged(TableItems.ValueChangeEvent<E> event) {
        handleAggregation();
        refreshActionsState();
    }

    protected void handleAggregation() {
        if (isAggregatable() && aggregationCells != null) {
            component.aggregate(new AggregationContainer.Context(getItems().getItemIds()));

            // trigger aggregation repaint
            component.markAsDirty();
        }
    }

    @Override
    public void tableSourceSelectedItemChanged(TableItems.SelectedItemChangeEvent<E> event) {
        refreshActionsState();
    }

    @Override
    public void tableSourceStateChanged(TableItems.StateChangeEvent event) {
        refreshActionsState();
    }

    protected TableDataContainer<E> createTableDataContainer(TableItems<E> tableItems) {
        if (tableItems instanceof TableItems.Sortable) {
            return new AggregatableSortableDataContainer<>((TableItems.Sortable<E>) tableItems, this);
        }
        return new AggregatableTableDataContainer<>(tableItems, this);
    }

    protected class AggregatableTableDataContainer<I> extends TableDataContainer<I> implements AggregationContainer {

        protected Collection<Object> aggregationProperties = null;

        public AggregatableTableDataContainer(TableItems<I> tableItems,
                                              TableItemsEventsDelegate<I> dataEventsDelegate) {
            super(tableItems, dataEventsDelegate);
        }

        @Override
        public Collection getAggregationPropertyIds() {
            if (aggregationProperties != null) {
                return Collections.unmodifiableCollection(aggregationProperties);
            }
            return Collections.emptyList();
        }

        @Override
        public void addContainerPropertyAggregation(Object propertyId, Type type) {
            if (aggregationProperties == null) {
                aggregationProperties = new ArrayList<>();
            } else if (aggregationProperties.contains(propertyId)) {
                throw new IllegalStateException(String.format("Aggregation property %s already exists", propertyId));
            }
            aggregationProperties.add(propertyId);
        }

        @Override
        public void removeContainerPropertyAggregation(Object propertyId) {
            if (aggregationProperties != null) {
                aggregationProperties.remove(propertyId);
                if (aggregationProperties.isEmpty()) {
                    aggregationProperties = null;
                }
            }
        }

        @Override
        public Map<Object, Object> aggregate(Context context) {
            return __aggregate(this, context);
        }

        @Override
        public Map<Object, Object> aggregateValues(Context context) {
            return __aggregateValues(this, context);
        }
    }

    protected class AggregatableSortableDataContainer<I> extends SortableDataContainer<I>
            implements AggregationContainer {

        protected Collection<Object> aggregationProperties = null;

        public AggregatableSortableDataContainer(TableItems.Sortable<I> tableDataSource,
                                                 TableItemsEventsDelegate<I> dataEventsDelegate) {
            super(tableDataSource, dataEventsDelegate);
        }

        @Override
        public Collection getAggregationPropertyIds() {
            if (aggregationProperties != null) {
                return Collections.unmodifiableCollection(aggregationProperties);
            }
            return Collections.emptyList();
        }

        @Override
        public void addContainerPropertyAggregation(Object propertyId, AggregationContainer.Type type) {
            if (aggregationProperties == null) {
                aggregationProperties = new ArrayList<>();
            } else if (aggregationProperties.contains(propertyId)) {
                throw new IllegalStateException(String.format("Aggregation property %s already exists", propertyId));
            }
            aggregationProperties.add(propertyId);
        }

        @Override
        public void removeContainerPropertyAggregation(Object propertyId) {
            if (aggregationProperties != null) {
                aggregationProperties.remove(propertyId);
                if (aggregationProperties.isEmpty()) {
                    aggregationProperties = null;
                }
            }
        }

        @Override
        public Map<Object, Object> aggregate(AggregationContainer.Context context) {
            return __aggregate(this, context);
        }

        @Override
        public Map<Object, Object> aggregateValues(Context context) {
            return __aggregateValues(this, context);
        }
    }

    protected boolean canBeSorted(@Nullable TableItems<E> tableItems) {
        return tableItems instanceof TableItems.Sortable;
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        AppUI ui = AppUI.getCurrent();
        if (id != null && ui != null && ui.isPerformanceTestMode()) {
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

    protected String getColumnCaption(Object columnId) {
        if (columnId instanceof MetaPropertyPath) {
            return ((MetaPropertyPath) columnId).getMetaProperty().getName();
        } else {
            return columnId.toString();
        }
    }

    protected String getColumnCaption(Object columnId, Column column) {
        String caption = column.getCaption();

        if (caption != null) {
            return caption;
        }

        return StringUtils.capitalize(getColumnCaption(columnId));
    }

    protected void createStubsForGeneratedColumns() {
        for (Column column : columnsOrder) {
            if (!(column.getId() instanceof MetaPropertyPath)
                    && component.getColumnGenerator(column.getId()) == null) {

                if (column.getValueProvider() == null && column.getType() == null) {
                    component.addGeneratedColumn(column.getId(), VOID_COLUMN_GENERATOR);
                } else {
                    component.addGeneratedColumn(column.getId(), VALUE_PROVIDER_GENERATOR);
                }
            }
        }
    }

    protected List<Object> getInitialVisibleColumnIds(EntityTableItems<E> entityTableSource) {
        List<Object> result = new ArrayList<>();

        MetaClass metaClass = entityTableSource.getEntityMetaClass();
        for (Column column : columnsOrder) {
            if (column.getId() instanceof MetaPropertyPath) {
                MetaPropertyPath propertyPath = (MetaPropertyPath) column.getId();

                UiEntityAttributeContext attributeContext =
                        new UiEntityAttributeContext(metaClass, propertyPath.toString());
                accessManager.applyRegisteredConstraints(attributeContext);

                if (attributeContext.isViewPermitted()) {
                    result.add(column.getId());
                }
            } else {
                result.add(column.getId());
            }
        }
        return result;
    }

    protected void setVisibleColumns(List<?> columnsOrder) {
        component.setVisibleColumns(columnsOrder.toArray());
    }

    protected void setColumnHeader(Object columnId, String caption) {
        component.setColumnHeader(columnId, caption);
    }

    @Override
    public void setRowHeaderMode(io.jmix.ui.component.Table.RowHeaderMode rowHeaderMode) {
        switch (rowHeaderMode) {
            case NONE: {
                component.setRowHeaderMode(com.vaadin.v7.ui.Table.RowHeaderMode.HIDDEN);
                break;
            }
            case ICON: {
                component.setRowHeaderMode(com.vaadin.v7.ui.Table.RowHeaderMode.ICON_ONLY);
                break;
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void setRequired(Table.Column column, boolean required, String message) {
        if (required) {
            if (requiredColumns == null) {
                requiredColumns = new HashMap<>();
            }
            requiredColumns.put(column, message);
        } else {
            if (requiredColumns != null) {
                requiredColumns.remove(column);
            }
        }
    }

    @Override
    public String getStyleName() {
        String styleName = super.getStyleName();
        for (String internalStyle : internalStyles) {
            styleName = styleName.replace(internalStyle, "");
        }
        return StringUtils.normalizeSpace(styleName);
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        for (String internalStyle : internalStyles) {
            componentComposition.addStyleName(internalStyle);
        }
    }

    @Override
    public void setStyleProvider(@Nullable Table.StyleProvider<? super E> styleProvider) {
        if (styleProvider != null) {
            if (this.styleProviders == null) {
                this.styleProviders = new LinkedList<>();
            } else {
                this.styleProviders.clear();
            }

            this.styleProviders.add(styleProvider);
        } else {
            this.styleProviders = null;
        }

        component.refreshCellStyles();
    }

    @Override
    public void addStyleProvider(StyleProvider<? super E> styleProvider) {
        if (this.styleProviders == null) {
            this.styleProviders = new LinkedList<>();
        }

        if (!this.styleProviders.contains(styleProvider)) {
            this.styleProviders.add(styleProvider);

            component.refreshCellStyles();
        }
    }

    @Override
    public void removeStyleProvider(StyleProvider<? super E> styleProvider) {
        if (this.styleProviders != null) {
            if (this.styleProviders.remove(styleProvider)) {
                component.refreshCellStyles();
            }
        }
    }

    @Override
    public void setIconProvider(@Nullable Function<? super E, String> iconProvider) {
        this.iconProvider = iconProvider;
        if (iconProvider != null) {
            setRowHeaderMode(RowHeaderMode.ICON);
        } else {
            setRowHeaderMode(RowHeaderMode.NONE);
        }
        component.refreshRowCache();
    }

    // For vaadin component extensions
    @Nullable
    protected Resource getItemIcon(Object itemId) {
        if (iconProvider == null
                || getItems() == null) {
            return null;
        }

        E item = getItems().getItem(itemId);
        if (item == null) {
            return null;
        }
        String resourceUrl = iconProvider.apply(item);
        return iconResolver.getIconResource(resourceUrl);
    }

    @Override
    public void setItemDescriptionProvider(@Nullable BiFunction<? super E, String, String> provider) {
        if (this.itemDescriptionProvider != provider) {
            itemDescriptionProvider = provider;

            component.setItemDescriptionGenerator(
                    new ItemDescriptionGenerator(this::generateCellDescription));
        }
    }

    @Nullable
    @Override
    public BiFunction<? super E, String, String> getItemDescriptionProvider() {
        return itemDescriptionProvider;
    }

    @Override
    public int getRowHeaderWidth() {
        return component.getColumnWidth(ROW_HEADER_PROPERTY_ID);
    }

    @Override
    public void setRowHeaderWidth(int width) {
        component.setColumnWidth(ROW_HEADER_PROPERTY_ID, width);
    }

    @Nullable
    @Override
    public Action getEnterPressAction() {
        return enterPressAction;
    }

    @Override
    public void setEnterPressAction(Action action) {
        enterPressAction = action;
    }

    @Nullable
    @Override
    public Action getItemClickAction() {
        return itemClickAction;
    }

    @Override
    public void setItemClickAction(Action action) {
        if (itemClickAction != null) {
            removeAction(itemClickAction);
        }
        itemClickAction = action;
        if (!getActions().contains(action)) {
            addAction(action);
        }
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
            if (panel.getParent() != null
                    && panel.getParent() != this) {
                throw new IllegalStateException("Component already has parent");
            }

            if (topPanel == null) {
                topPanel = createTopPanel();
                topPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
                componentComposition.addComponentAsFirst(topPanel);
            }
            Component bp = panel.unwrap(Component.class);
            topPanel.addComponent(bp);
            topPanel.setExpandRatio(bp, 1);

            if (panel instanceof VisibilityChangeNotifier) {
                ((VisibilityChangeNotifier) panel).addVisibilityChangeListener(event ->
                        updateCompositionStylesTopPanelVisible()
                );
            }
            panel.setParent(this);
        }

        updateCompositionStylesTopPanelVisible();
    }

    protected HorizontalLayout createTopPanel() {
        HorizontalLayout topPanel = new HorizontalLayout();
        topPanel.setMargin(false);
        topPanel.setSpacing(false);
        topPanel.setStyleName("c-table-top");
        return topPanel;
    }

    @Override
    public void addGeneratedColumn(String columnId, ColumnGenerator<? super E> generator) {
        checkNotNullArgument(columnId, "columnId is null");
        checkNotNullArgument(generator, "generator is null for column id '%s'", columnId);

        EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();

        MetaPropertyPath targetCol = entityTableSource != null ?
                entityTableSource.getEntityMetaClass().getPropertyPath(columnId) : null;

        Object generatedColumnId = targetCol != null ? targetCol : columnId;

        Column column = getColumn(columnId);
        Column associatedRuntimeColumn = null;
        if (column == null) {
            Column<E> newColumn = new Column<>(generatedColumnId);

            columns.put(newColumn.getId(), newColumn);
            columnsOrder.add(newColumn);

            associatedRuntimeColumn = newColumn;
            newColumn.setOwner(this);
        }

        // save column order
        Object[] visibleColumns = component.getVisibleColumns();

        boolean removeOldGeneratedColumn = component.getColumnGenerator(generatedColumnId) != null;
        // replace generator for column if exist
        if (removeOldGeneratedColumn) {
            component.removeGeneratedColumn(generatedColumnId);
        }

        component.addGeneratedColumn(
                generatedColumnId,
                new CustomColumnGenerator(generator, associatedRuntimeColumn) {
                    @Nullable
                    @Override
                    public Object generateCell(com.vaadin.v7.ui.Table source, Object itemId, Object columnId) {
                        EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();
                        if (entityTableSource == null) {
                            return null;
                        }

                        E entity = entityTableSource.getItem(itemId);

                        io.jmix.ui.component.Component component = getColumnGenerator().generateCell(entity);
                        if (component == null) {
                            return null;
                        }

                        if (component instanceof PlainTextCell) {
                            return ((PlainTextCell) component).getText();
                        }

                        if (component instanceof BelongToFrame) {
                            BelongToFrame belongToFrame = (BelongToFrame) component;
                            if (belongToFrame.getFrame() == null) {
                                belongToFrame.setFrame(getFrame());
                            }
                        }
                        component.setParent(WebAbstractTable.this);

                        AbstractComponent vComponent = component.unwrapComposition(AbstractComponent.class);

                        if (component instanceof HasValueSource) {
                            HasValueSource<?> hasValueSource = (HasValueSource) component;
                            vComponent.setData(hasValueSource);
                        }

                        // vaadin8 rework
                        // wrap field for show required asterisk
                        if ((vComponent instanceof com.vaadin.v7.ui.Field)
                                && (((com.vaadin.v7.ui.Field) vComponent).isRequired())) {
                            VerticalLayout layout = new VerticalLayout();
                            layout.setMargin(false);
                            layout.setSpacing(false);
                            layout.addComponent(vComponent);

                            if (vComponent.getWidth() < 0) {
                                layout.setWidthUndefined();
                            }

                            layout.addComponent(vComponent);
                            vComponent = layout;
                        }
                        return vComponent;
                    }
                }
        );

        if (removeOldGeneratedColumn) {
            // restore column order
            component.setVisibleColumns(visibleColumns);
        }
    }

    @Override
    public void addGeneratedColumn(String columnId, int index, ColumnGenerator<? super E> generator) {
        addGeneratedColumn(columnId, generator);
        columnsOrder.add(index, columnsOrder.remove(columnsOrder.size() - 1));
        component.setVisibleColumns(columnsOrder.stream()
                .map(Table.Column::getId)
                .toArray());
    }

    @Override
    public void addGeneratedColumn(String columnId, ColumnGenerator<? super E> generator,
                                   Class<? extends io.jmix.ui.component.Component> componentClass) {
        // web ui doesn't make any improvements with componentClass known
        addGeneratedColumn(columnId, generator);
    }

    @Override
    public void removeGeneratedColumn(String columnId) {
        EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();

        MetaPropertyPath targetCol = entityTableSource != null ?
                entityTableSource.getEntityMetaClass().getPropertyPath(columnId) : null;
        removeGeneratedColumnInternal(targetCol == null ? columnId : targetCol);
    }

    @Override
    public void addAggregationProperty(String columnId, AggregationInfo.Type type) {
        addAggregationProperty(getColumn(columnId), type);
    }

    @Override
    public void addAggregationProperty(Column column, AggregationInfo.Type type) {
        checkAggregation(column.getAggregation());

        component.addContainerPropertyAggregation(column.getId(), WebWrapperUtils.convertAggregationType(type));

        if (column.getAggregation() != null) {
            addAggregationCell(column);
        }
    }

    @Override
    public void removeAggregationProperty(String columnId) {
        component.removeContainerPropertyAggregation(getColumn(columnId).getId());
        removeAggregationCell(getColumn(columnId));
    }

    @Override
    public void setColumnCaption(String columnId, @Nullable String caption) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnCaption(column, caption);
    }

    @Override
    public void setColumnCaption(Column column, @Nullable String caption) {
        checkNotNullArgument(column, "column must be non null");

        if (!Objects.equals(column.getCaption(), caption)) {
            column.setCaption(caption);
        }
        component.setColumnHeader(column.getId(), caption);
    }

    @Override
    public void setColumnDescription(String columnId, @Nullable String description) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnDescription(column, description);
    }

    @Override
    public void setColumnDescription(Column column, @Nullable String description) {
        checkNotNullArgument(column, "column must be non null");

        if (!Objects.equals(column.getDescription(), description)) {
            column.setDescription(description);
        }
        component.setColumnDescription(column.getId(), description);
    }

    @Override
    public boolean isTextSelectionEnabled() {
        return component.isTextSelectionEnabled();
    }

    @Override
    public void setTextSelectionEnabled(boolean value) {
        component.setTextSelectionEnabled(value);
    }

    @Override
    public void setColumnSortable(String columnId, boolean sortable) {
        Column column = getColumn(columnId);
        setColumnSortable(column, sortable);
    }

    @Override
    public void setColumnSortable(Column column, boolean sortable) {
        checkNotNullArgument(column, "column must be non null");
        if (column.isSortable() != sortable) {
            column.setSortable(sortable);
        }
        component.setColumnSortable(column.getId(), sortable);
    }

    @Override
    public void setColumnCollapsed(String columnId, boolean collapsed) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnCollapsed(column, collapsed);
    }

    @Override
    public void setColumnCollapsed(Column column, boolean collapsed) {
        if (!getColumnControlVisible()) {
            return;
        }

        checkNotNullArgument(column, "column must be non null");

        if (column.isCollapsed() != collapsed) {
            column.setCollapsed(collapsed);
        }
        component.setColumnCollapsed(column.getId(), collapsed);
    }

    @Override
    public void setColumnAlignment(Column column, ColumnAlignment alignment) {
        checkNotNullArgument(column, "column must be non null");

        if (column.getAlignment() != alignment) {
            column.setAlignment(alignment);
        }
        component.setColumnAlignment(column.getId(), WebWrapperUtils.convertColumnAlignment(alignment));
    }

    @Override
    public void setColumnAlignment(String columnId, ColumnAlignment alignment) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnAlignment(column, alignment);
    }

    @Override
    public void setColumnWidth(Column column, int width) {
        checkNotNullArgument(column, "column must be non null");

        if (column.getWidth() == null || column.getWidth() != width) {
            column.setWidth(width);
        }
        component.setColumnWidth(column.getId(), width);
    }

    @Override
    public void setColumnWidth(String columnId, int width) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnWidth(column, width);
    }

    @Override
    public void setColumnCaptionAsHtml(String columnId, boolean captionAsHtml) {
        Column<E> column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        setColumnCaptionAsHtml(column, captionAsHtml);
    }

    @Override
    public void setColumnCaptionAsHtml(Column column, boolean captionAsHtml) {
        checkNotNullArgument(column, "Column must be non null");

        if (column.getCaptionAsHtml() != captionAsHtml) {
            column.setCaptionAsHtml(captionAsHtml);
        }
        component.setColumnCaptionAsHtml(column.getId(), captionAsHtml);
    }

    @Deprecated
    @Override
    public void refresh() {
        // TableItems<E> tableItems = getItems();
        /*
        TODO: legacy-ui
        if (tableItems instanceof DatasourceTableItems) {
            ((DatasourceTableItems) tableItems).getDatasource().refresh();
        }*/
    }

    @Override
    public void repaint() {
        component.markAsDirtyRecursive();
    }

    @Override
    public void selectAll() {
        if (isMultiSelect()) {
            component.setValue(component.getItemIds());
        }
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

    protected Map<Object, Object> __aggregateValues(AggregationContainer container, AggregationContainer.Context context) {
        if (!(getItems() instanceof AggregatableTableItems)) {
            throw new IllegalStateException("Table items must implement AggregatableTableItems in " +
                    "order to use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos(container);

        Map<AggregationInfo, Object> results = ((AggregatableTableItems<E>) getItems()).aggregateValues(
                aggregationInfos.toArray(new AggregationInfo[0]),
                context.getItemIds()
        );

        return convertAggregationKeyMapToColumnIdKeyMap(container, results);
    }

    protected Map<Object, Object> __aggregate(AggregationContainer container, AggregationContainer.Context context) {
        if (!(getItems() instanceof AggregatableTableItems)) {
            throw new IllegalStateException("Table items must implement AggregatableTableItems in " +
                    "order to use aggregation");
        }

        List<AggregationInfo> aggregationInfos = getAggregationInfos(container);

        Map<AggregationInfo, String> results = ((AggregatableTableItems<E>) getItems()).aggregate(
                aggregationInfos.toArray(new AggregationInfo[0]),
                context.getItemIds()
        );

        Map<Object, Object> resultsByColumns = convertAggregationKeyMapToColumnIdKeyMap(container, results);

        if (aggregationCells != null) {
            resultsByColumns = __handleAggregationResults(context, resultsByColumns);
        }
        return resultsByColumns;
    }

    protected List<AggregationInfo> getAggregationInfos(AggregationContainer container) {
        List<AggregationInfo> aggregationInfos = new ArrayList<>();
        for (Object propertyId : container.getAggregationPropertyIds()) {
            Table.Column column = columns.get(propertyId);
            AggregationInfo aggregation = column.getAggregation();
            if (aggregation != null) {
                checkAggregation(aggregation);
                aggregationInfos.add(aggregation);
            }
        }
        return aggregationInfos;
    }

    protected Map<Object, Object> convertAggregationKeyMapToColumnIdKeyMap(AggregationContainer container,
                                                                           Map<AggregationInfo, ?> aggregationInfoMap) {
        Map<Object, Object> resultsByColumns = new LinkedHashMap<>();
        for (Object propertyId : container.getAggregationPropertyIds()) {
            Table.Column column = columns.get(propertyId);
            if (column.getAggregation() != null) {
                resultsByColumns.put(column.getId(), aggregationInfoMap.get(column.getAggregation()));
            }
        }
        return resultsByColumns;
    }

    protected Map<Object, Object> __handleAggregationResults(AggregationContainer.Context context,
                                                             Map<Object, Object> results) {
        for (Map.Entry<Object, Object> entry : results.entrySet()) {
            Table.Column<E> column = columns.get(entry.getKey());
            if (aggregationCells.get(column) != null) {
                Object value = entry.getValue();
                String cellText = getFormattedValue(column, value);
                entry.setValue(cellText);
            }
        }
        return results;
    }

    protected String getFormattedValue(Column<E> column, @Nullable Object value) {
        String cellText;
        if (value == null) {
            cellText = "";
        } else {
            if (value instanceof String) {
                cellText = (String) value;
            } else {
                Function<Object, String> formatter = column.getFormatter();
                if (formatter != null) {
                    cellText = formatter.apply(value);
                } else {
                    Datatype datatype = datatypeRegistry.find(value.getClass());
                    if (datatype != null) {
                        cellText = datatype.format(value, this.locale);
                    } else {
                        cellText = value.toString();
                    }
                }
            }
        }
        return cellText;
    }

    protected void removeAggregationCell(Table.Column column) {
        if (aggregationCells != null) {
            aggregationCells.remove(column);
        }
    }

    protected void addAggregationCell(Table.Column column) {
        if (aggregationCells == null) {
            aggregationCells = new HashMap<>();
        }
        aggregationCells.put(column, "");
    }

    protected boolean handleSpecificVariables(Map<String, Object> variables) {
        if (isUsePresentations() && presentations != null) {
            TablePresentations p = getPresentations();

            if (p.getCurrent() != null && p.isAutoSave(p.getCurrent()) && needUpdatePresentation(variables)) {
                updatePresentationSettings(p);
            }
        }

        return false;
    }

    protected void updatePresentationSettings(TablePresentations p) {
        if (userSettingsTools != null) {
            ComponentSettings settings = getSettingsFromPresentation(p.getCurrent());
            getSettingsBinder().saveSettings(this, new SettingsWrapperImpl(settings));

            String rawSettings = userSettingsTools.toSettingsString(settings);
            p.setSettings(p.getCurrent(), rawSettings);
        }
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return (ComponentSettingsBinder) applicationContext.getBean(TableSettingsBinder.NAME);
    }

    protected boolean needUpdatePresentation(Map<String, Object> variables) {
        return variables.containsKey("colwidth") || variables.containsKey("sortcolumn")
                || variables.containsKey("sortascending") || variables.containsKey("columnorder")
                || variables.containsKey("collapsedcolumns") || variables.containsKey("groupedcolumns");
    }

    @Override
    public List<Table.Column> getNotCollapsedColumns() {
        Object[] componentVisibleColumns = component.getVisibleColumns();
        if (componentVisibleColumns == null)
            return Collections.emptyList();

        List<Table.Column> visibleColumns = new ArrayList<>(componentVisibleColumns.length);
        for (Object key : componentVisibleColumns) {
            if (!component.isColumnCollapsed(key)) {
                Column column = columns.get(key);
                if (column != null) {
                    visibleColumns.add(column);
                }
            }
        }
        return visibleColumns;
    }

    @Override
    public void usePresentations(boolean use) {
        usePresentations = use;
    }

    @Override
    public boolean isUsePresentations() {
        return usePresentations;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void resetPresentation() {
        if (defaultTableSettings != null) {
            DataLoadingSettingsBinder binder = (DataLoadingSettingsBinder) getSettingsBinder();
            binder.applySettings(this, new SettingsWrapperImpl(defaultTableSettings));
            binder.applyDataLoadingSettings(this, new SettingsWrapperImpl(defaultTableSettings));

            if (presentations != null) {
                presentations.setCurrent(null);
            }
        }
    }

    @Override
    public void loadPresentations() {
        if (isUsePresentations()) {
            presentations = createTablePresentations();

            setTablePresentationsLayout(createTablePresentationsLayout());
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Nullable
    @Override
    public TablePresentations getPresentations() {
        if (isUsePresentations()) {
            return presentations;
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Override
    public void applyPresentation(Object id) {
        if (isUsePresentations() && presentations != null) {
            TablePresentation p = presentations.getPresentation(id);
            if (p != null) {
                applyPresentation(p);
            }
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    @Override
    public void applyPresentationAsDefault(Object id) {
        if (isUsePresentations() && presentations != null) {
            TablePresentation p = presentations.getPresentation(id);
            if (p != null) {
                presentations.setDefault(p);
                applyPresentation(p);
            }
        } else {
            throw new UnsupportedOperationException("Component doesn't use presentations");
        }
    }

    protected void applyPresentation(TablePresentation p) {
        if (presentations != null) {
            applyPresentationSettings(p);

            presentations.setCurrent(p);
            component.markAsDirty();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void applyPresentationSettings(TablePresentation p) {
        ComponentSettings settings = getSettingsFromPresentation(p);
        DataLoadingSettingsBinder binder = (DataLoadingSettingsBinder) getSettingsBinder();
        binder.applySettings(this, new SettingsWrapperImpl(settings));
        binder.applyDataLoadingSettings(this, new SettingsWrapperImpl(settings));
    }

    protected ComponentSettings getSettingsFromPresentation(TablePresentation p) {
        Class<? extends ComponentSettings> settingsClass = getSettingsBinder().getSettingsClass();
        ComponentSettings settings = SettingsHelper.createSettings(settingsClass, getId());

        String settingsString = presentations.getSettingsString(p);
        if (settingsString != null && userSettingsTools != null) {
            ComponentSettings convertedSettings = userSettingsTools.toComponentSettings(settingsString, settingsClass);
            if (convertedSettings != null) {
                settings = convertedSettings;
            }
        }
        return settings;
    }

    protected TablePresentationsLayout createTablePresentationsLayout() {
        return new TablePresentationsLayout(this, getSettingsBinder(), applicationContext);
    }

    protected TablePresentations createTablePresentations() {
        return (TablePresentations) applicationContext.getBean(TablePresentations.NAME, this);
    }

    @Nullable
    @Override
    public Object getDefaultPresentationId() {
        if (presentations == null) {
            return null;
        }
        TablePresentation def = presentations.getDefault();
        return def == null ? null : EntityValues.<UUID>getId(def);
    }

    @Override
    public void setDefaultSettings(SettingsWrapper wrapper) {
        this.defaultTableSettings = wrapper.getSettings();
    }

    @Nullable
    @Override
    public ComponentSettings getDefaultSettings() {
        return defaultTableSettings;
    }

    @Override
    public Subscription addColumnCollapseListener(Consumer<ColumnCollapseEvent> listener) {
        if (columnCollapseListener == null) {
            columnCollapseListener = this::onColumnCollapseStateChange;
            component.addColumnCollapseListener(columnCollapseListener);
        }

        getEventHub().subscribe(ColumnCollapseEvent.class, listener);

        return () -> removeColumnCollapseListener(listener);
    }

    @Override
    public void removeColumnCollapseListener(Consumer<ColumnCollapseEvent> listener) {
        unsubscribe(ColumnCollapseEvent.class, listener);

        if (!hasSubscriptions(ColumnCollapseEvent.class)
                && columnCollapseListener != null) {
            component.removeColumnCollapseListener(columnCollapseListener);
            columnCollapseListener = null;
        }
    }

    protected void onColumnCollapseStateChange(com.vaadin.v7.ui.Table.ColumnCollapseEvent e) {
        Column collapsedColumn = getColumn(e.getPropertyId().toString());
        boolean collapsed = component.isColumnCollapsed(e.getPropertyId());

        ColumnCollapseEvent<E> event = new ColumnCollapseEvent<>(this, collapsedColumn, collapsed);
        publish(ColumnCollapseEvent.class, event);
    }

    @Override
    @Deprecated
    public void setCellClickListener(String columnId, Consumer<CellClickEvent<E>> clickListener) {
        checkNotNullArgument(getColumn(columnId), String.format("column with id '%s' not found", columnId));

        component.setClickListener(getColumn(columnId).getId(), (itemId, columnId1) -> {
            TableItems<E> tableItems = getItems();
            if (tableItems == null) {
                return;
            }

            E item = tableItems.getItem(itemId);
            CellClickEvent<E> event = new CellClickEvent<>(this, item, columnId1.toString());
            clickListener.accept(event);
        });
    }

    @Override
    @Deprecated
    public void removeClickListener(String columnId) {
        component.removeClickListener(getColumn(columnId).getId());
    }

    @Override
    public void addCellClickListener(String columnId) {
        Table.Column<E> column = getColumn(columnId);
        checkNotNullArgument(column, String.format("column with id '%s' not found", columnId));

        component.addTableCellClickListener(column.getId(), this::onCellClick);
    }

    protected void onCellClick(JmixEnhancedTable.TableCellClickEvent event) {
        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return;
        }

        E item = tableItems.getItem(event.getItemId());
        if (item == null) {
            return;
        }

        Table.Column<E> column = getColumn(String.valueOf(event.getColumnId()));
        if (column == null) {
            return;
        }

        Column.ClickEvent<E> clickEvent = new Column.ClickEvent<>(column, item, event.isText());
        column.fireClickEvent(clickEvent);
    }

    @Override
    public void removeCellClickListener(String columnId) {
        Table.Column<E> column = getColumn(columnId);
        checkNotNullArgument(column, String.format("column with id '%s' not found", columnId));

        component.removeTableCellClickListener(column.getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectionListener(Consumer<SelectionEvent<E>> listener) {
        return getEventHub().subscribe(SelectionEvent.class, (Consumer) listener);
    }

    @Override
    public void showCustomPopup(io.jmix.ui.component.Component popupComponent) {
        Component vComponent = popupComponent.unwrap(com.vaadin.ui.Component.class);
        component.showCustomPopup(vComponent);
        component.setCustomPopupAutoClose(false);
    }

    @Override
    public void showCustomPopupActions(List<Action> actions) {
        VerticalLayout customContextMenu = new VerticalLayout();
        customContextMenu.setMargin(false);
        customContextMenu.setSpacing(false);
        customContextMenu.setWidthUndefined();
        customContextMenu.setStyleName("c-cm-container");

        for (Action action : actions) {
            JmixButton contextMenuButton = createContextMenuButton();
            initContextMenuButton(contextMenuButton, action);

            customContextMenu.addComponent(contextMenuButton);
        }

        if (customContextMenu.getComponentCount() > 0) {
            component.showCustomPopup(customContextMenu);
            component.setCustomPopupAutoClose(true);
        }
    }

    @Override
    public boolean isColumnHeaderVisible() {
        return component.getColumnHeaderMode() != ColumnHeaderMode.HIDDEN;
    }

    @Override
    public void setColumnHeaderVisible(boolean visible) {
        component.setColumnHeaderMode(visible ?
                ColumnHeaderMode.EXPLICIT_DEFAULTS_ID :
                ColumnHeaderMode.HIDDEN);
    }

    @Override
    public boolean isShowSelection() {
        return component.isSelectable();
    }

    @Override
    public void setShowSelection(boolean showSelection) {
        component.setSelectable(showSelection);
    }

    @Override
    public void setColumnExpandRatio(Column column, float ratio) {
        checkNotNullArgument(column, "Column must be non null");

        component.setColumnExpandRatio(column.getId(), ratio);
    }

    @Override
    public float getColumnExpandRatio(Column column) {
        checkNotNullArgument(column, "Column must be non null");

        return component.getColumnExpandRatio(column.getId());
    }

    @Nullable
    protected String generateCellStyle(@Nullable Object itemId, @Nullable Object propertyId) {
        String style = null;
        if (propertyId != null && itemId != null
                && !component.isColumnEditable(propertyId)
                && (component.getColumnGenerator(propertyId) == null
                || isValueGeneratedColumn(propertyId))) {

            MetaPropertyPath propertyPath;
            if (propertyId instanceof MetaPropertyPath) {
                propertyPath = (MetaPropertyPath) propertyId;
            } else {
                EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();

                propertyPath = entityTableSource != null ?
                        entityTableSource.getEntityMetaClass().getPropertyPath(propertyId.toString()) : null;
            }

            style = generateDefaultCellStyle(itemId, propertyId, propertyPath);
        }

        if (styleProviders != null) {
            String generatedStyle = getGeneratedCellStyle(itemId, propertyId);
            // we use style names without v-table-cell-content prefix, so we add cs prefix
            // all cells with custom styles will have v-table-cell-content-cs style name in class
            if (style != null) {
                if (generatedStyle != null) {
                    style = CUSTOM_STYLE_NAME_PREFIX + generatedStyle + " " + style;
                }
            } else if (generatedStyle != null) {
                style = CUSTOM_STYLE_NAME_PREFIX + generatedStyle;
            }
        }

        return style == null ? null : (CUSTOM_STYLE_NAME_PREFIX + style);
    }

    protected boolean isValueGeneratedColumn(Object propertyId) {
        return component.getColumnGenerator(propertyId) instanceof AbbreviatedColumnGenerator
                || component.getColumnGenerator(propertyId) == VALUE_PROVIDER_GENERATOR;
    }

    @Nullable
    protected String generateDefaultCellStyle(Object itemId, Object propertyId,
                                              @Nullable MetaPropertyPath propertyPath) {
        String style = null;

        String stringPropertyId = propertyId.toString();

        Column column = getColumn(stringPropertyId);
        if (column != null)
            if (column.getValueProvider() != null) {
                // column ValueProvider supports Boolean type
                if (dataBinding != null
                        && column.getType() == Boolean.class
                        && column.getFormatter() == null) {

                    JmixEntity item = dataBinding.getTableItems().getItem(itemId);
                    if (item != null) {
                        Boolean value = (Boolean) column.getValueProvider().apply(item);
                        if (BooleanUtils.isTrue(value)) {
                            style = BOOLEAN_CELL_STYLE_TRUE;
                        } else {
                            style = BOOLEAN_CELL_STYLE_FALSE;
                        }
                    }
                }
            } else if (propertyPath != null) {
                String isLink = column.getXmlDescriptor() == null ?
                        null : column.getXmlDescriptor().attributeValue("link");

                if (propertyPath.getRange().isClass()) {
                    if (StringUtils.isNotEmpty(isLink) && Boolean.parseBoolean(isLink)) {
                        style = "c-table-cell-link";
                    }
                } else if (propertyPath.getRange().isDatatype()) {
                    if (StringUtils.isNotEmpty(isLink) && Boolean.parseBoolean(isLink)) {
                        style = "c-table-cell-link";
                    } else if (column.getMaxTextLength() != null) {
                        style = generateClickableCellStyles(itemId, column, propertyPath);
                    }
                }

                if (propertyPath.getRangeJavaClass() == Boolean.class
                        && column.getFormatter() == null
                        && dataBinding != null) {
                    JmixEntity item = dataBinding.getTableItems().getItem(itemId);
                    if (item != null) {
                        Boolean value = EntityValues.getValueEx(item, propertyPath);
                        if (BooleanUtils.isTrue(value)) {
                            style = BOOLEAN_CELL_STYLE_TRUE;
                        } else {
                            style = BOOLEAN_CELL_STYLE_FALSE;
                        }
                    }
                }
            }

        return style;
    }

    @Nullable
    protected String generateClickableCellStyles(Object itemId, Column column, MetaPropertyPath propertyPath) {
        EntityTableItems<E> entityTableSource = (EntityTableItems<E>) getItems();
        if (entityTableSource == null) {
            throw new IllegalStateException("TableItems is not set");
        }

        E item = entityTableSource.getItemNN(itemId);

        Object value = EntityValues.getValueEx(item, propertyPath);
        String stringValue = metadataTools.format(value, propertyPath.getMetaProperty());

        if (column.getMaxTextLength() != null) {
            boolean isMultiLineCell = StringUtils.contains(stringValue, "\n");
            if ((stringValue.length() > column.getMaxTextLength() + MAX_TEXT_LENGTH_GAP)
                    || isMultiLineCell) {
                return "c-table-cell-textcut";
            } else {
                // use special marker stylename
                return "c-table-clickable-text";
            }
        }
        return null;
    }

    @Override
    public void setAggregationDistributionProvider(@Nullable AggregationDistributionProvider<E> distributionProvider) {
        this.distributionProvider = distributionProvider;

        component.setAggregationDistributionProvider(this::distributeAggregation);
    }

    protected boolean distributeAggregation(AggregationInputValueChangeContext context) {
        if (distributionProvider != null) {
            String value = context.getValue();
            Object columnId = context.getColumnId();
            try {
                Object parsedValue = getParsedAggregationValue(value, columnId);
                TableItems<E> tableItems = getItems();
                Collection<E> items = tableItems == null ?
                        Collections.emptyList() : tableItems.getItems();

                AggregationDistributionContext<E> distributionContext =
                        new AggregationDistributionContext<>(getColumn(columnId.toString()),
                                parsedValue, items, context.isTotalAggregation());

                distributionProvider.onDistribution(distributionContext);
            } catch (ValueConversionException e) {
                showParseErrorNotification(e.getLocalizedMessage());
                return false; // rollback to previous value
            } catch (ParseException e) {
                showParseErrorNotification(messages.getMessage("validationFail"));
                return false; // rollback to previous value
            }
        }
        return true;
    }

    @Nullable
    @Override
    public AggregationDistributionProvider<E> getAggregationDistributionProvider() {
        return distributionProvider;
    }

    @Override
    public void requestFocus(E item, String columnId) {
        Preconditions.checkNotNullArgument(item);
        Preconditions.checkNotNullArgument(columnId);

        component.requestFocus(EntityValues.getId(item), getColumn(columnId).getId());
    }

    @Override
    public void scrollTo(E item) {
        Preconditions.checkNotNullArgument(item);
        if (!component.getItemIds().contains(EntityValues.getId(item))) {
            throw new IllegalArgumentException("Unable to find item in Table");
        }

        component.setCurrentPageFirstItemId(EntityValues.getId(item));
    }

    protected void handleColumnCollapsed(com.vaadin.v7.ui.Table.ColumnCollapseEvent event) {
        Object propertyId = event.getPropertyId();
        boolean columnCollapsed = component.isColumnCollapsed(propertyId);

        columns.get(propertyId).setCollapsed(columnCollapsed);
    }

    @SuppressWarnings("unchecked")
    protected void beforeComponentPaint() {
        com.vaadin.v7.ui.Table.CellStyleGenerator generator = component.getCellStyleGenerator();
        if (generator instanceof WebAbstractTable.StyleGeneratorAdapter) {
            ((StyleGeneratorAdapter) generator).resetExceptionHandledFlag();
        }
    }

    protected class StyleGeneratorAdapter implements com.vaadin.v7.ui.Table.CellStyleGenerator {
        protected boolean exceptionHandled = false;

        @Nullable
        @Override
        public String getStyle(com.vaadin.v7.ui.Table source, Object itemId, Object propertyId) {
            if (exceptionHandled) {
                return null;
            }

            try {
                return generateCellStyle(itemId, propertyId);
            } catch (Exception e) {
                LoggerFactory.getLogger(WebAbstractTable.class).error("Uncaught exception in Table StyleProvider", e);
                this.exceptionHandled = true;
                return null;
            }
        }

        public void resetExceptionHandledFlag() {
            this.exceptionHandled = false;
        }
    }

    @Nullable
    protected Object getValueExIgnoreUnfetched(@Nullable JmixEntity instance, String[] properties) {
        Object currentValue = null;
        JmixEntity currentInstance = instance;
        for (String property : properties) {
            if (currentInstance == null) {
                break;
            }

            if (!entityStates.isLoaded(currentInstance, property)) {
                LoggerFactory.getLogger(WebAbstractTable.class)
                        .warn("Ignored unfetched attribute {} of instance {} in Table cell",
                                property, currentInstance);
                return null;
            }

            currentValue = EntityValues.getValue(currentInstance, property);
            if (currentValue == null) {
                break;
            }

            currentInstance = currentValue instanceof JmixEntity ? (JmixEntity) currentValue : null;
        }
        return currentValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        return getEventHub().subscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        unsubscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @Nullable
    @Override
    public Object createInstallHandler(Class<?> targetObjectType, FrameOwner frameOwner, Method method) {
        if (targetObjectType == StyleProvider.class) {
            return new InstalledStyleProvider(frameOwner, method);
        }
        return null;
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
        this.emptyStateClickLinkHandler = handler;
    }

    @Nullable
    @Override
    public Consumer<EmptyStateClickEvent<E>> getEmptyStateLinkClickHandler() {
        return emptyStateClickLinkHandler;
    }

    protected static class InstalledStyleProvider implements StyleProvider {
        private final FrameOwner frameOwner;
        private final Method method;

        public InstalledStyleProvider(FrameOwner frameOwner, Method method) {
            this.frameOwner = frameOwner;
            this.method = method;
        }

        @Override
        public String getStyleName(JmixEntity entity, @Nullable String property) {
            try {
                return (String) method.invoke(frameOwner, entity, property);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Exception on @Install invocation", e);
            }
        }

        @Override
        public String toString() {
            return "InstalledStyleProvider{" +
                    "frameOwner=" + frameOwner.getClass() +
                    ", method=" + method +
                    '}';
        }

    }

    protected Object getParsedAggregationValue(String value, Object columnId) throws ParseException {
        Object parsedValue = value;

        for (Column column : getColumns()) {
            if (column.getId().equals(columnId)) {
                AggregationInfo aggregationInfo = column.getAggregation();
                if (aggregationInfo == null || aggregationInfo.getFormatter() != null) {
                    return parsedValue;
                }

                MetaPropertyPath propertyPath = aggregationInfo.getPropertyPath();
                Class<?> resultClass;
                Range range = propertyPath != null ? propertyPath.getRange() : null;
                if (range != null && range.isDatatype()) {
                    if (aggregationInfo.getType() == AggregationInfo.Type.COUNT) {
                        return parsedValue;
                    }

                    if (aggregationInfo.getStrategy() == null) {
                        Class<?> rangeJavaClass = propertyPath.getRangeJavaClass();
                        Aggregation aggregation = aggregations.get(rangeJavaClass);
                        resultClass = aggregation.getResultClass();
                    } else {
                        resultClass = aggregationInfo.getStrategy().getResultClass();
                    }

                } else if (aggregationInfo.getStrategy() == null) {
                    return parsedValue;
                } else {
                    resultClass = aggregationInfo.getStrategy().getResultClass();
                }

                parsedValue = datatypeRegistry.get(resultClass).parse(value, locale);

                break;
            }
        }
        return parsedValue;
    }

    protected void showParseErrorNotification(String message) {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(getFrame().getFrameOwner());
        screenContext.getNotifications().create(Notifications.NotificationType.TRAY)
                .withDescription(message)
                .show();
    }

    protected void initEmptyState() {
        component.setEmptyStateLinkClickHandler(() -> {
            if (emptyStateClickLinkHandler != null) {
                emptyStateClickLinkHandler.accept(new EmptyStateClickEvent<>(this));
            }
        });

        if (dataBinding != null) {
            dataBinding.addItemSetChangeListener(event -> showEmptyStateIfPossible());
        }

        showEmptyStateIfPossible();
    }

    protected void showEmptyStateIfPossible() {
        boolean emptyItems = (dataBinding != null && dataBinding.getTableItems().size() == 0) || getItems() == null;
        boolean notEmptyMessages = !Strings.isNullOrEmpty(component.getEmptyStateMessage())
                || !Strings.isNullOrEmpty(component.getEmptyStateLinkMessage());

        component.setShowEmptyState(emptyItems && notEmptyMessages);
    }

    protected class ItemDescriptionGenerator implements AbstractSelect.ItemDescriptionGenerator {

        protected final BiFunction<Object, Object, String> itemDescriptionProvider;

        public ItemDescriptionGenerator(BiFunction<Object, Object, String> itemDescriptionProvider) {
            this.itemDescriptionProvider = itemDescriptionProvider;
        }

        @Nullable
        @Override
        public String generateDescription(Component source, Object itemId, Object propertyId) {

            try {
                return itemDescriptionProvider.apply(itemId, propertyId);
            } catch (Exception e) {
                LoggerFactory.getLogger(WebAbstractTable.class)
                        .error("Uncaught exception in Table ItemDescriptionProvider", e);
                return null;
            }
        }
    }
}
