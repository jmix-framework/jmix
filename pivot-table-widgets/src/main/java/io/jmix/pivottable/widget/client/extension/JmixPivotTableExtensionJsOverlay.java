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

package io.jmix.pivottable.widget.client.extension;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.DateTimeFormat;

public class JmixPivotTableExtensionJsOverlay {

    protected Element pivotElement;

    public JmixPivotTableExtensionJsOverlay(Element pivotElement) {
        this.pivotElement = pivotElement;
    }

    public String convertPivotTableToJson(JsPivotExtensionOptions config) {
        return convertPivotTableToJson(pivotElement, config);
    }

    protected static boolean isDate(String value, String format) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        if (format == null || format.isEmpty()) {
            return false;
        }

        DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(format);
        try {
            dateTimeFormat.parse(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected static native String convertPivotTableToJson(Element pivotElement, JsPivotExtensionOptions config) /*-{
        var tableElements = pivotElement.getElementsByClassName('pvtTable');
        if (tableElements.length === 0) {
            return;
        }

        var table = tableElements[0];

        var resultObject = {};
        resultObject.dataNumRows = table.attributes['data-numrows'] ? table.attributes['data-numrows'].value : null;
        resultObject.dataNumCols = table.attributes['data-numcols'] ? table.attributes['data-numcols'].value : null;

        var boldClassNames = ['pvtTotal rowTotal', 'pvtTotal colTotal', 'pvtGrandTotal'];

        var isNumeric = function(num){
            return !isNaN(num)
        };

        var dateTimeFormat = config.dateTimeParseFormat;
        var dateFormat = config.dateParseFormat;
        var timeFormat = config.timeParseFormat;
        var isDate = $entry(function(value, format){
            return @io.jmix.pivottable.widget.client.extension.JmixPivotTableExtensionJsOverlay::isDate(Ljava/lang/String;Ljava/lang/String;)(value, format);
        });

        // appendIndex needs to set the original order of rows in json
        var getRowsAndCells = function (tableRows, appendIndex) {
            var rows = [];
            for (var i = 0; i < tableRows.length; i++) {
                var tableRow = tableRows[i];

                var row = {};
                row.tableRowNumber = i + appendIndex;
                row.cells = [];

                for (var j = 0; j < tableRow.cells.length; j++) {
                    var rowCell = tableRow.cells[j];

                    var cell = {};

                    // check for bold
                    if (rowCell.nodeName === 'TH' || boldClassNames.indexOf(rowCell.className) > -1) {
                        cell.isBold = true;
                    } else {
                        cell.isBold = false;
                    }

                    if (rowCell.className) {
                        cell.className = rowCell.className;
                    }

                    cell.colSpan = rowCell.colSpan;
                    cell.rowSpan = rowCell.rowSpan;

                    if (rowCell.outerText) {
                        cell.value = rowCell.outerText;
                    } else if (rowCell.innerText) {
                        cell.value = rowCell.innerText;
                    }

                    if (rowCell.attributes['data-value']) {
                        if (rowCell.attributes['data-value'].value !== 'null'
                                && rowCell.attributes['data-value'].value !== "") {
                            cell.value = rowCell.attributes['data-value'].value;
                        }
                    }

                    // check for numeric or string
                    if (isNumeric(cell.value)) {
                        cell.type = 'NUMERIC'
                    } else if (isDate(cell.value, dateTimeFormat)) {
                        cell.type = 'DATE_TIME'
                    } else if (isDate(cell.value, dateFormat)) {
                        cell.type = 'DATE'
                    } else if (isDate(cell.value, timeFormat)) {
                        cell.type = 'TIME'
                    } else {
                       cell.type = 'STRING'
                    }

                    row.cells.push(cell);
                }
                rows.push(row);
            }
            return rows;
        };

        var tableHead = table.tHead;
        resultObject.headRows = getRowsAndCells(tableHead.rows, 0);

        var tableBody = table.tBodies[0];
        resultObject.bodyRows = getRowsAndCells(tableBody.rows, resultObject.headRows.length);

        return JSON.stringify(resultObject);
    }-*/;
}
