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

package io.jmix.pivottable.model.extension;

public class PivotDataSeparatedCell {

    protected String id;

    protected String value;

    protected int indexRow;
    protected int indexCol;

    protected int colSpan;
    protected int rowSpan;

    protected PivotDataCell.Type type;

    protected Boolean isBold = false;

    public PivotDataSeparatedCell() {
    }

    public PivotDataSeparatedCell(String value, int rowSpan, int colSpan, PivotDataCell.Type type, Boolean isBold) {
        this.value = value;

        this.rowSpan = rowSpan;
        this.colSpan = colSpan;

        this.type = type;
        this.isBold = isBold;
    }

    public PivotDataSeparatedCell(PivotDataSeparatedCell cell) {
        this.id = cell.getId();

        this.value = cell.getValue();

        this.rowSpan = cell.getRowSpan();
        this.colSpan = cell.getColSpan();

        this.type = cell.getType();
        this.isBold = cell.isBold();

        this.indexCol = cell.getIndexCol();
        this.indexRow = cell.getIndexRow();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getIndexRow() {
        return indexRow;
    }

    public void setIndexRow(int indexRow) {
        this.indexRow = indexRow;
    }

    public int getIndexCol() {
        return indexCol;
    }

    public void setIndexCol(int indexCol) {
        this.indexCol = indexCol;
    }

    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public PivotDataCell.Type getType() {
        return type;
    }

    public void setType(PivotDataCell.Type type) {
        this.type = type;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }
}
