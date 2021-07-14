/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.screen;

import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Button;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.pivottable.component.PivotTable;
import io.jmix.pivottable.component.PivotTableExtension;
import io.jmix.pivottable.component.impl.PivotExcelExporter;
import io.jmix.pivottable.component.impl.PivotTableExtensionImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@UiController("ui_PivotTableScreen")
@UiDescriptor("pivot-table-screen.xml")
public class PivotTableScreen extends Screen {

    public static final String DATA_ITEMS = "dataItems";
    public static final String PROPERTIES = "properties";
    public static final String NATIVE_JSON = "nativeJson";

    protected PivotTableExtension pivotTableExtension;

    @Autowired
    protected PivotTable pivotTable;
    @Autowired
    protected Button exportExcel;

    @WindowParam(name = DATA_ITEMS)
    protected List<DataItem> dataItems;

    @WindowParam(name = PROPERTIES, required = true)
    protected Map<String, String> properties;

    @WindowParam(name = NATIVE_JSON)
    protected String nativeJson;

    @Autowired
    protected ObjectProvider<PivotExcelExporter> excelExporterObjectProvider;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (dataItems != null) {
            for (DataItem dataItem : dataItems) {
                pivotTable.addData(dataItem);
            }
        }

        pivotTable.addProperties(properties);

        if (nativeJson != null) {
            pivotTable.setNativeJson(nativeJson);
        }

        PivotExcelExporter excelExporter = excelExporterObjectProvider.getObject();
        pivotTableExtension = new PivotTableExtensionImpl(pivotTable, excelExporter);
    }

    @Subscribe("exportExcel")
    public void exportExcel(Button.ClickEvent event) {
        pivotTableExtension.exportTableToXls();
    }

    @Subscribe("pivotTable")
    protected void onPivotTableRefreshEvent(PivotTable.RefreshEvent event) {
        exportExcel.setEnabled(pivotTableExtension.isRendererSupported(event.getRenderer()));
    }
}