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

import io.jmix.reports.yarg.structure.BandData;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.xmlbeans.XmlCursor;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.spreadsheetdrawing.CTAnchorClientData;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTPicture;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.ObjectFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XlsxUtils {

    private static final long PX_PER_INCH = 96;
    private static final long EMU_PER_INCH = 914400;

    /**
     * One area of cell references, on a quoted or plain sheet name (group 1) or without one: a rectangle or a cell
     * (groups 2-5), whole rows (groups 6-7) or whole columns (groups 8-9), e.g. {@code 'My sheet'!$A$1:$F$20},
     * {@code Sheet1!$1:$20} or {@code $A:$F}.
     */
    private static final Pattern PRINT_AREA_PATTERN = Pattern.compile(
            "(?:('(?:[^']|'')+'|[^'!,]+)!)?(?:"
                    + "\\$?([A-Z]{1,3})\\$?(\\d{1,7})(?::\\$?([A-Z]{1,3})\\$?(\\d{1,7}))?"
                    + "|\\$?(\\d{1,7}):\\$?(\\d{1,7})"
                    + "|\\$?([A-Z]{1,3}):\\$?([A-Z]{1,3}))",
            Pattern.CASE_INSENSITIVE);

    private XlsxUtils() {
    }

    public static int getNumberFromColumnReference(String columnReference) {
        int sum = 0;

        for (int i = 0; i < columnReference.length(); i++) {
            char c = columnReference.charAt(i);
            int number = ((int) c) - 64 - 1;

            int pow = columnReference.length() - i - 1;
            sum += Math.pow(26, pow) * (number + 1);
        }
        return sum;
    }

    public static String getColumnReferenceFromNumber(int number) {
        int remain = 0;
        StringBuilder ref = new StringBuilder();
        do {

            remain = (number - 1) % 26;
            number = (number - 1) / 26;

            ref.append((char) (remain + 64 + 1));
        } while (number > 0);

        return ref.reverse().toString();
    }

    public static long convertPxToEmu(long px) {
        return px * EMU_PER_INCH / PX_PER_INCH;
    }

    public static Integer computeColumnIndex(String cellName) {

        String columnLetters = cellName.replaceAll("\\d", "");

        double sum = 0;
        int len = columnLetters.length();
        for (int i = 0; i < len; i++) {
            sum += (columnLetters.charAt(i) - 'A' + 1) * Math.pow(26, len - i - 1);
        }

        return (int) sum;
    }

    public static CTPicture createPicture(String imageRelID) {

        ObjectFactory dmlSpreadsheetDrawingObjectFactory = new ObjectFactory();

        CTPicture picture = dmlSpreadsheetDrawingObjectFactory.createCTPicture();

        org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

        CTBlipFillProperties blipFillProperties = dmlObjectFactory.createCTBlipFillProperties();
        picture.setBlipFill(blipFillProperties);

        CTBlip blip = dmlObjectFactory.createCTBlip();
        blipFillProperties.setBlip(blip);
        blip.setCstate(org.docx4j.dml.STBlipCompression.NONE);
        blip.setEmbed(imageRelID);

        return picture;
    }

    public static RelationshipsPart attachImageToCell(Drawing drawing, Integer col, Integer row, XlsxImage image, String imageRelID) {
        CTPicture picture = createPicture(imageRelID);

        CTTwoCellAnchor anchor = new CTTwoCellAnchor();

        anchor.setFrom(new CTMarker());
        anchor.getFrom().setCol(col);
        anchor.getFrom().setColOff(image.getDx1());
        anchor.getFrom().setRow(row);
        anchor.getFrom().setRowOff(image.getDy1());

        anchor.setTo(new CTMarker());
        anchor.getTo().setCol(col);
        anchor.getTo().setColOff(image.getDx2());
        anchor.getTo().setRow(row);
        anchor.getTo().setRowOff(image.getDy2());

        anchor.setPic(picture);
        anchor.getPic().setSpPr(picture.getSpPr());
        anchor.setClientData(new CTAnchorClientData());
        drawing.getJaxbElement().getEGAnchor().add(anchor);

        return drawing.getRelationshipsPart();
    }

    public static Drawing getOrCreateWorksheetDrawing(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart) {
        Drawing drawing = null;
        try {
            PartName partName = new PartName(StringUtils.replaceIgnoreCase(worksheetPart.getPartName().getName(),
                    "worksheets/sheet", "drawings/drawing"));
            drawing = (Drawing) pkg.getParts().get(partName);
            if (drawing == null) {
                drawing = addCTDrawing(worksheetPart, partName);
                worksheetPart.addTargetPart(drawing);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        return drawing;
    }

    public static Drawing addCTDrawing(WorksheetPart worksheetPart, PartName drawingPart) throws Docx4JException {
        Drawing drawing = new Drawing(drawingPart);
        drawing.setContents(new CTDrawing());
        Relationship relationship = worksheetPart.addTargetPart(drawing);
        org.xlsx4j.sml.CTDrawing smlDrawing = new org.xlsx4j.sml.CTDrawing();
        smlDrawing.setId(relationship.getId());
        smlDrawing.setParent(worksheetPart.getContents());
        worksheetPart.getContents().setDrawing(smlDrawing);
        return drawing;
    }

    public static void deleteCTAnchor(XSSFPicture xssfPicture) {
        XSSFDrawing drawing = xssfPicture.getDrawing();
        try (XmlCursor cursor = xssfPicture.getCTPicture().newCursor()) {
            cursor.toParent();
            if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor) {
                for (int i = 0; i < drawing.getCTDrawing().getTwoCellAnchorList().size(); i++) {
                    if (cursor.getObject().equals(drawing.getCTDrawing().getTwoCellAnchorArray(i))) {
                        drawing.getCTDrawing().removeTwoCellAnchor(i);
                    }
                }
            } else if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor) {
                for (int i = 0; i < drawing.getCTDrawing().getOneCellAnchorList().size(); i++) {
                    if (cursor.getObject().equals(drawing.getCTDrawing().getOneCellAnchorArray(i))) {
                        drawing.getCTDrawing().removeOneCellAnchor(i);
                    }
                }
            } else if (cursor.getObject() instanceof org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTAbsoluteAnchor) {
                for (int i = 0; i < drawing.getCTDrawing().getAbsoluteAnchorList().size(); i++) {
                    if (cursor.getObject().equals(drawing.getCTDrawing().getAbsoluteAnchorArray(i))) {
                        drawing.getCTDrawing().removeAbsoluteAnchor(i);
                    }
                }
            }
        }
    }

    /**
     * Parses a print area that is one area of cell references: a rectangle, a cell, whole rows or whole columns,
     * e.g. {@code 'My sheet'!$A$1:$F$20}, {@code Sheet1!$1:$20} or {@code Sheet1!$A:$F}. Whole rows span all the
     * columns and whole columns all the rows of the spreadsheet version. The sheet name is kept the way
     * {@link Range#fromFormula(String)} keeps it: without the enclosing quotes, with the doubled ones inside; it is
     * {@code null} if the formula names no sheet. Returns {@code null} for other print areas, e.g. several areas or
     * a formula.
     */
    @Nullable
    public static Range parsePrintArea(@Nullable String formula, SpreadsheetVersion version) {
        Matcher matcher = formula != null ? PRINT_AREA_PATTERN.matcher(formula) : null;
        if (matcher == null || !matcher.matches()) {
            return null;
        }
        String sheet = matcher.group(1);
        if (sheet != null && sheet.startsWith("'")) {
            sheet = sheet.substring(1, sheet.length() - 1);
        }
        int firstColumn;
        int firstRow;
        int lastColumn;
        int lastRow;
        if (matcher.group(2) != null) {
            firstColumn = getColumnNumber(matcher.group(2));
            firstRow = Integer.parseInt(matcher.group(3));
            lastColumn = matcher.group(4) != null ? getColumnNumber(matcher.group(4)) : firstColumn;
            lastRow = matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) : firstRow;
        } else if (matcher.group(6) != null) {
            firstColumn = 1;
            firstRow = Integer.parseInt(matcher.group(6));
            lastColumn = version.getMaxColumns();
            lastRow = Integer.parseInt(matcher.group(7));
        } else {
            firstColumn = getColumnNumber(matcher.group(8));
            firstRow = 1;
            lastColumn = getColumnNumber(matcher.group(9));
            lastRow = version.getMaxRows();
        }
        return new Range(sheet, Math.min(firstColumn, lastColumn), Math.min(firstRow, lastRow),
                Math.max(firstColumn, lastColumn), Math.max(firstRow, lastRow));
    }

    /**
     * Parses a print area of one or more areas of cell references separated with commas, see
     * {@link #parsePrintArea(String, SpreadsheetVersion)}. Returns {@code null} if any of them cannot be parsed,
     * e.g. for a formula.
     */
    @Nullable
    public static List<Range> parsePrintAreas(@Nullable String formula, SpreadsheetVersion version) {
        if (formula == null) {
            return null;
        }
        List<Range> printAreas = new ArrayList<>();
        // a comma inside a quoted sheet name is followed by an odd number of quotes
        for (String area : formula.split(",(?=(?:[^']*'[^']*')*[^']*$)", -1)) {
            Range printArea = parsePrintArea(area, version);
            if (printArea == null) {
                return null;
            }
            printAreas.add(printArea);
        }
        return printAreas;
    }

    private static int getColumnNumber(String columnReference) {
        return getNumberFromColumnReference(columnReference.toUpperCase(Locale.ROOT));
    }

    /**
     * Returns whether the range spans all the rows of the spreadsheet version, i.e. consists of whole columns.
     */
    public static boolean spansAllRows(Range range, SpreadsheetVersion version) {
        return range.getFirstRow() == 1 && range.getLastRow() == version.getMaxRows();
    }

    /**
     * Returns whether the range spans all the columns of the spreadsheet version, i.e. consists of whole rows.
     */
    public static boolean spansAllColumns(Range range, SpreadsheetVersion version) {
        return range.getFirstColumn() == 1 && range.getLastColumn() == version.getMaxColumns();
    }

    /**
     * Converts a print area to a cell range address, e.g. to format it with
     * {@link CellRangeAddress#formatAsString(String, boolean)}. An area spanning all the columns of the spreadsheet
     * version becomes whole rows, e.g. {@code $1:$20}, which also suit applications with fewer columns; an area
     * spanning all the rows becomes whole columns, e.g. {@code $A:$F}.
     */
    public static CellRangeAddress toCellRangeAddress(Range printArea, SpreadsheetVersion version) {
        boolean wholeRows = spansAllColumns(printArea, version);
        boolean wholeColumns = !wholeRows && spansAllRows(printArea, version);
        return new CellRangeAddress(
                wholeColumns ? -1 : printArea.getFirstRow() - 1, wholeColumns ? -1 : printArea.getLastRow() - 1,
                wholeRows ? -1 : printArea.getFirstColumn() - 1, wholeRows ? -1 : printArea.getLastColumn() - 1);
    }

    /**
     * Formats a print area as a defined name formula, e.g. {@code 'My sheet'!$A$1:$F$20}, see
     * {@link #toCellRangeAddress(Range, SpreadsheetVersion)} for whole rows and whole columns. The sheet name is
     * expected the way {@link #parsePrintArea(String, SpreadsheetVersion)} returns it.
     */
    public static String formatPrintArea(Range printArea, SpreadsheetVersion version) {
        return "'" + printArea.getSheet() + "'!" + toCellRangeAddress(printArea, version).formatAsString(null, true);
    }

    /**
     * Returns the cells a drawing object is anchored in: only the anchor cell for an object sized in EMUs, or
     * {@code null} for an object that is not anchored in cells.
     */
    @Nullable
    public static Range getAnchorRange(String sheetName, Object anchor) {
        if (anchor instanceof CTTwoCellAnchor twoCellAnchor) {
            return new Range(sheetName, twoCellAnchor.getFrom().getCol() + 1, twoCellAnchor.getFrom().getRow() + 1,
                    twoCellAnchor.getTo().getCol() + 1, twoCellAnchor.getTo().getRow() + 1);
        }
        if (anchor instanceof CTOneCellAnchor oneCellAnchor) {
            CTMarker from = oneCellAnchor.getFrom();
            return new Range(sheetName, from.getCol() + 1, from.getRow() + 1, from.getCol() + 1, from.getRow() + 1);
        }
        return null;
    }

    /**
     * Returns the template range of each rendered band mapped to the range covering the blocks of all its instances.
     * The block of a band instance covers the range it is rendered to and the blocks of its nested bands rendered on
     * the same sheet.
     *
     * @param rootBand      root band of the report
     * @param renderedRange the range a band instance is rendered to, or {@code null} if it is not rendered
     * @param templateRange the template range of a band, requested with one of its rendered instances
     */
    public static RangeDependencies getRenderedBandBlocks(BandData rootBand,
                                                          Function<BandData, @Nullable Range> renderedRange,
                                                          Function<BandData, Range> templateRange) {
        Map<String, Range> blocks = new LinkedHashMap<>();
        Map<String, BandData> renderedBands = new HashMap<>();
        for (List<BandData> bands : rootBand.getChildrenBands().values()) {
            for (BandData band : bands) {
                addRenderedBandBlock(band, renderedRange, blocks, renderedBands);
            }
        }
        RangeDependencies bandBlocks = new RangeDependencies();
        blocks.forEach((bandName, block) ->
                bandBlocks.addDependency(templateRange.apply(renderedBands.get(bandName)), block));
        return bandBlocks;
    }

    /**
     * Adds the block of the band instance and the blocks of its nested bands to the blocks of their band names and
     * returns the block of the band instance, or {@code null} if it is not rendered.
     */
    @Nullable
    private static Range addRenderedBandBlock(BandData band, Function<BandData, @Nullable Range> renderedRange,
                                              Map<String, Range> blocks, Map<String, BandData> renderedBands) {
        Range range = renderedRange.apply(band);
        int lastRow = range != null ? range.getLastRow() : 0;
        for (List<BandData> children : band.getChildrenBands().values()) {
            for (BandData child : children) {
                Range childBlock = addRenderedBandBlock(child, renderedRange, blocks, renderedBands);
                if (range != null && childBlock != null && childBlock.getSheet().equals(range.getSheet())) {
                    lastRow = Math.max(lastRow, childBlock.getLastRow());
                }
            }
        }
        if (range == null) {
            return null;
        }
        Range block = new Range(range.getSheet(), range.getFirstColumn(), range.getFirstRow(), range.getLastColumn(),
                lastRow);
        blocks.merge(band.getName(), block, (first, second) -> new Range(first.getSheet(),
                Math.min(first.getFirstColumn(), second.getFirstColumn()),
                Math.min(first.getFirstRow(), second.getFirstRow()),
                Math.max(first.getLastColumn(), second.getLastColumn()),
                Math.max(first.getLastRow(), second.getLastRow())));
        renderedBands.putIfAbsent(band.getName(), band);
        return block;
    }

    /**
     * Re-bases a template print area onto the rendered report.
     * <p>
     * The rows cover everything rendered by the bands that intersect the area, and the objects that intersect it as
     * far as they lie inside it: an object keeps its size, so the part inside the area is covered wherever the object
     * is rendered, from the first row of the sheet on.
     * <p>
     * The columns cover the columns the content of the area is rendered to: the parts of those bands and objects
     * inside the area, a band with the columns it grows by, e.g. as a vertical band. The area keeps the blank columns
     * it has around its content, unless another band is rendered there. An area spanning all the columns keeps them.
     *
     * @param printArea  print area of the template
     * @param bandBlocks template ranges of the bands mapped to the ranges their instances are rendered to, the rows
     *                   of their nested bands included
     * @param objects    template ranges of the objects that keep their size, e.g. merged regions and pictures,
     *                   mapped to the ranges they are rendered to
     * @param version    spreadsheet version that limits the number of rows and columns
     * @return the print area of the report, or {@code null} to leave the print area as it is: it spans whole
     * columns, which cover every rendered row anyway, or nothing intersecting it is rendered
     */
    @Nullable
    public static Range rebasePrintArea(Range printArea, RangeDependencies bandBlocks, RangeDependencies objects,
                                        SpreadsheetVersion version) {
        if (spansAllRows(printArea, version)) {
            return null;
        }
        Span rows = new Span();
        Span templateColumns = new Span();
        Span renderedColumns = new Span();
        for (Range templateRange : bandBlocks.templates()) {
            if (!printArea.intersects(templateRange)) {
                continue;
            }
            for (Range block : bandBlocks.resultsForTemplate(templateRange)) {
                rows.add(block.getFirstRow(), block.getLastRow());
                addColumns(printArea, templateRange, block, templateColumns, renderedColumns);
            }
        }
        for (Range templateRange : objects.templates()) {
            if (!printArea.intersects(templateRange)) {
                continue;
            }
            for (Range object : objects.resultsForTemplate(templateRange)) {
                int shift = object.getFirstRow() - templateRange.getFirstRow();
                int firstRow = Math.max(templateRange.getFirstRow(), printArea.getFirstRow()) + shift;
                int lastRow = Math.min(templateRange.getLastRow(), printArea.getLastRow()) + shift;
                if (lastRow >= 1 && firstRow <= version.getMaxRows()) {
                    rows.add(Math.max(firstRow, 1), Math.min(lastRow, version.getMaxRows()));
                    addColumns(printArea, templateRange, object, templateColumns, renderedColumns);
                }
            }
        }
        if (rows.isEmpty()) {
            return null;
        }
        if (spansAllColumns(printArea, version)) {
            return new Range(printArea.getSheet(), 1, rows.first, version.getMaxColumns(), rows.last);
        }
        int firstColumn = renderedColumns.first - (templateColumns.first - printArea.getFirstColumn());
        int lastColumn = renderedColumns.last + (printArea.getLastColumn() - templateColumns.last);
        // the blank columns around the content give way to other bands rendered there
        for (Range templateRange : bandBlocks.templates()) {
            if (printArea.intersects(templateRange)) {
                continue;
            }
            for (Range block : bandBlocks.resultsForTemplate(templateRange)) {
                if (!block.getSheet().equals(printArea.getSheet())
                        || block.getLastRow() < rows.first || block.getFirstRow() > rows.last) {
                    continue;
                }
                if (block.getFirstColumn() < renderedColumns.first && block.getLastColumn() >= firstColumn) {
                    firstColumn = Math.min(block.getLastColumn() + 1, renderedColumns.first);
                }
                if (block.getLastColumn() > renderedColumns.last && block.getFirstColumn() <= lastColumn) {
                    lastColumn = Math.max(block.getFirstColumn() - 1, renderedColumns.last);
                }
            }
        }
        return new Range(printArea.getSheet(), Math.max(firstColumn, 1), rows.first,
                Math.min(lastColumn, version.getMaxColumns()), rows.last);
    }

    /**
     * Adds the columns of the part of a band or an object inside the print area: as in the template, and as rendered,
     * moved and grown with the band or the object.
     */
    private static void addColumns(Range printArea, Range templateRange, Range renderedRange, Span templateColumns,
                                   Span renderedColumns) {
        int firstColumn = Math.max(templateRange.getFirstColumn(), printArea.getFirstColumn());
        int lastColumn = Math.min(templateRange.getLastColumn(), printArea.getLastColumn());
        templateColumns.add(firstColumn, lastColumn);
        renderedColumns.add(firstColumn + renderedRange.getFirstColumn() - templateRange.getFirstColumn(),
                lastColumn + renderedRange.getLastColumn() - templateRange.getLastColumn());
    }

    /**
     * Rows or columns, empty until some are added.
     */
    private static final class Span {
        private int first = Integer.MAX_VALUE;
        private int last = Integer.MIN_VALUE;

        private void add(int first, int last) {
            this.first = Math.min(this.first, first);
            this.last = Math.max(this.last, last);
        }

        private boolean isEmpty() {
            return first > last;
        }
    }
}