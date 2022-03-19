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
package io.jmix.ui.xml.layout.loader;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.SimplePagination;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.aggregation.AggregationStrategy;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.component.data.table.EmptyTableItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.DataLoadersHelper;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.DeclarativeColumnGenerator;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTableLoader<T extends Table> extends ActionsHolderLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractTableLoader.class);

    protected ComponentLoader buttonsPanelLoader;
    protected Element panelElement;

    protected String sortedColumnId;
    protected Table.SortDirection sortDirection;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);
        loadEditable(resultComponent, element);

        loadAlign(resultComponent, element);
        loadStyleName(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadSortable(resultComponent, element);
        loadReorderingAllowed(resultComponent, element);
        loadColumnControlVisible(resultComponent, element);
        loadAggregatable(resultComponent, element);
        loadAggregationStyle(resultComponent, element);

        loadActions(resultComponent, element);
        loadContextMenuEnabled(resultComponent, element);
        loadMultiLineCells(resultComponent, element);

        loadColumnHeaderVisible(resultComponent, element);
        loadShowSelection(resultComponent, element);
        loadTextSelectionEnabled(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
        loadEmptyStateMessage(resultComponent, element);
        loadEmptyStateLinkMessage(resultComponent, element);

        loadMinHeight(element, resultComponent::setMinHeight);
        loadMinWidth(element, resultComponent::setMinWidth);

        Element rowsElement = element.element("rows");

        if (rowsElement != null) {
            String rowHeaderMode = rowsElement.attributeValue("rowHeaderMode");
            if (StringUtils.isBlank(rowHeaderMode)) {
                rowHeaderMode = rowsElement.attributeValue("headerMode");
                if (StringUtils.isNotBlank(rowHeaderMode)) {
                    log.warn("Attribute headerMode is deprecated. Use rowHeaderMode.");
                }
            }

            if (!StringUtils.isEmpty(rowHeaderMode)) {
                resultComponent.setRowHeaderMode(Table.RowHeaderMode.valueOf(rowHeaderMode));
            }
        }

        String rowHeaderMode = element.attributeValue("rowHeaderMode");
        if (!StringUtils.isEmpty(rowHeaderMode)) {
            resultComponent.setRowHeaderMode(Table.RowHeaderMode.valueOf(rowHeaderMode));
        }

        loadButtonsPanel(resultComponent);
        loadPagination(resultComponent, element);

        loadTableData();

        String multiselect = element.attributeValue("multiselect");
        if (StringUtils.isNotEmpty(multiselect)) {
            resultComponent.setMultiSelect(Boolean.parseBoolean(multiselect));
        }
    }

    protected void loadTableData() {
        TableDataHolder holder = initTableDataHolder();
        if (!holder.isContainerLoaded()
                && holder.getMetaClass() == null) {
            throw new GuiDevelopmentException("Table doesn't have data binding",
                    context, "Table ID", element.attributeValue("id"));
        }

        List<Table.Column> availableColumns;

        Element columnsElement = element.element("columns");
        if (columnsElement != null) {
            FetchPlan fetchPlan = holder.getFetchPlan();
            if (fetchPlan == null) {
                fetchPlan = getViewRepository().getFetchPlan(holder.getMetaClass(), FetchPlan.BASE);
            }
            availableColumns = loadColumns(resultComponent, columnsElement, holder.getMetaClass(), fetchPlan);
        } else {
            availableColumns = new ArrayList<>();
        }

        for (Table.Column column : availableColumns) {
            loadRequired(resultComponent, column);
        }

        setupDataContainer(holder);

        if (resultComponent.getItems() == null) {
            //noinspection unchecked
            resultComponent.setItems(createEmptyTableItems(holder.getMetaClass()));
        }

        for (Table.Column column : availableColumns) {
            if (column.getXmlDescriptor() != null) {
                String generatorMethod = column.getXmlDescriptor().attributeValue("generator");
                if (StringUtils.isNotEmpty(generatorMethod)) {
                    //noinspection unchecked
                    resultComponent.addGeneratedColumn(String.valueOf(column),
                            applicationContext.getBean(DeclarativeColumnGenerator.class,
                                    resultComponent, generatorMethod));
                }
            }
        }
    }

    protected TableDataHolder initTableDataHolder() {
        TableDataHolder holder = new TableDataHolder();

        String containerId = element.attributeValue("dataContainer");
        if (Strings.isNullOrEmpty(containerId)) {
            loadMetaClass(element, holder::setMetaClass);
            return holder;
        }

        FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
        ScreenData screenData = UiControllerUtils.getScreenData(frameOwner);
        InstanceContainer container = screenData.getContainer(containerId);

        CollectionContainer collectionContainer;
        if (container instanceof CollectionContainer) {
            collectionContainer = ((CollectionContainer) container);
        } else {
            throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
        }

        if (container instanceof CollectionPropertyContainer) {
            initMasterDataLoaderListener((CollectionPropertyContainer) container);
        }

        if (collectionContainer instanceof HasLoader) {
            holder.setDataLoader(((HasLoader) collectionContainer).getLoader());
        }

        holder.setMetaClass(collectionContainer.getEntityMetaClass());
        holder.setContainer(collectionContainer);
        holder.setFetchPlan(collectionContainer.getFetchPlan());

        return holder;
    }

    protected void initMasterDataLoaderListener(CollectionPropertyContainer collectionContainer) {
        DataLoader masterDataLoader = DataLoadersHelper.getMasterDataLoader(collectionContainer);

        masterDataLoaderPostLoadListener = masterDataLoader instanceof InstanceLoader
                ? ((InstanceLoader) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : masterDataLoader instanceof CollectionLoader
                ? ((CollectionLoader) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : null;
    }

    protected void onMasterDataLoaderPostLoad(Object o) {
        setColumnSort();

        if (masterDataLoaderPostLoadListener != null) {
            masterDataLoaderPostLoadListener.remove();
        }
    }

    protected void setColumnSort() {
        if (sortedColumnId != null && sortDirection != null) {
            resultComponent.sort(sortedColumnId, sortDirection);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setupDataContainer(TableDataHolder holder) {
        if (holder.getContainer() != null) {
            resultComponent.setItems(createContainerTableSource(holder.getContainer()));
        }
    }

    protected Metadata getMetadata() {
        return applicationContext.getBean(Metadata.class);
    }

    protected FetchPlanRepository getViewRepository() {
        return applicationContext.getBean(FetchPlanRepository.class);
    }

    @SuppressWarnings("unchecked")
    protected ContainerTableItems createContainerTableSource(CollectionContainer container) {
        return new ContainerTableItems(container);
    }

    protected TableItems createEmptyTableItems(MetaClass metaClass) {
        return new EmptyTableItems(metaClass);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }

    protected void loadTextSelectionEnabled(Table table, Element element) {
        String textSelectionEnabled = element.attributeValue("textSelectionEnabled");
        if (StringUtils.isNotEmpty(textSelectionEnabled)) {
            table.setTextSelectionEnabled(Boolean.parseBoolean(textSelectionEnabled));
        }
    }

    protected void loadMultiLineCells(Table table, Element element) {
        String multiLineCells = element.attributeValue("multiLineCells");
        if (StringUtils.isNotEmpty(multiLineCells)) {
            table.setMultiLineCells(Boolean.parseBoolean(multiLineCells));
        }
    }

    protected void loadContextMenuEnabled(Table table, Element element) {
        String contextMenuEnabled = element.attributeValue("contextMenuEnabled");
        if (StringUtils.isNotEmpty(contextMenuEnabled)) {
            table.setContextMenuEnabled(Boolean.parseBoolean(contextMenuEnabled));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadPagination(Table table, Element element) {
        Element paginationElement = element.element("simplePagination");
        if (paginationElement != null) {

            ComponentLoader<SimplePagination> loader = getLayoutLoader()
                    .getLoader(paginationElement, SimplePagination.NAME);
            loader.createComponent();
            loader.loadComponent();

            SimplePagination pagination = loader.getResultComponent();
            table.setPagination(pagination);
        }
    }

    protected List<Table.Column> loadColumnsByInclude(Table component, Element columnsElement, MetaClass metaClass,
                                                      FetchPlan fetchPlan) {
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

        List<Table.Column> columns = new ArrayList<>(appliedProperties.size());
        List<Element> columnElements = columnsElement.elements("column");
        Set<Element> overriddenColumns = new HashSet<>();

        DocumentFactory documentFactory = DatatypeElementFactory.getInstance();

        for (String property : appliedProperties) {
            Element column = getOverriddenColumn(columnElements, property);
            if (column == null) {
                column = documentFactory.createElement("column");
                column.add(documentFactory.createAttribute(column, "id", property));
            } else {
                overriddenColumns.add(column);
            }

            String visible = column.attributeValue("visible");
            if (StringUtils.isEmpty(visible) || Boolean.parseBoolean(visible)) {
                columns.add(loadColumn(component, column, metaClass));
            }
        }

        // load remains columns
        List<Element> remainedColumns = columnsElement.elements("column");
        for (Element column : remainedColumns) {
            if (overriddenColumns.contains(column)) {
                continue;
            }

            // check property and add
            String propertyId = column.attributeValue("id");
            if (StringUtils.isNotEmpty(propertyId)) {
                MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyId);
                if (propertyPath == null || getMetadataTools().fetchPlanContainsProperty(fetchPlan, propertyPath)) {
                    String visible = column.attributeValue("visible");
                    if (StringUtils.isEmpty(visible) || Boolean.parseBoolean(visible)) {
                        columns.add(loadColumn(component, column, metaClass));
                    }
                }
            }
        }

        return columns;
    }

    protected List<Table.Column> loadColumns(Table component, Element columnsElement, MetaClass metaClass,
                                             FetchPlan fetchPlan) {
        String includeAll = columnsElement.attributeValue("includeAll");
        if (StringUtils.isNotBlank(includeAll)
                && Boolean.parseBoolean(includeAll)) {
            return loadColumnsByInclude(component, columnsElement, metaClass, fetchPlan);
        }

        List<Element> columnElements = columnsElement.elements("column");

        List<Table.Column> columns = new ArrayList<>(columnElements.size());
        for (Element columnElement : columnElements) {
            String visible = columnElement.attributeValue("visible");
            if (StringUtils.isEmpty(visible) || Boolean.parseBoolean(visible)) {
                columns.add(loadColumn(component, columnElement, metaClass));
            }
        }
        return columns;
    }

    protected void loadAggregatable(Table component, Element element) {
        String aggregatable = element.attributeValue("aggregatable");
        if (StringUtils.isNotEmpty(aggregatable)) {
            component.setAggregatable(Boolean.parseBoolean(aggregatable));
            String showTotalAggregation = element.attributeValue("showTotalAggregation");
            if (StringUtils.isNotEmpty(showTotalAggregation)) {
                component.setShowTotalAggregation(Boolean.parseBoolean(showTotalAggregation));
            }
        }
    }

    protected void loadAggregationStyle(Table component, Element element) {
        String aggregationStyle = element.attributeValue("aggregationStyle");
        if (!StringUtils.isEmpty(aggregationStyle)) {
            component.setAggregationStyle(Table.AggregationStyle.valueOf(aggregationStyle));
        }
    }

    protected void createButtonsPanel(T table, Element element) {
        panelElement = element.element("buttonsPanel");
        if (panelElement != null) {
            ButtonsPanelLoader loader = (ButtonsPanelLoader) getLayoutLoader()
                    .getLoader(panelElement, ButtonsPanel.NAME);
            loader.createComponent();
            ButtonsPanel panel = loader.getResultComponent();

            table.setButtonsPanel(panel);

            buttonsPanelLoader = loader;
        }
    }

    protected void loadButtonsPanel(T component) {
        if (buttonsPanelLoader != null) {
            buttonsPanelLoader.loadComponent();
            ButtonsPanel panel = (ButtonsPanel) buttonsPanelLoader.getResultComponent();

            String alwaysVisible = panelElement.attributeValue("alwaysVisible");
            if (alwaysVisible != null) {
                panel.setAlwaysVisible(Boolean.parseBoolean(alwaysVisible));
            }

            if (panel.getCaption() != null) {
                log.debug("The caption '{}' of ButtonsPanel inside of Table will be ignored",
                        panel.getCaption());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadRequired(Table component, Table.Column column) {
        Element element = column.getXmlDescriptor();
        String required = element.attributeValue("required");
        if (StringUtils.isNotEmpty(required)) {
            String requiredMsg = element.attributeValue("requiredMessage");
            component.setRequired(column, Boolean.parseBoolean(required), loadResourceString(requiredMsg));
        }
    }

    protected Table.Column loadColumn(Table component, Element element, MetaClass metaClass) {
        Object id = loadColumnId(element, metaClass);
        Table.Column column = component.addColumn(id);

        String editable = element.attributeValue("editable");
        if (StringUtils.isNotEmpty(editable)) {
            column.setEditable(Boolean.parseBoolean(editable));
        }

        String collapsed = element.attributeValue("collapsed");
        if (StringUtils.isNotEmpty(collapsed)) {
            column.setCollapsed(Boolean.parseBoolean(collapsed));
        }

        String sortable = element.attributeValue("sortable");
        if (StringUtils.isNotEmpty(sortable)) {
            column.setSortable(Boolean.parseBoolean(sortable));
        }

        String sort = element.attributeValue("sort");
        if (StringUtils.isNotBlank(sort)) {
            loadColumnSort(column, sort);
        }

        loadCaption(column, element);
        loadDescription(column, element);

        if (column.getCaption() == null) {
            String columnCaption;
            if (column.getId() instanceof MetaPropertyPath) {
                MetaPropertyPath mpp = (MetaPropertyPath) column.getId();
                MetaProperty metaProperty = mpp.getMetaProperty();
                String propertyName = metaProperty.getName();

                MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(mpp);
                columnCaption = getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
            } else {
                Class<?> declaringClass = metaClass.getJavaClass();
                String className = declaringClass.getName();
                int i = className.lastIndexOf('.');
                if (i > -1)
                    className = className.substring(i + 1);
                columnCaption = getMessages().getMessage(declaringClass, className + "." + id);
            }
            column.setCaption(columnCaption);
        }

        column.setXmlDescriptor(element);

        String expandRatio = element.attributeValue("expandRatio");
        String width = loadThemeString(element.attributeValue("width"));
        if (StringUtils.isNotEmpty(expandRatio)) {
            column.setExpandRatio(Float.parseFloat(expandRatio));

            if (StringUtils.isNotEmpty(width)) {
                throw new GuiDevelopmentException(
                        "Properties 'width' and 'expandRatio' cannot be used simultaneously", context);
            }
        }

        if (StringUtils.isNotEmpty(width)) {
            if (StringUtils.endsWith(width, "px")) {
                width = StringUtils.substring(width, 0, width.length() - 2);
            }
            try {
                column.setWidth(Integer.parseInt(width));
            } catch (NumberFormatException e) {
                throw new GuiDevelopmentException("Property 'width' must contain only numeric value",
                        context, "width", element.attributeValue("width"));
            }
        }
        String align = element.attributeValue("align");
        if (StringUtils.isNotEmpty(align)) {
            column.setAlignment(Table.ColumnAlignment.valueOf(align));
        }

        column.setFormatter(loadFormatter(element));

        loadAggregation(column, element);
        loadMaxTextLength(column, element);
        loadCaptionAsHtml(column, element);

        return column;
    }

    protected Object loadColumnId(Element element, MetaClass metaClass) {
        String id = element.attributeValue("id");
        MetaPropertyPath metaPropertyPath = getMetadataTools().resolveMetaPropertyPathOrNull(metaClass, id);
        return metaPropertyPath != null ? metaPropertyPath : id;
    }

    protected void loadCaptionAsHtml(Table.Column component, Element element) {
        String captionAsHtml = element.attributeValue("captionAsHtml");
        if (captionAsHtml != null && !captionAsHtml.isEmpty()) {
            component.setCaptionAsHtml(Boolean.parseBoolean(captionAsHtml));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadAggregation(Table.Column column, Element columnElement) {
        Element aggregationElement = columnElement.element("aggregation");
        if (aggregationElement != null) {
            AggregationInfo aggregation = new AggregationInfo();
            aggregation.setPropertyPath((MetaPropertyPath) column.getId());
            String aggregationType = aggregationElement.attributeValue("type");
            if (StringUtils.isNotEmpty(aggregationType)) {
                aggregation.setType(AggregationInfo.Type.valueOf(aggregationType));
            }

            String aggregationEditable = aggregationElement.attributeValue("editable");
            if (StringUtils.isNotEmpty(aggregationEditable)) {
                aggregation.setEditable(Boolean.parseBoolean(aggregationEditable));
            }

            String valueDescription = aggregationElement.attributeValue("valueDescription");
            if (StringUtils.isNotEmpty(valueDescription)) {
                column.setValueDescription(loadResourceString(valueDescription));
            }

            Formatter formatter = loadFormatter(aggregationElement);
            aggregation.setFormatter(formatter == null ? column.getFormatter() : formatter);

            String strategyClass = aggregationElement.attributeValue("strategyClass");
            if (StringUtils.isNotEmpty(strategyClass)) {
                Class<?> aggregationClass = getClassManager().findClass(strategyClass);
                if (aggregationClass == null) {
                    throw new GuiDevelopmentException(String.format("Class %s is not found", strategyClass), context);
                }

                try {
                    Constructor<?> constructor = aggregationClass.getDeclaredConstructor();
                    AggregationStrategy customStrategy = (AggregationStrategy) constructor.newInstance();
                    applicationContext.getAutowireCapableBeanFactory().autowireBean(customStrategy);
                    aggregation.setStrategy(customStrategy);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to instantiate strategy for aggregation", e);
                }
            }

            if (aggregationType == null && strategyClass == null) {
                throw new GuiDevelopmentException("Incorrect aggregation - type or strategyClass is required", context);
            }

            column.setAggregation(aggregation);
        }
    }

    protected void loadMaxTextLength(Table.Column column, Element columnElement) {
        String maxTextLength = columnElement.attributeValue("maxTextLength");
        if (!StringUtils.isBlank(maxTextLength)) {
            column.setMaxTextLength(Integer.parseInt(maxTextLength));
        }
    }

    protected void loadSortable(Table component, Element element) {
        String sortable = element.attributeValue("sortable");
        if (StringUtils.isNotEmpty(sortable)) {
            component.setSortable(Boolean.parseBoolean(sortable));
        }
    }

    protected void loadReorderingAllowed(Table component, Element element) {
        String reorderingAllowed = element.attributeValue("reorderingAllowed");
        if (StringUtils.isNotEmpty(reorderingAllowed)) {
            component.setColumnReorderingAllowed(Boolean.parseBoolean(reorderingAllowed));
        }
    }

    protected void loadColumnControlVisible(Table component, Element element) {
        String columnControlVisible = element.attributeValue("columnControlVisible");
        if (StringUtils.isNotEmpty(columnControlVisible)) {
            component.setColumnControlVisible(Boolean.parseBoolean(columnControlVisible));
        }
    }

    protected void loadColumnHeaderVisible(Table component, Element element) {
        String columnHeaderVisible = element.attributeValue("columnHeaderVisible");
        if (StringUtils.isNotEmpty(columnHeaderVisible)) {
            component.setColumnHeaderVisible(Boolean.parseBoolean(columnHeaderVisible));
        }
    }

    protected void loadShowSelection(Table component, Element element) {
        String showSelection = element.attributeValue("showSelection");
        if (StringUtils.isNotEmpty(showSelection)) {
            component.setShowSelection(Boolean.parseBoolean(showSelection));
        }
    }

    protected Collection<String> getAppliedProperties(Element columnsElement, FetchPlan fetchPlan, MetaClass metaClass) {
        String exclude = columnsElement.attributeValue("exclude");
        List<String> excludes = StringUtils.isEmpty(exclude) ? Collections.emptyList() :
                Splitter.on(",").omitEmptyStrings().trimResults().splitToList(exclude);

        MetadataTools metadataTools = getMetadataTools();

        Stream<String> properties;
        if (metadataTools.isJpaEntity(metaClass) && fetchPlan != null) {
            properties = fetchPlan.getProperties().stream().map(FetchPlanProperty::getName);
        } else {
            properties = metaClass.getProperties().stream().map(MetadataObject::getName);
        }

        List<String> appliedProperties = properties.filter(s -> !excludes.contains(s)).collect(Collectors.toList());

        return appliedProperties;
    }

    @Nullable
    protected Element getOverriddenColumn(List<Element> columns, String property) {
        if (CollectionUtils.isEmpty(columns)) {
            return null;
        }

        for (Element element : columns) {
            String id = element.attributeValue("id");
            if (StringUtils.isNotEmpty(id) && id.equals(property)) {
                return element;
            }
        }
        return null;
    }

    protected void loadColumnSort(Table.Column column, String sort) {
        if (sortedColumnId != null) {
            throw new GuiDevelopmentException(String.format("Column '%s' cannot be sorted, because Table have already" +
                    " sorted '%s' column", column.getStringId(), sortedColumnId), getContext());
        }

        if (column.getMetaPropertyPath() == null) {
            throw new GuiDevelopmentException(
                    String.format("Can't sort column '%s' because it is not bounded with entity's property",
                            column.getStringId()),
                    getContext());
        }

        if (!column.isSortable()) {
            throw new GuiDevelopmentException(
                    String.format("Can't sort column '%s' because it is disabled for sorting by 'sortable' attribute",
                            column.getStringId()),
                    getContext());
        }

        sortDirection = Table.SortDirection.valueOf(sort);
        sortedColumnId = column.getStringId();
        getComponentContext().addPostInitTask((context, window) -> setColumnSort());
    }

    protected void loadEmptyStateMessage(Table table, Element element) {
        String emptyStateMessage = element.attributeValue("emptyStateMessage");
        if (!Strings.isNullOrEmpty(emptyStateMessage)) {
            table.setEmptyStateMessage(loadResourceString(emptyStateMessage));
        }
    }

    protected void loadEmptyStateLinkMessage(Table table, Element element) {
        String emptyStateLinkMessage = element.attributeValue("emptyStateLinkMessage");
        if (!Strings.isNullOrEmpty(emptyStateLinkMessage)) {
            table.setEmptyStateLinkMessage(loadResourceString(emptyStateLinkMessage));
        }
    }

    /**
     * Contains information about metaclass, data container, loader, fetch plan.
     */
    protected static class TableDataHolder {

        protected MetaClass metaClass;
        protected CollectionContainer container;
        protected DataLoader dataLoader;
        protected FetchPlan fetchPlan;

        public TableDataHolder() {
        }

        @Nullable
        public MetaClass getMetaClass() {
            return metaClass;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Nullable
        public CollectionContainer getContainer() {
            return container;
        }

        public void setContainer(CollectionContainer container) {
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
}
