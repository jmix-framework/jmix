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

import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.opencsv.CSVWriter;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.formatters.impl.xlsx.*;
import io.jmix.reports.yarg.formatters.impl.xlsx.hints.XslxHintProcessor;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.BandVisitor;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.util.docx4j.XmlCopyUtils;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTChart;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.SpreadsheetML.CalcChain;
import org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheDefinition;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;

public class XlsxFormatter extends AbstractFormatter {

    private static final Logger log = LoggerFactory.getLogger(XlsxFormatter.class);

    private static final String TRUE_AS_STRING = "1";
    private static final String FALSE_AS_STRING = "0";

    protected DocumentConverter documentConverter;
    protected Document template;
    protected Document result;

    protected RangeDependencies rangeDependencies = new RangeDependencies();
    protected BandsForRanges bandsForRanges = new BandsForRanges();
    protected LinkedHashMultimap<Range, Range> rangeVerticalIntersections = LinkedHashMultimap.create();

    protected Set<CellWithBand> innerFormulas = new HashSet<>();
    protected Set<CellWithBand> outerFormulas = new HashSet<>();

    protected Map<String, Range> lastRenderedRangeForBandName = new HashMap<>();
    protected Map<Worksheet, Long> lastRowForSheet = new HashMap<>();
    protected XslxHintProcessor hintProcessor = new XslxHintProcessor();

    protected BandData previousRangeBandData;
    protected int previousRangesRightOffset;

    protected boolean formulasPostProcessingEvaluationEnabled = false;

    protected Unmarshaller unmarshaller;
    protected Marshaller marshaller;

    public XlsxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.xlsx);
    }

    public void setDocumentConverter(DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    public void setFormulasPostProcessingEvaluationEnabled(boolean formulasPostProcessingEvaluationEnabled) {
        this.formulasPostProcessingEvaluationEnabled = formulasPostProcessingEvaluationEnabled;
    }

    @Override
    public void renderDocument() {
        init();

        validateTemplateContainsNamedRange();

        hintProcessor.init(template, result);
        findVerticalDependencies();

        result.clearWorkbook();
        result.clearBandDefinedNames(rootBand);

        for (BandData childBand : rootBand.getChildrenList()) {
            checkThreadInterrupted();
            writeBand(childBand);
        }

        updateOutlines();
        updateMergeRegions();
        updateCharts();
        updateFormulas();
        updatePivotTables();
        updateConditionalFormatting();
        updateHeaderAndFooter();
        updateSheetNames();
        hintProcessor.apply();

        saveAndClose();
    }

    protected void validateTemplateContainsNamedRange() {
        if (Objects.isNull(template.getWorkbook().getDefinedNames())) {
            throw wrapWithReportingException("An error occurred while rendering document from template. Template does not contain named ranges");
        }
    }

    protected void saveAndClose() {
        try {
            checkThreadInterrupted();

            //Remove calcChain until it is well-formed and to get rid of MS Excel errors on file opening in some cases
            SpreadsheetMLPackage smlPackage = result.getPackage();
            Parts parts = smlPackage.getParts();
            CalcChain calcChain = null;
            try {
                calcChain = (CalcChain) parts.get(new PartName("/xl/calcChain.xml"));
            } catch (InvalidFormatException e) {
                log.warn("Invalid format of part name", e);
            }
            if (calcChain != null) {
                calcChain.remove();
            }

            if (ReportOutputType.csv.equals(outputType)) {
                saveXlsxAsCsv(result, outputStream);
                outputStream.flush();
            } else {
                ByteArrayOutputStream intermediateBos = new ByteArrayOutputStream();
                writeToOutputStream(result.getPackage(), intermediateBos);
                if (isFormulasPostProcessingEvaluationRequired()) {
                    intermediateBos = evaluateFormulas(intermediateBos.toByteArray());
                }

                if (ReportOutputType.xlsx.equals(outputType)) {
                    outputStream.write(intermediateBos.toByteArray());
                    outputStream.flush();
                } else if (ReportOutputType.pdf.equals(outputType)) {
                    if (documentConverter != null) {
                        documentConverter.convertToPdf(DocumentConverter.FileType.SPREADSHEET, intermediateBos.toByteArray(), outputStream);
                        outputStream.flush();
                    } else {
                        throw new UnsupportedOperationException(
                                "XlsxFormatter could not convert result to pdf without Libre/Open office connected. " +
                                        "Please setup Libre/Open office connection details.");
                    }
                } else if (ReportOutputType.html.equals(outputType)) {
                    if (documentConverter != null) {
                        documentConverter.convertToHtml(DocumentConverter.FileType.SPREADSHEET, intermediateBos.toByteArray(), outputStream);
                        outputStream.flush();
                    } else {
                        throw new UnsupportedOperationException(
                                "XlsxFormatter could not convert result to html without Libre/Open office connected. " +
                                        "Please setup Libre/Open office connection details.");
                    }
                } else {
                    throw new UnsupportedOperationException(String.format("XlsxFormatter could not output file with type [%s]", outputType));
                }
            }
        } catch (Docx4JException e) {
            throw wrapWithReportingException("An error occurred while saving result report", e);
        } catch (IOException e) {
            throw wrapWithReportingException("An error occurred while saving result report to " + outputType.getId(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected boolean isFormulasPostProcessingEvaluationRequired() {
        return formulasPostProcessingEvaluationEnabled
                && (innerFormulas.size() > 0 || outerFormulas.size() > 0);
    }

    protected ByteArrayOutputStream evaluateFormulas(byte[] content) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(content);
            org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook(bis);
            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos;
        } catch (IOException e) {
            throw new ReportingException(e);
        }
    }

    protected void init() {
        try {
            template = Document.create(SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));
            result = Document.create(SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));
            result.getWorkbook().getCalcPr().setCalcMode(STCalcMode.AUTO);
            result.getWorkbook().getCalcPr().setFullCalcOnLoad(true);
            marshaller = XmlCopyUtils.createMarshaller(Context.jcSML);
            unmarshaller = XmlCopyUtils.createUnmarshaller(Context.jcSML);
        } catch (Exception e) {
            throw wrapWithReportingException(String.format("An error occurred while loading template [%s]", reportTemplate.getDocumentName()), e);
        }
    }

    protected void findVerticalDependencies() {
        DefinedNames definedNames = template.getWorkbook().getDefinedNames();
        if (definedNames != null) {
            List<CTDefinedName> definedName = definedNames.getDefinedName();
            for (CTDefinedName name1 : definedName) {
                if (hintProcessor.isHintDefinedName(name1.getName())) continue;
                for (CTDefinedName name2 : definedName) {
                    if (hintProcessor.isHintDefinedName(name2.getName())) continue;
                    if (!name1.equals(name2)) {
                        Range range1 = Range.fromFormula(name1.getValue());
                        Range range2 = Range.fromFormula(name2.getValue());
                        if (range1.intersectsByVertical(range2)) {
                            rangeVerticalIntersections.put(range1, range2);
                            rangeVerticalIntersections.put(range2, range1);
                        }
                    }
                }
            }
        }
    }

    protected void updateOutlines() {
        for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
            Worksheet resultWorksheet;
            try {
                resultWorksheet = sheetWrapper.getWorksheet().getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException("Unable to get worksheet contents");
            }
            Worksheet templateWorksheet = template.getSheetByName(sheetWrapper.getName());

            if (templateWorksheet.getSheetFormatPr() != null) {
                resultWorksheet.setSheetFormatPr(XmlUtils.deepCopy(templateWorksheet.getSheetFormatPr(), Context.jcSML));
            }
        }
    }

    protected void updateCharts() {
        for (Map.Entry<Range, Document.ChartWrapper> entry : result.getChartSpaces().entrySet()) {
            for (Range templateRange : rangeDependencies.templates()) {
                if (templateRange.intersects(entry.getKey())) {
                    List<Range> chartBandResultRanges = rangeDependencies.resultsForTemplate(templateRange);
                    if (chartBandResultRanges.size() > 0) {
                        Range firstResultRange = getFirst(chartBandResultRanges);

                        shiftChart(entry.getValue(), templateRange, firstResultRange);

                        CTChart chart = entry.getValue().getChartSpace().getChart();
                        CTPlotArea plotArea = chart.getPlotArea();
                        List<Object> areaChartOrArea3DChartOrLineChart = plotArea.getAreaChartOrArea3DChartOrLineChart();
                        for (Object series : areaChartOrArea3DChartOrLineChart) {
                            processSeries(series);
                        }
                    }
                }
            }
        }
    }

    private void processSeries(Object series) {
        List areas = ChartUtils.getAreas(series);
        if (areas != null) {
            for (Object area : areas) {
                CTAxDataSource captions = ChartUtils.getAreaCat(area);
                if (captions != null && captions.getStrRef() != null) {
                    Range temlpateCaptionsRange = Range.fromFormula(captions.getStrRef().getF());
                    for (Range bandRange : rangeDependencies.templates()) {
                        if (bandRange.contains(temlpateCaptionsRange)) {
                            List<Range> seriesResultRanges = rangeDependencies.resultsForTemplate(bandRange);

                            Range seriesFirstRange = getFirst(seriesResultRanges);
                            Range seriesLastRange = getLast(seriesResultRanges);

                            Offset offset = calculateOffset(temlpateCaptionsRange, seriesFirstRange);
                            Offset initialOffset = calculateOffset(temlpateCaptionsRange, bandRange);
                            temlpateCaptionsRange = temlpateCaptionsRange.shift(
                                    offset.downOffset - initialOffset.downOffset,
                                    offset.rightOffset - initialOffset.rightOffset);

                            Offset grow = calculateOffset(seriesFirstRange, seriesLastRange);
                            temlpateCaptionsRange.grow(grow.downOffset, grow.rightOffset);

                            captions.getStrRef().setF(temlpateCaptionsRange.toFormula());
                            break;
                        }
                    }
                }

                CTNumDataSource data = ChartUtils.getAreaVal(area);
                if (data != null && data.getNumRef() != null) {
                    Range templateDataRange = Range.fromFormula(data.getNumRef().getF());
                    for (Range bandRange : rangeDependencies.templates()) {
                        if (bandRange.contains(templateDataRange)) {
                            List<Range> seriesResultRanges = rangeDependencies.resultsForTemplate(bandRange);

                            Range seriesFirstRange = getFirst(seriesResultRanges);
                            Range seriesLastRange = getLast(seriesResultRanges);

                            Offset offset = calculateOffset(templateDataRange, seriesFirstRange);
                            Offset initialOffset = calculateOffset(templateDataRange, bandRange);
                            templateDataRange = templateDataRange.shift(
                                    offset.downOffset - initialOffset.downOffset,
                                    offset.rightOffset - initialOffset.rightOffset);

                            Offset grow = calculateOffset(seriesFirstRange, seriesLastRange);
                            templateDataRange.grow(grow.downOffset, grow.rightOffset);

                            data.getNumRef().setF(templateDataRange.toFormula());
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void shiftChart(Document.ChartWrapper chart, Range templateRange, Range firstResultRange) {
        Offset offset = calculateOffset(templateRange, firstResultRange);
        CTTwoCellAnchor anchor = chart.getAnchor();
        anchor.getFrom().setRow(anchor.getFrom().getRow() + offset.downOffset);
        anchor.getFrom().setCol(anchor.getFrom().getCol() + offset.rightOffset);
        anchor.getTo().setRow(anchor.getTo().getRow() + offset.downOffset);
        anchor.getTo().setCol(anchor.getTo().getCol() + offset.rightOffset);
    }

    //todo support formulas without range but with list of cells
    protected void updateFormulas() {
        processInnerFormulas();
        processOuterFormulas();
    }

    protected void updateConditionalFormatting() {
        for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
            Worksheet worksheet;
            try {
                worksheet = sheetWrapper.getWorksheet().getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException("Unable to get worksheet contents");
            }

            for (CTConditionalFormatting ctConditionalFormatting : worksheet.getConditionalFormatting()) {
                List<String> references = new ArrayList<>();
                for (String ref : ctConditionalFormatting.getSqref()) {
                    Range formulaRange = Range.fromRange(sheetWrapper.getName(), ref);
                    for (Range templateRange : rangeDependencies.templates()) {
                        if (templateRange.contains(formulaRange)) {
                            List<Range> resultRanges = new ArrayList<>(rangeDependencies.resultsForTemplate(templateRange));
                            for (Range resultRange : resultRanges) {
                                Offset offset = calculateOffset(templateRange, resultRange);
                                Range shift = formulaRange.copy().shift(offset.downOffset, offset.rightOffset);
                                references.add(shift.toRange());
                            }
                        }
                    }
                }

                ctConditionalFormatting.getSqref().clear();
                ctConditionalFormatting.getSqref().addAll(references);
            }
        }
    }

    protected void processOuterFormulas() {
        for (CellWithBand cellWithWithBand : outerFormulas) {
            Cell cellWithFormula = cellWithWithBand.cell;
            String oldFormula = cellWithFormula.getF().getValue();
            String newFormula = insertBandDataToString(cellWithWithBand.bandData, oldFormula);
            if (!oldFormula.equals(newFormula)) {
                cellWithFormula.getF().setValue(newFormula);
            }
            Row row = (Row) cellWithFormula.getParent();
            Worksheet worksheet = getWorksheet(row);
            Set<Range> formulaRanges = Range.fromCellFormula(result.getSheetName(worksheet), cellWithFormula);
            CellReference formulaCellReference = new CellReference(result.getSheetName(worksheet), cellWithFormula.getR());

            BandData formulaParentBand = null;
            BandData formulaBand = null;

            for (Range resultRange : rangeDependencies.results()) {
                if (resultRange.contains(formulaCellReference)) {
                    formulaBand = bandsForRanges.bandForResultRange(resultRange);
                    formulaParentBand = formulaBand.getParentBand();
                }
            }

            for (Range templateRange : rangeDependencies.templates()) {
                if (templateRange.containsAny(formulaRanges)) {
                    List<Range> resultRanges = new ArrayList<>(rangeDependencies.resultsForTemplate(templateRange));
                    List<Range> newRanges = new ArrayList<>();
                    for (Range resultRange : resultRanges) {
                        BandData bandData = bandsForRanges.bandForResultRange(resultRange);
                        boolean hasSameFormulaBand = false;
                        BandData nextParent = bandData.getParentBand();
                        while (nextParent != null) {
                            hasSameFormulaBand = nextParent.equals(formulaBand);
                            if (hasSameFormulaBand) {
                                break;
                            }
                            nextParent = nextParent.getParentBand();
                        }
                        if (hasSameFormulaBand) {
                            newRanges.add(resultRange);
                        }
                    }
                    if (newRanges.isEmpty()) {
                        for (Range resultRange : resultRanges) {
                            BandData bandData = bandsForRanges.bandForResultRange(resultRange);
                            boolean hasSameParentFormulaBand = false;
                            BandData nextParent = bandData.getParentBand();
                            while (nextParent != null) {
                                hasSameParentFormulaBand = nextParent.equals(formulaParentBand);
                                if (hasSameParentFormulaBand) {
                                    break;
                                }
                                nextParent = nextParent.getParentBand();
                            }
                            if (hasSameParentFormulaBand) {
                                newRanges.add(resultRange);
                            }
                        }
                    }

                    for (Range formulaRange : formulaRanges) {
                        if (newRanges.size() > 0) {
                            Range shiftedRange = calculateFormulaRangeChange(formulaRange, templateRange, newRanges);
                            updateFormula(cellWithFormula, formulaRange, shiftedRange);
                        } else {
                            cellWithFormula.setF(null);
                            cellWithFormula.setV("ERROR: Formula references to empty range");
                            cellWithFormula.setT(STCellType.STR);
                        }
                    }
                    break;
                }
            }
        }
    }

    protected Range calculateFormulaRangeChange(Range formulaRange, Range templateRange, List<Range> resultRanges) {
        Range firstResultRange = getFirst(resultRanges);
        Range lastResultRange = getLast(resultRanges);

        Offset offset = calculateOffset(templateRange, firstResultRange);
        Range shiftedRange = formulaRange.copy();
        shiftedRange = shiftedRange.shift(offset.downOffset, offset.rightOffset);

        Offset grow = calculateOffset(firstResultRange, lastResultRange);
        shiftedRange.grow(grow.downOffset, grow.rightOffset);
        return shiftedRange;
    }

    protected void processInnerFormulas() {
        for (CellWithBand cellWithWithBand : innerFormulas) {
            Cell cellWithFormula = cellWithWithBand.cell;
            String oldFormula = cellWithFormula.getF().getValue();
            String newFormula = insertBandDataToString(cellWithWithBand.bandData, oldFormula);
            if (!oldFormula.equals(newFormula)) {
                cellWithFormula.getF().setValue(newFormula);
            }
            Row row = (Row) cellWithFormula.getParent();
            Worksheet worksheet = getWorksheet(row);
            Set<Range> formulaRanges = Range.fromCellFormula(result.getSheetName(worksheet), cellWithFormula);
            for (Range templateRange : rangeDependencies.templates()) {
                if (templateRange.containsAny(formulaRanges)) {
                    List<Range> resultRanges = rangeDependencies.resultsForTemplate(templateRange);

                    CellReference cellReference = new CellReference(result.getSheetName(worksheet), cellWithFormula.getR());
                    for (Range resultRange : resultRanges) {
                        if (resultRange.contains(cellReference)) {
                            Offset offset = calculateOffset(templateRange, resultRange);

                            for (Range formulaRange : formulaRanges) {
                                Range shiftedFormulaRange = formulaRange.copy().shift(offset.downOffset, offset.rightOffset);
                                updateFormula(cellWithFormula, formulaRange, shiftedFormulaRange);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void updateFormula(Cell cellWithFormula, Range originalFormulaRange, Range formulaRange) {
        CTCellFormula formula = cellWithFormula.getF();
        formula.setValue(formula.getValue().replace(originalFormulaRange.toRange(), formulaRange.toRange()));
        if (originalFormulaRange.isOneCellRange() && formulaRange.isOneCellRange()) {
            //here we check that there are no alpha-numeric symbols around the single reference
            String pattern = "(?<!\\w+)" + originalFormulaRange.toFirstCellReference() + "(?!\\w+)";
            formula.setValue(formula.getValue().replaceAll(pattern, formulaRange.toFirstCellReference()));
        }
    }

    protected Offset calculateOffset(Range from, Range to) {
        int downOffset = to.getFirstRow() - from.getFirstRow();
        int rightOffset = to.getFirstColumn() - from.getFirstColumn();
        return new Offset(downOffset, rightOffset);
    }

    protected void updatePivotTables() {
        for (PivotCacheDefinition pivotCacheDefinition : result.getPivotCacheDefinitions()) {
            try {
                Optional.ofNullable(pivotCacheDefinition.getContents())
                        .map(CTPivotCacheDefinition::getCacheSource)
                        .map(CTCacheSource::getWorksheetSource)
                        .ifPresent(ws -> {
                            if (StringUtils.isNotBlank(ws.getRef())) {
                                Range pivotRange = Range.fromRange(ws.getSheet(), ws.getRef());
                                for (Range templateRange : rangeDependencies.templates()) {
                                    if (pivotRange.contains(templateRange)) {
                                        List<Range> resultRanges = rangeDependencies.resultsForTemplate(templateRange);
                                        if (CollectionUtils.isNotEmpty(resultRanges)) {
                                            Range lastResultRange = resultRanges.get(resultRanges.size() - 1);
                                            Offset offset = calculateOffset(templateRange, lastResultRange);
                                            pivotRange.grow(offset.downOffset, offset.rightOffset);
                                            ws.setRef(pivotRange.toRange());
                                        }
                                    }
                                }
                            }
                        });

            } catch (Docx4JException e) {
                throw wrapWithReportingException("The pivot table could not be updated", e);
            }
        }
    }

    protected void updateMergeRegions() {
        for (Range templateRange : rangeDependencies.templates()) {
            Worksheet templateSheet = template.getSheetByName(templateRange.getSheet());
            Worksheet resultSheet = result.getSheetByName(templateRange.getSheet());

            if (templateSheet.getMergeCells() != null) {
                if (resultSheet.getMergeCells() == null) {
                    CTMergeCells resultMergeCells = new CTMergeCells();
                    resultMergeCells.setParent(resultSheet);
                    resultSheet.setMergeCells(resultMergeCells);
                }
            }

            for (Range resultRange : rangeDependencies.resultsForTemplate(templateRange)) {
                if (templateSheet.getMergeCells() != null && templateSheet.getMergeCells().getMergeCell() != null) {
                    for (CTMergeCell templateMergeRegion : templateSheet.getMergeCells().getMergeCell()) {
                        Range mergeRange = Range.fromRange(templateRange.getSheet(), templateMergeRegion.getRef());
                        if (templateRange.contains(mergeRange) || templateRange.isOneCellRange() && mergeRange.contains(templateRange)) {
                            Offset offset = calculateOffset(templateRange, resultRange);
                            Range resultMergeRange = mergeRange.copy().shift(offset.downOffset, offset.rightOffset);
                            CTMergeCell resultMergeRegion = new CTMergeCell();
                            resultMergeRegion.setRef(resultMergeRange.toRange());
                            resultMergeRegion.setParent(resultSheet.getMergeCells());
                            resultSheet.getMergeCells().getMergeCell().add(resultMergeRegion);
                        }
                    }
                }
            }
        }
    }

    protected void writeBand(BandData childBand) {
        try {
            if (BandOrientation.HORIZONTAL == childBand.getOrientation()) {
                writeHBand(childBand);
            } else {
                writeVBand(childBand);
            }
        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            throw wrapWithReportingException(String.format("An error occurred while rendering band [%s]", childBand.getName()), e);
        }
    }

    protected void writeHBand(BandData band) {
        Range templateRange = getBandRange(band);
        if (templateRange != null) {
            Worksheet resultSheet = result.getSheetByName(templateRange.getSheet());
            List<Row> resultSheetRows = resultSheet.getSheetData().getRow();

            Row firstRow = findNextRowForHBand(band, templateRange, resultSheetRows);
            firstRow = ensureNecessaryRowsCreated(templateRange, resultSheet, firstRow);

            List<Cell> resultCells = copyCells(band, templateRange, resultSheetRows, firstRow, resultSheet);

            updateRangeMappings(band, templateRange, resultCells);

            //render children
            if (resultCells != null && !resultCells.isEmpty()) {
                for (BandData child : band.getChildrenList()) {
                    writeBand(child);
                }
            }
        }
    }

    protected void writeVBand(BandData band) {
        Range templateRange = getBandRange(band);
        if (templateRange != null) {
            Worksheet resultSheet = result.getSheetByName(templateRange.getSheet());
            List<Row> resultSheetRows = resultSheet.getSheetData().getRow();

            Row firstRow = findNextRowForVBand(band, templateRange, resultSheetRows);
            firstRow = ensureNecessaryRowsCreated(templateRange, resultSheet, firstRow);

            List<Cell> resultCells = copyCells(band, templateRange, resultSheetRows, firstRow, resultSheet);

            updateRangeMappings(band, templateRange, resultCells);
        }
    }

    protected void updateRangeMappings(BandData band, Range templateRange, List<Cell> resultCells) {
        if (resultCells != null && !resultCells.isEmpty()) {
            Range resultRange = Range.fromCells(templateRange.getSheet(), getFirst(resultCells).getR(),
                    resultCells.get(resultCells.size() - 1).getR());
            rangeDependencies.addDependency(templateRange, resultRange);
            bandsForRanges.add(band, templateRange, resultRange);
            lastRenderedRangeForBandName.put(band.getName(), resultRange);
        }
    }

    protected Row findNextRowForHBand(BandData band, Range templateRange, List<Row> resultSheetRows) {
        Row firstRow = null;
        boolean isFirstLevelBand = BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName());

        //we suppose that when we render HORIZONTAL first level band, it should not be any right offset
        if (isFirstLevelBand ||
                (previousRangeBandData != null && !previousRangeBandData.getParentBand().equals(band.getParentBand()))) {
            setPreviousRangeVerticalOffset(0, null);
        }

        Range lastRenderedRange = getLastRenderedBandForThisLevel(band);
        if (lastRenderedRange != null) {//this band has been already rendered at least once
            BandData lastRenderedBand = bandsForRanges.bandForResultRange(lastRenderedRange);
            LastRowBandVisitor bandVisitor = new LastRowBandVisitor();
            lastRenderedBand.visit(bandVisitor);

            if (resultSheetRows.size() > bandVisitor.lastRow) {//get next row
                firstRow = resultSheetRows.get(bandVisitor.lastRow);
            }
        } else if (!isFirstLevelBand) {
            firstRow = findNextRowForChildBand(band, templateRange, resultSheetRows);
        } else {//this is the first render
            firstRow = findNextRowForFirstRender(templateRange, resultSheetRows);
        }
        return firstRow;
    }

    private void setPreviousRangeVerticalOffset(int previousRangesRightOffset, BandData previousRangeBandData) {
        this.previousRangesRightOffset = previousRangesRightOffset;
        this.previousRangeBandData = previousRangeBandData;
    }

    protected Row findNextRowForVBand(BandData band, Range templateRange, List<Row> resultSheetRows) {
        Row firstRow = null;
        boolean isFirstLevelBand = BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName());
        this.previousRangesRightOffset = 0;

        Range lastRenderedRange = getLastRenderedBandForThisLevel(band);
        if (lastRenderedRange != null) {//this band has been already rendered at least once
            int shiftBetweenTemplateRangeAndLastRenderedRange = lastRenderedRange.getFirstColumn() - templateRange.getFirstColumn();
            int widthOfTheRange = templateRange.getLastColumn() - templateRange.getFirstColumn() + 1;
            setPreviousRangeVerticalOffset(shiftBetweenTemplateRangeAndLastRenderedRange + widthOfTheRange, band);
            if (resultSheetRows.size() > lastRenderedRange.getFirstRow() - 1) {//get current row
                firstRow = resultSheetRows.get(lastRenderedRange.getFirstRow() - 1);
            }
        } else if (!isFirstLevelBand) {
            firstRow = findNextRowForChildBand(band, templateRange, resultSheetRows);
        } else {//this is the first render
            firstRow = findNextRowForFirstRender(templateRange, resultSheetRows);
            if (previousRangeBandData != null && firstRow != null) {//row for rendering and previously rendered data already there
                Range lastRenderedBandRange = getLastRenderedBandForThisLevel(previousRangeBandData);
                if (firstRow.getR() != null && firstRow.getR().intValue() <= lastRenderedBandRange.getLastRow()) {
                    //we have intersection between previously rendered data and what we want to add
                    int lastRenderedColumn = lastRenderedBandRange.getLastColumn();
                    int firstTemplateColumn = templateRange.getFirstColumn();
                    if (lastRenderedColumn != firstTemplateColumn) {
                        previousRangesRightOffset = lastRenderedColumn - firstTemplateColumn + 1;
                    }
                    previousRangeBandData = band;
                }
            }
        }
        return firstRow;
    }

    protected Row findNextRowForChildBand(BandData band, Range templateRange, List<Row> resultSheetRows) {
        BandData parentBand = band.getParentBand();
        Range resultParentRange = bandsForRanges.resultForBand(parentBand);
        Range templateParentRange = bandsForRanges.templateForBand(parentBand);

        if (resultParentRange != null && templateParentRange != null) {
            if (templateParentRange.getFirstRow() == templateRange.getFirstRow()) {
                if (resultSheetRows.size() > resultParentRange.getFirstRow() - 1) {//get current row
                    return resultSheetRows.get(resultParentRange.getFirstRow() - 1);
                }
            } else {
                LastRowBandVisitor bandVisitor = new LastRowBandVisitor();
                band.getParentBand().visit(bandVisitor);
                if (resultSheetRows.size() > bandVisitor.lastRow) {//get next row
                    return resultSheetRows.get(bandVisitor.lastRow);
                }
            }
        }
        return null;
    }

    protected Row findNextRowForFirstRender(Range templateRange, List<Row> resultSheetRows) {
        Collection<Range> templateNeighbours = rangeVerticalIntersections.get(templateRange);
        for (Range templateNeighbour : templateNeighbours) {
            Collection<Range> resultRanges = rangeDependencies.resultsForTemplate(templateNeighbour);
            if (resultRanges.size() > 0) {
                Range firstResultRange = resultRanges.iterator().next();
                return resultSheetRows.get(firstResultRange.getFirstRow() - 1);//get current  row
            }
        }
        return null;
    }

    protected Row ensureNecessaryRowsCreated(Range templateRange, Worksheet resultSheet, Row firstRow) {
        if (firstRow == null) {
            firstRow = createNewRow(resultSheet);
        }

        if (resultSheet.getSheetData().getRow().size() < firstRow.getR() + templateRange.getLastRow() - templateRange.getFirstRow()) {
            for (int i = 0; i < templateRange.getLastRow() - templateRange.getFirstRow(); i++) {
                Row row = createNewRow(resultSheet);
            }
        }
        return firstRow;
    }

    protected List<Cell> copyCells(BandData band, Range templateRange, List<Row> resultSheetRows, Row firstRow, Worksheet resultSheet) {
        List<Cell> resultCells = new ArrayList<>();
        for (int i = 0; i <= templateRange.getLastRow() - templateRange.getFirstRow(); i++) {
            Range oneRowRange = new Range(templateRange.getSheet(),
                    templateRange.getFirstColumn(), templateRange.getFirstRow() + i,
                    templateRange.getLastColumn(), templateRange.getFirstRow() + i);
            Map<CellReference, Cell> cellsForOneRowRange = template.getCellsByRange(oneRowRange);
            List<Cell> templateCells = new ArrayList<>(cellsForOneRowRange.values());
            Row templateRow = !templateCells.isEmpty() ?
                    (Row) templateCells.get(0).getParent() :
                    resultSheetRows.get((int) (firstRow.getR() + i - 1));

            createFakeTemplateCellsForEmptyOnes(oneRowRange, cellsForOneRowRange, templateCells);

            Row resultRow = resultSheetRows.get((int) (firstRow.getR() + i - 1));

            List<Cell> currentRowResultCells = copyCells(templateRange, band, resultRow, templateCells);

            resultCells.addAll(currentRowResultCells);

            copyRowSettings(templateRow, resultRow, getTemplateWorksheet(templateRow), getWorksheet(resultRow));
        }
        return resultCells;
    }

    /**
     * XLSX document does not store empty cells and it might be an issue for formula calculations and etc.
     * So we need to create fake template cell for each empty cell.
     */
    protected void createFakeTemplateCellsForEmptyOnes(Range oneRowRange,
                                                       Map<CellReference, Cell> cellsForOneRowRange,
                                                       List<Cell> templateCells) {
        if (oneRowRange.toCellReferences().size() != templateCells.size()) {
            final HashBiMap<CellReference, Cell> referencesToCells = HashBiMap.create(cellsForOneRowRange);

            for (CellReference cellReference : oneRowRange.toCellReferences()) {
                if (!cellsForOneRowRange.containsKey(cellReference)) {
                    Cell newCell = Context.getsmlObjectFactory().createCell();
                    newCell.setV(null);
                    newCell.setT(STCellType.STR);
                    newCell.setR(cellReference.toReference());
                    templateCells.add(newCell);
                    referencesToCells.put(cellReference, newCell);
                }
            }

            templateCells.sort((o1, o2) -> {
                CellReference cellReference1 = referencesToCells.inverse().get(o1);
                CellReference cellReference2 = referencesToCells.inverse().get(o2);
                return cellReference1.compareTo(cellReference2);
            });
        }
    }

    protected Range getLastRenderedBandForThisLevel(BandData band) {
        List<BandData> sameLevelBands = band.getParentBand().getChildrenByName(band.getName());
        for (BandData sameLevelBand : sameLevelBands) {
            Range range = bandsForRanges.resultForBand(sameLevelBand);
            if (range != null) {
                return lastRenderedRangeForBandName.get(band.getName());
            }
        }

        return null;
    }

    protected Range getBandRange(BandData band) {
        CTDefinedName targetRange = template.getDefinedName(band.getName());
        if (targetRange == null) {
            log.info("Could not find named range for band {}", band.getName());
            return null;
        }

        return Range.fromFormula(targetRange.getValue());
    }

    protected Row createNewRow(Worksheet resultSheet) {
        Row newRow = Context.getsmlObjectFactory().createRow();
        Long currentRow = lastRowForSheet.get(resultSheet);
        currentRow = currentRow != null ? currentRow : 0;
        currentRow++;
        newRow.setR(currentRow);
        lastRowForSheet.put(resultSheet, currentRow);
        resultSheet.getSheetData().getRow().add(newRow);
        newRow.setParent(resultSheet.getSheetData());

        return newRow;
    }

    protected List<Cell> copyCells(Range templateRange, BandData bandData, Row newRow, List<Cell> templateCells) {
        List<Cell> resultCells = new ArrayList<>();

        Worksheet resultWorksheet = getWorksheet(newRow);
        for (Cell templateCell : templateCells) {
            checkThreadInterrupted();
            Cell newCell = copyCell(templateCell);

            if (newCell.getF() != null) {
                addFormulaForPostProcessing(templateRange, bandData, newRow, templateCell, newCell);
            }

            resultCells.add(newCell);

            CellReference tempRef = new CellReference(templateRange.getSheet(), templateCell);
            CellReference newRef = new CellReference(templateRange.getSheet(), newCell.getR());
            newRef.move(newRow.getR().intValue(), newRef.getColumn());

            //if we have vertical band or horizontal band right after vertical band - it should be shifted
            //only if there is vertical intersection with vertical band?
            newRef.shift(0, previousRangesRightOffset);

            newCell.setR(newRef.toReference());

            newRow.getC().add(newCell);
            newCell.setParent(newRow);

            WorksheetPart worksheetPart = null;
            for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
                Worksheet contents;
                try {
                    contents = sheetWrapper.getWorksheet().getContents();
                } catch (Docx4JException e) {
                    throw new RuntimeException("Unable to get worksheet contents", e);
                }

                if (contents == resultWorksheet) {
                    worksheetPart = sheetWrapper.getWorksheet();
                }
            }

            updateCell(worksheetPart, bandData, newCell);

            Col templateColumn = template.getColumnForCell(templateRange.getSheet(), tempRef);
            Col resultColumn = result.getColumnForCell(templateRange.getSheet(), newRef);

            if (templateColumn != null && resultColumn == null) {
                resultColumn = XmlUtils.deepCopy(templateColumn, Context.jcSML);
                resultColumn.setMin(newRef.getColumn());
                resultColumn.setMax(newRef.getColumn());
                resultColumn.setOutlineLevel(templateColumn.getOutlineLevel());

                resultWorksheet.getCols().get(0).getCol().add(resultColumn);
            }

            hintProcessor.add(tempRef, templateCell, newCell, bandData);

        }
        return resultCells;
    }

    protected Cell copyCell(Cell cell) {
        return XmlCopyUtils.copyCell(cell, unmarshaller, marshaller);
    }

    protected Worksheet getWorksheet(Row newRow) {
        SheetData resultSheetData = (SheetData) newRow.getParent();
        return (Worksheet) resultSheetData.getParent();
    }

    protected Worksheet getTemplateWorksheet(Row newRow) {
        SheetData resultSheetData = (SheetData) newRow.getParent();
        Worksheet worksheet = (Worksheet) resultSheetData.getParent();

        for (Document.SheetWrapper sheetWrapper : template.getWorksheets()) {
            if (sheetWrapper.getWorksheet().getJaxbElement() == worksheet) {
                return worksheet;
            }
        }

        int i = 0;
        for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
            if (sheetWrapper.getWorksheet().getJaxbElement() == worksheet) {
                break;
            }
            i++;
        }

        return template.getWorksheets().get(i).getWorksheet().getJaxbElement();
    }

    protected void addFormulaForPostProcessing(Range templateRange, BandData bandData, Row newRow, Cell templateCell, Cell newCell) {
        Worksheet worksheet = getWorksheet(newRow);
        Set<Range> formulaRanges = Range.fromCellFormula(result.getSheetName(worksheet), templateCell);
        if (templateRange.containsAny(formulaRanges)) {
            innerFormulas.add(new CellWithBand(bandData, newCell));
        } else {
            outerFormulas.add(new CellWithBand(bandData, newCell));
        }
    }

    protected void copyRowSettings(Row templateRow, Row newRow, Worksheet templateWorksheet, Worksheet resultWorksheet) {
        newRow.setHt(templateRow.getHt());
        newRow.setCustomHeight(true);
        CTPageBreak rowBreaks = templateWorksheet.getRowBreaks();
        if (rowBreaks != null && rowBreaks.getBrk() != null) {
            CTPageBreak resultWorksheetRowBreaks = resultWorksheet.getRowBreaks();
            for (CTBreak templateBreak : rowBreaks.getBrk()) {
                if (templateRow.getR().equals(templateBreak.getId())) {
                    CTBreak newBreak = XmlUtils.deepCopy(templateBreak, Context.jcSML);
                    newBreak.setId(newRow.getR());
                    resultWorksheetRowBreaks.getBrk().add(newBreak);
                }
            }

            long rowBreaksCount = resultWorksheetRowBreaks.getBrk().size();
            resultWorksheetRowBreaks.setCount(rowBreaksCount);
            resultWorksheetRowBreaks.setManualBreakCount(rowBreaksCount);
        }

        newRow.setOutlineLevel(templateRow.getOutlineLevel());
    }

    protected void updateCell(WorksheetPart worksheetPart, BandData bandData, Cell newCell) {
        String cellValue = template.getCellValue(newCell);

        if (cellValue == null) {
            newCell.setV("");
            return;
        }

        if (UNIVERSAL_ALIAS_PATTERN.matcher(cellValue).matches()) {
            String parameterName = unwrapParameterName(cellValue);
            String fullParameterName = bandData.getName() + "." + parameterName;
            Object value = bandData.getData().get(parameterName);

            if (value == null) {
                newCell.setV("");
                return;
            }

            String formatString = getFormatString(parameterName, fullParameterName);
            InlinerAndMatcher inlinerAndMatcher = getContentInlinerForFormat(formatString);
            if (inlinerAndMatcher != null) {
                inlinerAndMatcher.contentInliner.inlineToXlsx(result.getPackage(), worksheetPart, newCell, value, inlinerAndMatcher.matcher);
                return;
            }

            if (formatString != null) {
                newCell.setT(STCellType.STR);
                newCell.setV(formatValue(value, parameterName, fullParameterName));
            } else if (value instanceof Boolean) {
                newCell.setT(STCellType.B);
                newCell.setV((boolean) value ? TRUE_AS_STRING : FALSE_AS_STRING);
            } else if (value instanceof Number) {
                newCell.setT(STCellType.N);
                newCell.setV(String.valueOf(value));
            } else if (value instanceof Date) {
                newCell.setT(STCellType.N);
                newCell.setV(String.valueOf(DateUtil.getExcelDate((Date) value)));
            } else {
                newCell.setT(STCellType.STR);
                newCell.setV(formatValue(value, parameterName, fullParameterName));
            }
        } else {
            String value = insertBandDataToString(bandData, cellValue);
            newCell.setV(value);

            if (newCell.getT() == STCellType.S) {
                newCell.setT(STCellType.STR);
            }
        }
    }

    protected <T> T getFirst(List<T> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    protected <T> T getLast(List<T> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(list.size() - 1);
        }

        return null;
    }

    protected void saveXlsxAsCsv(Document document, OutputStream outputStream) throws IOException, Docx4JException {
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        for (Document.SheetWrapper sheetWrapper : document.getWorksheets()) {
            Worksheet worksheet = sheetWrapper.getWorksheet().getContents();
            for (Row row : worksheet.getSheetData().getRow()) {
                String rows[] = new String[row.getC().size()];
                List<Cell> cells = row.getC();

                boolean emptyRow = true;
                for (int i = 0; i < cells.size(); i++) {
                    checkThreadInterrupted();
                    Cell cell = cells.get(i);
                    String value = cell.getV();
                    rows[i] = value;
                    if (value != null && !value.isEmpty())
                        emptyRow = false;
                }

                if (!emptyRow)
                    writer.writeNext(rows);
            }
        }
        writer.close();
    }

    protected void writeToOutputStream(SpreadsheetMLPackage mlPackage, OutputStream outputStream) throws Docx4JException {
        Save save = new Save(mlPackage);
        save.save(outputStream);
    }

    protected void updateHeaderAndFooter() {
        for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
            Worksheet worksheet;
            try {
                worksheet = sheetWrapper.getWorksheet().getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException("Unable to get contents of worksheet", e);
            }
            if (worksheet.getHeaderFooter() != null) {
                CTHeaderFooter headerFooter = worksheet.getHeaderFooter();
                if (headerFooter.getOddHeader() != null) {
                    headerFooter.setOddHeader(insertBandDataToString(headerFooter.getOddHeader()));
                }
                if (headerFooter.getOddFooter() != null) {
                    headerFooter.setOddFooter(insertBandDataToString(headerFooter.getOddFooter()));
                }
            }
        }
    }

    protected void updateSheetNames() {
        Sheets sheets = result.getWorkbook().getSheets();
        if (sheets != null && sheets.getSheet() != null) {
            for (Sheet sheet : sheets.getSheet()) {
                if (sheet.getName() != null) {
                    sheet.setName(insertBandDataToString(sheet.getName()));
                }
            }
        }
    }

    protected String insertBandDataToString(String resultStr) {
        List<String> parametersToInsert = new ArrayList<>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(resultStr);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            BandPathAndParameterName bandPathAndParameterName = separateBandNameAndParameterName(parameterName);
            BandData bandData = findBandByPath(bandPathAndParameterName.getBandPath());
            Object value = bandData.getData().get(bandPathAndParameterName.getParameterName());
            String fullParameterName = bandData.getName() + "." + parameterName;
            String valueStr = formatValue(value, parameterName, fullParameterName);
            resultStr = inlineParameterValue(resultStr, parameterName, valueStr);
        }
        return resultStr;
    }

    protected static class CellWithBand {
        protected BandData bandData;
        protected Cell cell;

        public CellWithBand(BandData bandData, Cell cell) {
            this.bandData = bandData;
            this.cell = cell;
        }
    }

    protected static class Offset {
        int downOffset;
        int rightOffset;

        private Offset(int downOffset, int rightOffset) {
            this.downOffset = downOffset;
            this.rightOffset = rightOffset;
        }
    }

    protected class LastRowBandVisitor implements BandVisitor {
        private int lastRow = 0;

        @Override
        public boolean visit(BandData band) {
            Range range = bandsForRanges.resultForBand(band);
            if (range != null && range.getLastRow() > lastRow) {
                lastRow = range.getLastRow();
            }
            return false;
        }
    }
}
