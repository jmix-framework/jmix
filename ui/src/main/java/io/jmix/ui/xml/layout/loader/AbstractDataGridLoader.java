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
import com.google.common.collect.ImmutableMap;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.*;
import io.jmix.ui.component.DataGrid.Column;
import io.jmix.ui.component.data.DataGridItems;
import io.jmix.ui.component.data.aggregation.AggregationStrategy;
import io.jmix.ui.component.data.datagrid.ContainerDataGridItems;
import io.jmix.ui.component.data.datagrid.EmptyDataGridItems;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.DataLoadersHelper;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.ComponentLoader;
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

public abstract class AbstractDataGridLoader<T extends DataGrid> extends ActionsHolderLoader<T> {
    protected static final Map<String, Class<? extends DataGrid.Renderer>> RENDERERS_MAP =
            ImmutableMap.<String, Class<? extends DataGrid.Renderer>>builder()
                    .put("checkBoxRenderer", DataGrid.CheckBoxRenderer.class)
                    .put("componentRenderer", DataGrid.ComponentRenderer.class)
                    .put("dateRenderer", DataGrid.DateRenderer.class)
                    .put("iconRenderer", DataGrid.IconRenderer.class)
                    .put("htmlRenderer", DataGrid.HtmlRenderer.class)
                    .put("localDateRenderer", DataGrid.LocalDateRenderer.class)
                    .put("localDateTimeRenderer", DataGrid.LocalDateTimeRenderer.class)
                    .put("numberRenderer", DataGrid.NumberRenderer.class)
                    .put("progressBarRenderer", DataGrid.ProgressBarRenderer.class)
                    .put("textRenderer", DataGrid.TextRenderer.class)
                    .build();

    private static final Logger log = LoggerFactory.getLogger(AbstractDataGridLoader.class);

    protected ComponentLoader buttonsPanelLoader;
    protected Element panelElement;

    protected String sortedColumnId;
    protected DataGrid.SortDirection sortDirection;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer

    @Override
    public void createComponent() {
        resultComponent = createComponentInternal();
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    protected abstract T createComponentInternal();

    protected void createButtonsPanel(HasButtonsPanel dataGrid, Element element) {
        panelElement = element.element("buttonsPanel");
        if (panelElement != null) {
            LayoutLoader layoutLoader = getLayoutLoader();

            ButtonsPanelLoader loader = (ButtonsPanelLoader) layoutLoader.getLoader(panelElement, ButtonsPanel.NAME);
            loader.createComponent();
            ButtonsPanel panel = loader.getResultComponent();

            dataGrid.setButtonsPanel(panel);

            buttonsPanelLoader = loader;
        }
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);

        loadAlign(resultComponent, element);
        loadStyleName(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadEditorEnabled(resultComponent, element);
        loadEditorBuffered(resultComponent, element);
        loadEditorSaveCaption(resultComponent, element);
        loadEditorCancelCaption(resultComponent, element);
        loadEditorCrossFieldEnabled(resultComponent, element);

        loadActions(resultComponent, element);

        loadContextMenuEnabled(resultComponent, element);
        loadColumnsHidingAllowed(resultComponent, element);
        loadColumnResizeMode(resultComponent, element);
        loadSortable(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
        loadReorderingAllowed(resultComponent, element);
        loadHeaderVisible(resultComponent, element);
        loadFooterVisible(resultComponent, element);
        loadTextSelectionEnabled(resultComponent, element);
        loadBodyRowHeight(resultComponent, element);
        loadHeaderRowHeight(resultComponent, element);
        loadFooterRowHeight(resultComponent, element);
        loadEmptyStateMessage(resultComponent, element);
        loadEmptyStateLinkMessage(resultComponent, element);
        loadAggregatable(resultComponent, element);
        loadAggregationPosition(resultComponent, element);

        loadButtonsPanel(resultComponent);
        loadPagination(resultComponent, element);

        loadDataGridData();

        loadSelectionMode(resultComponent, element);
        loadFrozenColumnCount(resultComponent, element);
        loadTabIndex(resultComponent, element);

        loadMinHeight(element, resultComponent::setMinHeight);
        loadMinWidth(element, resultComponent::setMinWidth);
    }

    protected void loadDataGridData() {
        DataGridDataHolder holder = initDataGridDataHolder();
        if (!holder.isContainerLoaded()
                && holder.getMetaClass() == null) {
            throw new GuiDevelopmentException("DataGrid doesn't have data binding",
                    context, "DataGrid ID", element.attributeValue("id"));
        }

        Element columnsElement = element.element("columns");
        if (columnsElement != null) {
            FetchPlan fetchPlan = holder.getFetchPlan();
            if (fetchPlan == null) {
                fetchPlan = getViewRepository().getFetchPlan(holder.getMetaClass(), FetchPlan.LOCAL);
            }

            loadColumns(resultComponent, columnsElement, holder.getMetaClass(), fetchPlan);
        }

        setupDataContainer(holder);

        if (resultComponent.getItems() == null) {
            //noinspection unchecked
            resultComponent.setItems(createEmptyDataGridItems(holder.getMetaClass()));
        }
    }

    protected DataGridDataHolder initDataGridDataHolder() {
        DataGridDataHolder holder = new DataGridDataHolder();

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
            collectionContainer = (CollectionContainer) container;
        } else {
            throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
        }

        if (collectionContainer instanceof CollectionPropertyContainer) {
            initMasterDataLoaderListener((CollectionPropertyContainer) collectionContainer);
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
    protected void setupDataContainer(DataGridDataHolder holder) {
        if (holder.getContainer() != null) {
            resultComponent.setItems(createContainerDataGridSource(holder.getContainer()));
        }
    }

    protected Metadata getMetadata() {
        return applicationContext.getBean(Metadata.class);
    }

    protected FetchPlanRepository getViewRepository() {
        return applicationContext.getBean(FetchPlanRepository.class);
    }

    @SuppressWarnings("unchecked")
    protected DataGridItems createContainerDataGridSource(CollectionContainer container) {
        return new ContainerDataGridItems(container);
    }

    protected DataGridItems createEmptyDataGridItems(MetaClass metaClass) {
        return new EmptyDataGridItems(metaClass);
    }

    protected void loadEditorEnabled(DataGrid component, Element element) {
        String editorEnabled = element.attributeValue("editorEnabled");
        if (StringUtils.isNotEmpty(editorEnabled)) {
            component.setEditorEnabled(Boolean.parseBoolean(editorEnabled));
        }
    }

    protected void loadEditorBuffered(DataGrid component, Element element) {
        String editorBuffered = element.attributeValue("editorBuffered");
        if (StringUtils.isNotEmpty(editorBuffered)) {
            component.setEditorBuffered(Boolean.parseBoolean(editorBuffered));
        }
    }

    protected void loadEditorSaveCaption(DataGrid component, Element element) {
        String editorSaveCaption = element.attributeValue("editorSaveCaption");
        if (StringUtils.isNotEmpty(editorSaveCaption)) {
            editorSaveCaption = loadResourceString(editorSaveCaption);
            component.setEditorSaveCaption(editorSaveCaption);
        }
    }

    protected void loadEditorCancelCaption(DataGrid component, Element element) {
        String editorCancelCaption = element.attributeValue("editorCancelCaption");
        if (StringUtils.isNotEmpty(editorCancelCaption)) {
            editorCancelCaption = loadResourceString(editorCancelCaption);
            component.setEditorCancelCaption(editorCancelCaption);
        }
    }

    protected void loadEditorCrossFieldEnabled(DataGrid component, Element element) {
        String editorCrossFieldValidate = element.attributeValue("editorCrossFieldValidate");
        if (StringUtils.isNotEmpty(editorCrossFieldValidate)) {
            component.setEditorCrossFieldValidate(Boolean.parseBoolean(editorCrossFieldValidate));
        }
    }

    protected void loadColumnsHidingAllowed(DataGrid component, Element element) {
        String columnsCollapsingAllowed = element.attributeValue("columnsCollapsingAllowed");
        if (StringUtils.isNotEmpty(columnsCollapsingAllowed)) {
            component.setColumnsCollapsingAllowed(Boolean.parseBoolean(columnsCollapsingAllowed));
        }
    }

    protected void loadColumnResizeMode(DataGrid component, Element element) {
        String columnResizeMode = element.attributeValue("columnResizeMode");
        if (StringUtils.isNotEmpty(columnResizeMode)) {
            component.setColumnResizeMode(DataGrid.ColumnResizeMode.valueOf(columnResizeMode));
        }
    }

    protected void loadSortable(DataGrid component, Element element) {
        String sortable = element.attributeValue("sortable");
        if (StringUtils.isNotEmpty(sortable)) {
            component.setSortable(Boolean.parseBoolean(sortable));
        }
    }

    protected void loadReorderingAllowed(DataGrid component, Element element) {
        String reorderingAllowed = element.attributeValue("reorderingAllowed");
        if (StringUtils.isNotEmpty(reorderingAllowed)) {
            component.setColumnReorderingAllowed(Boolean.parseBoolean(reorderingAllowed));
        }
    }

    protected void loadTextSelectionEnabled(DataGrid dataGrid, Element element) {
        String textSelectionEnabled = element.attributeValue("textSelectionEnabled");
        if (StringUtils.isNotEmpty(textSelectionEnabled)) {
            dataGrid.setTextSelectionEnabled(Boolean.parseBoolean(textSelectionEnabled));
        }
    }

    protected void loadBodyRowHeight(DataGrid dataGrid, Element element) {
        Integer bodyRowHeight = loadSizeInPx(element, "bodyRowHeight");
        if (bodyRowHeight != null) {
            dataGrid.setBodyRowHeight(bodyRowHeight);
        }
    }

    protected void loadHeaderRowHeight(DataGrid dataGrid, Element element) {
        Integer headerRowHeight = loadSizeInPx(element, "headerRowHeight");
        if (headerRowHeight != null) {
            dataGrid.setHeaderRowHeight(headerRowHeight);
        }
    }

    protected void loadFooterRowHeight(DataGrid dataGrid, Element element) {
        Integer footerRowHeight = loadSizeInPx(element, "footerRowHeight");
        if (footerRowHeight != null) {
            dataGrid.setFooterRowHeight(footerRowHeight);
        }
    }

    protected void loadHeaderVisible(DataGrid component, Element element) {
        String columnHeaderVisible = element.attributeValue("headerVisible");
        if (StringUtils.isNotEmpty(columnHeaderVisible)) {
            component.setHeaderVisible(Boolean.parseBoolean(columnHeaderVisible));
        }
    }

    protected void loadFooterVisible(DataGrid component, Element element) {
        String columnFooterVisible = element.attributeValue("footerVisible");
        if (StringUtils.isNotEmpty(columnFooterVisible)) {
            component.setFooterVisible(Boolean.parseBoolean(columnFooterVisible));
        }
    }

    protected void loadContextMenuEnabled(DataGrid dataGrid, Element element) {
        String contextMenuEnabled = element.attributeValue("contextMenuEnabled");
        if (StringUtils.isNotEmpty(contextMenuEnabled)) {
            dataGrid.setContextMenuEnabled(Boolean.parseBoolean(contextMenuEnabled));
        }
    }

    protected void loadButtonsPanel(DataGrid component) {
        if (buttonsPanelLoader != null) {
            buttonsPanelLoader.loadComponent();
            ButtonsPanel panel = (ButtonsPanel) buttonsPanelLoader.getResultComponent();

            String alwaysVisible = panelElement.attributeValue("alwaysVisible");
            if (alwaysVisible != null) {
                panel.setAlwaysVisible(Boolean.parseBoolean(alwaysVisible));
            }

            if (panel.getCaption() != null) {
                log.debug("The caption '{}' of ButtonsPanel inside of DataGrid will be ignored",
                        panel.getCaption());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadPagination(DataGrid component, Element element) {
        Element paginationElement = element.element("simplePagination");
        if (paginationElement != null) {

            ComponentLoader<SimplePagination> loader = getLayoutLoader()
                    .getLoader(paginationElement, SimplePagination.NAME);
            loader.createComponent();
            loader.loadComponent();

            SimplePagination pagination = loader.getResultComponent();
            component.setPagination(pagination);
        }
    }

    protected List<Column> loadColumnsByInclude(DataGrid component, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

        List<Column> columns = new ArrayList<>(appliedProperties.size());
        List<Element> columnElements = columnsElement.elements("column");
        Set<Element> overriddenColumns = new HashSet<>();

        DocumentFactory documentFactory = DatatypeElementFactory.getInstance();

        for (String property : appliedProperties) {
            Element column = getOverriddenColumn(columnElements, property);
            if (column == null) {
                column = documentFactory.createElement("column");
                column.add(documentFactory.createAttribute(column, "property", property));
            } else {
                overriddenColumns.add(column);
            }

            columns.add(loadColumn(component, column, metaClass));
        }

        // load remains columns
        List<Element> remainedColumns = columnsElement.elements("column");
        for (Element column : remainedColumns) {
            if (overriddenColumns.contains(column)) {
                continue;
            }

            // check property and add
            String propertyId = column.attributeValue("property");
            if (StringUtils.isNotEmpty(propertyId)) {
                MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyId);
                if (propertyPath == null || getMetadataTools().fetchPlanContainsProperty(fetchPlan, propertyPath)) {
                    columns.add(loadColumn(component, column, metaClass));
                }
            }
        }

        return columns;
    }

    protected List<Column> loadColumns(DataGrid component, Element columnsElement, MetaClass metaClass, FetchPlan view) {
        String includeAll = columnsElement.attributeValue("includeAll");
        if (StringUtils.isNotBlank(includeAll)) {
            if (Boolean.parseBoolean(includeAll)) {
                return loadColumnsByInclude(component, columnsElement, metaClass, view);
            }
        }

        List<Element> columnElements = columnsElement.elements("column");

        List<Column> columns = new ArrayList<>(columnElements.size());
        for (Element columnElement : columnElements) {
            columns.add(loadColumn(component, columnElement, metaClass));
        }
        return columns;
    }

    protected Column loadColumn(DataGrid component, Element element, MetaClass metaClass) {
        String id = element.attributeValue("id");
        String property = element.attributeValue("property");

        if (id == null) {
            if (property != null) {
                id = property;
            } else {
                throw new GuiDevelopmentException("A column must have whether id or property specified",
                        context, "DataGrid ID", component.getId());
            }
        }

        Column column;
        if (property != null) {
            MetaPropertyPath metaPropertyPath = getMetadataTools().resolveMetaPropertyPathOrNull(metaClass, property);
            column = component.addColumn(id, metaPropertyPath);
        } else {
            column = component.addColumn(id, null);
        }

        String expandRatio = element.attributeValue("expandRatio");
        if (StringUtils.isNotEmpty(expandRatio)) {
            column.setExpandRatio(Integer.parseInt(expandRatio));
        }

        String collapsed = element.attributeValue("collapsed");
        if (StringUtils.isNotEmpty(collapsed)) {
            column.setCollapsed(Boolean.parseBoolean(collapsed));
        }

        String collapsible = element.attributeValue("collapsible");
        if (StringUtils.isNotEmpty(collapsible)) {
            column.setCollapsible(Boolean.parseBoolean(collapsible));
        }

        String collapsingToggleCaption = element.attributeValue("collapsingToggleCaption");
        if (StringUtils.isNotEmpty(collapsingToggleCaption)) {
            collapsingToggleCaption = loadResourceString(collapsingToggleCaption);
            column.setCollapsingToggleCaption(collapsingToggleCaption);
        }

        String sortable = element.attributeValue("sortable");
        if (StringUtils.isNotEmpty(sortable)) {
            column.setSortable(Boolean.parseBoolean(sortable));
        }

        String resizable = element.attributeValue("resizable");
        if (StringUtils.isNotEmpty(resizable)) {
            column.setResizable(Boolean.parseBoolean(resizable));
        }

        String editable = element.attributeValue("editable");
        if (StringUtils.isNotEmpty(editable)) {
            column.setEditable(Boolean.parseBoolean(editable));
        }

        String sort = element.attributeValue("sort");
        if (StringUtils.isNotBlank(sort)) {
            loadColumnSort(component, column, sort);
        }

        String caption = loadCaption(element);

        if (caption == null) {
            String columnCaption;
            if (column.getPropertyPath() != null) {
                MetaProperty metaProperty = column.getPropertyPath().getMetaProperty();
                String propertyName = metaProperty.getName();
                MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(column.getPropertyPath());
                columnCaption = getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
            } else {
                Class<?> declaringClass = metaClass.getJavaClass();
                String className = declaringClass.getName();
                int i = className.lastIndexOf('.');
                if (i > -1) {
                    className = className.substring(i + 1);
                }
                columnCaption = getMessages().getMessage(declaringClass, className + "." + id);
            }
            column.setCaption(columnCaption);
        } else {
            column.setCaption(caption);
        }

        ((Component.HasXmlDescriptor) column).setXmlDescriptor(element);

        Integer width = loadSizeInPx(element, "width");
        if (width != null) {
            column.setWidth(width);
        }

        Integer minimumWidth = loadSizeInPx(element, "minimumWidth");
        if (minimumWidth != null) {
            column.setMinimumWidth(minimumWidth);
        }

        Integer maximumWidth = loadSizeInPx(element, "maximumWidth");
        if (maximumWidth != null) {
            column.setMaximumWidth(maximumWidth);
        }

        loadColumnVisualDisplay(column, element);

        loadAggregation(column, element);

        return column;
    }

    protected void loadColumnVisualDisplay(DataGrid.Column column, Element columnElement) {
        column.setRenderer(loadRenderer(columnElement));
    }

    @Nullable
    protected DataGrid.Renderer loadRenderer(Element columnElement) {
        for (Map.Entry<String, Class<? extends DataGrid.Renderer>> entry : RENDERERS_MAP.entrySet()) {
            Element rendererElement = columnElement.element(entry.getKey());
            if (rendererElement != null) {
                return loadRendererByClass(rendererElement, entry.getValue());
            }
        }

        return null;
    }

    protected DataGrid.Renderer loadRendererByClass(Element rendererElement, Class<? extends DataGrid.Renderer> rendererClass) {
        DataGrid.Renderer renderer = applicationContext.getBean(rendererClass);

        if (renderer instanceof DataGrid.HasNullRepresentation) {
            loadNullRepresentation(rendererElement, (DataGrid.HasNullRepresentation) renderer);
        }

        if (renderer instanceof DataGrid.HasDateTimeFormatter) {
            loadFormatPattern(rendererElement, (DataGrid.HasDateTimeFormatter) renderer);
        }

        if (renderer instanceof DataGrid.HasFormatString) {
            loadFormatString(rendererElement, (DataGrid.HasFormatString) renderer);
        }

        return renderer;
    }

    protected void loadNullRepresentation(Element rendererElement, DataGrid.HasNullRepresentation renderer) {
        String nullRepresentation = rendererElement.attributeValue("nullRepresentation");
        if (StringUtils.isNotEmpty(nullRepresentation)) {
            renderer.setNullRepresentation(nullRepresentation);
        }
    }

    protected void loadFormatPattern(Element rendererElement, DataGrid.HasDateTimeFormatter renderer) {
        String formatPattern = rendererElement.attributeValue("format");
        if (StringUtils.isNotEmpty(formatPattern)) {
            renderer.setFormatPattern(formatPattern);
        }
    }

    protected void loadFormatString(Element rendererElement, DataGrid.HasFormatString renderer) {
        String formatString = rendererElement.attributeValue("format");
        if (StringUtils.isNotEmpty(formatString)) {
            renderer.setFormatString(formatString);
        }
    }

    @Nullable
    protected String loadCaption(Element element) {
        if (element.attribute("caption") != null) {
            String caption = element.attributeValue("caption");

            return loadResourceString(caption);
        }
        return null;
    }

    @Nullable
    protected Integer loadSizeInPx(Element element, String propertyName) {
        String value = loadThemeString(element.attributeValue(propertyName));
        if (!StringUtils.isBlank(value)) {
            if (StringUtils.endsWith(value, "px")) {
                value = StringUtils.substring(value, 0, value.length() - 2);
            }
            try {
                // Only integer allowed in XML
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new GuiDevelopmentException("Property '" + propertyName + "' must contain only numeric value",
                        context, propertyName, element.attributeValue(propertyName));
            }
        }
        return null;
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }

    protected void loadSelectionMode(DataGrid component, Element element) {
        String selectionMode = element.attributeValue("selectionMode");
        if (StringUtils.isNotEmpty(selectionMode)) {
            component.setSelectionMode(DataGrid.SelectionMode.valueOf(selectionMode));
        }
    }

    protected void loadFrozenColumnCount(DataGrid component, Element element) {
        String frozenColumnCount = element.attributeValue("frozenColumnCount");
        if (StringUtils.isNotEmpty(frozenColumnCount)) {
            component.setFrozenColumnCount(Integer.parseInt(frozenColumnCount));
        }
    }

    protected Collection<String> getAppliedProperties(Element columnsElement, @Nullable FetchPlan fetchPlan, MetaClass metaClass) {
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
        for (Element element : columns) {
            String propertyAttr = element.attributeValue("property");
            if (StringUtils.isNotEmpty(propertyAttr) && propertyAttr.equals(property)) {
                return element;
            }
        }
        return null;
    }

    protected void loadColumnSort(DataGrid component, Column column, String sort) {
        if (sortedColumnId != null) {
            throw new GuiDevelopmentException(String.format("Column '%s' cannot be sorted because DataGrid have already" +
                    " sorted '%s' column", column.getId(), sortedColumnId), getContext());
        }

        if (column.getPropertyPath() == null) {
            throw new GuiDevelopmentException(
                    String.format("Can't sort column '%s' because it is not bounded with entity's property", column.getId()),
                    getContext());
        }

        if (!column.isSortable()) {
            throw new GuiDevelopmentException(
                    String.format("Can't sort column '%s' because it is disabled for sorting by 'sortable' attribute", column.getId()),
                    getContext());
        }

        sortDirection = DataGrid.SortDirection.valueOf(sort);
        sortedColumnId = column.getId();
        getComponentContext().addPostInitTask((context, window) -> setColumnSort());
    }

    protected void loadEmptyStateMessage(DataGrid dataGrid, Element element) {
        String emptyStateMessage = element.attributeValue("emptyStateMessage");
        if (StringUtils.isNotBlank(emptyStateMessage)) {
            dataGrid.setEmptyStateMessage(loadResourceString(emptyStateMessage));
        }
    }

    protected void loadEmptyStateLinkMessage(DataGrid dataGrid, Element element) {
        String emptyStateLinkMessage = element.attributeValue("emptyStateLinkMessage");
        if (StringUtils.isNotBlank(emptyStateLinkMessage)) {
            dataGrid.setEmptyStateLinkMessage(loadResourceString(emptyStateLinkMessage));
        }
    }

    protected void loadAggregatable(DataGrid component, Element element) {
        String aggregatable = element.attributeValue("aggregatable");
        if (StringUtils.isNotEmpty(aggregatable)) {
            component.setAggregatable(Boolean.parseBoolean(aggregatable));
        }
    }

    protected void loadAggregationPosition(DataGrid component, Element element) {
        String aggregationPosition = element.attributeValue("aggregationPosition");
        if (!StringUtils.isEmpty(aggregationPosition)) {
            component.setAggregationPosition(DataGrid.AggregationPosition.valueOf(aggregationPosition));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadAggregation(DataGrid.Column column, Element columnElement) {
        Element aggregationElement = columnElement.element("aggregation");
        if (aggregationElement != null) {
            AggregationInfo aggregation = new AggregationInfo();
            aggregation.setPropertyPath(column.getPropertyPath());

            loadAggregationType(aggregation, aggregationElement);

            loadValueDescription(column, aggregationElement);

            Formatter formatter = loadFormatter(aggregationElement);
            aggregation.setFormatter(formatter == null ? (Formatter<Object>) column.getDescriptionProvider() : formatter);
            column.setAggregation(aggregation);

            loadStrategyClass(aggregation, aggregationElement);

            if (aggregation.getType() == null && aggregation.getStrategy() == null) {
                throw new GuiDevelopmentException("Incorrect aggregation - type or strategyClass is required", context);
            }
        }
    }

    protected void loadAggregationType(AggregationInfo aggregation, Element aggregationElement) {
        String aggregationType = aggregationElement.attributeValue("type");
        if (StringUtils.isNotEmpty(aggregationType)) {
            aggregation.setType(AggregationInfo.Type.valueOf(aggregationType));
        }
    }

    protected void loadValueDescription(DataGrid.Column column, Element aggregationElement) {
        String valueDescription = aggregationElement.attributeValue("valueDescription");
        if (StringUtils.isNotEmpty(valueDescription)) {
            column.setValueDescription(loadResourceString(valueDescription));
        }
    }

    protected void loadStrategyClass(AggregationInfo aggregation, Element aggregationElement) {
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
    }

    /**
     * Contains information about metaclass, data container, loader, fetch plan.
     */
    protected static class DataGridDataHolder {

        protected MetaClass metaClass;
        protected CollectionContainer container;
        protected DataLoader dataLoader;
        protected FetchPlan fetchPlan;

        public DataGridDataHolder() {
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
