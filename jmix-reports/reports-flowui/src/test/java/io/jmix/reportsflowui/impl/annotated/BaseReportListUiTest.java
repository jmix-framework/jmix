/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reportsflowui.impl.annotated;

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.StandardListView;

public class BaseReportListUiTest<R,V extends StandardListView<R>> extends BaseReportUiTest {

    protected DataGridItems<R> dataGridItems;
    protected final String dataGridId;
    protected V listView;
    protected Class<V> listViewClass;

    protected BaseReportListUiTest(String dataGridId, Class<V> listViewClass) {
        this.dataGridId = dataGridId;
        this.listViewClass = listViewClass;
    }

    protected V getListView() {
        viewNavigators.view(UiTestUtils.getCurrentView(), listViewClass).navigate();
        listView = UiTestUtils.getCurrentView();

        return listView;
    }

    protected DataGrid<R> getMainDataGrid() {
        getListView();

        return findComponent(listView, dataGridId);
    }

    protected DataGridItems<R> getDataGridItems() {
        return getMainDataGrid().getItems();
    }
}
