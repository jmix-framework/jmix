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
package io.jmix.reports.yarg.formatters.impl.xls;

import org.apache.poi.ss.util.CellReference;

public class Cell {
    private int row;
    private int col;

    public Cell(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public Cell(CellReference originalCell) {
        this(originalCell.getCol(), originalCell.getRow());
    }

    public Cell(Cell cell) {
        col = cell.getCol();
        row = cell.getRow();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public CellReference toCellReference() {
        return new CellReference(row, col);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof Cell) {
            if (getCol() != ((Cell)obj).getCol()) return false;
            if (getRow() != ((Cell)obj).getRow()) return false;

            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return Character.toString((char) ('A' + (char) (col))) + (row +1);
    }
}
