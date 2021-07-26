/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.components.HasPresentations;
import com.haulmont.cuba.gui.components.RowsCount;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattrui.DynAttrEmbeddingStrategies;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.xml.layout.loader.GroupTableLoader;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("rawtypes")
public class CubaGroupTableLoader extends GroupTableLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(io.jmix.ui.component.GroupTable.NAME);
        loadId(resultComponent, element);
        createButtonsPanel(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadPresentations((HasPresentations) resultComponent, element,
                applicationContext, getComponentContext());
        ComponentLoaderHelper.loadSettingsEnabled((GroupTable) resultComponent, element);
    }

    @Override
    protected TableDataHolder initTableDataHolder() {
        TableDataHolder dataHolder = super.initTableDataHolder();
        if (dataHolder.isContainerLoaded()) {
            return dataHolder;
        }

        Element rowsElement = element.element("rows");
        if (rowsElement == null) {
            return dataHolder;
        }

        CollectionDatasource datasource = DatasourceLoaderHelper.loadTableDatasource(
                element, rowsElement, context, (ComponentLoaderContext) getComponentContext()
        );

        CubaGroupTableDataHolder cubaDataHolder = new CubaGroupTableDataHolder();
        cubaDataHolder.setDatasource(datasource);
        cubaDataHolder.setMetaClass(datasource.getMetaClass());
        cubaDataHolder.setFetchPlan(datasource.getView());

        return cubaDataHolder;
    }

    @Override
    protected void setupDataContainer(TableDataHolder holder) {
        if (holder instanceof CubaGroupTableDataHolder) {
            CollectionDatasource datasource = ((CubaGroupTableDataHolder) holder).getDatasource();
            if (datasource == null) {
                return;
            }
            ((GroupTable) resultComponent).setDatasource(datasource);

            DynAttrEmbeddingStrategies embeddingStrategies = applicationContext.getBean(DynAttrEmbeddingStrategies.class);
            embeddingStrategies.embedAttributes(resultComponent, getComponentContext().getFrame());
        } else {
            super.setupDataContainer(holder);
        }
    }

    @Override
    protected void loadTableData() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        // must be before datasource setting
        ComponentLoaderHelper.loadRowsCount((GroupTable) resultComponent, element, () -> uiComponents.create(RowsCount.NAME));

        super.loadTableData();
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        Optional<Action> actionOpt = ComponentLoaderHelper.loadInvokeAction(
                context,
                actionsHolder,
                element,
                loadActionId(element),
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))));

        if (actionOpt.isPresent()) {
            return actionOpt.get();
        }

        actionOpt = ComponentLoaderHelper.loadLegacyListAction(
                context,
                actionsHolder,
                element,
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))));

        return actionOpt.orElseGet(() ->
                super.loadDeclarativeAction(actionsHolder, element));
    }

    @Override
    protected Table.Column loadColumn(Table component, Element element, MetaClass metaClass) {
        Table.Column column = super.loadColumn(component, element, metaClass);
        ComponentLoaderHelper.loadTableColumnType(column, element, applicationContext);

        if (column instanceof com.haulmont.cuba.gui.components.Table.Column) {
            loadBoolean(element, "groupAllowed",
                    ((com.haulmont.cuba.gui.components.Table.Column<?>) column)::setGroupAllowed);
        }

        return column;
    }

    @Nullable
    @Override
    protected Formatter<?> loadFormatter(Element element) {
        return ComponentLoaderHelper.loadFormatter(element, getClassManager(), getContext());
    }

    protected static class CubaGroupTableDataHolder extends TableDataHolder {

        protected CollectionDatasource datasource;

        public CollectionDatasource getDatasource() {
            return datasource;
        }

        public void setDatasource(CollectionDatasource datasource) {
            this.datasource = datasource;
        }

        @Override
        public boolean isContainerLoaded() {
            return super.isContainerLoaded() || datasource != null;
        }
    }
}
