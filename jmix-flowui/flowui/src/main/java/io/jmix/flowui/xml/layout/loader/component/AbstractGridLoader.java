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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Splitter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnRendering;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridSubMenu;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.impl.FetchPlanRepositoryImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.GridContextMenuItemComponent;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.data.provider.EmptyValueProvider;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.grid.JmixGridContextMenu;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.DataLoadersHelper;
import io.jmix.flowui.xml.layout.inittask.AssignActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.component.datagrid.RendererProvider;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElementFactory;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGridLoader<T extends Grid & EnhancedDataGrid & HasActions>
        extends AbstractComponentLoader<T> {

    public static final String COLUMN_ELEMENT_NAME = "column";
    public static final String EDITOR_ACTIONS_COLUMN_ELEMENT_NAME = "editorActionsColumn";

    protected ActionLoaderSupport actionLoaderSupport;
    protected MetadataTools metaDataTools;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer
    protected FetchPlanRepositoryImpl fetchPlanRepository;
    protected ClassManager classManager;

    protected List<DataGridColumn<?>> pendingToFilterableColumns = new ArrayList<>();

    @Override
    public void loadComponent() {
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "rowsDraggable", resultComponent::setRowsDraggable);
        loadBoolean(element, "allRowsVisible", resultComponent::setAllRowsVisible);
        loadEnum(element, GridDropMode.class, "dropMode", resultComponent::setDropMode);
        loadBoolean(element, "detailsVisibleOnClick", resultComponent::setDetailsVisibleOnClick);
        loadEnum(element, SelectionMode.class, "selectionMode", resultComponent::setSelectionMode);
        loadBoolean(element, "columnReorderingAllowed", resultComponent::setColumnReorderingAllowed);
        loadEnum(element, NestedNullBehavior.class, "nestedNullBehavior", resultComponent::setNestedNullBehavior);
        loadBoolean(element, "editorBuffered", editorBuffered ->
                resultComponent.getEditor().setBuffered(editorBuffered));
        loadBoolean(element, "aggregatable", resultComponent::setAggregatable);
        loadEnum(element, EnhancedDataGrid.AggregationPosition.class, "aggregationPosition",
                resultComponent::setAggregationPosition);
        loadEnum(element, ColumnRendering.class, "columnRendering", resultComponent::setColumnRendering);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadData();
        loadMultiSort();

        loadContextMenu();
        loadActions();
    }

    protected void loadData() {
        GridDataHolder holder = initDataGridDataHolder();

        if (!holder.isContainerLoaded()
                && holder.getMetaClass() == null) {
            String message = String.format(
                    "%s doesn't have data binding. Set either dataContainer and property or metaClass attribute.",
                    resultComponent.getClass().getSimpleName()
            );

            throw new GuiDevelopmentException(message, context, "Component ID", element.attributeValue("id"));
        }
        Element columns = element.element("columns");

        if (columns != null) {
            FetchPlan fetchPlan = holder.getFetchPlan();
            assert holder.getMetaClass() != null || fetchPlan != null;

            if (fetchPlan == null) {
                fetchPlan = getFetchPlanRepository().getFetchPlan(holder.getMetaClass(), FetchPlan.LOCAL);
            }

            loadColumns(resultComponent, columns, holder.getMetaClass(), fetchPlan);
        }

        setupDataProvider(holder);

        // filters must be initialized after the data provider
        // check for parent was implemented for the case
        // when a column was deleted due to security constraints
        pendingToFilterableColumns.forEach(column ->
                column.getParent()
                        .ifPresent(__ -> column.setFilterable(true))
        );
        pendingToFilterableColumns.clear();
    }

    protected void loadMultiSort() {
        boolean multiSort = loadBoolean(element, "multiSort")
                .orElse(false);
        Grid.MultiSortPriority multiSortPriority = loadEnum(element, Grid.MultiSortPriority.class, "multiSortPriority")
                .orElse(Grid.MultiSortPriority.PREPEND);
        boolean multiSortOnShiftClickOnly = loadBoolean(element, "multiSortOnShiftClickOnly")
                .orElse(false);

        resultComponent.setMultiSort(multiSort, multiSortPriority, multiSortOnShiftClickOnly);
    }

    protected void loadColumns(T resultComponent, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Boolean includeAll = loadBoolean(columnsElement, "includeAll").orElse(false);
        boolean sortable = loadBoolean(columnsElement, "sortable")
                .orElse(true);
        boolean resizable = loadBoolean(columnsElement, "resizable")
                .orElse(false);

        if (columnsElement.elements(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME).size() > 1) {
            throw new GuiDevelopmentException("DataGrid can contain only one editorActionsColumn",
                    context, "Component ID", resultComponent.getId());
        }

        if (includeAll) {
            loadColumnsByInclude(resultComponent, columnsElement, metaClass, fetchPlan, sortable, resizable);
            // In case of includeAll, EditorActionsColumn will be place at the end
            loadEditorActionsColumns(resultComponent, columnsElement);
        } else {
            List<Element> columnElements = columnsElement.elements();
            for (Element columnElement : columnElements) {
                loadColumnsElementChild(resultComponent, columnElement, metaClass, sortable, resizable);
            }
        }
    }

    protected void loadColumnsElementChild(T resultComponent, Element columnElement, MetaClass metaClass,
                                           boolean sortableColumns, boolean resizableColumns) {
        switch (columnElement.getName()) {
            case COLUMN_ELEMENT_NAME:
                loadColumn(resultComponent, columnElement, metaClass, sortableColumns, resizableColumns);
                break;
            case EDITOR_ACTIONS_COLUMN_ELEMENT_NAME:
                loadEditorActionsColumn(resultComponent, columnElement);
                break;
            default:
                throw new GuiDevelopmentException("Unknown columns' child element: " + columnElement.getName(),
                        context, "Component ID", resultComponent.getId());
        }
    }

    protected void loadEditorActionsColumns(T resultComponent, Element columnsElement) {
        List<Element> editorActionsColumns = columnsElement.elements(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME);
        if (CollectionUtils.isEmpty(editorActionsColumns)) {
            return;
        }

        for (Element columnElement : editorActionsColumns) {
            loadEditorActionsColumn(resultComponent, columnElement);
        }
    }

    protected void loadEditorActionsColumn(T resultComponent, Element columnElement) {
        if (columnElement.elements().size() == 0) {
            throw new GuiDevelopmentException("'editorActionsColumn' cannot be empty",
                    context, "Component ID", resultComponent.getId());
        }

        Editor editor = resultComponent.getEditor();
        Grid.Column<?> editColumn = createEditColumn(resultComponent, columnElement, editor);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setPadding(false);

        Button saveButton = loadEditorButton(columnElement, "saveButton");
        if (saveButton != null) {
            saveButton.addClickListener(__ -> editor.save());
            actions.add(saveButton);
        }

        Button cancelButton = loadEditorButton(columnElement, "cancelButton");
        if (cancelButton != null) {
            cancelButton.addClickListener(__ -> editor.cancel());
            actions.add(cancelButton);
        }

        Button closeButton = loadEditorButton(columnElement, "closeButton");
        if (closeButton != null) {
            closeButton.addClickListener(__ -> editor.closeEditor());
            actions.add(closeButton);
        }

        editColumn.setEditorComponent(actions);

        //If the key is null then NPE will rise when the settings are applied
        loadString(columnElement, "key").ifPresentOrElse(
                editColumn::setKey,
                () -> editColumn.setKey(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME));
        loadString(columnElement, "width", editColumn::setWidth);
        loadBoolean(columnElement, "autoWidth", editColumn::setAutoWidth);
        loadBoolean(columnElement, "resizable", editColumn::setResizable);
        loadInteger(columnElement, "flexGrow", editColumn::setFlexGrow);
        loadResourceString(columnElement, "header", context.getMessageGroup(), editColumn::setHeader);
        loadResourceString(columnElement, "footer", context.getMessageGroup(), editColumn::setFooter);
        loadBoolean(columnElement, "visible", editColumn::setVisible);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Grid.Column<?> createEditColumn(T resultComponent, Element columnElement, Editor editor) {
        return resultComponent.addComponentColumn(item -> {
            Button editButton = loadEditorButton(columnElement, "editButton");
            if (editButton != null) {
                editButton.addClickListener(__ -> {
                    if (editor.isOpen()) {
                        editor.cancel();
                    }
                    editor.editItem(item);
                });
                return editButton;
            } else {
                // Vaadin throws NPE if null is returned
                return new Span();
            }
        });
    }

    @Nullable
    protected Button loadEditorButton(Element columnElement, String buttonElementName) {
        Element buttonElement = columnElement.element(buttonElementName);
        if (buttonElement != null) {
            JmixButton button = factory.create(JmixButton.class);

            componentLoader().loadText(button, buttonElement);
            componentLoader().loadIcon(buttonElement, button::setIcon);
            componentLoader().loadTitle(button, buttonElement, context);
            componentLoader().loadClassNames(button, buttonElement);
            componentLoader().loadThemeNames(button, buttonElement);
            loadBoolean(buttonElement, "iconAfterText", button::setIconAfterText);

            return button;
        }

        return null;
    }

    protected void loadColumnsByInclude(T component, Element columnsElement, MetaClass metaClass,
                                        FetchPlan fetchPlan, boolean sortableColumns, boolean resizableColumns) {
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

        List<Element> columnElements = columnsElement.elements(COLUMN_ELEMENT_NAME);
        Set<Element> overriddenColumns = new HashSet<>();

        DocumentFactory documentFactory = DatatypeElementFactory.getInstance();

        for (String property : appliedProperties) {
            Element column = getOverriddenColumn(columnElements, property);
            if (column == null) {
                column = documentFactory.createElement(COLUMN_ELEMENT_NAME);
                column.add(documentFactory.createAttribute(column, "property", property));
            } else {
                overriddenColumns.add(column);
            }

            loadColumn(component, column, metaClass, sortableColumns, resizableColumns);
        }

        // load remains columns
        List<Element> remainedColumns = columnsElement.elements(COLUMN_ELEMENT_NAME);
        for (Element column : remainedColumns) {
            if (overriddenColumns.contains(column)) {
                continue;
            }

            // check property and add
            String propertyId = loadString(column, "property").orElse(null);
            if (propertyId != null) {
                MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyId);
                if (propertyPath == null || getMetaDataTools().fetchPlanContainsProperty(fetchPlan, propertyPath)) {
                    loadColumn(component, column, metaClass, sortableColumns, resizableColumns);
                }
            }
        }
    }

    protected void loadColumn(T component, Element element, MetaClass metaClass,
                              boolean sortableColumns, boolean resizableColumns) {
        String property = loadString(element, "property")
                .orElse(null);

        MetaPropertyPath metaPropertyPath = property != null
                ? getMetaDataTools().resolveMetaPropertyPathOrNull(metaClass, property)
                : null;

        String key = loadString(element, "key")
                .orElseGet(() -> {
                    // We check 'metaPropertyPath' but returns 'property', because we need
                    // a string that matches the meta property, i.e. if 'metaPropertyPath'
                    // is found for `property` then this `property` can be used as a key.
                    if (metaPropertyPath != null) {
                        return property;
                    } else {
                        throw new GuiDevelopmentException("Either key or property must be defined for a column",
                                context, "Component ID", component.getId());
                    }
                });

        DataGridColumn<?> column = metaPropertyPath != null
                ? addColumn(key, metaPropertyPath)
                : addEmptyColumn(key);

        loadString(element, "width", column::setWidth);
        loadResourceString(element, "header", context.getMessageGroup(), column::setHeader);
        loadResourceString(element, "footer", context.getMessageGroup(), column::setFooter);
        loadBoolean(element, "frozen", column::setFrozen);
        loadInteger(element, "flexGrow", column::setFlexGrow);
        loadBoolean(element, "autoWidth", column::setAutoWidth);
        loadBoolean(element, "visible", column::setVisible);
        loadEnum(element, ColumnTextAlign.class, "textAlign", column::setTextAlign);

        loadColumnSortable(element, column, sortableColumns);
        loadColumnResizable(element, column, resizableColumns);
        loadColumnFilterable(element, column);
        loadColumnEditable(element, column, property);
        loadAggregationInfo(element, column);

        loadRenderer(element, metaPropertyPath)
                .ifPresent(column::setRenderer);
    }

    @SuppressWarnings("unchecked")
    protected DataGridColumn<?> addEmptyColumn(String key) {
        return (DataGridColumn<?>) resultComponent.addColumn(new EmptyValueProvider<>())
                .setKey(key);
    }

    @SuppressWarnings("rawtypes")
    protected Optional<Renderer> loadRenderer(Element columnElement, @Nullable MetaPropertyPath metaPropertyPath) {
        if (columnElement.elements().isEmpty()
                || metaPropertyPath == null) {
            return Optional.empty();
        }

        Map<String, RendererProvider> providers = applicationContext.getBeansOfType(RendererProvider.class);
        for (RendererProvider<?> provider : providers.values()) {
            for (Element element : columnElement.elements()) {
                if (provider.supports(element.getName())) {
                    return Optional.of(provider.createRenderer(element, metaPropertyPath, context));
                }
            }
        }

        return Optional.empty();
    }

    protected void loadColumnSortable(Element element, DataGridColumn<?> column, boolean sortableColumns) {
        loadBoolean(element, "sortable")
                .ifPresentOrElse(column::setSortable, () -> column.setSortable(sortableColumns));
    }

    protected void loadColumnResizable(Element element, DataGridColumn<?> column, boolean resizableColumns) {
        loadBoolean(element, "resizable")
                .ifPresentOrElse(column::setResizable, () -> column.setResizable(resizableColumns));
    }

    protected void loadColumnFilterable(Element element, DataGridColumn<?> column) {
        loadBoolean(element, "filterable")
                .ifPresent(filterable -> {
                    if (filterable) {
                        pendingToFilterableColumns.add(column);
                    }
                });
    }

    protected void loadColumnEditable(Element element, DataGridColumn<?> column, String property) {
        loadBoolean(element, "editable", editable -> {
            if (Boolean.TRUE.equals(editable)) {
                setDefaultEditComponent(column, property);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void loadAggregationInfo(Element columnElement, DataGridColumn<?> column) {
        Element aggregationElement = columnElement.element("aggregation");

        if (aggregationElement != null) {
            AggregationInfo aggregation = new AggregationInfo();

            aggregation.setPropertyPath(resultComponent.getColumnMetaPropertyPath(column));

            loadEnum(aggregationElement, AggregationInfo.Type.class, "type", aggregation::setType);
            loadResourceString(aggregationElement, "cellTitle", context.getMessageGroup(), aggregation::setCellTitle);
            loadStrategyClassFqn(aggregation, aggregationElement);
            componentLoader().loadFormatter(aggregation, aggregationElement);

            resultComponent.addAggregation(column, aggregation);

            if (aggregation.getType() == null && aggregation.getStrategy() == null) {
                throw new GuiDevelopmentException("Incorrect aggregation - type or strategyClass is required", context);
            }
        }
    }

    protected void loadStrategyClassFqn(AggregationInfo aggregation, Element element) {
        loadString(element, "strategyClass")
                .ifPresent(strategyClass -> {
                    Class<?> aggregationClass = getClassManager().findClass(strategyClass);

                    if (aggregationClass == null) {
                        String message = String.format("Aggregation class %s is not found", strategyClass);
                        throw new GuiDevelopmentException(message, context);
                    }

                    try {
                        Constructor<?> constructor = aggregationClass.getDeclaredConstructor();
                        AggregationStrategy<?, ?> customStrategy =
                                (AggregationStrategy<?, ?>) constructor.newInstance();
                        applicationContext.getAutowireCapableBeanFactory().autowireBean(customStrategy);
                        aggregation.setStrategy(customStrategy);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to instantiate strategy for aggregation", e);
                    }
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setDefaultEditComponent(DataGridColumn<?> column, String property) {
        Editor<?> editor = resultComponent.getEditor();
        if (editor instanceof DataGridEditor) {
            ((DataGridEditor) editor).initColumnDefaultEditorComponent(column, property);
        }
    }

    protected DataGridColumn<?> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        return resultComponent.addColumn(key, metaPropertyPath);
    }

    protected Collection<String> getAppliedProperties(Element columnsElement,
                                                      @Nullable FetchPlan fetchPlan, MetaClass metaClass) {
        String exclude = loadString(columnsElement, "exclude").orElse(null);
        List<String> excludes = exclude == null ? Collections.emptyList() :
                Splitter.on(",").omitEmptyStrings().trimResults().splitToList(exclude);

        Stream<String> properties;
        if (getMetaDataTools().isJpaEntity(metaClass) && fetchPlan != null) {
            properties = fetchPlan.getProperties().stream().map(FetchPlanProperty::getName);
        } else {
            properties = metaClass.getProperties().stream().map(MetadataObject::getName);
        }

        return properties.filter(s -> !excludes.contains(s)).collect(Collectors.toList());
    }

    @Nullable
    protected Element getOverriddenColumn(List<Element> columns, String property) {
        for (Element element : columns) {
            String propertyAttr = element.attributeValue("property");
            if (StringUtils.isNotEmpty(propertyAttr) && propertyAttr.equals(property)) {
                return element;
            }
        }
        return null;
    }

    protected void loadMetaClass(Element element, Consumer<MetaClass> setter) {
        loadString(element, "metaClass", metaClassStr ->
                setter.accept(applicationContext.getBean(Metadata.class).getClass(metaClassStr)));
    }

    protected void initMasterDataLoaderListener(CollectionPropertyContainer<?> collectionContainer) {
        DataLoader masterDataLoader = DataLoadersHelper.getMasterDataLoader(collectionContainer);

        masterDataLoaderPostLoadListener = masterDataLoader instanceof InstanceLoader
                ? ((InstanceLoader<?>) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : masterDataLoader instanceof CollectionLoader
                ? ((CollectionLoader<?>) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : null;
    }

    protected GridDataHolder initDataGridDataHolder() {
        GridDataHolder holder = new GridDataHolder();

        String containerId = loadString(element, "dataContainer").orElse(null);
        if (containerId == null) {
            loadMetaClass(element, holder::setMetaClass);
            return holder;
        }

        InstanceContainer<?> container = getComponentContext().getViewData().getContainer(containerId);

        CollectionContainer<?> collectionContainer;
        if (container instanceof CollectionContainer) {
            collectionContainer = (CollectionContainer<?>) container;
        } else {
            throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
        }

        if (collectionContainer instanceof CollectionPropertyContainer) {
            initMasterDataLoaderListener((CollectionPropertyContainer<?>) collectionContainer);
        }

        if (collectionContainer instanceof HasLoader) {
            //noinspection ConstantConditions
            holder.setDataLoader(((HasLoader) collectionContainer).getLoader());
        }

        holder.setMetaClass(collectionContainer.getEntityMetaClass());
        holder.setContainer(collectionContainer);
        //noinspection ConstantConditions
        holder.setFetchPlan(collectionContainer.getFetchPlan());

        return holder;
    }

    /**
     * Contains information about metaClass, data container, loader, fetch plan.
     */
    protected static class GridDataHolder {


        protected MetaClass metaClass;

        protected CollectionContainer<?> container;
        protected DataLoader dataLoader;
        protected FetchPlan fetchPlan;

        public GridDataHolder() {
        }

        @Nullable
        public MetaClass getMetaClass() {
            return metaClass;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Nullable
        public CollectionContainer<?> getContainer() {
            return container;
        }

        public void setContainer(CollectionContainer<?> container) {
            this.container = container;
        }

        @Nullable
        public DataLoader getDataLoader() {
            return dataLoader;
        }

        public void setDataLoader(DataLoader dataLoader) {
            this.dataLoader = dataLoader;
        }

        @Nullable
        public FetchPlan getFetchPlan() {
            return fetchPlan;
        }

        public void setFetchPlan(FetchPlan fetchPlan) {
            this.fetchPlan = fetchPlan;
        }

        public boolean isContainerLoaded() {
            return container != null;
        }
    }

    protected void onMasterDataLoaderPostLoad(Object o) {
        if (masterDataLoaderPostLoadListener != null) {
            masterDataLoaderPostLoadListener.remove();
        }
    }

    protected FetchPlanRepositoryImpl getFetchPlanRepository() {
        if (fetchPlanRepository == null) {
            fetchPlanRepository = applicationContext.getBean(FetchPlanRepositoryImpl.class, context);
        }
        return fetchPlanRepository;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    protected MetadataTools getMetaDataTools() {
        if (metaDataTools == null) {
            metaDataTools = applicationContext.getBean(MetadataTools.class, context);
        }
        return metaDataTools;
    }

    protected ClassManager getClassManager() {
        if (classManager == null) {
            classManager = applicationContext.getBean(ClassManager.class);
        }
        return classManager;
    }

    protected abstract void setupDataProvider(GridDataHolder holder);

    protected void loadContextMenu() {
        Element contextMenuElement = element.element("contextMenu");
        if (contextMenuElement == null) {
            return;
        }
        JmixGridContextMenu<?> contextMenu = resultComponent.getContextMenu();

        loadId(contextMenu, contextMenuElement);
        loadVisible(contextMenu, contextMenuElement);

        componentLoader().loadCss(contextMenu, contextMenuElement);
        componentLoader().loadClassNames(contextMenu, contextMenuElement);
        componentLoader().loadEnabled(contextMenu, contextMenuElement);

        for (Element childItemElement : contextMenuElement.elements()) {
            addContextMenuItem(contextMenu::addItem, contextMenu::add, childItemElement);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void addContextMenuItem(Function<Component, GridMenuItem<?>> menuItemGenerator,
                                      Consumer<Component> separatorConsumer,
                                      Element childElement) {
        switch (childElement.getName()) {
            case "item":
                GridContextMenuItemComponent<?> component = new GridContextMenuItemComponent<>();
                component.setApplicationContext(applicationContext);
                GridMenuItem menuItem = menuItemGenerator.apply(component);
                component.setMenuItem(menuItem);
                loadContextMenuItem(component, menuItem, childElement);
                break;
            case "separator":
                separatorConsumer.accept(new Hr());
                break;
            default:
                throw new GuiDevelopmentException("Unknown context menu child element: " + childElement.getName(),
                        context, "Component ID", resultComponent.getId());
        }
    }

    protected void loadContextMenuItem(GridContextMenuItemComponent<?> component,
                                       GridMenuItem<?> menuItem,
                                       Element itemElement) {
        loadId(menuItem, itemElement);
        loadVisible(menuItem, itemElement);

        componentLoader().loadCss(menuItem, itemElement);
        componentLoader().loadEnabled(menuItem, itemElement);
        componentLoader().loadClassNames(menuItem, itemElement);

        componentLoader().loadText(component, itemElement);
        componentLoader().loadWhiteSpace(component, itemElement);
        componentLoader().loadIcon(itemElement, component::setPrefixComponent);

        loadContextMenuItemAction(component, itemElement);

        GridSubMenu<?> subMenu = menuItem.getSubMenu();
        for (Element contextMenuChildItemElement : itemElement.elements()) {
            addContextMenuItem(subMenu::addItem, subMenu::add, contextMenuChildItemElement);
        }
    }

    protected void loadContextMenuItemAction(GridContextMenuItemComponent<?> component, Element element) {
        loadString(element, "action")
                .ifPresent(actionId -> getComponentContext().addInitTask(
                        new AssignActionInitTask<>(component, actionId, getComponentContext().getView())));
    }

    protected void loadActions() {
        getActionLoaderSupport().loadActions(resultComponent, element);
    }
}
