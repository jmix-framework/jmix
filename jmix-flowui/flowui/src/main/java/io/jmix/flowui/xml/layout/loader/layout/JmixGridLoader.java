/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.layout;

import com.google.common.base.Splitter;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.impl.FetchPlanRepositoryImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.flowui.component.grid.JmixGrid;
import io.jmix.flowui.component.grid.JmixGridDataProvider;
import io.jmix.flowui.data.provider.StringPresentationValueProvider;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.DataLoadersHelper;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElementFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//FIXME: kremnevda,  23.04.2022
public class JmixGridLoader extends AbstractComponentLoader<JmixGrid<?>> {

    protected List<ComponentLoader<?>> pendingLoadComponents = new ArrayList<>();

    protected ActionLoaderSupport actionLoaderSupport;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer
    protected MetadataTools metaDataTools;
    protected FetchPlanRepositoryImpl fetchPlanRepository;

    public ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    public MetadataTools getMetaDataTools() {
        if (metaDataTools == null) {
            metaDataTools = applicationContext.getBean(MetadataTools.class, context);
        }
        return metaDataTools;
    }

    public FetchPlanRepositoryImpl getFetchPlanRepository() {
        if (fetchPlanRepository == null) {
            fetchPlanRepository = applicationContext.getBean(FetchPlanRepositoryImpl.class, context);
        }
        return fetchPlanRepository;
    }

    @Override
    protected JmixGrid<?> createComponent() {
        return factory.create(JmixGrid.class);
    }

    @Override
    public void loadComponent() {
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "multiSort", resultComponent::setMultiSort);
        loadBoolean(element, "rowsDraggable", resultComponent::setRowsDraggable);
        loadBoolean(element, "allRowsVisible", resultComponent::setAllRowsVisible);
        loadEnum(element, GridDropMode.class, "dropMode", resultComponent::setDropMode);
        loadBoolean(element, "detailsVisibleOnClick", resultComponent::setDetailsVisibleOnClick);
        loadEnum(element, SelectionMode.class, "selectionMode", resultComponent::setSelectionMode);
        loadBoolean(element, "columnReorderingAllowed", resultComponent::setColumnReorderingAllowed);
        loadBoolean(element, "verticalScrollingEnabled", resultComponent::setVerticalScrollingEnabled);
        loadEnum(element, NestedNullBehavior.class, "nestedNullBehavior", resultComponent::setNestedNullBehavior);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadData();
//        loadSubComponents();
        loadActions();
    }

    protected void loadData() {
        DataGridDataHolder holder = initDataGridDataHolder();
        if (!holder.isContainerLoaded()
                && holder.getMetaClass() == null) {
            throw new GuiDevelopmentException("DataGrid doesn't have data binding",
                    context, "DataGrid ID", element.attributeValue("id"));
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

        setupDataContainer(holder);

        if (resultComponent.getItems() == null) {
            //noinspection unchecked
            //TODO: kremnevda,  21.04.2022
//            resultComponent.setItems(createEmptyDataGridItems(holder.getMetaClass()));
        }
    }

    @SuppressWarnings("rawtypes")
    protected List<Column> loadColumns(JmixGrid resultComponent, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Boolean includeAll = loadBoolean(columnsElement, "includeAll").orElse(false);

        if (includeAll) {
            return loadColumnsByInclude(resultComponent, columnsElement, metaClass, fetchPlan);
        }

        List<Element> columnElements = columnsElement.elements("column");

        List<Column> columns = new ArrayList<>(columnElements.size());
        for (Element columnElement : columnElements) {
            columns.add(loadColumn(resultComponent, columnElement, metaClass));
        }

        return columns;
    }

    protected List<Column> loadColumnsByInclude(JmixGrid component, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
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
            String propertyId = loadString(column, "property").orElse(null);
            if (propertyId != null) {
                MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyId);
                if (propertyPath == null || getMetaDataTools().fetchPlanContainsProperty(fetchPlan, propertyPath)) {
                    columns.add(loadColumn(component, column, metaClass));
                }
            }
        }

        return columns;
    }

    //TODO: kremnevda, refactor 21.04.2022
    @SuppressWarnings("rawtypes")
    protected Column loadColumn(JmixGrid component, Element element, MetaClass metaClass) {
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

        Column column = null;
        if (property != null) {
            MetaPropertyPath metaPropertyPath = getMetaDataTools().resolveMetaPropertyPathOrNull(metaClass, property);
            column = component.addColumn(new StringPresentationValueProvider<>(metaPropertyPath, getMetaDataTools()));
        } else {
//            column = component.addColumn(id, null);
        }

        loadString(element, "key", column::setKey);
        loadString(element, "width", column::setWidth);
        loadString(element, "header", column::setHeader);
        loadString(element, "footer", column::setFooter);
        loadBoolean(element, "frozen", column::setFrozen);
        loadBoolean(element, "sortable", column::setSortable);
        loadInteger(element, "flexGrow", column::setFlexGrow);
        loadBoolean(element, "resizable", column::setResizable);
        loadBoolean(element, "autoWidth", column::setAutoWidth);
        loadEnum(element, ColumnTextAlign.class, "textAlign", column::setTextAlign);

//        String expandRatio = element.attributeValue("expandRatio");
//        if (StringUtils.isNotEmpty(expandRatio)) {
//            column.setExpandRatio(Integer.parseInt(expandRatio));
//        }
//
//        String collapsed = element.attributeValue("collapsed");
//        if (StringUtils.isNotEmpty(collapsed)) {
//            column.setCollapsed(Boolean.parseBoolean(collapsed));
//        }
//
//        String collapsible = element.attributeValue("collapsible");
//        if (StringUtils.isNotEmpty(collapsible)) {
//            column.setCollapsible(Boolean.parseBoolean(collapsible));
//        }
//
//        String collapsingToggleCaption = element.attributeValue("collapsingToggleCaption");
//        if (StringUtils.isNotEmpty(collapsingToggleCaption)) {
//            collapsingToggleCaption = loadResourceString(collapsingToggleCaption);
//            column.setCollapsingToggleCaption(collapsingToggleCaption);
//        }
//
//        String sortable = element.attributeValue("sortable");
//        if (StringUtils.isNotEmpty(sortable)) {
//            column.setSortable(Boolean.parseBoolean(sortable));
//        }
//
//        String resizable = element.attributeValue("resizable");
//        if (StringUtils.isNotEmpty(resizable)) {
//            column.setResizable(Boolean.parseBoolean(resizable));
//        }
//
//        String editable = element.attributeValue("editable");
//        if (StringUtils.isNotEmpty(editable)) {
//            column.setEditable(Boolean.parseBoolean(editable));
//        }
//
//        String sort = element.attributeValue("sort");
//        if (StringUtils.isNotBlank(sort)) {
//            loadColumnSort(component, column, sort);
//        }
//
//        String caption = loadCaption(element);
//
//        if (caption == null) {
//            String columnCaption;
//            if (column.getPropertyPath() != null) {
//                MetaProperty metaProperty = column.getPropertyPath().getMetaProperty();
//                String propertyName = metaProperty.getName();
//                MetaClass propertyMetaClass = getMetadataTools().getPropertyEnclosingMetaClass(column.getPropertyPath());
//                columnCaption = getMessageTools().getPropertyCaption(propertyMetaClass, propertyName);
//            } else {
//                Class<?> declaringClass = metaClass.getJavaClass();
//                String className = declaringClass.getName();
//                int i = className.lastIndexOf('.');
//                if (i > -1) {
//                    className = className.substring(i + 1);
//                }
//                columnCaption = getMessages().getMessage(declaringClass, className + "." + id);
//            }
//            column.setCaption(columnCaption);
//        } else {
//            column.setCaption(caption);
//        }
//
//        ((Component.HasXmlDescriptor) column).setXmlDescriptor(element);
//
//        Integer width = loadSizeInPx(element, "width");
//        if (width != null) {
//            column.setWidth(width);
//        }
//
//        Integer minimumWidth = loadSizeInPx(element, "minimumWidth");
//        if (minimumWidth != null) {
//            column.setMinimumWidth(minimumWidth);
//        }
//
//        Integer maximumWidth = loadSizeInPx(element, "maximumWidth");
//        if (maximumWidth != null) {
//            column.setMaximumWidth(maximumWidth);
//        }
//
//        loadColumnVisualDisplay(column, element);
//
//        loadAggregation(column, element);

        return column;
    }

    protected void loadActions() {
        Element actions = element.element("actions");
        if (actions == null) {
            return;
        }

        for (Element subElement : actions.elements("action")) {
            resultComponent.addAction(getActionLoaderSupport().loadDeclarativeAction(subElement));

//            getComponentContext().addInitTask(
//                    new AssignActionsInitTask<JmixGrid<?>>(
//                            resultComponent,
//                            subElement.attributeValue("id"),
//                            getComponentContext().getScreen()
//                    )
//            );
        }
    }

    protected void loadSubComponents() {
        for (ComponentLoader<?> componentLoader : pendingLoadComponents) {
            componentLoader.loadComponent();
        }

        pendingLoadComponents.clear();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void setupDataContainer(DataGridDataHolder holder) {
        if (holder.getContainer() != null) {
            resultComponent.setItems(new JmixGridDataProvider(holder.getContainer()));
        }
    }

    public static class GridColumnLoader extends AbstractComponentLoader<Column<?>> {

        @Override
        protected Column<?> createComponent() {
            return factory.create(Column.class);
        }

        @Override
        public void loadComponent() {
            loadString(element, "key", resultComponent::setKey);
            loadString(element, "width", resultComponent::setWidth);
            loadString(element, "header", resultComponent::setHeader);
            loadString(element, "footer", resultComponent::setFooter);
            loadBoolean(element, "frozen", resultComponent::setFrozen);
            loadBoolean(element, "sortable", resultComponent::setSortable);
            loadInteger(element, "flexGrow", resultComponent::setFlexGrow);
            loadBoolean(element, "resizable", resultComponent::setResizable);
            loadBoolean(element, "autoWidth", resultComponent::setAutoWidth);
            loadEnum(element, ColumnTextAlign.class, "textAlign", resultComponent::setTextAlign);
        }
    }

    protected Collection<String> getAppliedProperties(Element columnsElement, @Nullable FetchPlan fetchPlan, MetaClass metaClass) {
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

    //TODO: kremnevda,  21.04.2022
//    protected DataGridItems createEmptyDataGridItems(MetaClass metaClass) {
//        return new EmptyDataGridItems(metaClass);
//    }

    protected DataGridDataHolder initDataGridDataHolder() {
        DataGridDataHolder holder = new DataGridDataHolder();

        String containerId = loadString(element, "dataContainer").orElse(null);
        if (containerId == null) {
            loadMetaClass(element, holder::setMetaClass);
            return holder;
        }

        InstanceContainer<?> container = getComponentContext().getScreenData().getContainer(containerId);

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

    protected void onMasterDataLoaderPostLoad(Object o) {
        //TODO: kremnevda, sort? 21.04.2022
        if (masterDataLoaderPostLoadListener != null) {
            masterDataLoaderPostLoadListener.remove();
        }
    }

    /**
     * Contains information about metaclass, data container, loader, fetch plan.
     */
    protected static class DataGridDataHolder {

        protected MetaClass metaClass;

        protected CollectionContainer<?> container;
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
}
