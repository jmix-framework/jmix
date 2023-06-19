/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.formatters.impl.doc;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XText;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.view.XSelectionSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;

public class TableManager {
    protected XTextTable xTextTable;
    protected String tableName;

    public TableManager(XComponent xComponent, String tableName) throws NoSuchElementException, WrappedTargetException {
        xTextTable = getTableByName(xComponent, tableName);
        this.tableName = tableName;
    }

    public XTextTable getXTextTable() {
        return xTextTable;
    }

    public String getTableName() {
        return tableName;
    }

    public static List<String> getTablesNames(XComponent xComponent) {
        XNameAccess tables = as(XTextTablesSupplier.class, xComponent).getTextTables();
        return new ArrayList<String>(Arrays.asList(tables.getElementNames()));
    }

    public static XTextTable getTableByName(XComponent xComponent, String tableName) throws NoSuchElementException, WrappedTargetException {
        XNameAccess tables = as(XTextTablesSupplier.class, xComponent).getTextTables();
        return (XTextTable) ((Any) tables.getByName(tableName)).getObject();
    }

    public int findRowWithAliases() {
        XTextTable xTextTable = getXTextTable();
        for (int currentRow = 0; currentRow < xTextTable.getRows().getCount(); currentRow++) {
            try {
                for (int i = 0; i < xTextTable.getColumns().getCount(); i++) {
                    XCell xCell = null;
                    try {
                        xCell = getXCell(i, currentRow);
                    } catch (IndexOutOfBoundsException e) {
                        //stop loop - this row has less columns than first one
                        break;
                    }
                    String templateText = as(XText.class, xCell).getString();
                    if (AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(templateText).find()) {
                        return currentRow;
                    }
                }
            } catch (Exception e) {
                throw new ReportFormattingException(e);
            }
        }

        return -1;
    }

    public XText findFirstEntryInRow(Pattern pattern, int row) {
        try {
            for (int i = 0; i < xTextTable.getColumns().getCount(); i++) {
                XText xText = as(XText.class, getXCell(i, row));
                String templateText = xText.getString();

                if (pattern.matcher(templateText).find()) {
                    return xText;
                }
            }
        } catch (Exception e) {
            throw new ReportFormattingException(e);
        }
        return null;
    }

    public XCell getXCell(int col, int row) throws IndexOutOfBoundsException {
        return as(XCellRange.class, xTextTable).getCellByPosition(col, row);
    }

    public void selectRow(XController xController, int row) throws com.sun.star.uno.Exception {
        List<String> thisRowCells = getCellNamesForTheRow(row);
        String firstCellName = thisRowCells.get(0);
        String lastCellName = thisRowCells.get(thisRowCells.size() - 1);


        XTextTableCursor xTextTableCursor = xTextTable.createCursorByCellName(firstCellName);
        xTextTableCursor.gotoCellByName(lastCellName, true);
        // It works only if XCellRange was created via cursor. why????
        if (firstCellName.equalsIgnoreCase(lastCellName)) {
            XCell cell = as(XCellRange.class, xTextTable).getCellByPosition(0, row);
            as(XSelectionSupplier.class, xController).select(new Any(new Type(XCell.class), cell));
        } else {
            XCellRange xCellRange = as(XCellRange.class, xTextTable).getCellRangeByName(xTextTableCursor.getRangeName());
            // and why do we need Any here?
            as(XSelectionSupplier.class, xController).select(new Any(new Type(XCellRange.class), xCellRange));
        }
    }

    /**
     * @param row - row order number [0..n]
     * @return list of cell names for the row
     */
    public List<String> getCellNamesForTheRow(int row) {
        List<String> thisRowCells = new ArrayList<String>();
        for (String cellName : xTextTable.getCellNames()) {
            if (cellName.matches("[A-z]" + (row + 1))) {
                thisRowCells.add(cellName);
            }
        }
        return thisRowCells;
    }

    public void deleteRow(int row) {
        XTableRows xTableRows = xTextTable.getRows();
        xTableRows.removeByIndex(row, 1);
    }

    public void insertEmptyRow(int indexAfter) {
        XTableRows xTableRows = xTextTable.getRows();
        xTableRows.insertByIndex(indexAfter + 1, 1);
    }

    public void copyRow(XDispatchHelper xDispatchHelper, XController xController, int row) throws com.sun.star.uno.Exception {
        selectRow(xController, row);
        XDispatchProvider xDispatchProvider = as(XDispatchProvider.class, xController.getFrame());
        copy(xDispatchHelper, xDispatchProvider);
        insertEmptyRow(row);
        selectRow(xController, row + 1);
        paste(xDispatchHelper, xDispatchProvider);
    }

    public void copy(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Copy", "", 0, new PropertyValue[]{new PropertyValue()});
    }

    public void paste(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Paste", "", 0, new PropertyValue[]{new PropertyValue()});
    }
}
