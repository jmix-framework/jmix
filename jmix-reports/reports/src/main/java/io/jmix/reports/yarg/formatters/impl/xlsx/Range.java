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

package io.jmix.reports.yarg.formatters.impl.xlsx;

import org.apache.commons.lang3.StringUtils;
import org.xlsx4j.sml.Cell;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.reports.yarg.formatters.impl.xlsx.XlsxUtils.getColumnReferenceFromNumber;

public class Range {
    public final static Pattern UNLIMITED_ROW_FORMULA_RANGE_PATTERN = Pattern.compile("'?(.+?)'?!\\$([0-9]+):\\$([0-9]+)");
    public final static Pattern UNLIMITED_COLUMN_FORMULA_RANGE_PATTERN = Pattern.compile("'?(.+?)'?!\\$([A-z]+[0-9]*):\\$([A-z]+[0-9]*)");
    public final static Pattern FORMULA_RANGE_PATTERN = Pattern.compile("'?(.+?)'?!\\$(.*)\\$(.*):\\$(.*)\\$(.*)");
    public final static Pattern SINGLE_CELL_RANGE_PATTERN = Pattern.compile("'?(.+?)'?!\\$(.*)\\$(.*)");
    public final static Pattern NOT_STRICT_RANGE_PATTERN = Pattern.compile("([A-z]+[0-9]+):?([A-z]+[0-9]+)?");
    public final static Pattern STRICT_RANGE_PATTERN = Pattern.compile("([A-z]+[0-9]+):([A-z]+[0-9]+)");

    public final static int MIN_LENGTH_RANGE = 1;
    public final static int MAX_LENGTH_RANGE = 255;

    private String sheet;
    private int firstColumn;
    private int firstRow;
    private int lastColumn;
    private int lastRow;

    public Range(String sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
    }

    public static Range fromCells(String sheetName, String firstCellRef, String lastCellRef) {
        int startColumn, startRow, lastColumn, lastRow;

        CellReference firstCell = new CellReference(sheetName, firstCellRef);
        CellReference lastCell = new CellReference(sheetName, lastCellRef);
        startColumn = firstCell.getColumn();
        startRow = firstCell.getRow();
        lastColumn = lastCell.getColumn();
        lastRow = lastCell.getRow();

        return new Range(sheetName, startColumn, startRow, lastColumn, lastRow);
    }

    public static Range fromFormula(String range) {
        Matcher matcher = FORMULA_RANGE_PATTERN.matcher(range);
        Matcher matcher2 = UNLIMITED_ROW_FORMULA_RANGE_PATTERN.matcher(range);
        Matcher matcher3 = UNLIMITED_COLUMN_FORMULA_RANGE_PATTERN.matcher(range);
        Matcher matcher4 = SINGLE_CELL_RANGE_PATTERN.matcher(range);

        if (matcher.find()) {
            String sheet = matcher.group(1);
            String startColumnStr = matcher.group(2);
            String startRowStr = matcher.group(3);
            String lastColumnStr = matcher.group(4);
            String lastRowStr = matcher.group(5);

            return getRange(range, sheet, startColumnStr, startRowStr, lastColumnStr, lastRowStr);
        } else if (matcher2.find()) {
            String sheet = matcher2.group(1);

            String startRowStr = matcher2.group(2);
            String lastRowStr = matcher2.group(3);

            return getRange(range, sheet, null, startRowStr, null, lastRowStr);
        } else if (matcher3.find()) {
            String sheet = matcher3.group(1);

            String startColumnStr = matcher3.group(2);
            String lastColumnStr = matcher3.group(3);

            return getRange(range, sheet, startColumnStr, null, lastColumnStr, null);
        } else if (matcher4.find()) {
            String sheet = matcher4.group(1);

            String startColumnStr = matcher4.group(2);
            String startRowStr = matcher4.group(3);

            return getRange(range, sheet, startColumnStr, startRowStr, startColumnStr, startRowStr);
        } else {
            throw new RuntimeException(String.format("Wrong range value %s", range));
        }
    }

    private static Range getRange(String range, String sheet, String startColumnStr, String startRowStr, String lastColumnStr, String lastRowStr) {
        try {
            int startRow = startRowStr != null ? Integer.valueOf(startRowStr) : MIN_LENGTH_RANGE;
            int startColumn = startColumnStr != null ? XlsxUtils.getNumberFromColumnReference(startColumnStr) : MIN_LENGTH_RANGE;
            int lastRow = lastRowStr != null ? Integer.valueOf(lastRowStr) : MAX_LENGTH_RANGE;
            int lastColumn = lastColumnStr != null ? XlsxUtils.getNumberFromColumnReference(lastColumnStr) : MAX_LENGTH_RANGE;

            return new Range(sheet, startColumn, startRow, lastColumn, lastRow);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Wrong range value %s. Error: %s", range, e.getMessage()));
        }
    }

    public static Range fromRange(String sheet, String range) {
        Matcher matcher = NOT_STRICT_RANGE_PATTERN.matcher(range);
        if (matcher.find()) {
            String firstCell = matcher.group(1);
            String lastCell = matcher.group(2);
            if (StringUtils.isEmpty(lastCell)) {
                lastCell = firstCell;
            }

            return fromCells(sheet, firstCell, lastCell);
        } else {
            throw new RuntimeException(String.format("Wrong range value %s", range));
        }
    }

    public static Set<Range> fromCellFormula(String sheet, Cell cellWithFormula) {
        Matcher matcher = Range.STRICT_RANGE_PATTERN.matcher(cellWithFormula.getF().getValue());
        Set<Range> ranges = new HashSet<Range>();
        while (matcher.find()) {
            String rangeStr = matcher.group();
            Range formulaRange = Range.fromRange(sheet, rangeStr);
            ranges.add(formulaRange);
        }

        matcher = Range.NOT_STRICT_RANGE_PATTERN.matcher(cellWithFormula.getF().getValue());
        while (matcher.find()) {
            String rangeStr = matcher.group();
            Range formulaRange = Range.fromRange(sheet, rangeStr);
            ranges.add(formulaRange);
        }

        return ranges;
    }

    public boolean contains(CellReference cellReference) {
        return cellReference.getSheet().equals(getSheet())
                && getFirstColumn() <= cellReference.getColumn() && getFirstRow() <= cellReference.getRow()
                && getLastColumn() >= cellReference.getColumn() && getLastRow() >= cellReference.getRow();
    }

    public boolean contains(Range range) {
        return range.getSheet().equals(getSheet())
                && getFirstColumn() <= range.getFirstColumn() && getFirstRow() <= range.getFirstRow()
                && getLastColumn() >= range.getLastColumn() && getLastRow() >= range.getLastRow();
    }

    public boolean containsAny(Collection<Range> range) {
        for (Range theRange : range) {
            if (contains(theRange)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<Range> range) {
        for (Range theRange : range) {
            if (!contains(theRange)) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsByVertical(Range range) {
        return this.getSheet().equals(range.getSheet()) && (
                this.getFirstRow() >= range.getFirstRow() && this.getFirstRow() <= range.getLastRow() ||
                        this.getLastRow() >= range.getFirstRow() && this.getLastRow() <= range.getLastRow() ||
                        range.getFirstRow() >= this.getFirstRow() && range.getFirstRow() <= this.getLastRow() ||
                        range.getLastRow() >= this.getFirstRow() && range.getLastRow() <= this.getLastRow()
        );
    }

    public boolean intersects(Range range) {
        return this.getSheet().equals(range.getSheet())
                && (
                this.getFirstRow() >= range.getFirstRow() && this.getFirstRow() <= range.getLastRow() ||
                        this.getLastRow() >= range.getFirstRow() && this.getLastRow() <= range.getLastRow() ||
                        range.getFirstRow() >= this.getFirstRow() && range.getFirstRow() <= this.getLastRow() ||
                        range.getLastRow() >= this.getFirstRow() && range.getLastRow() <= this.getLastRow())
                && (
                this.getFirstColumn() >= range.getFirstColumn() && this.getFirstColumn() <= range.getLastColumn() ||
                        this.getLastColumn() >= range.getFirstColumn() && this.getLastColumn() <= range.getLastColumn() ||
                        range.getFirstColumn() >= this.getFirstColumn() && range.getFirstColumn() <= this.getLastColumn() ||
                        range.getLastColumn() >= this.getFirstColumn() && range.getLastColumn() <= this.getLastColumn()
        );
    }


    public Range shift(int downShift, int rightShift) {
        firstColumn += rightShift;
        firstRow += downShift;
        lastColumn += rightShift;
        lastRow += downShift;

        return this;
    }

    public Range shiftDown(int shift) {
        return shift(shift, 0);
    }

    public Range shiftRight(int shift) {
        return shift(0, shift);
    }

    public Range grow(int downGrow, int rightGrow) {
        lastColumn += rightGrow;
        lastRow += downGrow;

        return this;
    }

    public Range growDown(int grow) {
        return grow(grow, 0);
    }

    public Range growRight(int grow) {
        return grow(0, grow);
    }

    public Range copy() {
        return new Range(sheet, firstColumn, firstRow, lastColumn, lastRow);
    }

    @Override
    public String toString() {
        return toFormula();
    }

    public String toFormula() {
        return String.format("'%s'!$%s$%d:$%s$%d",
                getSheet(),
                getColumnReferenceFromNumber(getFirstColumn()),
                getFirstRow(),
                getColumnReferenceFromNumber(getLastColumn()),
                getLastRow());
    }

    public String toRange() {
        return String.format("%s%d:%s%d",
                getColumnReferenceFromNumber(getFirstColumn()),
                getFirstRow(),
                getColumnReferenceFromNumber(getLastColumn()),
                getLastRow());
    }

    public String toFirstCellReference() {
        return String.format("%s%d",
                getColumnReferenceFromNumber(getFirstColumn()),
                getFirstRow());
    }

    public List<CellReference> toCellReferences() {
        List<CellReference> references = new ArrayList<CellReference>();
        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                references.add(new CellReference(sheet, row, column));
            }
        }

        return references;
    }

    public boolean isOneCellRange() {
        return firstColumn == lastColumn && firstRow == lastRow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (getLastColumn() != range.getLastColumn()) return false;
        if (getLastRow() != range.getLastRow()) return false;
        if (getFirstColumn() != range.getFirstColumn()) return false;
        if (getFirstRow() != range.getFirstRow()) return false;
        if (getSheet() != null ? !getSheet().equals(range.getSheet()) : range.getSheet() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getSheet() != null ? getSheet().hashCode() : 0;
        result = 31 * result + getFirstColumn();
        result = 31 * result + getFirstRow();
        result = 31 * result + getLastColumn();
        result = 31 * result + getLastRow();
        return result;
    }

    public String getSheet() {
        return sheet;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public int getLastRow() {
        return lastRow;
    }
}