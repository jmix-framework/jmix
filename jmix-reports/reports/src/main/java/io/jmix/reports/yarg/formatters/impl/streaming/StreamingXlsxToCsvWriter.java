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
package io.jmix.reports.yarg.formatters.impl.streaming;

import com.opencsv.CSVWriter;
import io.jmix.reports.yarg.exception.ReportFormattingException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.jspecify.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a streamed XLSX file into CSV with a single SAX pass, so memory stays O(1) regardless of the
 * row count: semicolon-separated, one line per non-empty sheet row, in document order.
 *
 * <p>The output approximates the non-streaming engine's {@code saveXlsxAsCsv} but is deliberately not
 * byte-identical, because a streamed workbook stores values differently from the docx4j template cells
 * the old engine copies verbatim:
 * <ul>
 *   <li>Numbers are read back as doubles and normalized by {@code formatNumeric} (whole numbers without
 *       a trailing {@code .0}, never scientific notation). A value the non-streaming engine emitted as
 *       {@code 5.0}, {@code 45000.0} (a date serial) or {@code 1.2E12} is rendered here as {@code 5},
 *       {@code 45000} and {@code 1200000000000}.</li>
 *   <li>Formula cells carry no cached value (the workbook is written with {@code fullCalcOnLoad}), so
 *       they come out empty; a row whose only content is a formula is treated as empty and skipped.</li>
 *   <li>Empty cells are position-padded from each cell's reference, so a value that does not start in
 *       column A keeps its column index instead of being packed to the left as the old engine does.</li>
 * </ul>
 */
public class StreamingXlsxToCsvWriter {

    public static void convert(File xlsxFile, OutputStream outputStream) {
        try (OPCPackage pkg = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                    ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            try (InputStream sheetStream = reader.getSheetsData().next()) {
                XMLReader parser = XMLHelper.newXMLReader();
                parser.setContentHandler(new SheetToCsvHandler(writer));
                parser.parse(new InputSource(sheetStream));
            }
            writer.flush();
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while converting streamed XLSX to CSV", e);
        }
    }

    protected static class SheetToCsvHandler extends DefaultHandler {

        protected final CSVWriter writer;
        protected final List<String> currentRow = new ArrayList<>();
        protected final StringBuilder currentValue = new StringBuilder();
        protected boolean inValue = false;
        protected boolean cellHasValue = false;
        protected boolean cellHasFormula = false;
        protected int currentColumn = -1;
        @Nullable
        protected String currentCellType;

        protected SheetToCsvHandler(CSVWriter writer) {
            this.writer = writer;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            switch (qName) {
                case "row":
                    currentRow.clear();
                    break;
                case "c":
                    currentValue.setLength(0);
                    cellHasValue = false;
                    cellHasFormula = false;
                    currentColumn = columnIndex(attributes.getValue("r"));
                    currentCellType = attributes.getValue("t");
                    break;
                case "f":
                    cellHasFormula = true;
                    break;
                case "v":
                case "t":
                    inValue = true;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (inValue) {
                currentValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "v":
                case "t":
                    inValue = false;
                    cellHasValue = true;
                    break;
                case "c":
                    // SXSSF omits <c> elements for empty cells; pad the columns skipped before this one
                    // from its r reference so later columns keep their position instead of shifting left.
                    int target = currentColumn >= 0 ? currentColumn : currentRow.size();
                    while (currentRow.size() < target) {
                        currentRow.add(null);
                    }
                    // A formula cell carries a stale cached value (POI writes <v>0</v> for the unevaluated
                    // formula, and the workbook asks Excel to recalc on open), so emit it empty rather than
                    // the misleading cached number — matching this converter's documented contract.
                    String cellText = (cellHasValue && !cellHasFormula) ? currentValue.toString() : null;
                    if (cellText != null && isNumericType(currentCellType)) {
                        cellText = formatNumeric(cellText);
                    }
                    currentRow.add(cellText);
                    break;
                case "row":
                    boolean emptyRow = currentRow.stream().allMatch(v -> v == null || v.isEmpty());
                    if (!emptyRow) {
                        writer.writeNext(currentRow.toArray(new String[0]));
                    }
                    break;
                default:
                    break;
            }
        }

        /** Column index (0-based) parsed from a cell reference such as {@code B3}, or -1 when absent. */
        protected int columnIndex(@Nullable String cellRef) {
            if (cellRef == null || cellRef.isEmpty()) {
                return -1;
            }
            return new CellReference(cellRef).getCol();
        }

        /** A numeric cell has no {@code t} attribute or {@code t="n"}; strings/booleans/errors carry a type. */
        protected boolean isNumericType(@Nullable String type) {
            return type == null || "n".equals(type);
        }

        /**
         * Normalizes a numeric {@code <v>} read back from the streamed workbook: a whole number without a
         * trailing {@code .0} and never in scientific notation (the workbook stores every number as a
         * double, so a plain integer would otherwise come out as {@code 5.0} or {@code 1.2E12}). Values
         * beyond the exact-integer range of a double keep whatever precision the double preserved. This is
         * close to, but not byte-identical with, the non-streaming engine's raw {@code cell.getV()} output
         * (see the class Javadoc).
         */
        protected String formatNumeric(String raw) {
            try {
                double value = Double.parseDouble(raw);
                if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
                    return Long.toString((long) value);
                }
                return new BigDecimal(raw).toPlainString();
            } catch (NumberFormatException e) {
                return raw;
            }
        }
    }
}
