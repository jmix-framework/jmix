/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui.action;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.gridexportflowui.exporter.excel.ExcelExporter;
import org.springframework.context.ApplicationContext;

/**
 * Action for export a {@link ListDataComponent} content in XLSX format.
 * <p>
 * Should be defined for a {@link ListDataComponent} (e.g. {@link DataGrid}).
 */
@ActionType(ExcelExportAction.ID)
public class ExcelExportAction extends ExportAction {

    public static final String ID = "grdexp_excelExport";

    public ExcelExportAction() {
        this(ID);
    }

    public ExcelExportAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        icon = ComponentUtils.convertToIcon(VaadinIcon.FILE_TABLE);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        withExporter(ExcelExporter.class);
    }
}
