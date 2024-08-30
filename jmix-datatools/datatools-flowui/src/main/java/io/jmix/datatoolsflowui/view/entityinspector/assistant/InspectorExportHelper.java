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

package io.jmix.datatoolsflowui.view.entityinspector.assistant;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.gridexportflowui.GridExportFlowuiConfiguration;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.function.Consumer;

@org.springframework.stereotype.Component("datatl_InspectorExportHelper")
@ConditionalOnClass(GridExportFlowuiConfiguration.class)
public class InspectorExportHelper {

    protected Actions actions;
    protected UiComponents uiComponents;

    public InspectorExportHelper(Actions actions, UiComponents uiComponents) {
        this.actions = actions;
        this.uiComponents = uiComponents;
    }

    public void assignExcelExportAction(DataGrid<Object> dataGrid, Consumer<Component> addMethod) {
        ExcelExportAction excelExportAction = actions.create(ExcelExportAction.ID);
        excelExportAction.setTarget(dataGrid);

        JmixButton excelExportButton = uiComponents.create(JmixButton.class);
        excelExportButton.setAction(excelExportAction);

        addMethod.accept(excelExportButton);
    }
}
