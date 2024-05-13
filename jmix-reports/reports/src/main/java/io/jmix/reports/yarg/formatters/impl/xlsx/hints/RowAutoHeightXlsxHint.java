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

package io.jmix.reports.yarg.formatters.impl.xlsx.hints;

import io.jmix.reports.yarg.formatters.impl.xlsx.CellReference;
import io.jmix.reports.yarg.formatters.impl.xlsx.Document;
import io.jmix.reports.yarg.formatters.impl.xlsx.Range;
import io.jmix.reports.yarg.formatters.impl.xlsx.StyleSheet;
import io.jmix.reports.yarg.structure.BandData;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.xlsx4j.sml.CTMergeCell;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.*;

/**
 * Recalculates row heights depending on the cell values and fonts.
 */
public class RowAutoHeightXlsxHint implements XlsxHint {
    protected static final Logger log = LoggerFactory.getLogger(RowAutoHeightXlsxHint.class);

    /**
     * Max value of the row height in Excel (in points).
     */
    protected static final double MAX_ROW_HEIGHT = 409.0;
    protected static final String defaultChar = "0";
    protected static final int DEFAULT_FONT_SIZE = 11;

    protected Document document;
    protected FontRenderContext fontRenderContext;
    protected List<CellDataObject> data = new ArrayList<>();

    public RowAutoHeightXlsxHint(Document document) {
        this.document = document;
        this.fontRenderContext = new FontRenderContext(null, true, true);
    }

    @Override
    public String getName() {
        return "rowAutoHeight";
    }

    @Override
    public void add(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
        CellDataObject cellDataObject = new CellDataObject(templateCell, resultCell, bandData, params);

        String resultSheet = document.getWorksheets().stream()
                .filter(sheetWrapper -> sheetWrapper.getWorksheet() == WorksheetPart.getWorksheetPart(resultCell))
                .map(Document.SheetWrapper::getName)
                .findFirst()
                .orElse(null);

        cellDataObject.setResultSheet(resultSheet);

        data.add(cellDataObject);
    }

    @Override
    public void apply() {
        double defaultCharWidth = getDefaultCharWidth();
        Map<Row, Double> rowMaxHeights = new HashMap<>();
        Map<Double, Integer> columnWidths = new HashMap<>();
        List<Range> mergedCells = getMergedCells();

        for (CellDataObject cellData : data) {
            Cell resultCell = cellData.resultCell;
            Row resultRow = (Row) resultCell.getParent();

            String cellValue = document.getCellValue(resultCell);
            CellReference cellReference = cellData.toCellReference();
            Range mergedCell = getMergedCell(cellReference, mergedCells);

            if (isMaxHeight(resultRow.getHt()) || isMaxHeight(rowMaxHeights.get(resultRow))
                    || StringUtils.isEmpty(cellValue) || (mergedCell != null && !mergedCell.isOneRowRange())) {
                continue;
            }

            StyleSheet.Font font = document.getCellFont(resultCell);
            if (font == null) {
                log.debug("Unable to get font for cell {}", resultCell.getR());
                continue;
            }

            Integer columnWidthInPixels = mergedCell == null ? getColumnWidthInPixels(cellReference, defaultCharWidth, columnWidths) :
                    getMergedCellWidthInPixels(mergedCell, defaultCharWidth, columnWidths);
            if (columnWidthInPixels != null) {
                int cellWidth = calculateCellWidth(columnWidthInPixels, resultCell);
                double cellHeight = calculateCellHeight(font, cellValue, cellWidth);

                if (cellHeight > MAX_ROW_HEIGHT) {
                    rowMaxHeights.put(resultRow, MAX_ROW_HEIGHT);
                } else {
                    double newHeight = resultRow.getHt() != null ? Double.max(resultRow.getHt(), cellHeight) : cellHeight;
                    rowMaxHeights.merge(resultRow, newHeight, Double::max);
                }
            }
        }

        rowMaxHeights.forEach(Row::setHt);
    }

    @Nullable
    protected Integer getColumnWidthInPixels(CellReference cellReference, double defaultCharWidth, Map<Double, Integer> columnWidths) {
        Double sourceColumnWidth = document.getColumnWidth(cellReference);
        if (sourceColumnWidth == null) {
            log.debug("Unable to get width for cell {}", cellReference);
            return null;
        }

        //get a count of default chars that can be displayed in a column
        double charsCount = calculateCharsCount(sourceColumnWidth, defaultCharWidth);

        return columnWidths.computeIfAbsent(sourceColumnWidth, width -> getColumnWidthInPixels(charsCount));
    }

    protected Integer getMergedCellWidthInPixels(Range range, double defaultCharWidth, Map<Double, Integer> columnWidths) {
        double sourceMergedCellWidth = range.toCellReferences().stream()
                .mapToDouble(mergedCellRef -> document.getColumnWidth(mergedCellRef))
                .filter(Objects::nonNull)
                .sum();

        double charsCount = calculateCharsCount(sourceMergedCellWidth, defaultCharWidth);
        return columnWidths.computeIfAbsent(sourceMergedCellWidth, width -> getColumnWidthInPixels(charsCount));
    }

    protected List<Range> getMergedCells() {
        return document.getWorksheets().stream()
                .filter(sheetWrapper -> sheetWrapper.getWorksheet().getJaxbElement().getMergeCells() != null)
                .map(sheetWrapper -> {
                    String sheetName = sheetWrapper.getName();
                    List<CTMergeCell> mergeCells = sheetWrapper.getWorksheet().getJaxbElement().getMergeCells().getMergeCell();
                    return mergeCells.stream().map(ctMergeCell -> Range.fromRange(sheetName, ctMergeCell.getRef())).toList();
                })
                .flatMap(Collection::stream)
                .toList();
    }

    protected int calculateCellWidth(int columnWidthInPixels, Cell resultCell) {
        StyleSheet.CellXfs cellStyle = document.getCellStyle(resultCell);
        int indent = cellStyle != null && cellStyle.getIndent() != null ? cellStyle.getIndent().intValue() : 0;
        int indentInPixels = getIndentInPixels(indent);

        //remove indent, additional small padding to be sure that value will be fully shown in the cell
        int availableCellWidth = columnWidthInPixels - indentInPixels - 3;

        return availableCellWidth < 0 ? columnWidthInPixels : availableCellWidth;
    }

    @Nullable
    protected Range getMergedCell(CellReference cellReference, List<Range> mergedCells) {
        return mergedCells.stream()
                .filter(range -> range.contains(cellReference))
                .findFirst()
                .orElse(null);
    }

    protected double calculateCellHeight(StyleSheet.Font font, String cellValue, int cellWidthInPixels) {
        AttributedString attrStr = createAttributedString(font, cellValue);

        LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), fontRenderContext);
        int nextPos = 0;
        int lineCnt = 0;
        while (measurer.getPosition() < cellValue.length()) {
            nextPos = measurer.nextOffset(cellWidthInPixels);
            lineCnt++;
            measurer.setPosition(nextPos);
        }

        double fontSize = font.getSize() != null ? font.getSize() : DEFAULT_FONT_SIZE;
        double cellHeight = (fontSize * 1.2) * lineCnt; //approximate cell height

        return Math.ceil(cellHeight * 4) / 4f; //ceil to the nearest 0.25
    }

    /**
     * Calculates column width in pixels as a width of the string containing a provided count of {@link #defaultChar}.
     *
     * @param defaultCharCount number of {@link #defaultChar} that can be displayed in column.
     * @return column width in pixels
     */
    protected int getColumnWidthInPixels(double defaultCharCount) {
        double widthInPixels = getDefaultCharsWidth((int) defaultCharCount);

        return (int) Math.floor(widthInPixels);
    }


    protected double calculateCharsCount(double sourceColumnWidth, double defaultCharWidth) {
        //get column width in points including 5 pixels padding
        float columnWidthInPoints = (int) ((sourceColumnWidth * defaultCharWidth + 5.0) / defaultCharWidth * 256.0) / 256.0f;
        int columnWidthInPixels = (int) (((256.0f * columnWidthInPoints + (int) (128.0f / defaultCharWidth)) / 256.0f) * defaultCharWidth);

        //get a count of chars = column width in points without 5 pixels padding
        return (int) ((columnWidthInPixels - 5.0) / defaultCharWidth * 100.0 + 0.5) / 100.0;
    }

    protected double getDefaultCharWidth() {
        return getDefaultCharsWidth(1);
    }

    protected double getDefaultCharsWidth(int count) {
        StyleSheet.Font defaultFont = document.getDefaultFont();

        AttributedString str = createAttributedString(defaultFont, StringUtils.repeat(defaultChar, count));
        TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);

        //string width without additional padding
        return layout.getBounds().getWidth() + layout.getBounds().getX();
    }

    protected int getIndentInPixels(int indent) {
        if (indent == 0) {
            return 0;
        }
        StyleSheet.Font defaultFont = document.getDefaultFont();
        AttributedString str = createAttributedString(defaultFont, StringUtils.repeat(StringUtils.SPACE, indent * 3)); // 1 indent "point" = 3 whitespaces in the default font
        TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
        return (int) Math.ceil(layout.getAdvance());
    }

    protected boolean isMaxHeight(Double rowHeight) {
        return Objects.equals(rowHeight, MAX_ROW_HEIGHT);
    }

    protected AttributedString createAttributedString(StyleSheet.Font font, String value) {
        AttributedString attrStr = new AttributedString(value);
        attrStr.addAttribute(TextAttribute.FAMILY, font.getName());
        attrStr.addAttribute(TextAttribute.SIZE, font.getSize() != null ? font.getSize() : DEFAULT_FONT_SIZE);
        if (font.isBold()) {
            attrStr.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if (font.isItalic()) {
            attrStr.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        if (font.isUnderline()) {
            attrStr.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        return attrStr;
    }

    protected static class CellDataObject {
        protected Cell templateCell;
        protected Cell resultCell;
        protected BandData bandData;
        protected List<String> params;
        protected String resultSheet;

        public CellDataObject(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
            this.templateCell = templateCell;
            this.resultCell = resultCell;
            this.bandData = bandData;
            this.params = params;
        }

        public String getResultSheet() {
            return resultSheet;
        }

        public void setResultSheet(String resultSheet) {
            this.resultSheet = resultSheet;
        }

        public CellReference toCellReference() {
            return new CellReference(resultSheet, resultCell);
        }
    }

}
