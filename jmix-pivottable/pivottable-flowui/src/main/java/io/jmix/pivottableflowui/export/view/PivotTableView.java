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
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.export.PivotTableExcelExporter;
import io.jmix.pivottableflowui.export.PivotTableExporter;
import io.jmix.pivottableflowui.export.PivotTableExporterImpl;
import io.jmix.pivottableflowui.kit.data.DataItem;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "pivot-table-view")
@ViewController("PivotTableView")
@ViewDescriptor("pivot-table-view.xml")
public class PivotTableView extends StandardView {

    @ViewComponent
    protected PivotTable pivotTable;
    @ViewComponent
    protected JmixButton exportExcel;

    protected PivotTableExporter pivotTableExporter;
    @Autowired
    private Downloader downloader;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        PivotTableExcelExporter excelExporter = getApplicationContext().getBean(PivotTableExcelExporter.class);
        pivotTableExporter = new PivotTableExporterImpl(pivotTable, excelExporter);
    }

    public void setProperties(Map<String, String> properties) {
        pivotTable.addProperties(properties);

        pivotTable.addColumns(properties.values().toArray(new String[0]));
    }

    public void setDataItems(List<DataItem> dataItems) {
        pivotTable.setData(dataItems.toArray(new DataItem[0]));
    }

    public void setNativeJson(String nativeJson) {
        pivotTable.setNativeJson(nativeJson);
    }

    @Subscribe(id = "exportExcel", subject = "clickListener")
    public void exportExcel(final ClickEvent<JmixButton> event) {
        pivotTableExporter.setFileName("export.xls");
        pivotTableExporter.exportTableToXls(downloader);
    }

    @Subscribe(id = "pivotTable")
    protected void onPivotTableRefreshEvent(PivotTableRefreshEvent event) {
        exportExcel.setEnabled(pivotTableExporter.isRendererSupported(event.getRenderer()));
    }
}
