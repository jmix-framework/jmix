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

package io.jmix.vaadincommercialcomponents.component.loader;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.vaadincommercialcomponents.component.spreadsheet.JmixSpreadsheet;

public class SpreadsheetLoader extends AbstractComponentLoader<JmixSpreadsheet> {

    @Override
    protected JmixSpreadsheet createComponent() {
        return factory.create(JmixSpreadsheet.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "chartsEnabled", resultComponent::setChartsEnabled);
        loadInteger(element, "activeSheetIndex", resultComponent::setActiveSheetIndex);
        loadInteger(element, "activeSheetWithPOIIndex", resultComponent::setActiveSheetWithPOIIndex);

        loadInteger(element, "maxColumns", resultComponent::setMaxColumns);
        loadInteger(element, "maxRows", resultComponent::setMaxRows);
        loadInteger(element, "defaultColumnWidth", resultComponent::setDefaultColumnWidth);
        loadInteger(element, "defaultRowHeight", resultComponent::setDefaultRowHeight);

        loadInteger(element, "rowBufferSize", resultComponent::setRowBufferSize);
        loadInteger(element, "colBufferSize", resultComponent::setColBufferSize);
        loadInteger(element, "defaultRowCount", resultComponent::setDefaultRowCount);
        loadInteger(element, "defaultColumnCount", resultComponent::setDefaultColumnCount);

        loadResourceString(element, "statusLabelValue", context.getMessageGroup(),
                resultComponent::setStatusLabelValue);
        loadResourceString(element, "invalidFormulaErrorMessage", context.getMessageGroup(),
                resultComponent::setInvalidFormulaErrorMessage);

        loadString(element, "selection", resultComponent::setSelection);
        loadString(element, "defaultPercentageFormat", resultComponent::setDefaultPercentageFormat);
        loadBoolean(element, "functionBarVisible", resultComponent::setFunctionBarVisible);
        loadBoolean(element, "sheetSelectionBarVisible", resultComponent::setSheetSelectionBarVisible);
        loadBoolean(element, "reportStyle", resultComponent::setReportStyle);
        loadInteger(element, "minimumRowHeightForComponents", resultComponent::setMinimumRowHeightForComponents);

        loadEnum(element, Spreadsheet.SpreadsheetTheme.class, "theme", resultComponent::setTheme);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
    }
}
