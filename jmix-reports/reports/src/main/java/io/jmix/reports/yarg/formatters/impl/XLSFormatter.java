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

package io.jmix.reports.yarg.formatters.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.jmix.reports.yarg.formatters.impl.xls.Area;
import io.jmix.reports.yarg.formatters.impl.xls.AreaDependencyManager;
import io.jmix.reports.yarg.formatters.impl.xls.Cell;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XlsFontCache;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XlsStyleCache;
import io.jmix.reports.yarg.formatters.impl.xls.caches.XslStyleHelper;
import io.jmix.reports.yarg.formatters.impl.xls.hints.*;
import io.jmix.reports.yarg.formatters.impl.xlsx.Range;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.UnsupportedFormatException;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static io.jmix.reports.yarg.formatters.impl.xls.HSSFCellHelper.getCellFromReference;
import static io.jmix.reports.yarg.formatters.impl.xls.HSSFPicturesHelper.getAllAnchors;
import static io.jmix.reports.yarg.formatters.impl.xls.HSSFRangeHelper.*;

/**
 * Document formatter for '.xls' file types
 */

//todo : we need to rewrite logic in the way similar to XlsxFormatter (store rendered ranges in memory) - use bandsToResultRanges etc.
public class XLSFormatter extends AbstractFormatter {
    protected static final String DYNAMIC_HEIGHT_STYLE = "styleWithoutHeight";

    protected HSSFWorkbook templateWorkbook;
    protected HSSFWorkbook resultWorkbook;

    protected HSSFSheet currentTemplateSheet = null;

    protected XlsFontCache fontCache = new XlsFontCache();
    protected XlsStyleCache styleCache = new XlsStyleCache();

    protected int rownum = 0;
    protected int colnum = 0;
    protected int rowsAddedByVerticalBand = 0;
    protected int rowsAddedByHorizontalBand = 0;

    protected Map<String, List<SheetRange>> mergeRegionsForRangeNames = new HashMap<>();
    protected Map<HSSFSheet, HSSFSheet> templateToResultSheetsMapping = new HashMap<>();
    protected Map<String, Bounds> templateBounds = new HashMap<>();

    protected AreaDependencyManager areaDependencyManager = new AreaDependencyManager();
    protected Map<Area, List<Area>> areasDependency = areaDependencyManager.getAreasDependency();

    protected List<Integer> orderedPicturesId = new ArrayList<>();
    protected Map<String, EscherAggregate> sheetToEscherAggregate = new HashMap<>();

    protected Map<HSSFSheet, HSSFPatriarch> drawingPatriarchsMap = new HashMap<>();
    protected List<XlsHint> hints = new ArrayList<>();

    protected DocumentConverter documentConverter;

    protected BiMap<BandData, Range> bandsToResultRanges = HashBiMap.create();

    public XLSFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.xls);
        supportedOutputTypes.add(ReportOutputType.pdf);

        hints.add(new CustomCellStyleHint(fontCache, styleCache));
        hints.add(new CopyColumnWidthHint());
        hints.add(new AutoWidthHint());
        hints.add(new CustomWidthHint());
    }

    public void setDocumentConverter(DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    @Override
    public void renderDocument() {
        initWorkbook();

        processDocument();

        applyHints();

        outputDocument();
    }

    protected void initWorkbook() {
        try {
            templateWorkbook = new HSSFWorkbook(reportTemplate.getDocumentContent());
            resultWorkbook = new HSSFWorkbook(reportTemplate.getDocumentContent());
        } catch (IOException e) {
            throw wrapWithReportingException("An error occurred while parsing xls template " + reportTemplate.getDocumentName(), e);
        }

        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);
            templateToResultSheetsMapping.put(templateSheet, resultSheet);

            initMergeRegions(templateSheet);
            copyCharts(resultSheet);
            removeMergedRegions(resultSheet);
            cleanupCells(resultSheet);
        }

        copyPicturesToResultWorkbook();

        initNamedStyleCache();
    }

    protected void initNamedStyleCache() {
        for (short i = 0; i < resultWorkbook.getNumCellStyles(); i++) {
            HSSFCellStyle cellStyle = resultWorkbook.getCellStyleAt(i);
            if (StringUtils.isNotBlank(cellStyle.getUserStyleName())) {
                styleCache.addNamedStyle(cellStyle);
            }
        }
    }

    protected void processDocument() {
        for (BandData childBand : rootBand.getChildrenList()) {
            checkThreadInterrupted();
            writeBand(childBand);
        }

        updateFormulas();
        copyPictures();
    }

    protected void applyHints() {
        for (XlsHint option : hints) {
            option.apply();
        }
    }

    protected void outputDocument() {
        checkThreadInterrupted();
        if (ReportOutputType.xls.equals(outputType)) {
            try {
                resultWorkbook.write(outputStream);
            } catch (Exception e) {
                throw wrapWithReportingException("An error occurred while writing result to file.", e);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        } else if (ReportOutputType.pdf.equals(outputType)) {
            if (documentConverter != null) {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resultWorkbook.write(stream);
                    documentConverter.convertToPdf(DocumentConverter.FileType.SPREADSHEET, stream.toByteArray(), outputStream);
                } catch (IOException e) {
                    throw wrapWithReportingException("An error occurred while converting xls to pdf.", e);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            } else {
                throw new UnsupportedFormatException("Could not convert xls files to pdf because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
        }
    }

    protected void copyPicturesToResultWorkbook() {
        List<HSSFPictureData> allPictures = templateWorkbook.getAllPictures();
        for (HSSFPictureData allPicture : allPictures) {
            int i = resultWorkbook.addPicture(allPicture.getData(), Workbook.PICTURE_TYPE_JPEG);
            orderedPicturesId.add(i);
        }
    }

    protected void removeMergedRegions(HSSFSheet resultSheet) {
        for (int i = 0, size = resultSheet.getNumMergedRegions(); i < size; i++) {
            resultSheet.removeMergedRegion(0);//each time we remove region - they "move to left" so region 1 become region 0
        }
    }

    protected void cleanupCells(HSSFSheet resultSheet) {
        for (int i = resultSheet.getFirstRowNum(); i <= resultSheet.getLastRowNum(); i++) {
            HSSFRow row = resultSheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    HSSFCell cell = row.getCell(j);
                    if (cell != null) {
                        row.removeCell(cell);
                    }
                }
            }
        }
    }

    protected void copyCharts(HSSFSheet resultSheet) {
        HSSFChart[] sheetCharts = HSSFChart.getSheetCharts(resultSheet);
        if (sheetCharts == null || sheetCharts.length == 0) {//workaround for charts. If there is charts on sheet - we can not use getDrawPatriarch as it removes all charts (because does not support them)
            HSSFPatriarch drawingPatriarch = resultSheet.createDrawingPatriarch();
            if (drawingPatriarch == null) {
                drawingPatriarch = resultSheet.createDrawingPatriarch();
            }

            drawingPatriarchsMap.put(resultSheet, drawingPatriarch);
        }
    }

    protected void updateFormulas() {
        for (Map.Entry<Area, List<Area>> entry : areasDependency.entrySet()) {
            Area original = entry.getKey();

            for (Area dependent : entry.getValue()) {
                updateFormulas(original, dependent);
            }
        }
    }

    protected void copyPictures() {
        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);

            copyPicturesFromTemplateToResult(templateSheet, resultSheet);
        }
    }

    protected void writeBand(BandData band) {
        String rangeName = band.getName();
        try {
            HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, rangeName);

            if (templateSheet != currentTemplateSheet) { //todo: reimplement. store rownum for each sheet.
                currentTemplateSheet = templateSheet;
                rownum = 0;
            }

            HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

            if (BandOrientation.HORIZONTAL == band.getOrientation()) {
                colnum = 0;
                writeHorizontalBand(band, templateSheet, resultSheet);
            } else {
                writeVerticalBand(band, templateSheet, resultSheet);
            }
        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            throw wrapWithReportingException(String.format("An error occurred while rendering band [%s]", rangeName), e);
        }
    }

    /**
     * Method writes horizontal band
     * Note: Only one band for row is supported. Now we think that many bands for row aren't usable.
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    protected void writeHorizontalBand(BandData band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);
        if (templateRange == null) {
            throw wrapWithReportingException(String.format("No such named range in xls file: %s", rangeName));
        }
        CellReference[] crefs = templateRange.getAllReferencedCells();

        CellReference topLeft, bottomRight;
        AreaReference resultRange;

        int rowsAddedByHorizontalBandBackup = rowsAddedByHorizontalBand;
        int rownumBackup = rownum;

        if (crefs != null) {
            addRangeBounds(band, crefs);

            ArrayList<HSSFRow> resultRows = new ArrayList<>();

            int currentRowNum = -1;
            int currentRowCount = -1;
            int currentColumnCount = 0;
            int offset = 0;

            topLeft = new CellReference(rownum + rowsAddedByHorizontalBand, 0);
            // no child bands - merge regions now
            if (band.getChildrenList().isEmpty()) {
                copyMergeRegions(resultSheet, rangeName, rownum + rowsAddedByHorizontalBand,
                        getCellFromReference(crefs[0], templateSheet).getColumnIndex());
            }

            for (CellReference cellRef : crefs) {
                HSSFCell templateCell = getCellFromReference(cellRef, templateSheet);
                HSSFRow resultRow;
                if (templateCell.getRowIndex() != currentRowNum) { //create new row
                    resultRow = resultSheet.createRow(rownum + rowsAddedByHorizontalBand);
                    copyPageBreaks(templateSheet, resultSheet, templateCell.getRowIndex(), resultRow.getRowNum());
                    rowsAddedByHorizontalBand += 1;

                    //todo move to options
                    if (templateCell.getCellStyle().getParentStyle() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName().equals(DYNAMIC_HEIGHT_STYLE)
                            ) {
                        //resultRow.setHeight(templateCell.getRow().getHeight());
                    } else {
                        resultRow.setHeight(templateCell.getRow().getHeight());
                    }
                    resultRows.add(resultRow);

                    currentRowNum = templateCell.getRowIndex();
                    currentRowCount++;
                    currentColumnCount = 0;
                    offset = templateCell.getColumnIndex();
                } else {                                          // or write cell to current row
                    resultRow = resultRows.get(currentRowCount);
                    currentColumnCount++;
                }

                copyCellFromTemplate(templateCell, resultRow, offset + currentColumnCount, band);
            }

            bottomRight = new CellReference(rownum + rowsAddedByHorizontalBand - 1, offset + currentColumnCount);
            resultRange = new AreaReference(topLeft, bottomRight, SpreadsheetVersion.EXCEL97);

            areaDependencyManager.addDependency(new Area(band.getName(), Area.AreaAlign.HORIZONTAL, templateRange),
                    new Area(band.getName(), Area.AreaAlign.HORIZONTAL, resultRange));
            bandsToResultRanges.put(band, new Range(resultSheet.getSheetName(),
                    resultRange.getFirstCell().getCol() + 1, resultRange.getFirstCell().getRow() + 1,
                    resultRange.getLastCell().getCol() + 1, resultRange.getLastCell().getRow() + 1
            ));
        }

        for (BandData child : band.getChildrenList()) {
            writeBand(child);
        }

        // scheduled merge regions
        if (!band.getChildrenList().isEmpty() && crefs != null) {
            copyMergeRegions(resultSheet, rangeName, rownumBackup + rowsAddedByHorizontalBandBackup,
                    getCellFromReference(crefs[0], templateSheet).getColumnIndex());
        }

        rownum += rowsAddedByHorizontalBand;
        rowsAddedByHorizontalBand = 0;
        rownum += rowsAddedByVerticalBand;
        rowsAddedByVerticalBand = 0;
    }

    /**
     * Method writes vertical band
     * Note: no child support for vertical band ;)
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    protected void writeVerticalBand(BandData band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        CellReference[] crefs = getRangeContent(templateWorkbook, rangeName);

        Set<Integer> addedRowNumbers = new HashSet<>();

        if (crefs != null) {
            addRangeBounds(band, crefs);

            Bounds thisBounds = templateBounds.get(band.getName());
            Bounds parentBounds = templateBounds.get(band.getParentBand().getName());
            Range parentRange = bandsToResultRanges.get(band.getParentBand());

            int localRowNum = parentBounds != null && parentRange != null ?
                    parentRange.getFirstRow() - 1 + thisBounds.row0 - parentBounds.row0 :
                    rownum;

            colnum = colnum == 0 ? getCellFromReference(crefs[0], templateSheet).getColumnIndex() : colnum;
            copyMergeRegions(resultSheet, rangeName, localRowNum, colnum);

            int firstRow = crefs[0].getRow();
            int firstColumn = crefs[0].getCol();

            for (CellReference cref : crefs) {//create necessary rows
                int currentRow = cref.getRow();
                final int rowOffset = currentRow - firstRow;
                if (!rowExists(resultSheet, localRowNum + rowOffset)) {
                    HSSFRow resultRow = resultSheet.createRow(localRowNum + rowOffset);
                    copyPageBreaks(templateSheet, resultSheet, cref.getRow(), resultRow.getRowNum());
                }
                addedRowNumbers.add(cref.getRow());
            }

            CellReference topLeft = null;
            CellReference bottomRight = null;
            for (CellReference cref : crefs) {
                int currentRow = cref.getRow();
                int currentColumn = cref.getCol();
                final int rowOffset = currentRow - firstRow;
                final int columnOffset = currentColumn - firstColumn;

                HSSFCell templateCell = getCellFromReference(cref, templateSheet);
                resultSheet.setColumnWidth(colnum + columnOffset, templateSheet.getColumnWidth(templateCell.getColumnIndex()));
                HSSFCell resultCell = copyCellFromTemplate(templateCell, resultSheet.getRow(localRowNum + rowOffset), colnum + columnOffset, band);
                if (topLeft == null) {
                    topLeft = new CellReference(resultCell);
                }
                bottomRight = new CellReference(resultCell);
            }

            colnum += crefs[crefs.length - 1].getCol() - firstColumn + 1;

            AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);
            AreaReference resultRange = new AreaReference(topLeft, bottomRight, SpreadsheetVersion.EXCEL97);
            areaDependencyManager.addDependency(new Area(band.getName(), Area.AreaAlign.VERTICAL, templateRange),
                    new Area(band.getName(), Area.AreaAlign.VERTICAL, resultRange));
            bandsToResultRanges.put(band, new Range(resultSheet.getSheetName(),
                    resultRange.getFirstCell().getCol() + 1, resultRange.getFirstCell().getRow() + 1,
                    resultRange.getLastCell().getCol() + 1, resultRange.getLastCell().getRow() + 1
            ));
        }

        //for first level vertical bands we should increase rownum by number of rows added by vertical band
        //nested vertical bands do not add rows, they use parent space
        if (BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName())) {
            List<BandData> sameBands = band.getParentBand().getChildrenByName(band.getName());
            if (sameBands.size() > 0 && sameBands.get(sameBands.size() - 1) == band) {//check if this vertical band is last vertical band with same name
                rownum += addedRowNumbers.size();
                //      rowsAddedByVerticalBand = 0;
            }
        }
    }

    /**
     * Method creates mapping [rangeName : List&lt;CellRangeAddress&gt;].
     * List contains all merge regions for this named range.
     * Attention: if merged regions writes wrong - look on methods isMergeRegionInsideNamedRange or isNamedRangeInsideMergeRegion
     * todo: how to recognize if merge region must be copied with named range
     *
     * @param currentSheet Sheet which contains merge regions
     */
    protected void initMergeRegions(HSSFSheet currentSheet) {
        int rangeNumber = templateWorkbook.getNumberOfNames();
        for (int i = 0; i < rangeNumber; i++) {
            HSSFName aNamedRange = templateWorkbook.getNameAt(i);

            String refersToFormula = aNamedRange.getRefersToFormula();
            if (!AreaReference.isContiguous(refersToFormula)) {
                continue;
            }

            AreaReference aref = new AreaReference(refersToFormula, SpreadsheetVersion.EXCEL97);
            Integer rangeFirstRow = aref.getFirstCell().getRow();
            Integer rangeFirstColumn = (int) aref.getFirstCell().getCol();
            Integer rangeLastRow = aref.getLastCell().getRow();
            Integer rangeLastColumn = (int) aref.getLastCell().getCol();

            for (int j = 0; j < currentSheet.getNumMergedRegions(); j++) {
                CellRangeAddress mergedRegion = currentSheet.getMergedRegion(j);
                if (mergedRegion != null) {
                    Integer regionFirstRow = mergedRegion.getFirstRow();
                    Integer regionFirstColumn = mergedRegion.getFirstColumn();
                    Integer regionLastRow = mergedRegion.getLastRow();
                    Integer regionLastColumn = mergedRegion.getLastColumn();

                    boolean mergedInsideNamed = isMergeRegionInsideNamedRange(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    boolean namedInsideMerged = isNamedRangeInsideMergeRegion(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    if (mergedInsideNamed || namedInsideMerged) {
                        String name = aNamedRange.getNameName();
                        SheetRange sheetRange = new SheetRange(mergedRegion, currentSheet.getSheetName());
                        if (mergeRegionsForRangeNames.get(name) == null) {
                            ArrayList<SheetRange> list = new ArrayList<>();
                            list.add(sheetRange);
                            mergeRegionsForRangeNames.put(name, list);
                        } else {
                            mergeRegionsForRangeNames.get(name).add(sheetRange);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create new merge regions in result sheet identically to range's merge regions from template.
     * Not support copy of frames and rules
     *
     * @param resultSheet            - result sheet
     * @param rangeName              - range name
     * @param firstTargetRangeRow    - first column of target range
     * @param firstTargetRangeColumn - first column of target range
     */
    protected void copyMergeRegions(HSSFSheet resultSheet, String rangeName,
                                    int firstTargetRangeRow, int firstTargetRangeColumn) {
        int rangeNameIdx = templateWorkbook.getNameIndex(rangeName);
        if (rangeNameIdx == -1) return;

        HSSFName aNamedRange = templateWorkbook.getNameAt(rangeNameIdx);
        AreaReference aref = new AreaReference(aNamedRange.getRefersToFormula(), SpreadsheetVersion.EXCEL97);
        int column = aref.getFirstCell().getCol();
        int row = aref.getFirstCell().getRow();

        List<SheetRange> regionsList = mergeRegionsForRangeNames.get(rangeName);
        if (regionsList != null)
            for (SheetRange sheetRange : regionsList) {
                if (resultSheet.getSheetName().equals(sheetRange.getSheetName())) {
                    CellRangeAddress cra = sheetRange.getCellRangeAddress();
                    if (cra != null) {
                        int regionHeight = cra.getLastRow() - cra.getFirstRow() + 1;
                        int regionWidth = cra.getLastColumn() - cra.getFirstColumn() + 1;

                        int regionVOffset = cra.getFirstRow() - row;
                        int regionHOffset = cra.getFirstColumn() - column;

                        CellRangeAddress newRegion = cra.copy();
                        newRegion.setFirstColumn(regionHOffset + firstTargetRangeColumn);
                        newRegion.setLastColumn(regionHOffset + regionWidth - 1 + firstTargetRangeColumn);

                        newRegion.setFirstRow(regionVOffset + firstTargetRangeRow);
                        newRegion.setLastRow(regionVOffset + regionHeight - 1 + firstTargetRangeRow);

                        boolean skipRegion = false;

                        for (int mergedIndex = 0; mergedIndex < resultSheet.getNumMergedRegions(); mergedIndex++) {
                            CellRangeAddress mergedRegion = resultSheet.getMergedRegion(mergedIndex);

                            if (!intersects(newRegion, mergedRegion)) {
                                continue;
                            }

                            skipRegion = true;
                        }

                        if (!skipRegion) {
                            resultSheet.addMergedRegion(newRegion);
                        }
                    }
                }
            }
    }

    protected boolean intersects(CellRangeAddress x, CellRangeAddress y) {
        return (x.getFirstColumn() <= y.getLastColumn() &&
                x.getLastColumn() >= y.getFirstColumn() &&
                x.getLastRow() >= y.getFirstRow() &&
                x.getFirstRow() <= y.getLastRow())
                // or
                || (y.getFirstColumn() <= x.getLastColumn() &&
                y.getLastColumn() >= x.getFirstColumn() &&
                y.getLastRow() >= x.getFirstRow() &&
                y.getFirstRow() <= x.getLastRow());
    }

    /**
     * copies template cell to result row into result column. Fills this cell with data from band
     *
     * @param templateCell - template cell
     * @param resultRow    - result row
     * @param resultColumn - result column
     * @param band         - band
     */
    private HSSFCell copyCellFromTemplate(HSSFCell templateCell, HSSFRow resultRow, int resultColumn, BandData band) {
        checkThreadInterrupted();
        if (templateCell == null) return null;

        HSSFCell resultCell = resultRow.createCell(resultColumn);

        HSSFCellStyle templateStyle = templateCell.getCellStyle();
        HSSFCellStyle resultStyle = copyCellStyle(templateStyle);
        resultCell.setCellStyle(resultStyle);

        String templateCellValue = "";
        CellType cellType = templateCell.getCellType();

        if (cellType != CellType.FORMULA && cellType != CellType.NUMERIC) {
            HSSFRichTextString richStringCellValue = templateCell.getRichStringCellValue();
            templateCellValue = richStringCellValue != null ? richStringCellValue.getString() : "";

            templateCellValue = extractStyles(templateCell, resultCell, templateCellValue, band);
        }

        if (cellType == CellType.STRING && containsJustOneAlias(templateCellValue)) {
            updateValueCell(rootBand, band, templateCellValue, resultCell,
                    drawingPatriarchsMap.get(resultCell.getSheet()));
        } else {
            String cellValue = inlineBandDataToCellString(templateCell, templateCellValue, band);
            setValueToCell(resultCell, cellValue, cellType);
        }

        return resultCell;
    }

    /**
     * Copies template cell to result cell and fills it with bandData data
     *
     * @param bandData          - bandData
     * @param templateCellValue - template cell value
     * @param resultCell        - result cell
     */
    protected void updateValueCell(BandData rootBand, BandData bandData, String templateCellValue, HSSFCell resultCell, HSSFPatriarch patriarch) {
        String parameterName = templateCellValue;
        parameterName = unwrapParameterName(parameterName);
        String fullParameterName = bandData.getName() + "." + parameterName;

        if (StringUtils.isEmpty(parameterName)) return;

        if (!bandData.getData().containsKey(parameterName)) {
            resultCell.setCellValue((String) null);
            return;
        }

        Object value = bandData.getData().get(parameterName);

        if (value == null) {
            resultCell.setCellType(CellType.BLANK);
            return;
        }

        String formatString = getFormatString(parameterName, fullParameterName);
        InlinerAndMatcher inlinerAndMatcher = getContentInlinerForFormat(formatString);
        if (inlinerAndMatcher != null) {
            inlinerAndMatcher.contentInliner.inlineToXls(patriarch, resultCell, value, inlinerAndMatcher.matcher);
            return;
        }

        if (formatString != null) {
            resultCell.setCellValue(new HSSFRichTextString(formatValue(value, parameterName, fullParameterName)));
        } else if (value instanceof Number) {
            resultCell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            resultCell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            resultCell.setCellValue((Date) value);
        } else {
            resultCell.setCellValue(new HSSFRichTextString(formatValue(value, parameterName, fullParameterName)));
        }
    }

    protected void setValueToCell(HSSFCell resultCell, String cellValue, CellType cellType) {
        if (StringUtils.isNotEmpty(cellValue)) {
            switch (cellType) {
                case FORMULA:
                    resultCell.setCellFormula(cellValue);
                    break;
                case STRING:
                    resultCell.setCellValue(new HSSFRichTextString(cellValue));
                    break;
                default:
                    resultCell.setCellValue(cellValue);
                    break;
            }

        } else {
            resultCell.setCellType(CellType.BLANK);
        }
    }

    protected String inlineBandDataToCellString(HSSFCell cell, String templateCellValue, BandData band) {
        String resultStr = "";
        if (cell.getCellType() == CellType.STRING) {
            if (templateCellValue != null) resultStr = templateCellValue;
        } else {
            if (cell.toString() != null) resultStr = cell.toString();
        }

        if (StringUtils.isNotEmpty(resultStr)) return insertBandDataToString(band, resultStr);

        return "";
    }

    /**
     * This method adds range bounds to cache. Key is bandName
     *
     * @param band  - band
     * @param crefs - range
     */
    protected void addRangeBounds(BandData band, CellReference[] crefs) {
        if (templateBounds.containsKey(band.getName()))
            return;
        Bounds bounds = new Bounds(crefs[0].getRow(), crefs[0].getCol(), crefs[crefs.length - 1].getRow(), crefs[crefs.length - 1].getCol());
        templateBounds.put(band.getName(), bounds);
    }

    protected void updateFormulas(Area templateArea, Area dependentResultArea) {
        HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, templateArea.getName());
        HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

        AreaReference area = dependentResultArea.toAreaReference();
        for (CellReference cell : area.getAllReferencedCells()) {
            HSSFCell resultCell = getCellFromReference(cell, resultSheet);

            if (resultCell.getCellType() == CellType.FORMULA) {
                Ptg[] ptgs = HSSFFormulaParser.parse(resultCell.getCellFormula(), resultWorkbook);

                for (Ptg ptg : ptgs) {
                    if (ptg instanceof AreaPtg) {
                        areaDependencyManager.updateAreaPtg(templateArea, dependentResultArea, (AreaPtg) ptg);
                    } else if (ptg instanceof RefPtg) {
                        areaDependencyManager.updateRefPtg(templateArea, dependentResultArea, (RefPtg) ptg);
                    }
                }

                String calculatedFormula = HSSFFormulaParser.toFormulaString(templateWorkbook, ptgs);
                resultCell.setCellFormula(calculatedFormula);
            }
        }
    }

    protected String extractStyles(HSSFCell templateCell, HSSFCell resultCell, String templateCellValue, BandData bandData) {
        for (XlsHint hint : hints) {
            XlsHint.CheckResult check = hint.check(templateCellValue);
            if (check.result) {
                templateCellValue = check.cellValue;
                hint.add(templateCell, resultCell, bandData);
            }
        }

        templateCellValue = StringUtils.stripEnd(templateCellValue, null);

        return templateCellValue;
    }

    protected HSSFCellStyle copyCellStyle(HSSFCellStyle templateStyle) {
        HSSFCellStyle style = styleCache.getCellStyleByTemplate(templateStyle);

        if (style == null) {
            HSSFCellStyle newStyle = resultWorkbook.createCellStyle();

            XslStyleHelper.cloneStyleRelations(templateStyle, newStyle);
            HSSFFont templateFont = templateStyle.getFont(templateWorkbook);
            HSSFFont font = fontCache.getFontByTemplate(templateFont);
            if (font != null)
                newStyle.setFont(font);
            else {
                XslStyleHelper.cloneFont(templateStyle, newStyle);
                fontCache.addCachedFont(templateFont, newStyle.getFont(resultWorkbook));
            }
            styleCache.addCachedStyle(templateStyle, newStyle);
            style = newStyle;
        }

        return style;
    }

    /**
     * Returns EscherAggregate from sheet
     *
     * @param sheet - HSSFSheet
     * @return - EscherAggregate from sheet
     */
    protected EscherAggregate getEscherAggregate(HSSFSheet sheet) {
        EscherAggregate agg = sheetToEscherAggregate.get(sheet.getSheetName());
        if (agg == null) {
            agg = sheet.getDrawingEscherAggregate();
            sheetToEscherAggregate.put(sheet.getSheetName(), agg);
        }
        return agg;
    }

    /**
     * Copies all pictures from template sheet to result sheet, shift picture depending on area dependencies
     *
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    protected void copyPicturesFromTemplateToResult(HSSFSheet templateSheet, HSSFSheet resultSheet) {
        List<HSSFClientAnchor> list = getAllAnchors(getEscherAggregate(templateSheet));

        int i = 0;
        if (CollectionUtils.isNotEmpty(orderedPicturesId)) {//just a shitty workaround for anchors without pictures
            for (HSSFClientAnchor anchor : list) {
                Cell topLeft = getCellFromTemplate(new Cell(anchor.getCol1(), anchor.getRow1()));
                anchor.setCol1(topLeft.getCol());
                anchor.setRow1(topLeft.getRow());

                anchor.setCol2(topLeft.getCol() + anchor.getCol2() - anchor.getCol1());
                anchor.setRow2(topLeft.getRow() + anchor.getRow2() - anchor.getRow1());

                HSSFPatriarch sheetPatriarch = drawingPatriarchsMap.get(resultSheet);
                if (sheetPatriarch != null) {
                    sheetPatriarch.createPicture(anchor, orderedPicturesId.get(i++));
                }
            }
        }
    }

    protected boolean rowExists(HSSFSheet sheet, int rowNumber) {
        return sheet.getRow(rowNumber) != null;
    }

    protected Cell getCellFromTemplate(Cell cell) {
        Cell newCell = new Cell(cell);
        updateCell(newCell);
        return newCell;
    }

    protected void updateCell(Cell cell) {
        Area templateArea = areaDependencyManager.getTemplateAreaByCoordinate(cell.getCol(), cell.getRow());
        List<Area> resultAreas = areasDependency.get(templateArea);

        if (CollectionUtils.isNotEmpty(resultAreas)) {
            Area destination = resultAreas.get(0);

            int col = cell.getCol() - templateArea.getTopLeft().getCol() + destination.getTopLeft().getCol();
            int row = cell.getRow() - templateArea.getTopLeft().getRow() + destination.getTopLeft().getRow();

            cell.setCol(col);
            cell.setRow(row);
        }
    }

    protected void copyPageBreaks(HSSFSheet templateSheet, HSSFSheet resultSheet, int templateRowIndex, int resultRowIndex) {
        int[] rowBreaks = templateSheet.getRowBreaks();
        for (int rowBreak : rowBreaks) {
            if (rowBreak == templateRowIndex) {
                resultSheet.setRowBreak(resultRowIndex);
                break;
            }
        }
    }

    //---------------------Utility classes------------------------

    /**
     * Cell range at sheet
     */
    protected static class SheetRange {
        private CellRangeAddress cellRangeAddress;
        private String sheetName;

        private SheetRange(CellRangeAddress cellRangeAddress, String sheetName) {
            this.cellRangeAddress = cellRangeAddress;
            this.sheetName = sheetName;
        }

        public CellRangeAddress getCellRangeAddress() {
            return cellRangeAddress;
        }

        public String getSheetName() {
            return sheetName;
        }
    }

    /**
     * Bounds of region [(x,y) : (x1, y1)]
     */
    protected static class Bounds {
        public final int row0;
        public final int column0;
        public final int row1;
        public final int column1;

        private Bounds(int row0, int column0, int row1, int column1) {
            this.row0 = row0;
            this.column0 = column0;
            this.row1 = row1;
            this.column1 = column1;
        }
    }
}
