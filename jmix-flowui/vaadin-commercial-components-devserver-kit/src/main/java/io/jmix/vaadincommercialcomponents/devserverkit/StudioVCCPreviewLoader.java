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

package io.jmix.vaadincommercialcomponents.devserverkit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import jakarta.annotation.Nullable;
import org.dom4j.Element;

import java.util.List;

public class StudioVCCPreviewLoader implements StudioPreviewComponentLoader {

    @Override
    public boolean isSupported(Element element) {
        String elementName = element.getName();
        return "http://jmix.io/schema/vaadin-commercial-components/ui".equals(element.getNamespaceURI())
                && (List.of("spreadsheet", "board").contains(elementName));
    }

    @Nullable
    @Override
    public Component load(Element element, Element viewElement) {
        if ("spreadsheet".equals(element.getName())) {
            Spreadsheet resultComponent = new Spreadsheet();
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

            loadString(element, "statusLabelValue", resultComponent::setStatusLabelValue);
            loadString(element, "invalidFormulaErrorMessage", resultComponent::setInvalidFormulaErrorMessage);

//            loadResourceString(element, "statusLabelValue", context.getMessageGroup(),
//                    resultComponent::setStatusLabelValue);
//            loadResourceString(element, "invalidFormulaErrorMessage", context.getMessageGroup(),
//                    resultComponent::setInvalidFormulaErrorMessage);

            loadString(element, "selection", resultComponent::setSelection);
            loadString(element, "defaultPercentageFormat", resultComponent::setDefaultPercentageFormat);
            loadBoolean(element, "functionBarVisible", resultComponent::setFunctionBarVisible);
            loadBoolean(element, "sheetSelectionBarVisible", resultComponent::setSheetSelectionBarVisible);
            loadBoolean(element, "reportStyle", resultComponent::setReportStyle);
            loadInteger(element, "minimumRowHeightForComponents", resultComponent::setMinimumRowHeightForComponents);

            loadEnum(element, Spreadsheet.SpreadsheetTheme.class, "theme", resultComponent::setTheme);
            loadSizeAttributes(resultComponent, element);
            loadClassNames(resultComponent, element);
        } else if ("board".equals(element.getName())) {
            Board resultComponent = new Board();
            loadEnabled(resultComponent, element);
            loadSizeAttributes(resultComponent, element);
            loadClassNames(resultComponent, element);
            element.elements("boardRow").forEach(rowElement -> {
                Row row = new Row();
                loadEnabled(row, rowElement);
                loadSizeAttributes(row, rowElement);
                loadClassNames(row, rowElement);
                resultComponent.addRow(row);
            });
        }
        return null;
    }
}
