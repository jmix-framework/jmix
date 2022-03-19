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

package io.jmix.pivottable.component.impl;

import io.jmix.pivottable.model.extension.PivotData;
import io.jmix.pivottable.model.extension.PivotDataCell;
import io.jmix.pivottable.model.extension.PivotDataRow;
import io.jmix.pivottable.model.extension.PivotDataSeparatedCell;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jmix.pivottable.component.impl.PivotExcelExporter.MAX_ROW_INDEX;

/**
 * Helps to convert {@link PivotDataRow} to list with separated cells and provides methods for merging cells.
 */
public class PivotDataExcelHelper {

    protected PivotData pivotData;

    protected List<PivotDataSeparatedCell> cellsToMerged = new ArrayList<>();
    protected List<String> cellIdsToMerged = new ArrayList<>();

    protected List<List<PivotDataSeparatedCell>> rows;

    public PivotDataExcelHelper(PivotData pivotData) {
        this.pivotData = pivotData;

        initRows();
    }

    protected void initRows() {
        rows = new ArrayList<>();

        List<PivotDataRow> dataRows = pivotData.getAllRows();

        for (int indexRow = 0; indexRow < dataRows.size(); indexRow++) {
            PivotDataRow currentRow = dataRows.get(indexRow);

            int prevIndexRow = indexRow - 1;

            List<PivotDataSeparatedCell> prevRow = prevIndexRow < 0 ? null : rows.get(prevIndexRow);

            // convert PivotDataRow to row with separated cells
            List<PivotDataSeparatedCell> separatedCells = createRow(currentRow, prevRow);

            rows.add(separatedCells);
        }
    }

    /**
     * @return pivot table data with separated cells (all merged cells were separated and each separated cell has
     * common value and id)
     */
    public List<List<PivotDataSeparatedCell>> getRows() {
        return rows;
    }

    /**
     * @return number of exactly excel columns and -1 if there is no columns
     */
    public int getOriginColumnsNumber() {
        if (!CollectionUtils.isEmpty(rows)) {
            // get first row because they have the same size
            int size = rows.iterator().next().size();
            return size == 0 ? -1 : size;
        }
        return -1;
    }

    /**
     * @return list with unique cells id which should be merged
     */
    public List<String> getCellIdsToMerged() {
        return cellIdsToMerged;
    }

    /**
     * @param id PivotDataSeparatedCell id that should be merged
     * @return first row index of all cells that should be merged by common id
     */
    public int getFirstRowById(String id) {
        int firstRow = MAX_ROW_INDEX;

        for (PivotDataSeparatedCell cell : cellsToMerged) {
            if (cell.getId().equals(id) && firstRow > cell.getIndexRow()) {
                firstRow = cell.getIndexRow();
            }
        }

        return firstRow;
    }

    /**
     * @param id PivotDataSeparatedCell id that should be merged
     * @return last row index of all cells that should be merged by common id
     */
    public int getLastRowById(String id) {
        int lastRow = 0;

        for (PivotDataSeparatedCell cell : cellsToMerged) {
            if (cell.getId().equals(id) && lastRow < cell.getIndexRow()) {
                lastRow = cell.getIndexRow();
            }
        }

        return lastRow;
    }

    /**
     * @param id PivotDataSeparatedCell id that should be merged
     * @return first col index of all cells that should be merged by common id
     */
    public int getFirstColById(String id) {
        int firstCol = Integer.MAX_VALUE;

        for (PivotDataSeparatedCell cell : cellsToMerged) {
            if (cell.getId().equals(id) && firstCol > cell.getIndexCol()) {
                firstCol = cell.getIndexCol();
            }
        }

        return firstCol;
    }

    /**
     * @param id PivotDataSeparatedCell id that should be merged
     * @return last col index of all cells that should be merged by common id
     */
    public int getLastColById(String id) {
        int lastCol = 0;

        for (PivotDataSeparatedCell cell : cellsToMerged) {
            if (cell.getId().equals(id) && lastCol < cell.getIndexCol()) {
                lastCol = cell.getIndexCol();
            }
        }

        return lastCol;
    }

    /**
     * @return a collection of cell range addresses that should be merged in the {@link HSSFSheet}
     */
    public List<CellRangeAddress> getCellRangeAddresses() {
        List<String> ids = getCellIdsToMerged();
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        List<CellRangeAddress> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            int firstRow = getFirstRowById(id);
            int lastRow = getLastRowById(id);

            if (firstRow >= MAX_ROW_INDEX) {
                break;
            }
            if (lastRow > MAX_ROW_INDEX) {
                lastRow = MAX_ROW_INDEX;
            }
            result.add(
                    new CellRangeAddress(firstRow, lastRow,
                            getFirstColById(id), getLastColById(id)));
        }
        return result;
    }

    protected List<PivotDataSeparatedCell> createRow(PivotDataRow currentRow, List<PivotDataSeparatedCell> prevRow) {
        List<PivotDataSeparatedCell> result = new ArrayList<>();
        int currentRowIndex = currentRow.getTableRowNumber();

        if (prevRow == null) { // if is first row

            List<PivotDataCell> cells = currentRow.getCells();
            int separatedCellsCount = 0; // index of column in created row

            for (int indexCol = 0; indexCol < cells.size(); indexCol++) {

                PivotDataCell pivotDataCell = cells.get(indexCol);
                PivotDataSeparatedCell cell = convertPivotCellToSeparatedCell(pivotDataCell);
                cell.setIndexRow(currentRowIndex);
                cell.setIndexCol(separatedCellsCount);

                int colSpan = pivotDataCell.getColSpan();
                int rowSpan = pivotDataCell.getRowSpan();

                if (colSpan > 1) {
                    cell.setId(currentRowIndex + ";" + separatedCellsCount);
                    cellIdsToMerged.add(cell.getId());
                    separatedCellsCount += createAndAddSeparatedCells(cell, result); // create cells on the row according to colSpan

                } else if (rowSpan > 1) {
                    cell.setId(currentRowIndex + ";" + separatedCellsCount);
                    cellIdsToMerged.add(cell.getId());
                    cellsToMerged.add(cell);

                    result.add(cell);
                } else {
                    result.add(cell);
                }

                separatedCellsCount++;
            }
        } else { // if not first row
            int cellCount = 0; // index of column PivotDataRow
            List<PivotDataCell> cells = currentRow.getCells();

            for (int indexCol = 0; indexCol < prevRow.size(); indexCol++) {
                PivotDataSeparatedCell aboveCell = prevRow.get(indexCol);

                // as we have merged cells in pivotTable we should check
                // if cell in above row has rowSpan and it affect to the current row, so
                // current cell should be merged with above and we add above cell instead of current row cell
                if (aboveCell.getRowSpan() > 1 && addAboveCell(aboveCell, currentRowIndex)) {
                    PivotDataSeparatedCell resultCell = new PivotDataSeparatedCell(aboveCell);
                    resultCell.setIndexRow(currentRowIndex);

                    cellsToMerged.add(resultCell);
                    result.add(resultCell);
                } else {
                    PivotDataCell pivotDataCell = cells.get(cellCount);

                    PivotDataSeparatedCell cell = convertPivotCellToSeparatedCell(pivotDataCell);
                    cell.setIndexRow(currentRowIndex);
                    cell.setIndexCol(indexCol);

                    int colSpan = pivotDataCell.getColSpan();
                    int rowSpan = pivotDataCell.getRowSpan();

                    if (colSpan > 1) {
                        cell.setId(currentRowIndex + ";" + indexCol);
                        cellIdsToMerged.add(cell.getId());
                        indexCol += createAndAddSeparatedCells(cell, result); // create cells on the row according to colSpan

                    } else if (rowSpan > 1) {
                        cell.setId(currentRowIndex + ";" + indexCol);
                        cellIdsToMerged.add(cell.getId());
                        cellsToMerged.add(cell);

                        result.add(cell);
                    } else {
                        result.add(cell);
                    }

                    cellCount++;
                }
            }
        }
        return result;
    }

    protected boolean addAboveCell(PivotDataSeparatedCell aboveCell, int indexRow) {
        String startSpanRow = aboveCell.getId().split(";")[0];
        int endSpanRow = Integer.parseInt(startSpanRow) + aboveCell.getRowSpan() - 1;

        return indexRow <= endSpanRow;
    }

    protected int createAndAddSeparatedCells(PivotDataSeparatedCell separatedCell, List<PivotDataSeparatedCell> cellsRow) {
        List<PivotDataSeparatedCell> result = new ArrayList<>();

        for (int i = 1; i < separatedCell.getColSpan(); i++) {
            PivotDataSeparatedCell cell = new PivotDataSeparatedCell();
            cell.setId(separatedCell.getId());
            cell.setIndexRow(separatedCell.getIndexRow());
            cell.setIndexCol(separatedCell.getIndexCol() + i);
            cell.setColSpan(separatedCell.getColSpan());
            cell.setRowSpan(separatedCell.getRowSpan());
            cell.setValue(separatedCell.getValue());
            cell.setType(separatedCell.getType());
            cell.setBold(separatedCell.isBold());

            result.add(cell);
        }

        cellsRow.add(separatedCell);
        cellsRow.addAll(result);

        cellsToMerged.add(separatedCell);
        cellsToMerged.addAll(result);

        return result.size();
    }

    protected PivotDataSeparatedCell convertPivotCellToSeparatedCell(PivotDataCell pivotDataCell) {
        return new PivotDataSeparatedCell(
                pivotDataCell.getValue(),
                pivotDataCell.getRowSpan(),
                pivotDataCell.getColSpan(),
                pivotDataCell.getType(),
                pivotDataCell.getBold());
    }
}