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

package io.jmix.reports.yarg.formatters.impl.xls.hints;

import io.jmix.reports.yarg.formatters.impl.xls.HSSFWorkbookHelper;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XlsFontCache;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XlsStyleCache;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XslStyleHelper;
import io.jmix.reports.yarg.structure.BandData;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.regex.Matcher;

/**
 * Apply custom style to target cell
 */
public class CustomCellStyleHint extends AbstractHint {

    private XlsFontCache fontCache;
    private XlsStyleCache styleCache;

    public CustomCellStyleHint(XlsFontCache fontCache, XlsStyleCache styleCache) {
        super("##style=([A-z0-9]+)");
        this.fontCache = fontCache;
        this.styleCache = styleCache;
    }

    @Override
    public void apply() {
        for (DataObject dataObject : data) {
            HSSFCell templateCell = dataObject.templateCell;
            HSSFCell resultCell = dataObject.resultCell;
            BandData bandData = dataObject.bandData;

            HSSFWorkbook resultWorkbook = resultCell.getSheet().getWorkbook();
            HSSFWorkbook templateWorkbook = templateCell.getSheet().getWorkbook();

            String templateCellValue = templateCell.getStringCellValue();

            Matcher matcher = pattern.matcher(templateCellValue);
            if (matcher.find()) {
                String paramName = matcher.group(1);
                String styleName = (String) bandData.getParameterValue(paramName);
                if (styleName == null) continue;

                HSSFCellStyle cellStyle = styleCache.getStyleByName(styleName);
                if (cellStyle == null) continue;

                HSSFCellStyle resultStyle = styleCache.getNamedCachedStyle(cellStyle);

                if (resultStyle == null) {
                    HSSFCellStyle newStyle = resultWorkbook.createCellStyle();
                    // color
                    newStyle.setFillBackgroundColor(cellStyle.getFillBackgroundColor());
                    newStyle.setFillForegroundColor(cellStyle.getFillForegroundColor());
                    newStyle.setFillPattern(cellStyle.getFillPattern());

                    // borders
                    newStyle.setBorderLeft(cellStyle.getBorderLeft());
                    newStyle.setBorderRight(cellStyle.getBorderRight());
                    newStyle.setBorderTop(cellStyle.getBorderTop());
                    newStyle.setBorderBottom(cellStyle.getBorderBottom());

                    // border colors
                    newStyle.setLeftBorderColor(cellStyle.getLeftBorderColor());
                    newStyle.setRightBorderColor(cellStyle.getRightBorderColor());
                    newStyle.setBottomBorderColor(cellStyle.getBottomBorderColor());
                    newStyle.setTopBorderColor(cellStyle.getTopBorderColor());

                    // alignment
                    newStyle.setAlignment(cellStyle.getAlignment());
                    newStyle.setVerticalAlignment(cellStyle.getVerticalAlignment());
                    // misc
                    DataFormat dataFormat = resultWorkbook.getCreationHelper().createDataFormat();
                    newStyle.setDataFormat(dataFormat.getFormat(cellStyle.getDataFormatString()));
                    newStyle.setHidden(cellStyle.getHidden());
                    newStyle.setLocked(cellStyle.getLocked());
                    newStyle.setIndention(cellStyle.getIndention());
                    newStyle.setRotation(cellStyle.getRotation());
                    newStyle.setWrapText(cellStyle.getWrapText());
                    // font
                    HSSFFont cellFont = cellStyle.getFont(templateWorkbook);
                    HSSFFont newFont = fontCache.getFontByTemplate(cellFont);

                    if (newFont == null) {
                        newFont = resultWorkbook.createFont();

                        newFont.setFontName(cellFont.getFontName());
                        newFont.setItalic(cellFont.getItalic());
                        newFont.setStrikeout(cellFont.getStrikeout());
                        newFont.setTypeOffset(cellFont.getTypeOffset());
                        newFont.setBold(cellFont.getBold());
                        newFont.setCharSet(cellFont.getCharSet());
                        newFont.setColor(cellFont.getColor());
                        newFont.setUnderline(cellFont.getUnderline());
                        newFont.setFontHeight(cellFont.getFontHeight());
                        newFont.setFontHeightInPoints(cellFont.getFontHeightInPoints());
                        fontCache.addCachedFont(cellFont, newFont);
                    }
                    newStyle.setFont(newFont);

                    resultStyle = newStyle;
                    styleCache.addCachedNamedStyle(cellStyle, resultStyle);
                }

                fixNeighbourCellBorders(cellStyle, resultCell);

                resultCell.setCellStyle(resultStyle);

                Sheet sheet = resultCell.getSheet();
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                    if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {

                        int firstRow = mergedRegion.getFirstRow();
                        int lastRow = mergedRegion.getLastRow();
                        int firstCol = mergedRegion.getFirstColumn();
                        int lastCol = mergedRegion.getLastColumn();

                        for (int row = firstRow; row <= lastRow; row++)
                            for (int col = firstCol; col <= lastCol; col++)
                                sheet.getRow(row).getCell(col).setCellStyle(resultStyle);

                        // cell includes only in one merged region
                        break;
                    }
                }
            }
        }
    }

    private void fixNeighbourCellBorders(HSSFCellStyle cellStyle, HSSFCell resultCell) {
        HSSFSheet sheet = resultCell.getRow().getSheet();
        // disable neighboring cells border
        int columnIndex = resultCell.getColumnIndex();
        int rowIndex = resultCell.getRowIndex();
        // fix left border
        fixLeftBorder(cellStyle, sheet, columnIndex, resultCell);

        // fix right border
        fixRightBorder(cellStyle, sheet, columnIndex, resultCell);

        // fix up border
        fixUpBorder(cellStyle, sheet, columnIndex, rowIndex, resultCell);

        // fix down border
        fixDownBorder(cellStyle, sheet, columnIndex, rowIndex, resultCell);
    }

    private void fixLeftBorder(HSSFCellStyle cellStyle, HSSFSheet sheet, int columnIndex, HSSFCell resultCell) {
        if (columnIndex > 1) {
            fixLeftCell(sheet, resultCell.getRowIndex(), columnIndex - 1, cellStyle);
            // fix merged left border
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                    int firstRow = mergedRegion.getFirstRow();
                    int lastRow = mergedRegion.getLastRow();

                    for (int leftIndex = firstRow; leftIndex <= lastRow; leftIndex++) {
                        fixLeftCell(sheet, leftIndex, columnIndex - 1, cellStyle);
                    }
                    break;
                }
            }
        }
    }

    private void fixLeftCell(HSSFSheet sheet, int rowIndex, int columnIndex, HSSFCellStyle cellStyle) {
        HSSFCell leftCell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (leftCell != null) {
            HSSFCellStyle leftCellStyle = leftCell.getCellStyle();
            if (leftCellStyle.getBorderRight() != cellStyle.getBorderLeft() ||
                    leftCellStyle.getRightBorderColor() != cellStyle.getLeftBorderColor()) {
                HSSFCellStyle draftLeftStyle = HSSFWorkbookHelper.createDetachedCellStyle(sheet.getWorkbook());
                XslStyleHelper.cloneStyleRelations(leftCellStyle, draftLeftStyle);
                draftLeftStyle.setBorderRight(cellStyle.getBorderLeft());
                draftLeftStyle.setRightBorderColor(cellStyle.getLeftBorderColor());
                HSSFCellStyle newLeftStyle = styleCache.getCellStyleByTemplate(draftLeftStyle);
                if (newLeftStyle == null) {
                    newLeftStyle = HSSFWorkbookHelper.adoptDetachedCellStyle(sheet.getWorkbook(), draftLeftStyle);
                    styleCache.processCellStyle(newLeftStyle);
                }

                leftCell.setCellStyle(newLeftStyle);
            }
        }
    }

    private void fixRightBorder(HSSFCellStyle cellStyle, HSSFSheet sheet, int columnIndex, HSSFCell resultCell) {
        fixRightCell(sheet, resultCell.getRowIndex(), columnIndex + 1, cellStyle);
        // fix merged right border
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                int firstRow = mergedRegion.getFirstRow();
                int lastRow = mergedRegion.getLastRow();
                int regionWidth = mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1;

                for (int rightIndex = firstRow; rightIndex <= lastRow; rightIndex++) {
                    fixRightCell(sheet, rightIndex, columnIndex + regionWidth, cellStyle);
                }
                break;
            }
        }
    }

    private void fixRightCell(HSSFSheet sheet, int rowIndex, int columnIndex, HSSFCellStyle cellStyle) {
        HSSFCell rightCell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (rightCell != null) {
            HSSFCellStyle rightCellStyle = rightCell.getCellStyle();

            if (rightCellStyle.getBorderLeft() != cellStyle.getBorderRight() ||
                    rightCellStyle.getLeftBorderColor() != cellStyle.getRightBorderColor()) {
                HSSFCellStyle draftRightStyle = HSSFWorkbookHelper.createDetachedCellStyle(sheet.getWorkbook());
                XslStyleHelper.cloneStyleRelations(rightCellStyle, draftRightStyle);
                draftRightStyle.setBorderLeft(cellStyle.getBorderRight());
                draftRightStyle.setLeftBorderColor(cellStyle.getRightBorderColor());

                HSSFCellStyle newRightStyle = styleCache.getCellStyleByTemplate(draftRightStyle);
                if (newRightStyle == null) {
                    newRightStyle = HSSFWorkbookHelper.adoptDetachedCellStyle(sheet.getWorkbook(), draftRightStyle);
                    styleCache.processCellStyle(newRightStyle);
                }

                rightCell.setCellStyle(newRightStyle);
            }
        }
    }

    private void fixUpBorder(HSSFCellStyle cellStyle, HSSFSheet sheet, int columnIndex, int rowIndex, HSSFCell resultCell) {
        if (rowIndex > 0) {
            // fix simple up border
            fixUpCell(sheet, rowIndex - 1, columnIndex, cellStyle);
            // fix merged up border
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                    int firstColumn = mergedRegion.getFirstColumn();
                    int lastColumn = mergedRegion.getLastColumn();

                    for (int upIndex = firstColumn; upIndex <= lastColumn; upIndex++) {
                        fixUpCell(sheet, rowIndex - 1, upIndex, cellStyle);
                    }
                    break;
                }
            }
        }
    }

    private void fixUpCell(HSSFSheet sheet, int rowIndex, int columnIndex, HSSFCellStyle cellStyle) {
        HSSFCell upCell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (upCell != null) {
            HSSFCellStyle upCellStyle = upCell.getCellStyle();

            if (upCellStyle.getBorderBottom() != cellStyle.getBorderTop() ||
                    upCellStyle.getBottomBorderColor() != cellStyle.getTopBorderColor()) {
                HSSFCellStyle draftUpStyle = HSSFWorkbookHelper.createDetachedCellStyle(sheet.getWorkbook());
                XslStyleHelper.cloneStyleRelations(upCellStyle, draftUpStyle);
                draftUpStyle.setBorderBottom(cellStyle.getBorderTop());
                draftUpStyle.setBottomBorderColor(cellStyle.getTopBorderColor());

                HSSFCellStyle newUpStyle = styleCache.getCellStyleByTemplate(draftUpStyle);
                if (newUpStyle == null) {
                    newUpStyle = HSSFWorkbookHelper.adoptDetachedCellStyle(sheet.getWorkbook(), draftUpStyle);
                    styleCache.processCellStyle(newUpStyle);
                }


                upCell.setCellStyle(newUpStyle);
            }
        }
    }

    private void fixDownBorder(HSSFCellStyle cellStyle, HSSFSheet sheet, int columnIndex, int rowIndex, HSSFCell resultCell) {
        // fix simple down border
        fixDownCell(sheet, rowIndex + 1, columnIndex, cellStyle);
        // fix merged down border
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(resultCell.getRowIndex(), resultCell.getColumnIndex())) {
                int firstColumn = mergedRegion.getFirstColumn();
                int lastColumn = mergedRegion.getLastColumn();
                int regionHeight = mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1;

                for (int downIndex = firstColumn; downIndex <= lastColumn; downIndex++) {
                    fixDownCell(sheet, rowIndex + regionHeight, downIndex, cellStyle);
                }
                break;
            }
        }
    }

    private void fixDownCell(HSSFSheet sheet, int rowIndex, int columnIndex, HSSFCellStyle cellStyle) {
        HSSFRow nextRow = sheet.getRow(rowIndex);
        if (nextRow != null) {
            HSSFCell downCell = nextRow.getCell(columnIndex);
            if (downCell != null) {
                HSSFCellStyle downCellStyle = downCell.getCellStyle();

                if (downCellStyle.getBorderTop() != cellStyle.getBorderBottom() ||
                        downCellStyle.getTopBorderColor() != cellStyle.getBottomBorderColor()) {
                    HSSFCellStyle draftDownStyle = HSSFWorkbookHelper.createDetachedCellStyle(sheet.getWorkbook());
                    XslStyleHelper.cloneStyleRelations(downCellStyle, draftDownStyle);
                    draftDownStyle.setBorderTop(cellStyle.getBorderBottom());
                    draftDownStyle.setTopBorderColor(cellStyle.getBottomBorderColor());

                    HSSFCellStyle newDownStyle = styleCache.getCellStyleByTemplate(draftDownStyle);
                    if (newDownStyle == null) {
                        newDownStyle = HSSFWorkbookHelper.adoptDetachedCellStyle(sheet.getWorkbook(), draftDownStyle);
                        styleCache.processCellStyle(newDownStyle);
                    }

                    downCell.setCellStyle(newDownStyle);
                }
            }
        }
    }
}