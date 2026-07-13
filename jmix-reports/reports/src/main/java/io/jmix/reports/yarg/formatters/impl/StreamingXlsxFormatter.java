/*
 * Copyright 2026 Haulmont.
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
package io.jmix.reports.yarg.formatters.impl;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.StreamingReportFormatter;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingBandFeed;
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingStyleCache;
import io.jmix.reports.yarg.formatters.impl.streaming.StreamingXlsxToCsvWriter;
import io.jmix.reports.yarg.formatters.impl.streaming.TemplateBand;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PageMargin;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.jspecify.annotations.Nullable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTConditionalFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

import javax.xml.namespace.QName;

import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.AttributedString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Streaming XLSX report formatter built on Apache POI SXSSF. Writes the sheet strictly top-to-bottom;
 * all post-processing is computed at row-write time because flushed rows cannot be revisited. The
 * template is read with XSSF (templates are small); result rows are created through
 * {@link SXSSFWorkbook} and flushed to disk outside the sliding window, so the heap footprint of the
 * output document stays bounded regardless of the row count. The engine is selected for reports that
 * mark a band as {@code streaming} (see {@code ReportBand#isStreaming()}); the non-streaming
 * {@link XlsxFormatter} remains the default.
 *
 * <p><b>Supported</b> (everything computable at the moment a row is written):
 * <ul>
 *     <li>{@code ${alias}} fields with value typing and formats matching the non-streaming engine
 *     (strings, numbers, booleans, dates, times, format strings, multi-alias text), with one documented
 *     exception for very large integers noted below;</li>
 *     <li>horizontal bands, multi-row band blocks, nested bands, sibling bands, empty bands;</li>
 *     <li>merged cells: those inside a band are replicated and shifted per instance, those on static rows
 *     outside any band are carried over (shifted to the row's output position);</li>
 *     <li>in-row formulas (e.g. {@code =B2*C2}), references shifted per instance;</li>
 *     <li>trailing aggregate formulas below the data ({@code SUM(A1:A1)} grows over all rendered rows;
 *     an aggregate over an empty band renders the same error text as the non-streaming engine);
 *     single references below the data re-base onto the band's last rendered instance, references to
 *     static rows onto the row's final position; sheet-qualified references are never rewritten;</li>
 *     <li>row grouping (outline levels), conditional formatting, sheet-name and header substitution;</li>
 *     <li>{@code hint_style_<param>} dynamic named cell styles and {@code hint_rowAutoHeight};</li>
 *     <li>template freeze panes, page/print setup, user-defined names, the print area, data validations,
 *     and column widths, hidden state and column-level styles (copied to the result workbook).</li>
 * </ul>
 *
 * <p><b>Not supported</b> (fundamental to forward-only writing — the non-streaming engine must be used
 * instead):
 * <ul>
 *     <li>formulas referencing forward (a total <i>above</i> the data): a reference to a band laid out
 *     below the formula is rejected with an error; sideways references across bands;</li>
 *     <li>an aggregate below a band whose rendered rows are interleaved with a child band's rows
 *     (a master-detail layout) when the aggregate sums a column shared by both bands: the grown range
 *     is one contiguous span from the first to the last rendered master row, so it also covers the
 *     detail rows in between. This is harmless when master and detail occupy different columns (the
 *     extra cells are empty), but over-counts when they share the summed column;</li>
 *     <li>pre-evaluation of formula values: when converting to PDF/CSV/HTML the formula cells stay
 *     unevaluated until the file is opened in Excel (the workbook carries
 *     {@code <calcPr fullCalcOnLoad="1"/>});</li>
 *     <li>vertical and cross bands, side-by-side blocks and nested band rectangles (all rejected with
 *     an error), complex master-detail layouts;</li>
 *     <li>multi-sheet templates (rejected with an error; only single-sheet templates are supported);</li>
 *     <li>content inliners ({@code ${bitmap:WxH}}, {@code ${image:WxH}}, {@code ${html}}) — rejected
 *     with an error;</li>
 *     <li>hyperlinks (SXSSF does not carry them over from the template);</li>
 *     <li>charts and pivot tables.</li>
 * </ul>
 *
 * <p>Known behavioral differences from the non-streaming engine: {@code hint_rowAutoHeight} sets a
 * fixed measured height (SXSSF cannot emit {@code customHeight="false"}, so Excel does not re-measure
 * on open); parent/child band nesting is derived from the template row order, so a child band's named
 * range must be laid out below its parent's and a sibling band laid out between a parent and its child
 * is rejected with an error rather than silently reordered; an integer value beyond a double's exact
 * range (2^53) is written as a rounded double, because POI sets a numeric cell only through a double,
 * whereas the non-streaming engine keeps the exact digits in the raw file (give the field a format or
 * select the value as a string to keep it exact); and coordinate-based template settings that are copied
 * verbatim (print area and data-validation regions) keep their template positions — they are not re-based
 * as bands expand.
 *
 * <p>Data feeding: when the report's streaming band is backed by a {@code sql} or {@code jpql} dataset,
 * its rows are pulled from a live database cursor inside an open transaction (see
 * {@code StreamingBandFeed} and {@code StreamingReportDataLoader}); the band must be horizontal,
 * single-dataset and childless, other loader types (groovy, json) are rejected. Conditional formatting
 * over the fed band is applied as one contiguous range covering the whole rendered span. A
 * {@code hint_rowAutoHeight} range over a fed band measures every streamed row's text with an AWT layout
 * pass: this stays O(1) in memory but costs CPU per row, so a large fed export with auto-height is
 * measurably slower.
 *
 * <p>Output types: {@code xlsx} is written directly to the output stream. {@code csv} is derived from
 * the streamed workbook by a single SAX pass over a temp file ({@link StreamingXlsxToCsvWriter}), so it
 * stays O(1) in memory at any size. {@code pdf}/{@code html} go through the office
 * {@link DocumentConverter} and load the whole workbook bytes, so they are practically limited by what
 * the office process can handle; formula cells stay unevaluated in all converted outputs.
 */
public class StreamingXlsxFormatter extends AbstractFormatter implements StreamingReportFormatter {

    protected static final String HINT_NAME_PREFIX = "hint_";
    protected static final String STYLE_HINT_PREFIX = "hint_style_";
    protected static final String ROW_AUTO_HEIGHT_HINT = "hint_rowAutoHeight";
    /** Prefix of Excel built-in defined names: Print_Area, _FilterDatabase, Print_Titles, ... */
    protected static final String BUILT_IN_NAME_PREFIX = "_xlnm";
    /** Reserved built-in name labels POI may expose without the {@code _xlnm} prefix; never copied as user names. */
    protected static final Set<String> RESERVED_NAMES = Set.of(
            "Print_Area", "Print_Titles", "_FilterDatabase", "Consolidate_Area", "Sheet_Title", "Database");
    protected static final String SPREADSHEET_ML_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    protected static final double NANOS_PER_DAY = 86_400_000_000_000.0d;

    /** Max row height in Excel, in points (mirrors the non-streaming RowAutoHeightXlsxHint). */
    protected static final double MAX_ROW_HEIGHT = 409.0;
    protected static final int DEFAULT_FONT_SIZE = 11;
    /** SXSSF sliding window; large enough to cover a single band's multi-row block. */
    protected int rowAccessWindowSize = 100;
    /** Hard row cap of the .xlsx format; exceeding it is rejected with a clear error, not a raw POI throw. */
    protected int maxResultRows = SpreadsheetVersion.EXCEL2007.getMaxRows();

    protected XSSFWorkbook templateWorkbook;
    protected SXSSFWorkbook resultWorkbook;
    protected Map<String, TemplateBand> templateBands = new HashMap<>();
    protected List<CellRangeAddress> templateMerges = new ArrayList<>();
    protected StreamingStyleCache styleCache;

    /** Band name -> [first, last] output row (0-based) actually rendered for the band, across all instances. */
    protected Map<String, int[]> renderedBandRows = new HashMap<>();

    /** Band name -> output row where the band's LAST rendered instance starts. */
    protected Map<String, Integer> lastInstanceStarts = new HashMap<>();
    /** Template row (static, outside bands) -> output row it was written to. */
    protected Map<Integer, Integer> staticRowOutputs = new HashMap<>();
    /** Parse/render context for formula tokens; sheet index 0 (single-sheet templates only). */
    protected XSSFEvaluationWorkbook evaluationWorkbook;

    /** {@code hint_style_<param>} template ranges: cells whose style name comes from band data. */
    protected List<HintRange> styleHints = new ArrayList<>();
    /** {@code hint_rowAutoHeight} template ranges: rows whose height is measured from the written text. */
    protected List<HintRange> rowAutoHeightHints = new ArrayList<>();
    protected FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);

    /** Optional supply of the hot band's rows from a live cursor; see {@link StreamingBandFeed}. */
    @Nullable
    protected StreamingBandFeed streamingBandFeed;

    /** All band names of the report definition; when null, names are derived from the band data tree. */
    @Nullable
    protected Set<String> reportBandNames;

    /** Office converter for pdf/html output; when absent those output types fail with a clear error. */
    @Nullable
    protected DocumentConverter documentConverter;

    /** Template conditional formattings, re-targeted onto rendered ranges after the walk. */
    protected List<CTConditionalFormatting> templateConditionalFormattings = new ArrayList<>();
    /** Bands referenced by conditional formatting; only these track per-instance start rows. */
    protected Set<String> cfTrackedBands = new HashSet<>();
    /** Band name -> first output row of each rendered instance (only for {@link #cfTrackedBands}). */
    protected Map<String, List<Integer>> bandInstanceStarts = new HashMap<>();
    /** Band name -> precomputed render plan of the band's template rectangle. */
    protected Map<String, BandLayout> bandLayouts = new HashMap<>();
    /** Band name -> names of its direct child bands, derived from the band data tree. */
    protected Map<String, Set<String>> childBandNames = new HashMap<>();
    /** Template named cell style name -> index into {@code cellStyleXfs}. */
    protected Map<String, Integer> namedStyleXfIds = new HashMap<>();
    /** Template named cell style name -> materialized result-workbook style. */
    protected Map<String, CellStyle> namedResultStyles = new HashMap<>();

    public StreamingXlsxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.xlsx);
        supportedOutputTypes.add(ReportOutputType.csv);
        supportedOutputTypes.add(ReportOutputType.pdf);
        supportedOutputTypes.add(ReportOutputType.html);
    }

    public void setDocumentConverter(@Nullable DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    /** Must be set before {@link #consumeData()}; the window is fixed once the result workbook exists. */
    public void setRowAccessWindowSize(int rowAccessWindowSize) {
        this.rowAccessWindowSize = rowAccessWindowSize;
    }

    public void setMaxResultRows(int maxResultRows) {
        this.maxResultRows = maxResultRows;
    }

    public void setStreamingBandFeed(@Nullable StreamingBandFeed streamingBandFeed) {
        this.streamingBandFeed = streamingBandFeed;
    }

    @Override
    public void setReportBandNames(@Nullable Set<String> reportBandNames) {
        this.reportBandNames = reportBandNames;
    }

    @Override
    public void renderDocument() {
        consumeData();
        completeRendering();
    }

    @Override
    public void consumeData() {
        try {
            init();
            validateBandTree();
            writeWorkbook();
            finalizeWorkbook();
        } catch (RuntimeException | Error e) {
            // init() may fail after creating the result workbook (e.g. a template parse error); dispose it
            // here too. disposeResultWorkbook() is null-safe, so a failure before the workbook exists is fine.
            disposeResultWorkbook();
            throw e;
        } finally {
            // The template workbook is only needed while the sheet is written; the result document no
            // longer references it. Close it here so its OPCPackage is not leaked once per report run.
            closeTemplateWorkbook();
        }
    }

    @Override
    public void discard() {
        disposeResultWorkbook();
        closeTemplateWorkbook();
    }

    /** Disposes the result workbook (deleting its spool files) exactly once; null-safe and idempotent. */
    protected void disposeResultWorkbook() {
        if (resultWorkbook != null) {
            resultWorkbook.dispose();
            resultWorkbook = null;
        }
    }

    /** Releases the template workbook (and its backing OPCPackage). Best-effort and idempotent. */
    protected void closeTemplateWorkbook() {
        if (templateWorkbook != null) {
            try {
                templateWorkbook.close();
            } catch (IOException e) {
                // The template is read fully into memory, so a close failure only delays GC of the
                // in-memory package; never let it mask the primary rendering flow.
            }
            templateWorkbook = null;
        }
    }

    @Override
    public void completeRendering() {
        try {
            if (outputType == null || ReportOutputType.xlsx.equals(outputType)) {
                resultWorkbook.write(outputStream);
            } else {
                convertStreamedWorkbook();
            }
        } catch (IOException e) {
            throw new ReportFormattingException("Error writing streaming XLSX document", e);
        } finally {
            disposeResultWorkbook();
        }
    }

    /**
     * Non-xlsx output: the workbook is always streamed to a temp file first (formula values stay
     * unevaluated), then converted. CSV is derived with a single SAX pass (O(1) memory); PDF/HTML go
     * through the office {@code DocumentConverter} and are practically limited by what the office
     * process can chew.
     */
    protected void convertStreamedWorkbook() throws IOException {
        File tempFile = File.createTempFile("streaming-report", ".xlsx");
        try {
            try (OutputStream tempOut = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                resultWorkbook.write(tempOut);
            }
            if (ReportOutputType.csv.equals(outputType)) {
                StreamingXlsxToCsvWriter.convert(tempFile, outputStream);
                outputStream.flush();
            } else if (ReportOutputType.pdf.equals(outputType) || ReportOutputType.html.equals(outputType)) {
                if (documentConverter == null) {
                    throw new ReportFormattingException(
                            "Streaming XLSX formatter could not convert result to " + outputType.getId()
                                    + " without Libre/Open office connected. Please setup Libre/Open office connection details.");
                }
                byte[] workbookBytes = Files.readAllBytes(tempFile.toPath());
                if (ReportOutputType.pdf.equals(outputType)) {
                    documentConverter.convertToPdf(DocumentConverter.FileType.SPREADSHEET, workbookBytes, outputStream);
                } else {
                    documentConverter.convertToHtml(DocumentConverter.FileType.SPREADSHEET, workbookBytes, outputStream);
                }
                outputStream.flush();
            } else {
                throw new ReportFormattingException(String.format(
                        "Streaming XLSX formatter could not output file with type [%s]", outputType));
            }
        } finally {
            if (!tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }

    /**
     * The streaming engine writes strictly top-to-bottom, so structures that require revisiting flushed
     * rows are rejected up front with a clear error instead of producing a corrupt document.
     */
    protected void validateBandTree() {
        rejectUnsupportedBandOrientations(rootBand);
        rejectOverlappingBands();
        rejectMisplacedChildBands();
    }

    protected void rejectUnsupportedBandOrientations(BandData band) {
        BandOrientation orientation = band.getOrientation();
        if (orientation == BandOrientation.VERTICAL || orientation == BandOrientation.CROSS) {
            throw new ReportFormattingException(String.format(
                    "Streaming XLSX formatter does not support %s bands: %s",
                    orientation.name().toLowerCase(), band.getName()));
        }
        for (BandData child : band.getChildrenList()) {
            rejectUnsupportedBandOrientations(child);
        }
    }

    /**
     * Two bands whose template rows overlap cannot both be rendered forward-only: the top-to-bottom walk
     * emits each template row once, so a band sharing another band's rows (side-by-side blocks, or one
     * band rectangle nested inside another) would be silently dropped or double-counted. Reject such a
     * layout up front. Parent/child bands never overlap — a child's range sits below its parent's.
     */
    protected void rejectOverlappingBands() {
        List<TemplateBand> bands = new ArrayList<>(templateBands.values());
        for (int i = 0; i < bands.size(); i++) {
            for (int j = i + 1; j < bands.size(); j++) {
                TemplateBand a = bands.get(i);
                TemplateBand b = bands.get(j);
                if (a.firstRow <= b.lastRow && b.firstRow <= a.lastRow) {
                    throw new ReportFormattingException(String.format(
                            "Streaming XLSX formatter does not support overlapping band ranges: band [%s] "
                                    + "(rows %d-%d) and band [%s] (rows %d-%d) share template rows. Side-by-side "
                                    + "blocks and nested band rectangles cannot be rendered forward-only; lay the "
                                    + "bands out one below another or clear the band's streaming flag.",
                            a.name, a.firstRow + 1, a.lastRow + 1,
                            b.name, b.firstRow + 1, b.lastRow + 1));
                }
            }
        }
    }

    /**
     * A child band renders attached to its parent instance, so its template rows must sit below the
     * parent's. A child laid out above its parent cannot be rendered forward-only (its rows would have to
     * be written before the parent) and {@link #rejectOverlappingBands()} does not catch it when the two
     * ranges do not overlap — it would otherwise be silently dropped. Reject it up front instead.
     */
    protected void rejectMisplacedChildBands() {
        for (Map.Entry<String, Set<String>> entry : childBandNames.entrySet()) {
            TemplateBand parent = templateBands.get(entry.getKey());
            if (parent == null) {
                continue;
            }
            for (String childName : entry.getValue()) {
                TemplateBand child = templateBands.get(childName);
                if (child != null && child.firstRow <= parent.lastRow) {
                    throw new ReportFormattingException(String.format(
                            "Streaming XLSX formatter requires a child band to be laid out below its parent: "
                                    + "child band [%s] (rows %d-%d) is not below parent band [%s] (rows %d-%d). "
                                    + "Move the child band's named range below its parent's, or clear the "
                                    + "band's streaming flag.",
                            child.name, child.firstRow + 1, child.lastRow + 1,
                            parent.name, parent.firstRow + 1, parent.lastRow + 1));
                }
            }
        }
    }

    /**
     * Formula values are not pre-evaluated by the streaming engine, so the workbook asks Excel to run a
     * full recalculation on open. POI does not emit the optional {@code <calcPr/>} element by itself.
     */
    protected void finalizeWorkbook() {
        CTWorkbook ctWorkbook = resultWorkbook.getXSSFWorkbook().getCTWorkbook();
        if (!ctWorkbook.isSetCalcPr()) {
            ctWorkbook.addNewCalcPr();
        }
        ctWorkbook.getCalcPr().setFullCalcOnLoad(true);
    }

    /**
     * Merges duplicate {@code <row>} elements (same {@code r}) in each worksheet part into one row. The
     * report wizard's docx4j engine emits every cell in its own {@code <row r="N">} element; POI keeps only
     * the last such element when reading, dropping all columns but the one it carries. Normalizing the
     * template bytes before POI reads them restores the full row. A well-formed template (one {@code <row>}
     * per index) is returned unchanged, so it pays only a scan.
     */
    protected byte[] normalizeTemplateRows(byte[] xlsxBytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            boolean changed = false;
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(xlsxBytes));
                 ZipOutputStream zos = new ZipOutputStream(bos)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    byte[] content = zis.readAllBytes();
                    if (entry.getName().startsWith("xl/worksheets/") && entry.getName().endsWith(".xml")) {
                        byte[] merged = mergeDuplicateRows(content);
                        if (merged != null) {
                            content = merged;
                            changed = true;
                        }
                    }
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    zos.write(content);
                    zos.closeEntry();
                }
            }
            return changed ? bos.toByteArray() : xlsxBytes;
        } catch (IOException e) {
            throw new ReportFormattingException("Error normalizing the XLSX template", e);
        }
    }

    /**
     * Collapses duplicate {@code <row>} elements in one worksheet part, appending the cells of each
     * duplicate to the first row with that index. Returns the rewritten XML, or {@code null} when the
     * sheet has no duplicate rows (nothing to change).
     */
    @Nullable
    protected byte[] mergeDuplicateRows(byte[] sheetXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(sheetXml));
            NodeList sheetDataNodes = doc.getElementsByTagNameNS("*", "sheetData");
            if (sheetDataNodes.getLength() == 0) {
                return null;
            }
            Element sheetData = (Element) sheetDataNodes.item(0);
            List<Element> rows = new ArrayList<>();
            NodeList rowNodes = sheetData.getElementsByTagNameNS("*", "row");
            for (int i = 0; i < rowNodes.getLength(); i++) {
                rows.add((Element) rowNodes.item(i));
            }
            Map<String, Element> firstByRef = new LinkedHashMap<>();
            List<Element> duplicates = new ArrayList<>();
            for (Element row : rows) {
                String ref = row.getAttribute("r");
                Element first = firstByRef.get(ref);
                if (first == null) {
                    firstByRef.put(ref, row);
                    continue;
                }
                List<Element> cells = new ArrayList<>();
                NodeList cellNodes = row.getElementsByTagNameNS("*", "c");
                for (int i = 0; i < cellNodes.getLength(); i++) {
                    cells.add((Element) cellNodes.item(i));
                }
                for (Element cell : cells) {
                    first.appendChild(cell);
                }
                duplicates.add(row);
            }
            if (duplicates.isEmpty()) {
                return null;
            }
            for (Element duplicate : duplicates) {
                sheetData.removeChild(duplicate);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            return out.toByteArray();
        } catch (Exception e) {
            throw new ReportFormattingException("Error merging duplicate template rows", e);
        }
    }

    protected void init() {
        try (InputStream is = reportTemplate.getDocumentContent()) {
            byte[] normalizedTemplate = normalizeTemplateRows(is.readAllBytes());
            Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(normalizedTemplate));
            if (!(workbook instanceof XSSFWorkbook xssfWorkbook)) {
                try {
                    workbook.close();
                } catch (IOException ignored) {
                    // best-effort close of the rejected non-xlsx workbook
                }
                throw new ReportFormattingException(
                        "Streaming XLSX formatter requires an .xlsx template, got " + workbook.getClass().getSimpleName());
            }
            this.templateWorkbook = xssfWorkbook;
        } catch (IOException e) {
            throw new ReportFormattingException("Error reading XLSX template", e);
        }
        if (templateWorkbook.getNumberOfSheets() > 1) {
            int sheetCount = templateWorkbook.getNumberOfSheets();
            closeTemplateWorkbook();
            throw new ReportFormattingException(String.format(
                    "Streaming XLSX formatter supports single-sheet templates only, the template has %d sheets. "
                            + "Clear the band's streaming flag to render multi-sheet templates via the non-streaming engine.",
                    sheetCount));
        }
        this.evaluationWorkbook = XSSFEvaluationWorkbook.create(templateWorkbook);
        // compressTmpFiles: flushed rows spool to java.io.tmpdir as gzip instead of raw sheet XML,
        // which is an order of magnitude smaller for huge reports.
        this.resultWorkbook = new SXSSFWorkbook(null, rowAccessWindowSize, true);
        this.styleCache = new StreamingStyleCache(resultWorkbook);
        this.templateMerges = new ArrayList<>(templateWorkbook.getSheetAt(0).getMergedRegions());
        parseTemplateBands();
        parseNamedStyles();
        parseConditionalFormattings();
    }

    protected void parseConditionalFormattings() {
        templateConditionalFormattings.addAll(
                templateWorkbook.getSheetAt(0).getCTWorksheet().getConditionalFormattingList());
        for (CTConditionalFormatting cf : templateConditionalFormattings) {
            for (Object ref : cf.getSqref()) {
                CellRangeAddress range = CellRangeAddress.valueOf(ref.toString());
                TemplateBand band = bandContainingRows(range);
                if (band != null) {
                    cfTrackedBands.add(band.name);
                }
            }
        }
    }

    /**
     * The band whose template rows fully cover the range's rows, or {@code null} when the range sits on
     * static rows. Only rows matter for conditional-formatting re-targeting: a rule may legitimately be
     * one or more columns wider than the band's named range, and its columns are kept as authored.
     */
    @Nullable
    protected TemplateBand bandContainingRows(CellRangeAddress range) {
        for (TemplateBand band : templateBands.values()) {
            if (range.getFirstRow() >= band.firstRow && range.getLastRow() <= band.lastRow) {
                return band;
            }
        }
        return null;
    }

    /**
     * Bands are defined in the template as workbook named ranges whose name equals the band name.
     * Defined names prefixed with {@code hint_} carry per-cell processing hints, not bands. Excel
     * service names ({@code _xlnm.Print_Area}, {@code _xlnm._FilterDatabase}, {@code _xlnm.Print_Titles})
     * and names that do not match any report band (e.g. named constants) are ignored — treating them
     * as bands would silently skip template rows or fail on multi-area references.
     */
    protected void parseTemplateBands() {
        Set<String> bandNames = resolveBandNames();
        for (Name name : templateWorkbook.getAllNames()) {
            String definedName = name.getNameName();
            if (definedName.startsWith(STYLE_HINT_PREFIX)) {
                styleHints.add(toHintRange(name, definedName.substring(STYLE_HINT_PREFIX.length())));
                continue;
            }
            if (definedName.equals(ROW_AUTO_HEIGHT_HINT) || definedName.startsWith(ROW_AUTO_HEIGHT_HINT + "_")) {
                rowAutoHeightHints.add(toHintRange(name, ""));
                continue;
            }
            if (definedName.startsWith(HINT_NAME_PREFIX)) {
                continue;
            }
            if (definedName.startsWith(BUILT_IN_NAME_PREFIX)) {
                continue;
            }
            if (!bandNames.contains(definedName)) {
                continue;
            }
            AreaReference area = new AreaReference(name.getRefersToFormula(),
                    templateWorkbook.getSpreadsheetVersion());
            CellReference first = area.getFirstCell();
            CellReference last = area.getLastCell();
            templateBands.put(definedName, new TemplateBand(
                    definedName, first.getRow(), last.getRow(), first.getCol(), last.getCol()));
        }
    }

    protected HintRange toHintRange(Name name, String param) {
        AreaReference area = new AreaReference(name.getRefersToFormula(),
                templateWorkbook.getSpreadsheetVersion());
        CellReference first = area.getFirstCell();
        CellReference last = area.getLastCell();
        return new HintRange(param, first.getRow(), last.getRow(), first.getCol(), last.getCol());
    }

    /**
     * Band names the template walk recognizes: the report definition names supplied by the engine
     * (covers bands that produced no data), plus everything derivable from the band data tree and
     * the streaming feed for direct formatter usage.
     */
    protected Set<String> resolveBandNames() {
        Set<String> names = new HashSet<>();
        if (reportBandNames != null) {
            names.addAll(reportBandNames);
        }
        if (rootBand.getFirstLevelBandDefinitionNames() != null) {
            names.addAll(rootBand.getFirstLevelBandDefinitionNames());
        }
        collectBandDataNames(rootBand, names);
        if (streamingBandFeed != null) {
            names.add(streamingBandFeed.getBandName());
        }
        return names;
    }

    protected void collectBandDataNames(BandData band, Set<String> names) {
        for (BandData child : band.getChildrenList()) {
            names.add(child.getName());
            childBandNames.computeIfAbsent(band.getName(), k -> new HashSet<>()).add(child.getName());
            collectBandDataNames(child, names);
        }
    }

    /**
     * Reads the template's named cell styles ({@code cellStyles} in {@code xl/styles.xml}). The typed
     * {@code CTCellStyle} classes are absent from {@code poi-ooxml-lite}, so the mapping is extracted
     * with an XmlBeans cursor over the stylesheet document.
     */
    protected void parseNamedStyles() {
        try (XmlCursor cursor = templateWorkbook.getStylesSource().getCTStylesheet().newCursor()) {
            if (!cursor.toChild(new QName(SPREADSHEET_ML_NS, "cellStyles"))
                    || !cursor.toChild(new QName(SPREADSHEET_ML_NS, "cellStyle"))) {
                return;
            }
            do {
                String name = cursor.getAttributeText(new QName("name"));
                String xfId = cursor.getAttributeText(new QName("xfId"));
                if (name != null && xfId != null) {
                    namedStyleXfIds.put(name, Integer.parseInt(xfId));
                }
            } while (cursor.toNextSibling(new QName(SPREADSHEET_ML_NS, "cellStyle")));
        }
    }

    protected void writeWorkbook() {
        XSSFSheet templateSheet = templateWorkbook.getSheetAt(0);
        String sheetName = insertBandDataToStringByPath(templateSheet.getSheetName());
        SXSSFSheet resultSheet = resultWorkbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

        copyColumnWidths(templateSheet, resultSheet);
        copyTemplateSettings(templateSheet, resultSheet);

        WriteCursor cursor = new WriteCursor();
        int lastTemplateRow = lastTemplateRowToWalk(templateSheet);
        for (int r = 0; r <= lastTemplateRow; r++) {
            checkThreadInterrupted();
            TemplateBand band = bandStartingAt(r);
            if (band != null) {
                if (streamingBandFeed != null && band.name.equals(streamingBandFeed.getBandName())) {
                    writeHorizontalBand(band, templateSheet, resultSheet, cursor,
                            streamingBandFeed.iterator(), true);
                } else {
                    writeHorizontalBand(band, templateSheet, resultSheet, cursor, childBands(rootBand, band.name));
                }
                r = band.lastRow;
                continue;
            }
            copyStaticRow(templateSheet, resultSheet, r, cursor);
        }
        applyConditionalFormattings();
        applyStaticMerges(resultSheet);
    }

    /**
     * Re-targets template conditional formattings onto the rendered rows: a rule range fully inside a band
     * rectangle is replicated per rendered band instance (mirrors the non-streaming sqref re-mapping);
     * ranges outside any band are kept as is. Conditional formatting lives in the sheet part, not in the
     * streamed row window, so adding it after the walk is safe.
     */
    protected void applyConditionalFormattings() {
        if (templateConditionalFormattings.isEmpty()) {
            return;
        }
        CTWorksheet resultWorksheet =
                resultWorkbook.getXSSFWorkbook().getSheetAt(0).getCTWorksheet();
        for (CTConditionalFormatting cf : templateConditionalFormattings) {
            List<String> newRefs = new ArrayList<>();
            for (Object ref : cf.getSqref()) {
                CellRangeAddress range = CellRangeAddress.valueOf(ref.toString());
                TemplateBand band = bandContainingRows(range);
                if (band == null) {
                    // The range is not fully inside one band: re-base each endpoint independently
                    // (reusing the outer-formula endpoint mapping), keeping the columns as authored. A
                    // static endpoint follows its row to the actual output position (it may have shifted
                    // down as bands above expanded); an endpoint that falls inside a band — a range that
                    // starts in the band's data and runs into a static row below it — re-bases onto the
                    // band's rendered rows instead of staying pinned to the template row. Drop the rule
                    // when a referenced band produced no rows.
                    // If an endpoint falls in a band that produced no rows, resolveOuterAreaRow returns
                    // null; fall back to the endpoint's static output position (or its template row) so
                    // the rule still covers the rows that DID render, instead of dropping the whole rule
                    // and stripping conditional formatting from a static row it also covered.
                    Integer first = resolveOuterAreaRow(range.getFirstRow(), true);
                    Integer last = resolveOuterAreaRow(range.getLastRow(), false);
                    int firstRow = first != null ? first
                            : staticRowOutputs.getOrDefault(range.getFirstRow(), range.getFirstRow());
                    int lastRow = last != null ? last
                            : staticRowOutputs.getOrDefault(range.getLastRow(), range.getLastRow());
                    // A null-fallback endpoint (an empty band) can resolve above an already-shifted static
                    // endpoint; order them so the range is never inverted (a malformed sqref like "A4:A3").
                    newRefs.add(new CellRangeAddress(Math.min(firstRow, lastRow), Math.max(firstRow, lastRow),
                            range.getFirstColumn(), range.getLastColumn()).formatAsString());
                    continue;
                }
                List<Integer> starts = bandInstanceStarts.get(band.name);
                if (starts == null) {
                    // Fed band: per-instance starts are not tracked (unbounded), so the rule cannot be
                    // replicated per instance; apply one contiguous range over the whole rendered span.
                    int[] span = renderedBandRows.get(band.name);
                    if (span != null) {
                        newRefs.add(new CellRangeAddress(span[0], span[1],
                                range.getFirstColumn(), range.getLastColumn()).formatAsString());
                    }
                    continue;
                }
                for (int start : starts) {
                    int shift = start - band.firstRow;
                    newRefs.add(new CellRangeAddress(
                            range.getFirstRow() + shift, range.getLastRow() + shift,
                            range.getFirstColumn(), range.getLastColumn()).formatAsString());
                }
            }
            if (newRefs.isEmpty()) {
                continue;
            }
            CTConditionalFormatting copy = (CTConditionalFormatting) cf.copy();
            copy.setSqref(newRefs);
            resultWorksheet.addNewConditionalFormatting().set(copy);
        }
    }

    /**
     * Adds template merged regions that lie entirely on static rows outside any band (e.g. a merged title
     * or footer). A region overlapping a band is skipped here: one fully inside a band is replicated per
     * instance while the band renders, and one that partially overlaps a band has no well-defined extent
     * once the band expands. Static rows may have shifted down as bands above them grew, so each endpoint
     * is re-based onto the row's actual output position.
     */
    protected void applyStaticMerges(SXSSFSheet resultSheet) {
        for (CellRangeAddress merge : templateMerges) {
            if (overlapsAnyBand(merge)) {
                continue;
            }
            int firstRow = staticRowOutputs.getOrDefault(merge.getFirstRow(), merge.getFirstRow());
            int lastRow = staticRowOutputs.getOrDefault(merge.getLastRow(), merge.getLastRow());
            resultSheet.addMergedRegion(new CellRangeAddress(
                    firstRow, lastRow, merge.getFirstColumn(), merge.getLastColumn()));
        }
    }

    /** Whether any template row the range covers belongs to a band (a full or partial band overlap). */
    protected boolean overlapsAnyBand(CellRangeAddress range) {
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            if (bandContainingTemplateRow(row) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The last template row the top-to-bottom walk must visit. A merged region may extend below the last
     * row that holds a cell (its lower cells are empty and have no {@code Row} object), so it is included
     * here: otherwise those rows get no {@code staticRowOutputs} entry and {@link #applyStaticMerges} would
     * shift one merge endpoint while leaving the other on its template row (an inverted or collapsed box).
     */
    protected int lastTemplateRowToWalk(XSSFSheet templateSheet) {
        int last = templateSheet.getLastRowNum();
        for (CellRangeAddress merge : templateMerges) {
            last = Math.max(last, merge.getLastRow());
        }
        return last;
    }

    @Nullable
    protected TemplateBand bandStartingAt(int templateRow) {
        for (TemplateBand band : templateBands.values()) {
            if (band.firstRow == templateRow) {
                return band;
            }
        }
        return null;
    }

    protected void copyColumnWidths(XSSFSheet from, SXSSFSheet to) {
        int maxCol = 0;
        for (Row row : from) {
            maxCol = Math.max(maxCol, row.getLastCellNum());
        }
        // A column may carry a custom width without holding any cell (e.g. a spacer column); those widths
        // live in the sheet's <cols> definitions, not in the rows, so cover them too. getMax() is 1-based.
        for (CTCols cols : from.getCTWorksheet().getColsArray()) {
            for (CTCol col : cols.getColArray()) {
                maxCol = Math.max(maxCol, (int) col.getMax());
            }
        }
        for (int c = 0; c < maxCol; c++) {
            to.setColumnWidth(c, from.getColumnWidth(c));
            if (from.isColumnHidden(c)) {
                to.setColumnHidden(c, true);
            }
        }
        copyColumnStyles(from, to);
    }

    /**
     * Copies column-level default styles (e.g. a whole-column number format) declared in the template's
     * {@code <cols>} definitions. Only columns that explicitly set a style are copied — the usermodel
     * {@code getColumnStyle} returns the workbook default for unstyled columns, which must not be
     * propagated. {@code CTCol} min/max are 1-based.
     */
    protected void copyColumnStyles(XSSFSheet from, SXSSFSheet to) {
        StylesTable styles = templateWorkbook.getStylesSource();
        for (CTCols cols : from.getCTWorksheet().getColsArray()) {
            for (CTCol col : cols.getColArray()) {
                if (!col.isSetStyle()) {
                    continue;
                }
                CellStyle resultStyle = styleCache.resultStyleFor(styles.getStyleAt((int) col.getStyle()));
                for (int c = (int) col.getMin(); c <= (int) col.getMax(); c++) {
                    to.setDefaultColumnStyle(c - 1, resultStyle);
                }
            }
        }
    }

    /**
     * Carries over sheet- and workbook-level template settings that the fresh result workbook would
     * otherwise lose (the non-streaming engine renders on a template copy and keeps them for free): freeze
     * panes, page/print setup, user-defined names and the print area. Coordinate-based settings are copied
     * with their template positions; they are not re-based as bands expand (a documented limitation).
     */
    protected void copyTemplateSettings(XSSFSheet templateSheet, SXSSFSheet resultSheet) {
        copyFreezePane(templateSheet, resultSheet);
        copyPrintSetup(templateSheet, resultSheet);
        copyDefinedNames();
        copyPrintArea();
        copyDataValidations(templateSheet, resultSheet);
    }

    /**
     * Copies the template's data validations (e.g. drop-down lists) onto the result sheet. The validation
     * regions keep their template coordinates and are not re-based as bands expand.
     */
    protected void copyDataValidations(XSSFSheet from, SXSSFSheet to) {
        for (DataValidation validation : from.getDataValidations()) {
            to.addValidationData(validation);
        }
    }

    protected void copyFreezePane(XSSFSheet from, SXSSFSheet to) {
        PaneInformation pane = from.getPaneInformation();
        if (pane != null && pane.isFreezePane()) {
            to.createFreezePane(pane.getVerticalSplitPosition(), pane.getHorizontalSplitPosition());
        }
    }

    protected void copyPrintSetup(XSSFSheet from, SXSSFSheet to) {
        PrintSetup src = from.getPrintSetup();
        PrintSetup dst = to.getPrintSetup();
        dst.setLandscape(src.getLandscape());
        dst.setPaperSize(src.getPaperSize());
        dst.setScale(src.getScale());
        dst.setFitWidth(src.getFitWidth());
        dst.setFitHeight(src.getFitHeight());
        to.setFitToPage(from.getFitToPage());
        to.setAutobreaks(from.getAutobreaks());
        for (PageMargin margin : PageMargin.values()) {
            to.setMargin(margin, from.getMargin(margin));
        }
        to.getHeader().setLeft(from.getHeader().getLeft());
        to.getHeader().setCenter(from.getHeader().getCenter());
        to.getHeader().setRight(from.getHeader().getRight());
        to.getFooter().setLeft(from.getFooter().getLeft());
        to.getFooter().setCenter(from.getFooter().getCenter());
        to.getFooter().setRight(from.getFooter().getRight());
    }

    /**
     * Copies user-defined names into the result workbook. Hint names, band ranges and Excel built-ins
     * (print area/titles, filter database) are skipped: the first two are structural, the built-ins are
     * re-created by {@link #copyPrintArea()} or reserved. Copying user names keeps formulas that reference
     * a workbook name resolving instead of rendering {@code #NAME?}.
     */
    protected void copyDefinedNames() {
        for (Name name : templateWorkbook.getAllNames()) {
            String definedName = name.getNameName();
            if (definedName.startsWith(HINT_NAME_PREFIX)
                    || definedName.startsWith(BUILT_IN_NAME_PREFIX)
                    || RESERVED_NAMES.contains(definedName)
                    || templateBands.containsKey(definedName)) {
                continue;
            }
            Name copy = resultWorkbook.createName();
            copy.setNameName(definedName);
            copy.setRefersToFormula(name.getRefersToFormula());
        }
    }

    protected void copyPrintArea() {
        String printArea = templateWorkbook.getPrintArea(0);
        if (printArea == null) {
            return;
        }
        try {
            AreaReference area = new AreaReference(printArea, templateWorkbook.getSpreadsheetVersion());
            CellReference first = area.getFirstCell();
            CellReference last = area.getLastCell();
            resultWorkbook.setPrintArea(0, first.getCol(), last.getCol(), first.getRow(), last.getRow());
        } catch (IllegalArgumentException e) {
            // Multi-area or otherwise unparsable print area: skip rather than fail the render.
        }
    }

    /**
     * Creates the next result row, rejecting the .xlsx hard row limit with a clear error instead of the
     * raw {@link IllegalArgumentException} POI would throw mid-render after minutes of streaming.
     */
    protected SXSSFRow createResultRow(SXSSFSheet resultSheet, int outRowIdx) {
        if (outRowIdx >= maxResultRows) {
            throw new ReportFormattingException(String.format(
                    "Streaming XLSX report exceeds the %d-row limit of the .xlsx format; "
                            + "reduce the data set or split the report.", maxResultRows));
        }
        return resultSheet.createRow(outRowIdx);
    }

    protected List<BandData> childBands(BandData parent, String name) {
        List<BandData> byName = parent.getChildrenByName(name);
        return byName != null ? byName : List.of();
    }

    /**
     * Renders a horizontal band: the band's template rows are emitted once per {@link BandData} instance,
     * top to bottom. Everything derivable from the template alone (cell classification, parsed formulas,
     * merges, hint membership, auto-height metrics) is precomputed once per band in {@link BandLayout};
     * the per-instance loop only looks up values and writes cells.
     */
    protected void writeHorizontalBand(TemplateBand band, XSSFSheet templateSheet, SXSSFSheet resultSheet,
                                       WriteCursor cursor, List<BandData> instances) {
        writeHorizontalBand(band, templateSheet, resultSheet, cursor, instances.iterator(), false);
    }

    protected void writeHorizontalBand(TemplateBand band, XSSFSheet templateSheet, SXSSFSheet resultSheet,
                                       WriteCursor cursor, Iterator<BandData> instances, boolean fed) {
        BandLayout layout = bandLayout(band, templateSheet);
        if (fed && !layout.merges.isEmpty()) {
            // A fed band streams an unbounded number of rows; replicating a merge per row would keep
            // the whole sheet's merged-region list in memory (O(rows)) and defeat the streaming
            // memory guarantee. Reject up front instead of silently exhausting the heap.
            throw new ReportFormattingException(String.format(
                    "Streaming XLSX formatter does not support merged regions inside a streaming band [%s]. "
                            + "Remove the merge from the template or clear the band's streaming flag.", band.name));
        }
        while (instances.hasNext()) {
            BandData instance = instances.next();
            checkThreadInterrupted();
            int firstOutputRow = cursor.nextRow;
            int rowShift = firstOutputRow - band.firstRow;
            for (PreparedRow preparedRow : layout.rows) {
                int outRowIdx = cursor.nextRow++;
                SXSSFRow out = createResultRow(resultSheet, outRowIdx);
                if (preparedRow.absent) {
                    continue;
                }
                if (preparedRow.outlineLevel > 0) {
                    resultSheet.setRowOutlineLevel(outRowIdx, preparedRow.outlineLevel);
                }
                for (PreparedCell prepared : preparedRow.cells) {
                    Cell outCell = out.createCell(prepared.col);
                    outCell.setCellStyle(prepared.baseStyle);
                    applyPreparedStyleHint(prepared, outCell, instance);
                    renderPreparedCell(prepared, outCell, instance, rowShift, band);
                    applyPreparedAutoHeight(prepared, outCell, out);
                }
            }
            // Regions are added without POI's intersection validation: instances never overlap by
            // construction, and validating against all previously added regions would make merge
            // handling O(rows^2). The regions list itself still grows with the row count — a merge
            // inside a huge streamed band costs O(rows) heap, which is inherent to the xlsx format.
            for (CellRangeAddress merge : layout.merges) {
                resultSheet.addMergedRegionUnsafe(new CellRangeAddress(
                        merge.getFirstRow() + rowShift, merge.getLastRow() + rowShift,
                        merge.getFirstColumn(), merge.getLastColumn()));
            }
            recordRenderedRows(band.name, firstOutputRow, cursor.nextRow - 1);
            lastInstanceStarts.put(band.name, firstOutputRow);
            if (!fed && cfTrackedBands.contains(band.name)) {
                bandInstanceStarts.computeIfAbsent(band.name, k -> new ArrayList<>()).add(firstOutputRow);
            }
            if (!instance.getChildrenList().isEmpty()) {
                for (TemplateBand child : layout.childBands) {
                    List<BandData> childInstances = childBands(instance, child.name);
                    if (childInstances.isEmpty()) {
                        continue;
                    }
                    rejectContentBetween(band, child, templateSheet);
                    writeHorizontalBand(child, templateSheet, resultSheet, cursor, childInstances);
                }
            }
        }
    }

    /**
     * A child band renders attached to its parent instance, so anything physically located between the
     * parent's rows and the child's rows would be written out of order (after the child). Reject such a
     * layout up front instead of producing a silently reordered document: a static row, or another band
     * that is not a child of the same parent (an unrelated sibling), both break the ordering.
     */
    protected void rejectContentBetween(TemplateBand parent, TemplateBand child, XSSFSheet templateSheet) {
        Set<String> siblings = childBandNames.getOrDefault(parent.name, Set.of());
        for (int r = parent.lastRow + 1; r < child.firstRow; r++) {
            TemplateBand between = bandContainingTemplateRow(r);
            if (between == null) {
                if (templateSheet.getRow(r) != null) {
                    throw new ReportFormattingException(String.format(
                            "Streaming XLSX formatter does not support static content between a parent band [%s] "
                                    + "and its child band [%s] (template row %d): the child renders attached to the "
                                    + "parent instance, so the static row would be written out of order. Move the "
                                    + "content outside the parent/child block or clear the band's streaming flag.",
                            parent.name, child.name, r + 1));
                }
            } else if (!between.name.equals(child.name) && !siblings.contains(between.name)) {
                throw new ReportFormattingException(String.format(
                        "Streaming XLSX formatter does not support band [%s] laid out between a parent band [%s] "
                                + "and its child band [%s]: the child renders attached to the parent instance, so "
                                + "band [%s] would be reordered. Move the child band directly below its parent or "
                                + "clear the band's streaming flag.",
                        between.name, parent.name, child.name, between.name));
            }
        }
    }

    protected BandLayout bandLayout(TemplateBand band, XSSFSheet templateSheet) {
        return bandLayouts.computeIfAbsent(band.name, key -> prepareBandLayout(band, templateSheet));
    }

    protected BandLayout prepareBandLayout(TemplateBand band, XSSFSheet templateSheet) {
        BandLayout layout = new BandLayout(childTemplateBandsOf(band));
        for (CellRangeAddress merge : templateMerges) {
            if (merge.getFirstRow() >= band.firstRow && merge.getLastRow() <= band.lastRow
                    && merge.getFirstColumn() >= band.firstCol && merge.getLastColumn() <= band.lastCol) {
                layout.merges.add(merge);
            }
        }
        for (int tr = band.firstRow; tr <= band.lastRow; tr++) {
            XSSFRow templateRow = templateSheet.getRow(tr);
            if (templateRow == null) {
                layout.rows.add(PreparedRow.absentRow());
                continue;
            }
            PreparedRow row = new PreparedRow(templateRow.getOutlineLevel());
            for (int c = band.firstCol; c <= band.lastCol; c++) {
                XSSFCell templateCell = templateRow.getCell(c);
                if (templateCell == null) {
                    continue;
                }
                row.cells.add(prepareCell(band, templateCell, tr, c));
            }
            layout.rows.add(row);
        }
        return layout;
    }

    protected PreparedCell prepareCell(TemplateBand band, XSSFCell templateCell, int templateRow, int col) {
        PreparedCell prepared = new PreparedCell(templateCell, col);
        prepared.baseStyle = styleCache.resultStyleFor(templateCell.getCellStyle());
        prepared.styleHintParam = styleHintParamAt(templateRow, col);
        prepareAutoHeight(prepared, templateCell, templateRow, col);
        switch (templateCell.getCellType()) {
            case FORMULA -> {
                prepared.kind = PreparedCellKind.FORMULA;
                String cellFormula = templateCell.getCellFormula();
                if (UNIVERSAL_ALIAS_PATTERN.matcher(cellFormula).find()) {
                    // Aliases resolve to per-instance values, so the formula cannot be parsed once and
                    // cached; it is substituted and parsed per instance (mirrors the non-streaming
                    // processInnerFormulas, which substitutes ${alias} before shifting references).
                    prepared.formulaTemplate = cellFormula;
                } else {
                    prepared.formula = prepareFormula(cellFormula, band);
                }
            }
            case NUMERIC -> {
                prepared.kind = PreparedCellKind.CONSTANT_NUMERIC;
                prepared.constantNumeric = templateCell.getNumericCellValue();
            }
            case BOOLEAN -> {
                prepared.kind = PreparedCellKind.CONSTANT_BOOLEAN;
                prepared.constantBoolean = templateCell.getBooleanCellValue();
            }
            case STRING -> prepareStringCell(band, prepared, templateCell.getStringCellValue());
            default -> prepared.kind = PreparedCellKind.BLANK;
        }
        return prepared;
    }

    /**
     * Classifies a string template cell: a lone {@code ${alias}} becomes a typed value lookup, text with
     * aliases is split into literal/alias segments once (so no regex runs per row), plain text becomes a
     * constant. Content inliner formats are rejected here, before any row is written.
     */
    protected void prepareStringCell(TemplateBand band, PreparedCell prepared, String text) {
        // The single-alias test runs on the raw text, not a trimmed copy: the non-streaming engine
        // (XlsxFormatter.updateCell) matches the whole untrimmed cell value, so surrounding whitespace
        // makes the cell fall through to the text path (keeping the spaces) instead of being typed. Also,
        // containsJustOneAlias matches any ${...}, but the alias grammar rejects names with characters like
        // a hyphen; when the name does not resolve, treat the cell as literal text (the non-streaming engine
        // leaves it untouched) instead of writing a blank cell for a null parameter.
        String singleParam = containsJustOneAlias(text) ? unwrapParameterName(text) : null;
        if (singleParam != null) {
            prepared.kind = PreparedCellKind.SINGLE_ALIAS;
            prepared.param = singleParam;
            prepared.fullParameterName = band.name + "." + singleParam;
            prepared.formatString = getFormatString(singleParam, prepared.fullParameterName);
            if (prepared.formatString != null && getContentInlinerForFormat(prepared.formatString) != null) {
                throw wrapWithReportingException(String.format(
                        "Content inliner formats are not supported by the streaming XLSX formatter "
                                + "(parameter [%s], format [%s]). Clear the band's streaming flag to render "
                                + "inline images via the non-streaming engine", prepared.param, prepared.formatString));
            }
            return;
        }
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(text);
        List<TextSegment> segments = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                segments.add(TextSegment.literal(text.substring(last, matcher.start())));
            }
            String param = unwrapParameterName(matcher.group());
            segments.add(TextSegment.alias(param, band.name + "." + param));
            last = matcher.end();
        }
        if (segments.isEmpty()) {
            prepared.kind = PreparedCellKind.CONSTANT_STRING;
            prepared.constantString = text;
            return;
        }
        if (last < text.length()) {
            segments.add(TextSegment.literal(text.substring(last)));
        }
        prepared.kind = PreparedCellKind.TEXT_SEGMENTS;
        prepared.segments = segments;
    }

    /**
     * Parses an in-row formula once and records which reference tokens point inside the band's template
     * rows; per instance only those rows are re-assigned (base + shift) before rendering the tokens back.
     * String literals, function names, defined names and sheet-qualified references are never touched.
     */
    protected PreparedFormula prepareFormula(String formula, TemplateBand band) {
        Ptg[] ptgs = parseFormula(formula);
        List<RowEndpoint> endpoints = new ArrayList<>();
        for (int i = 0; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            if (ptg instanceof Ref3DPxg || ptg instanceof Area3DPxg) {
                continue; // sheet-qualified references are never rewritten
            }
            if (ptg instanceof RefPtgBase ref) {
                rejectSingleRefBelowBand(ref, band, formula);
                endpoints.add(new RowEndpoint(i, RowEndpointKind.SINGLE, ref.getRow(),
                        instanceShift(ref.getRow(), ref.isRowRelative(), band)));
            } else if (ptg instanceof AreaPtgBase area) {
                boolean firstIn = inBand(area.getFirstRow(), band);
                boolean lastIn = inBand(area.getLastRow(), band);
                if (firstIn && !lastIn && area.isLastRowRelative()) {
                    // The range starts inside the band but its relative end lies below it. Forward-only
                    // rendering cannot track that lower endpoint: it stays pinned to its template row while
                    // rows below the band shift down as the band grows, silently producing a wrong
                    // (eventually backwards) range. Reject the template instead of emitting bad output. An
                    // endpoint ABOVE the band is fine and kept below: it stays put, e.g. a running total
                    // SUM(A$1:An) anchored on a static row above the band.
                    throw wrapWithReportingException(String.format(
                            "The streaming XLSX formatter does not support an in-band formula whose range "
                                    + "starts inside its band [%s] and extends below it (formula [%s]); keep "
                                    + "the range within the band, move the aggregate to a row below the band, "
                                    + "or clear the band's streaming flag to use the non-streaming engine",
                            band.name, formula));
                }
                endpoints.add(new RowEndpoint(i, RowEndpointKind.AREA_FIRST, area.getFirstRow(),
                        instanceShift(area.getFirstRow(), area.isFirstRowRelative(), band)));
                endpoints.add(new RowEndpoint(i, RowEndpointKind.AREA_LAST, area.getLastRow(),
                        instanceShift(area.getLastRow(), area.isLastRowRelative(), band)));
            }
        }
        return new PreparedFormula(ptgs, endpoints);
    }

    protected boolean inBand(int row, TemplateBand band) {
        return row >= band.firstRow && row <= band.lastRow;
    }

    /**
     * A row endpoint tracks the band instance only when it is a relative reference inside the band. An
     * absolute ({@code $}) row is anchored by definition, and a row outside the band belongs to a static
     * row (or a not-yet-written forward row) — neither follows the instance.
     */
    protected boolean instanceShift(int row, boolean rowRelative, TemplateBand band) {
        return rowRelative && inBand(row, band);
    }

    /**
     * Rejects an in-band single-cell reference pointing to a row below the band. That row is written after
     * the band, so its output position is unknown while the band renders and cannot be resolved forward-only
     * — mirrors the rejection of an area whose relative end lies below the band. A reference inside the band
     * shifts per instance; one above the band re-bases onto the already-written row, so neither is rejected.
     */
    protected void rejectSingleRefBelowBand(RefPtgBase ref, TemplateBand band, String formula) {
        if (ref.getRow() > band.lastRow) {
            throw wrapWithReportingException(String.format(
                    "The streaming XLSX formatter does not support an in-band formula that references a row "
                            + "below its band [%s] (formula [%s]); forward-only rendering writes that row after "
                            + "the band, so its position is unknown while the band renders. Keep the reference "
                            + "within the band, move the formula to a row below the band, or clear the band's "
                            + "streaming flag to use the non-streaming engine", band.name, formula));
        }
    }

    protected String renderPreparedFormula(PreparedFormula formula, int rowShift) {
        for (RowEndpoint endpoint : formula.endpoints) {
            int newRow = endpoint.instanceShift()
                    ? endpoint.templateRow() + rowShift
                    // An out-of-band or absolute endpoint does not follow the instance. If it points at a
                    // static row already written above, re-base onto that row's actual output position (it
                    // may have shifted down as bands above it expanded); otherwise keep the template row.
                    : staticRowOutputs.getOrDefault(endpoint.templateRow(), endpoint.templateRow());
            Ptg ptg = formula.ptgs[endpoint.ptgIdx()];
            switch (endpoint.kind()) {
                case SINGLE -> ((RefPtgBase) ptg).setRow(newRow);
                case AREA_FIRST -> ((AreaPtgBase) ptg).setFirstRow(newRow);
                case AREA_LAST -> ((AreaPtgBase) ptg).setLastRow(newRow);
            }
        }
        return renderFormula(formula.ptgs);
    }

    protected void renderPreparedCell(PreparedCell prepared, Cell out, BandData instance, int rowShift,
                                      TemplateBand band) {
        switch (prepared.kind) {
            case BLANK -> { /* style only */ }
            case CONSTANT_STRING -> out.setCellValue(prepared.constantString);
            case CONSTANT_NUMERIC -> out.setCellValue(prepared.constantNumeric);
            case CONSTANT_BOOLEAN -> out.setCellValue(prepared.constantBoolean);
            case FORMULA -> out.setCellFormula(renderCellFormula(prepared, band, instance, rowShift));
            case SINGLE_ALIAS -> renderSingleAlias(prepared, out, instance);
            case TEXT_SEGMENTS -> renderSegments(prepared, out, instance);
        }
    }

    /**
     * Renders an in-band formula for one instance. Formulas without aliases are parsed once (cached in the
     * band layout) and only shifted; a formula carrying {@code ${alias}} is substituted and re-parsed per
     * instance, because its aliases resolve to per-instance values.
     */
    protected String renderCellFormula(PreparedCell prepared, TemplateBand band, BandData instance, int rowShift) {
        PreparedFormula formula = prepared.formula;
        if (prepared.formulaTemplate != null) {
            formula = prepareFormula(insertBandDataToString(instance, prepared.formulaTemplate), band);
        }
        return renderPreparedFormula(formula, rowShift);
    }

    /**
     * Writes a lone-alias cell value replicating the non-streaming engine's typing rules: the raw value's
     * Excel type (numeric, boolean, date serial) unless an explicit format string turns it into text.
     */
    protected void renderSingleAlias(PreparedCell prepared, Cell out, BandData instance) {
        Object value = instance.getData().get(prepared.param);
        if (value == null) {
            out.setBlank();
            return;
        }
        if (prepared.formatString != null) {
            out.setCellValue(formatValue(value, prepared.param, prepared.fullParameterName));
        } else if (value instanceof Boolean) {
            out.setCellValue((Boolean) value);
        } else if (value instanceof Number number) {
            writeNumericValue(out, number);
        } else if (value instanceof Date) {
            out.setCellValue(DateUtil.getExcelDate((Date) value));
        } else if (value instanceof LocalDate) {
            out.setCellValue(DateUtil.getExcelDate((LocalDate) value));
        } else if (value instanceof LocalDateTime) {
            out.setCellValue(DateUtil.getExcelDate((LocalDateTime) value));
        } else if (value instanceof LocalTime) {
            out.setCellValue(((LocalTime) value).toNanoOfDay() / NANOS_PER_DAY);
        } else {
            out.setCellValue(formatValue(value, prepared.param, prepared.fullParameterName));
        }
    }

    /**
     * Writes a numeric value as a numeric cell ({@code Number -> numeric}). POI can set a numeric cell only
     * through a {@code double}, so an integer whose magnitude exceeds a double's exact range (2^53) — a
     * BIGINT or snowflake id — loses its low digits here. This differs from the non-streaming engine, which
     * writes the exact decimal string into the numeric cell (Excel still rounds it to a double when the file
     * is opened, but the raw file keeps the exact digits). To keep such a value exact in the streamed file,
     * author an explicit field format so it is emitted as text, or select it as a string in the query.
     */
    protected void writeNumericValue(Cell out, Number number) {
        if (number instanceof Float) {
            // Float.doubleValue() widens e.g. 0.1f to 0.10000000149011612; the non-streaming engine
            // writes String.valueOf(value) ("0.1"), so round-trip through the float's own shortest
            // string to keep the value byte-compatible with the non-streaming engine.
            out.setCellValue(Double.parseDouble(number.toString()));
        } else {
            out.setCellValue(number.doubleValue());
        }
    }

    protected void renderSegments(PreparedCell prepared, Cell out, BandData instance) {
        StringBuilder sb = new StringBuilder();
        for (TextSegment segment : prepared.segments) {
            if (segment.literal != null) {
                sb.append(segment.literal);
            } else {
                Object value = instance.getData().get(segment.param);
                sb.append(formatValue(value, segment.param, segment.fullParameterName));
            }
        }
        out.setCellValue(sb.toString());
    }

    protected Ptg[] parseFormula(String formula) {
        try {
            return FormulaParser.parse(formula, evaluationWorkbook, FormulaType.CELL, 0, -1);
        } catch (FormulaParseException e) {
            throw wrapWithReportingException("Cannot parse template formula [" + formula + "]", e);
        }
    }

    protected String renderFormula(Ptg[] ptgs) {
        return FormulaRenderer.toFormulaString(evaluationWorkbook, ptgs);
    }

    @Nullable
    protected String styleHintParamAt(int templateRow, int templateCol) {
        for (HintRange hint : styleHints) {
            if (hint.contains(templateRow, templateCol)) {
                return hint.param;
            }
        }
        return null;
    }

    /**
     * {@code hint_style_<param>}: if the template cell lies in a style-hint range, the band supplies the
     * name of a template named cell style to apply. Unknown names or missing values keep the base style.
     */
    protected void applyPreparedStyleHint(PreparedCell prepared, Cell out, BandData instance) {
        if (prepared.styleHintParam == null) {
            return;
        }
        Object styleName = instance.getParameterValue(prepared.styleHintParam);
        if (styleName != null) {
            CellStyle named = resolveNamedStyle(styleName.toString(), prepared.templateCell);
            if (named != null) {
                out.setCellStyle(named);
            }
        }
    }

    /**
     * Materializes a template named cell style in the result workbook. Mirrors the non-streaming
     * {@code CustomCellStyleXlsxHint}: the named style's attributes are combined with the template
     * cell's border, registered as a regular cell style, and cloned into the result workbook once.
     */
    @Nullable
    protected CellStyle resolveNamedStyle(String styleName, XSSFCell templateCell) {
        Integer xfId = namedStyleXfIds.get(styleName);
        if (xfId == null) {
            return null;
        }
        XSSFCellStyle templateCellStyle = templateCell.getCellStyle();
        // The result combines the named style's font/fill/format with the calling cell's OWN border, so
        // cache per (name, cell style): two cells sharing a named style but with different borders must
        // not share one result style.
        String cacheKey = styleName + "#" + (templateCellStyle != null ? templateCellStyle.getIndex() : -1);
        CellStyle cached = namedResultStyles.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        StylesTable stylesTable = templateWorkbook.getStylesSource();
        CTXf styleXf = stylesTable.getCTStylesheet().getCellStyleXfs().getXfArray(xfId);
        CTXf cellXf = (CTXf) styleXf.copy();
        cellXf.setXfId(xfId);
        cellXf.setApplyFont(true);
        cellXf.setApplyFill(true);
        cellXf.setApplyNumberFormat(true);
        int newSize = stylesTable.putCellXf(cellXf);
        XSSFCellStyle materialized = stylesTable.getStyleAt(newSize - 1);
        // Clone the named font/fill/format into the result workbook via the usermodel API (the low-level
        // borderId of the template stylesheet does not survive), then overlay the cell's own border.
        XSSFCellStyle result = (XSSFCellStyle) resultWorkbook.createCellStyle();
        result.cloneStyleFrom(materialized);
        applyCellBorder(result, templateCellStyle);
        namedResultStyles.put(cacheKey, result);
        return result;
    }

    /** Overlays the template cell's own border (styles and colors) onto a named-style result cell. */
    protected void applyCellBorder(XSSFCellStyle result, @Nullable XSSFCellStyle source) {
        if (source == null) {
            return;
        }
        result.setBorderTop(source.getBorderTop());
        result.setBorderRight(source.getBorderRight());
        result.setBorderBottom(source.getBorderBottom());
        result.setBorderLeft(source.getBorderLeft());
        XSSFColor top = source.getTopBorderXSSFColor();
        if (top != null) {
            result.setTopBorderColor(top);
        }
        XSSFColor right = source.getRightBorderXSSFColor();
        if (right != null) {
            result.setRightBorderColor(right);
        }
        XSSFColor bottom = source.getBottomBorderXSSFColor();
        if (bottom != null) {
            result.setBottomBorderColor(bottom);
        }
        XSSFColor left = source.getLeftBorderXSSFColor();
        if (left != null) {
            result.setLeftBorderColor(left);
        }
    }

    /**
     * Precomputes the {@code hint_rowAutoHeight} metrics of a template cell: hint membership, the
     * (merge-aware) column width, the indent width and the font. Cells inside multi-row merges are
     * excluded up front.
     */
    protected void prepareAutoHeight(PreparedCell prepared, XSSFCell templateCell, int templateRow,
                                     int templateCol) {
        if (rowAutoHeightHints.isEmpty()) {
            return;
        }
        boolean inRange = false;
        for (HintRange hint : rowAutoHeightHints) {
            if (hint.contains(templateRow, templateCol)) {
                inRange = true;
                break;
            }
        }
        if (!inRange) {
            return;
        }
        CellRangeAddress merge = templateMergeAt(templateRow, templateCol);
        if (merge != null && merge.getFirstRow() != merge.getLastRow()) {
            return;
        }
        int columnWidthPx = mergedOrColumnWidthInPixels(templateCell.getSheet(), templateCol, merge);
        int indent = templateCell.getCellStyle() != null ? templateCell.getCellStyle().getIndention() : 0;
        prepared.autoHeight = true;
        prepared.autoHeightWidthPx = columnWidthPx;
        prepared.autoHeightIndentPx = getIndentInPixels(indent);
        prepared.autoHeightFont = templateCell.getCellStyle() != null
                ? templateCell.getCellStyle().getFont()
                : templateWorkbook.getStylesSource().getFontAt(0);
    }

    /**
     * {@code hint_rowAutoHeight}: measures the written text against the precomputed column width and
     * font, and raises the row height so wrapped text stays visible. Mirrors the non-streaming
     * {@code RowAutoHeightXlsxHint}; the height is capped at {@link #MAX_ROW_HEIGHT}.
     */
    protected void applyPreparedAutoHeight(PreparedCell prepared, Cell outCell, SXSSFRow outRow) {
        if (!prepared.autoHeight || outCell.getCellType() != CellType.STRING) {
            return;
        }
        String cellValue = outCell.getStringCellValue();
        if (cellValue == null || cellValue.isEmpty()) {
            return;
        }
        int availableWidth = prepared.autoHeightWidthPx - prepared.autoHeightIndentPx - 3;
        if (availableWidth <= 0) {
            availableWidth = prepared.autoHeightWidthPx;
        }
        if (availableWidth <= 0) {
            return;
        }
        double cellHeight = calculateCellHeight(prepared.autoHeightFont, cellValue, availableWidth);
        double newHeight = Math.min(cellHeight, MAX_ROW_HEIGHT);
        if (newHeight > outRow.getHeightInPoints()) {
            outRow.setHeightInPoints((float) newHeight);
        }
    }

    @Nullable
    protected CellRangeAddress templateMergeAt(int row, int col) {
        for (CellRangeAddress merge : templateMerges) {
            if (merge.isInRange(row, col)) {
                return merge;
            }
        }
        return null;
    }

    protected int mergedOrColumnWidthInPixels(XSSFSheet templateSheet, int col,
                                              @Nullable CellRangeAddress merge) {
        if (merge == null) {
            return (int) templateSheet.getColumnWidthInPixels(col);
        }
        double width = 0;
        for (int c = merge.getFirstColumn(); c <= merge.getLastColumn(); c++) {
            width += templateSheet.getColumnWidthInPixels(c);
        }
        return (int) width;
    }

    /** Measures wrapped text height: {@code fontSize * 1.3 * lineCount}, ceiled to the nearest 0.25pt. */
    protected double calculateCellHeight(XSSFFont font, String cellValue, int cellWidthInPixels) {
        AttributedString attrStr = createAttributedString(font, cellValue);
        LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), fontRenderContext);
        int lineCount = 0;
        while (measurer.getPosition() < cellValue.length()) {
            measurer.setPosition(measurer.nextOffset(cellWidthInPixels));
            lineCount++;
        }
        double fontSize = font.getFontHeightInPoints() > 0 ? font.getFontHeightInPoints() : DEFAULT_FONT_SIZE;
        double cellHeight = fontSize * 1.3 * lineCount;
        return Math.ceil(cellHeight * 4) / 4f;
    }

    /** One indent "point" equals three whitespaces in the default font. */
    protected int getIndentInPixels(int indent) {
        if (indent == 0) {
            return 0;
        }
        XSSFFont defaultFont = templateWorkbook.getStylesSource().getFontAt(0);
        AttributedString str = createAttributedString(defaultFont, " ".repeat(indent * 3));
        TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
        return (int) Math.ceil(layout.getAdvance());
    }

    protected AttributedString createAttributedString(XSSFFont font, String value) {
        AttributedString attrStr = new AttributedString(value);
        attrStr.addAttribute(TextAttribute.FAMILY, font.getFontName());
        attrStr.addAttribute(TextAttribute.SIZE,
                font.getFontHeightInPoints() > 0 ? (int) font.getFontHeightInPoints() : DEFAULT_FONT_SIZE);
        if (font.getBold()) {
            attrStr.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if (font.getItalic()) {
            attrStr.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        if (font.getUnderline() != Font.U_NONE) {
            attrStr.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        return attrStr;
    }

    /** Row grouping: replicates the template row's outline level onto the rendered row. */
    protected void copyRowOutlineLevel(XSSFRow templateRow, SXSSFSheet resultSheet, int outRowIdx) {
        int outlineLevel = templateRow.getOutlineLevel();
        if (outlineLevel > 0) {
            resultSheet.setRowOutlineLevel(outRowIdx, outlineLevel);
        }
    }

    /**
     * The template bands that are actual child bands of the given one, in row order. Children are decided
     * by the {@link BandData} tree (not by mere row position), so an unrelated sibling band laid out below
     * the parent is not pulled into the parent's render — it is emitted by the main template walk in its
     * template position instead.
     */
    protected List<TemplateBand> childTemplateBandsOf(TemplateBand parent) {
        Set<String> children = childBandNames.getOrDefault(parent.name, Set.of());
        return templateBands.values().stream()
                .filter(b -> b.firstRow > parent.lastRow && children.contains(b.name))
                .sorted(Comparator.comparingInt(b -> b.firstRow))
                .collect(Collectors.toList());
    }

    /**
     * Copies a template row that lies outside every band range. Aliases are resolved through band paths
     * ({@code ${Band.child.param}}); aliases that resolve to no band are left untouched.
     */
    protected void copyStaticRow(XSSFSheet templateSheet, SXSSFSheet resultSheet, int templateRowIdx,
                                 WriteCursor cursor) {
        XSSFRow templateRow = templateSheet.getRow(templateRowIdx);
        int outRowIdx = cursor.nextRow++;
        staticRowOutputs.put(templateRowIdx, outRowIdx);
        if (templateRow == null) {
            return;
        }
        SXSSFRow out = createResultRow(resultSheet, outRowIdx);
        copyRowOutlineLevel(templateRow, resultSheet, outRowIdx);
        for (int c = templateRow.getFirstCellNum(); c >= 0 && c < templateRow.getLastCellNum(); c++) {
            XSSFCell templateCell = templateRow.getCell(c);
            if (templateCell == null) {
                continue;
            }
            Cell outCell = out.createCell(c);
            outCell.setCellStyle(styleCache.resultStyleFor(templateCell.getCellStyle()));
            writeStaticCellValue(templateCell, outCell);
        }
    }

    protected void writeStaticCellValue(XSSFCell templateCell, Cell out) {
        switch (templateCell.getCellType()) {
            case STRING:
                out.setCellValue(insertBandDataToStringByPath(templateCell.getStringCellValue()));
                break;
            case NUMERIC:
                out.setCellValue(templateCell.getNumericCellValue());
                break;
            case BOOLEAN:
                out.setCellValue(templateCell.getBooleanCellValue());
                break;
            case FORMULA:
                writeOuterFormula(templateCell, out);
                break;
            default:
                break;
        }
    }

    /**
     * Writes a formula placed outside band ranges (typically a total row below the data). Range references
     * pointing into a band's template rectangle are grown to cover all rows the band actually rendered;
     * a reference to a band that rendered nothing turns the cell into an error text, matching the
     * non-streaming engine.
     */
    protected void writeOuterFormula(XSSFCell templateCell, Cell out) {
        String formula = insertBandDataToStringByPath(templateCell.getCellFormula());
        rejectForwardBandReference(formula, templateCell.getRowIndex());
        String grown = growOuterFormula(formula);
        if (grown == null) {
            out.setCellValue("ERROR: Formula references to empty range");
        } else {
            out.setCellFormula(grown);
        }
    }

    /**
     * A formula outside band ranges may reference a band ABOVE it (a running total below the data), but a
     * reference to a band laid out BELOW the formula cannot be resolved forward-only: the band's rows are
     * written after the formula, so the reference would otherwise silently render the empty-range error
     * text (as if the band had no data). Reject it up front with a clear message instead.
     */
    protected void rejectForwardBandReference(String formula, int formulaTemplateRow) {
        for (Ptg ptg : parseFormula(formula)) {
            if (ptg instanceof Ref3DPxg || ptg instanceof Area3DPxg) {
                continue;
            }
            if (ptg instanceof RefPtgBase ref) {
                rejectIfBandBelow(ref.getRow(), formulaTemplateRow, formula);
            } else if (ptg instanceof AreaPtgBase area) {
                rejectIfBandBelow(area.getFirstRow(), formulaTemplateRow, formula);
                rejectIfBandBelow(area.getLastRow(), formulaTemplateRow, formula);
            }
        }
    }

    /**
     * Rejects only when an area/single-reference ENDPOINT lands inside a band positioned below the formula
     * — the case that truly cannot be resolved forward-only, where {@link #growOuterFormula} would try to
     * grow the reference onto rows the band has not written yet and emit the misleading empty-range error.
     * A wide or whole-column reference (e.g. {@code SUM(A:A)}) whose endpoints do not fall on a
     * below-formula band is left as authored: it needs no growth and Excel computes it on open.
     */
    protected void rejectIfBandBelow(int referencedRow, int formulaTemplateRow, String formula) {
        TemplateBand band = bandContainingTemplateRow(referencedRow);
        if (band != null && band.firstRow > formulaTemplateRow) {
            throw wrapWithReportingException(String.format(
                    "The streaming XLSX formatter does not support a formula that references a band laid "
                            + "out below it (formula [%s] at row %d references band [%s] at rows %d-%d). "
                            + "Forward-only rendering cannot resolve a total placed above its data; move the "
                            + "formula below the band or clear the band's streaming flag.",
                    formula, formulaTemplateRow + 1, band.name, band.firstRow + 1, band.lastRow + 1));
        }
    }

    /**
     * Rewrites a formula placed outside band ranges: area references into a band grow over all rows the
     * band rendered; single references into a band re-base onto the same offset within the band's LAST
     * rendered instance (the "totals below the data" convention); references to static template rows
     * re-base onto the row's actual output position. A forward reference to a band laid out below the
     * formula is rejected up front (see {@link #rejectForwardBandReference}); a forward reference to a
     * static row not written yet is kept as in the template. Returns {@code null} when a
     * referenced band produced no rows.
     */
    @Nullable
    protected String growOuterFormula(String formula) {
        Ptg[] ptgs = parseFormula(formula);
        for (Ptg ptg : ptgs) {
            if (ptg instanceof Ref3DPxg || ptg instanceof Area3DPxg) {
                continue;
            }
            if (ptg instanceof AreaPtgBase area) {
                Integer first = resolveOuterAreaRow(area.getFirstRow(), true);
                Integer last = resolveOuterAreaRow(area.getLastRow(), false);
                if (first == null || last == null) {
                    return null;
                }
                area.setFirstRow(first);
                area.setLastRow(last);
            } else if (ptg instanceof RefPtgBase ref) {
                // Same endpoint mapping as an area's last endpoint: a reference into a band re-bases onto its
                // last rendered instance, one on a static row onto that row's output position, and a
                // reference into a band that produced no rows returns null (empty-range error).
                Integer row = resolveOuterAreaRow(ref.getRow(), false);
                if (row == null) {
                    return null;
                }
                ref.setRow(row);
            }
        }
        return renderFormula(ptgs);
    }

    /**
     * Maps one endpoint of an outer area reference to its rendered row, classifying each endpoint on its
     * own so an area straddling a band and static rows is handled correctly. An endpoint inside a band
     * preserves its offset within the band block: the first endpoint re-bases onto the first rendered
     * instance (its start plus the offset), the last endpoint onto the last rendered instance, so a range
     * covering only part of a multi-row band is not silently widened to the whole band. An endpoint on a
     * static row re-bases onto that row's output position, or keeps its template row when the row has not
     * been written yet (forward reference). Returns {@code null} when the endpoint points into a band that
     * produced no rows.
     */
    @Nullable
    protected Integer resolveOuterAreaRow(int templateRow, boolean firstEndpoint) {
        TemplateBand band = bandContainingTemplateRow(templateRow);
        if (band != null) {
            int offset = templateRow - band.firstRow;
            if (firstEndpoint) {
                int[] span = renderedBandRows.get(band.name);
                return span == null ? null : span[0] + offset;
            }
            Integer lastInstanceStart = lastInstanceStarts.get(band.name);
            return lastInstanceStart == null ? null : lastInstanceStart + offset;
        }
        Integer out = staticRowOutputs.get(templateRow);
        return out != null ? out : templateRow;
    }

    @Nullable
    protected TemplateBand bandContainingTemplateRow(int templateRow) {
        for (TemplateBand band : templateBands.values()) {
            if (templateRow >= band.firstRow && templateRow <= band.lastRow) {
                return band;
            }
        }
        return null;
    }

    protected void recordRenderedRows(String bandName, int firstOutputRow, int lastOutputRow) {
        int[] span = renderedBandRows.get(bandName);
        if (span == null) {
            renderedBandRows.put(bandName, new int[]{firstOutputRow, lastOutputRow});
        } else {
            span[1] = lastOutputRow;
        }
    }

    /**
     * Resolves {@code ${Band.path.param}} aliases against the band tree (mirrors the non-streaming
     * engine's sheet-name/header substitution). Aliases whose band path cannot be resolved are kept as is.
     */
    protected String insertBandDataToStringByPath(String resultStr) {
        List<String> parametersToInsert = new ArrayList<>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(resultStr);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            BandPathAndParameterName pathAndName = separateBandNameAndParameterName(parameterName);
            if (pathAndName.getBandPath().isEmpty()) {
                continue;
            }
            BandData bandData = findBandByPath(pathAndName.getBandPath());
            if (bandData == null) {
                continue;
            }
            Object value = bandData.getData().get(pathAndName.getParameterName());
            String valueStr = formatValue(value, parameterName, getFullParameterName(bandData, parameterName));
            resultStr = inlineParameterValue(resultStr, parameterName, valueStr);
        }
        return resultStr;
    }

    /** Cursor holding the next output row index to write. */
    protected static class WriteCursor {
        protected int nextRow = 0;
    }

    /** What a template cell contributes per band instance, classified once per band. */
    protected enum PreparedCellKind {
        BLANK, CONSTANT_STRING, CONSTANT_NUMERIC, CONSTANT_BOOLEAN, SINGLE_ALIAS, TEXT_SEGMENTS, FORMULA
    }

    /** A literal or alias piece of a text-with-aliases template cell. */
    protected static class TextSegment {
        @Nullable
        protected final String literal;
        @Nullable
        protected final String param;
        @Nullable
        protected final String fullParameterName;

        protected TextSegment(@Nullable String literal, @Nullable String param,
                              @Nullable String fullParameterName) {
            this.literal = literal;
            this.param = param;
            this.fullParameterName = fullParameterName;
        }

        protected static TextSegment literal(String literal) {
            return new TextSegment(literal, null, null);
        }

        protected static TextSegment alias(String param, String fullParameterName) {
            return new TextSegment(null, param, fullParameterName);
        }
    }

    /**
     * A template formula parsed once: the token array plus the row endpoints that must be re-targeted per
     * band instance. The tokens are mutated in place on each render; every endpoint carries its own
     * template row, so re-targeting always starts from the template, never from a previous instance.
     */
    protected static class PreparedFormula {
        protected final Ptg[] ptgs;
        protected final List<RowEndpoint> endpoints;

        protected PreparedFormula(Ptg[] ptgs, List<RowEndpoint> endpoints) {
            this.ptgs = ptgs;
            this.endpoints = endpoints;
        }
    }

    /** Which row of a reference token a {@link RowEndpoint} addresses. */
    protected enum RowEndpointKind {
        SINGLE, AREA_FIRST, AREA_LAST
    }

    /**
     * One re-targetable row of a reference token in a parsed formula: the token index, which row it
     * addresses, its template row, and whether it tracks the band instance (relative + inside the band)
     * or is re-based against {@link #staticRowOutputs}.
     */
    protected record RowEndpoint(int ptgIdx, RowEndpointKind kind, int templateRow, boolean instanceShift) {
    }

    /** Everything a band template cell needs per instance, precomputed once. */
    protected static class PreparedCell {
        protected final XSSFCell templateCell;
        protected final int col;
        protected PreparedCellKind kind = PreparedCellKind.BLANK;
        protected CellStyle baseStyle;
        protected String constantString;
        protected double constantNumeric;
        protected boolean constantBoolean;
        protected String param;
        protected String fullParameterName;
        @Nullable
        protected String formatString;
        protected List<TextSegment> segments;
        protected PreparedFormula formula;
        /** Raw formula text with unresolved {@code ${alias}}; substituted and parsed per instance. */
        @Nullable
        protected String formulaTemplate;
        @Nullable
        protected String styleHintParam;
        protected boolean autoHeight;
        protected int autoHeightWidthPx;
        protected int autoHeightIndentPx;
        protected XSSFFont autoHeightFont;

        protected PreparedCell(XSSFCell templateCell, int col) {
            this.templateCell = templateCell;
            this.col = col;
        }
    }

    /** One template row of a band: outline level and the prepared cells (absent rows render empty). */
    protected static class PreparedRow {
        protected final boolean absent;
        protected final int outlineLevel;
        protected final List<PreparedCell> cells = new ArrayList<>();

        protected PreparedRow(int outlineLevel) {
            this.absent = false;
            this.outlineLevel = outlineLevel;
        }

        private PreparedRow() {
            this.absent = true;
            this.outlineLevel = 0;
        }

        protected static PreparedRow absentRow() {
            return new PreparedRow();
        }
    }

    /** Precomputed render plan of a band: rows/cells, in-band merges and the ordered child bands. */
    protected static class BandLayout {
        protected final List<PreparedRow> rows = new ArrayList<>();
        protected final List<CellRangeAddress> merges = new ArrayList<>();
        protected final List<TemplateBand> childBands;

        protected BandLayout(List<TemplateBand> childBands) {
            this.childBands = childBands;
        }
    }

    /** A {@code hint_<name>_<param>} template rectangle. Coordinates are 0-based, inclusive. */
    protected static class HintRange {
        protected final String param;
        protected final int firstRow;
        protected final int lastRow;
        protected final int firstCol;
        protected final int lastCol;

        protected HintRange(String param, int firstRow, int lastRow, int firstCol, int lastCol) {
            this.param = param;
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
        }

        protected boolean contains(int row, int col) {
            return row >= firstRow && row <= lastRow && col >= firstCol && col <= lastCol;
        }
    }
}
