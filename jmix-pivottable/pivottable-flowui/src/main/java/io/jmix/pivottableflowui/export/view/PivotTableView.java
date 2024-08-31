/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.export.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.export.PivotTableExcelExporter;
import io.jmix.pivottableflowui.export.PivotTableExporter;
import io.jmix.pivottableflowui.export.PivotTableExporterImpl;
import io.jmix.pivottableflowui.kit.data.DataItem;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEvent;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Route(value = "pivot-table-view")
@ViewController("PivotTableView")
@ViewDescriptor("pivot-table-view.xml")
public class PivotTableView extends StandardView {

    protected PivotTableExporter pivotTableExtension;

    @ViewComponent
    protected PivotTable pivotTable;
    @ViewComponent
    protected JmixButton exportExcel;

    protected String nativeJson;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        PivotTableExcelExporter bean = getApplicationContext().getBean(PivotTableExcelExporter.class);
        pivotTableExtension = new PivotTableExporterImpl(pivotTable, bean);
    }

    public void setProperties(Map<String, String> properties) {
        pivotTable.addProperties(properties);

        pivotTable.addColumns(properties.values().toArray(new String[0]));
    }

    public void setDataItems(List<DataItem> dataItems) {
        DataItem[] arr = new DataItem[dataItems.size()];
        pivotTable.setData(dataItems.toArray(arr));
    }

    @Subscribe(id = "exportExcel", subject = "clickListener")
    public void exportExcel(final ClickEvent<JmixButton> event) {
        pivotTableExtension.exportTableToXls();
    }

    @Subscribe(id = "pivotTable")
    protected void onPivotTableRefreshEvent(PivotTableRefreshEvent event) {
        exportExcel.setEnabled(pivotTableExtension.isRendererSupported(event.getParams().getRenderer()));
    }
}
