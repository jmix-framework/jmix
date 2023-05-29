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

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.DataGrid;
import com.haulmont.cuba.gui.components.RowsCount;
import com.haulmont.cuba.gui.components.TreeDataGrid;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.dynattrui.DynAttrEmbeddingStrategies;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.formatter.Formatter;
import io.jmix.ui.xml.layout.loader.TreeDataGridLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("rawtypes")
public class CubaTreeDataGridLoader extends TreeDataGridLoader {

    @Override
    protected io.jmix.ui.component.TreeDataGrid createComponentInternal() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(io.jmix.ui.component.TreeDataGrid.NAME);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadSettingsEnabled((TreeDataGrid) resultComponent, element);
    }

    @Override
    protected void loadDataGridData() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        // must be before datasource setting
        ComponentLoaderHelper.loadRowsCount((DataGrid) resultComponent, element, () -> uiComponents.create(RowsCount.NAME));

        super.loadDataGridData();
    }

    @Override
    protected DataGridDataHolder initDataGridDataHolder() {
        DataGridDataHolder dataHolder = super.initDataGridDataHolder();
        if (dataHolder.isContainerLoaded()) {
            return dataHolder;
        }

        String datasourceId = element.attributeValue("datasource");
        if (Strings.isNullOrEmpty(datasourceId)) {
            return dataHolder;
        }

        CollectionDatasource datasource = DatasourceLoaderHelper.loadCollectionDatasource(
                datasourceId, context, (ComponentLoaderContext) getComponentContext()
        );

        CubaTreeDataGridDataHolder cubaDataHolder = new CubaTreeDataGridDataHolder();
        cubaDataHolder.setDatasource(datasource);
        cubaDataHolder.setMetaClass(datasource.getMetaClass());
        cubaDataHolder.setFetchPlan(datasource.getView());

        return cubaDataHolder;
    }

    @Override
    protected void setupDataContainer(DataGridDataHolder holder) {
        if (holder instanceof CubaTreeDataGridDataHolder) {
            CollectionDatasource datasource = ((CubaTreeDataGridDataHolder) holder).getDatasource();
            if (datasource == null) {
                return;
            }
            ((TreeDataGrid) resultComponent).setDatasource(datasource);

            DynAttrEmbeddingStrategies embeddingStrategies = applicationContext.getBean(DynAttrEmbeddingStrategies.class);
            embeddingStrategies.embedAttributes(resultComponent, getComponentContext().getFrame());
        } else {
            super.setupDataContainer(holder);
        }
    }

    @Override
    protected void loadColumnVisualDisplay(io.jmix.ui.component.DataGrid.Column column, Element columnElement) {
        ((DataGrid.Column) column).setGeneratedType(loadGeneratedType(columnElement));
        ((DataGrid.Column) column).setFormatter(loadFormatter(columnElement));

        super.loadColumnVisualDisplay(column, columnElement);
    }

    @Nullable
    protected Class loadGeneratedType(Element columnElement) {
        String colGenType = columnElement.attributeValue("generatedType");
        if (StringUtils.isNotEmpty(colGenType)) {
            return applicationContext.getBean(Scripting.class).loadClassNN(colGenType);
        }
        return null;
    }

    @Override
    @Nullable
    protected Formatter<?> loadFormatter(Element element) {
        return ComponentLoaderHelper.loadFormatter(element, getClassManager(), getContext());
    }

    @Nullable
    @Override
    protected io.jmix.ui.component.DataGrid.Renderer loadRenderer(Element columnElement) {
        io.jmix.ui.component.DataGrid.Renderer renderer = super.loadRenderer(columnElement);
        if (renderer == null && columnElement.element("renderer") != null) {
            renderer = ComponentLoaderHelper.loadLegacyRenderer(columnElement.element("renderer"),
                    context, getClassManager(), applicationContext);
        }

        return renderer;
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

    protected static class CubaTreeDataGridDataHolder extends DataGridDataHolder {

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
