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

package io.jmix.vaadincommercialcomponents.kit.meta;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.flowui.kit.meta.*;

@StudioUiKit(studioClassloaderDependencies = "io.jmix.flowui:vaadin-commercial-components-devserver-kit")
public interface StudioVaadinCommercialComponents {

    @StudioComponent(
            name = "Board",
            classFqn = "com.vaadin.flow.component.board.Board",
            category = "Vaadin Commercial",
            xmlElement = "board",
            xmlns = "http://jmix.io/schema/vaadin-commercial-components/ui",
            xmlnsAlias = "vcc",
            icon = "io/jmix/vaadincommercialcomponents/kit/meta/icon/component/board.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = "enabled", category = StudioProperty.Category.GENERAL,
                            type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            defaultValue = "100%")
            }
    )
    Board board();

    @StudioComponent(
            name = "Spreadsheet",
            classFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet",
            category = "Vaadin Commercial",
            xmlElement = "spreadsheet",
            xmlns = "http://jmix.io/schema/vaadin-commercial-components/ui",
            xmlnsAlias = "vcc",
            icon = "io/jmix/vaadincommercialcomponents/kit/meta/icon/component/spreadsheet.svg",
            properties = {
                    @StudioProperty(xmlAttribute = "alignSelf", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.POSITION,
                            classFqn = "com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment",
                            defaultValue = "AUTO",
                            options = {"START", "END", "CENTER", "STRETCH", "BASELINE", "AUTO"}),
                    @StudioProperty(xmlAttribute = "activeSheetIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "activeSheetWithPOIIndex", type = StudioPropertyType.INTEGER),
                    @StudioProperty(xmlAttribute = "classNames", type = StudioPropertyType.VALUES_LIST,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "css", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.LOOK_AND_FEEL),
                    @StudioProperty(xmlAttribute = "chartsEnabled", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "colspan", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.POSITION),
                    @StudioProperty(xmlAttribute = "defaultColumnWidth", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "defaultRowHeight", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "defaultColumnCount", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "defaultRowCount", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "defaultPercentageFormat", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL, defaultValue = "0.00%"),
                    @StudioProperty(xmlAttribute = "functionBarVisible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            defaultValue = "100%"),
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE),
                    @StudioProperty(xmlAttribute = "minimumRowHeightForComponents", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL, defaultValue = "30"),
                    @StudioProperty(xmlAttribute = "maxColumns", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "maxRows", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "rowBufferSize", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL, defaultValue = "200"),
                    @StudioProperty(xmlAttribute = "colBufferSize", type = StudioPropertyType.INTEGER,
                            category = StudioProperty.Category.GENERAL, defaultValue = "200"),
                    @StudioProperty(xmlAttribute = "reportStyle", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL, defaultValue = "false"),
                    @StudioProperty(xmlAttribute = "sheetSelectionBarVisible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.LOOK_AND_FEEL, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "statusLabelValue", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "invalidFormulaErrorMessage", type = StudioPropertyType.LOCALIZED_STRING,
                            category = StudioProperty.Category.VALIDATION),
                    @StudioProperty(xmlAttribute = "selection", type = StudioPropertyType.STRING,
                            category = StudioProperty.Category.GENERAL),
                    @StudioProperty(xmlAttribute = "theme", type = StudioPropertyType.ENUMERATION,
                            category = StudioProperty.Category.LOOK_AND_FEEL,
                            classFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet$SpreadsheetTheme",
                            options = {"LUMO", "VALO"}, defaultValue = "VALO"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN,
                            category = StudioProperty.Category.GENERAL,
                            defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            category = StudioProperty.Category.SIZE,
                            defaultValue = "100%")
            },
            supplyHandlers = {
                    @StudioSupplyHandler(
                            methodName = "setCellValueHandler",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueHandler"
                    ),
                    @StudioSupplyHandler(
                            methodName = "setCellDeletionHandler",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler"
                    ),
                    @StudioSupplyHandler(
                            methodName = "setHyperlinkCellClickHandler",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.HyperlinkCellClickHandler"
                    ),
                    @StudioSupplyHandler(
                            methodName = "setSpreadsheetComponentFactory",
                            parameterType = "com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory"
                    )
            },
            customSubscriptions = {
                    @StudioCustomSubscription(
                            methodName = "addSelectionChangeListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent"
                    ),
                    @StudioCustomSubscription(
                            methodName = "addCellValueChangeListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent"
                    ),
                    @StudioCustomSubscription(
                            methodName = "addFormulaValueChangeListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.FormulaValueChangeListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.FormulaValueChangeEvent"
                    ),
                    @StudioCustomSubscription(
                            methodName = "addProtectedEditListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.ProtectedEditEvent"
                    ),
                    @StudioCustomSubscription(
                            methodName = "addSheetChangeListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.SheetChangeListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.SheetChangeEvent"
                    ),
                    @StudioCustomSubscription(
                            methodName = "addRowHeaderDoubleClickListener",
                            parameterType = "com.vaadin.flow.component.spreadsheet.Spreadsheet.RowHeaderDoubleClickListener",
                            eventClassFqn = "com.vaadin.flow.component.spreadsheet.Spreadsheet.RowHeaderDoubleClickEvent"
                    )
            }
    )
    Spreadsheet spreadsheet();
}
