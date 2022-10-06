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
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
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
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.DataLoadersHelper;
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

public abstract class AbstractGridLoader<T extends Grid<?> & EnhancedDataGrid<?> & HasActions>
        extends AbstractComponentLoader<T> {

    protected ActionLoaderSupport actionLoaderSupport;
    protected MetadataTools metaDataTools;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer
    protected FetchPlanRepositoryImpl fetchPlanRepository;

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
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadData();
        getActionLoaderSupport().loadActions(resultComponent, element);
    }

    protected void loadData() {
        GridDataHolder holder = initDataGridDataHolder();

        setupDataProvider(holder);

        resultComponent.removeAllColumns();

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
    }

    protected void loadColumns(T resultComponent, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Boolean includeAll = loadBoolean(columnsElement, "includeAll").orElse(false);

        if (includeAll) {
            loadColumnsByInclude(resultComponent, columnsElement, metaClass, fetchPlan);
            return;
        }

        List<Element> columnElements = columnsElement.elements("column");
        for (Element columnElement : columnElements) {
            loadColumn(resultComponent, columnElement, metaClass);
        }
    }

    protected void loadColumnsByInclude(T component, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

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

            loadColumn(component, column, metaClass);
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
                    loadColumn(component, column, metaClass);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected void loadColumn(T component, Element element, MetaClass metaClass) {
        String property = loadString(element, "property")
                .orElseThrow(() ->
                        new GuiDevelopmentException("A column must have specified property",
                                context, "Component ID", component.getId())
                );

        MetaPropertyPath metaPropertyPath = getMetaDataTools().resolveMetaPropertyPathOrNull(metaClass, property);
        if (metaPropertyPath == null) {
            throw new GuiDevelopmentException("Cannot resolve the property path: " + property,
                    context, "Component ID", component.getId());
        }

        String key = loadString(element, "key")
                .orElseGet(() -> metaPropertyPath.getMetaProperty().getName());

        Column column = resultComponent.addColumn(key, metaPropertyPath);
        loadString(element, "width", column::setWidth);
        loadString(element, "header", column::setHeader);
        loadString(element, "footer", column::setFooter);
        loadBoolean(element, "frozen", column::setFrozen);
        loadBoolean(element, "sortable", column::setSortable);
        loadInteger(element, "flexGrow", column::setFlexGrow);
        loadBoolean(element, "resizable", column::setResizable);
        loadBoolean(element, "autoWidth", column::setAutoWidth);
        loadBoolean(element, "visible", column::setVisible);
        loadEnum(element, ColumnTextAlign.class, "textAlign", column::setTextAlign);
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

    protected abstract void setupDataProvider(GridDataHolder holder);
}
