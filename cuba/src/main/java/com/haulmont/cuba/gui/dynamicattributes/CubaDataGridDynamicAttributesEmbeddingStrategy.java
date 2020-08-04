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

package com.haulmont.cuba.gui.dynamicattributes;

import com.haulmont.cuba.gui.components.data.datagrid.DatasourceDataGridItems;
import io.jmix.dynattrui.impl.DataGridEmbeddingStrategy;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;

@org.springframework.stereotype.Component(CubaDataGridDynamicAttributesEmbeddingStrategy.NAME)
public class CubaDataGridDynamicAttributesEmbeddingStrategy extends DataGridEmbeddingStrategy {
    public static final String NAME = "cuba_CubaDataGridDynamicAttributesEmbeddingStrategy";

    @Override
    public boolean supportComponent(Component component) {
        return component instanceof DataGrid && ((DataGrid<?>) component).getItems() instanceof DatasourceDataGridItems;
    }

    @Override
    protected void setLoadDynamicAttributes(Component component) {
        DataGrid dataGrid = (DataGrid) component;
        if (dataGrid.getItems() instanceof DatasourceDataGridItems) {
            //noinspection rawtypes
            ((DatasourceDataGridItems) dataGrid.getItems()).getDatasource().setLoadDynamicAttributes(true);
        }
    }
}
