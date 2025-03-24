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

package io.jmix.reportsflowui.view.run;

import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import io.jmix.gridexportflowui.exporter.excel.ExcelExporter;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Helper that provides grid exporter.
 */
@Internal
@Component("sec_ReportExportHelper")
@ConditionalOnBean(ExcelExporter.class)
public class ReportExcelHelper {

    protected Actions actions;
    protected UiComponents uiComponents;

    public ReportExcelHelper(Actions actions, UiComponents uiComponents) {
        this.actions = actions;
        this.uiComponents = uiComponents;
    }

    public JmixButton createExportAction(DataGrid<KeyValueEntity> dataGrid, ReportOutputDocument document) {
        ExcelExportAction excelExportAction = actions.create(ExcelExportAction.ID);
        excelExportAction.withFileName(document.getReport().getName());
        dataGrid.addAction(excelExportAction);

        JmixButton excelButton = uiComponents.create(JmixButton.class);
        excelButton.setAction(excelExportAction);

        return excelButton;
    }
}
