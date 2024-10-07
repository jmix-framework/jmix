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

package io.jmix.pivottableflowui.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.component.PivotTableUtils;
import io.jmix.pivottableflowui.data.ListPivotTableItems;
import io.jmix.pivottableflowui.export.PivotTableExcelExporter;
import io.jmix.pivottableflowui.export.PivotTableExporter;
import io.jmix.pivottableflowui.export.PivotTableExporterImpl;
import io.jmix.pivottableflowui.kit.component.model.PivotTableOptions;
import io.jmix.pivottableflowui.kit.event.PivotTableRefreshEvent;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "pivot-table-view")
@ViewController("PivotTableView")
@ViewDescriptor("pivot-table-view.xml")
public class PivotTableView extends StandardView {

    @ViewComponent
    protected PivotTable<?> pivotTable;
    @ViewComponent
    protected JmixButton exportExcel;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected Messages messages;

    protected PivotTableExporter pivotTableExporter;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        PivotTableExcelExporter excelExporter = getApplicationContext().getBean(PivotTableExcelExporter.class);
        pivotTableExporter = new PivotTableExporterImpl(pivotTable, excelExporter);
    }

    public void setProperties(Map<String, String> properties) {
        pivotTable.addProperties(properties);

        pivotTable.addColumns(properties.values().toArray(new String[0]));
    }

    public void setDataItems(List<Object> items) {
        pivotTable.setItems(new ListPivotTableItems("id", UUID.class, items));
    }

    @Subscribe(id = "exportExcel", subject = "clickListener")
    public void exportExcel(final ClickEvent<JmixButton> event) {
        pivotTableExporter.setFileName(messages.getMessage("io.jmix.pivottableflowui.view/fileName"));
        pivotTableExporter.exportTableToXls(downloader);
    }

    @Subscribe(id = "pivotTable")
    protected void onPivotTableRefreshEvent(PivotTableRefreshEvent event) {
        exportExcel.setEnabled(pivotTableExporter.isRendererSupported(event.getDetail().getRenderer()));
    }

    public void setPivotTableOptions(PivotTableOptions options) {
        PivotTableUtils.setPivotTableOptions(pivotTable, options);
    }
}