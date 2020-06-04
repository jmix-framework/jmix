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
import com.haulmont.cuba.gui.components.DataGrid;
import com.haulmont.cuba.gui.components.TreeDataGrid;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattrui.DynAttrEmbeddingStrategies;
import io.jmix.ui.xml.layout.loader.TreeDataGridLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class CubaTreeDataGridLoader extends TreeDataGridLoader {

    @Override
    protected CubaTreeDataGridDataHolder initDataGridDataHolder() {
        CubaTreeDataGridDataHolder holder = new CubaTreeDataGridDataHolder();

        String datasourceId = element.attributeValue("datasource");
        if (Strings.isNullOrEmpty(datasourceId)) {
            return holder;
        }

        CollectionDatasource datasource = DatasourceLoaderHelper.loadCollectionDatasource(
                datasourceId, context, (ComponentLoaderContext) getComponentContext()
        );

        holder.setDatasource(datasource);
        holder.setMetaClass(datasource.getMetaClass());
        holder.setFetchPlan(datasource.getView());

        return holder;
    }

    @Override
    protected void setupDataContainer(DataGridDataHolder holder) {
        CollectionDatasource datasource = ((CubaTreeDataGridDataHolder) holder).getDatasource();
        if (datasource == null) {
            return;
        }
        ((TreeDataGrid) resultComponent).setDatasource(datasource);

        DynAttrEmbeddingStrategies embeddingStrategies = beanLocator.get(DynAttrEmbeddingStrategies.class);
        embeddingStrategies.embedAttributes(resultComponent, getComponentContext().getFrame());
    }

    @Override
    protected io.jmix.ui.component.DataGrid.Column loadColumn(io.jmix.ui.component.DataGrid component,
                                                              Element element,
                                                              MetaClass metaClass) {
        DataGrid.Column column = (DataGrid.Column) super.loadColumn(component, element, metaClass);
        column.setGeneratedType(loadGeneratedType(element));
        return column;
    }

    @Nullable
    protected Class loadGeneratedType(Element columnElement) {
        String colGenType = columnElement.attributeValue("generatedType");
        if (StringUtils.isNotEmpty(colGenType)) {
            return beanLocator.get(Scripting.class).loadClassNN(colGenType);
        }
        return null;
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
