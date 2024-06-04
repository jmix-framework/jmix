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

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.structure.BandData;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.PivotCacheDefinition;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.springframework.lang.Nullable;
import org.xlsx4j.sml.CTDefinedName;
import org.xlsx4j.sml.CTMergeCells;
import org.xlsx4j.sml.CTPageBreak;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTStylesheet;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Col;
import org.xlsx4j.sml.Cols;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Document {
    protected SpreadsheetMLPackage thePackage;
    protected List<SheetWrapper> worksheets = new ArrayList<>();

    protected Map<Range, ChartWrapper> chartSpaces = new HashMap<>();
    protected Workbook workbook;
    protected SharedStrings sharedStrings;
    protected StyleSheet styleSheet;
    protected List<PivotCacheDefinition> pivotCacheDefinitions = new ArrayList<>();
    protected HashSet<Part> handled = new HashSet<>();

    public static Document create(SpreadsheetMLPackage thePackage) {
        Document document = new Document();
        document.thePackage = thePackage;
        RelationshipsPart rp = thePackage.getRelationshipsPart();
        document.traverse(null, rp);

        return document;
    }

    public SpreadsheetMLPackage getPackage() {
        return thePackage;
    }

    public Map<Range, ChartWrapper> getChartSpaces() {
        return chartSpaces;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public List<SheetWrapper> getWorksheets() {
        return worksheets;
    }

    public List<PivotCacheDefinition> getPivotCacheDefinitions() {
        return pivotCacheDefinitions;
    }

    public Worksheet getSheetByName(String name) {
        for (SheetWrapper sheetWrapper : worksheets) {
            if (sheetWrapper.getName().equals(name)) {
                return getWorksheetContents(sheetWrapper);
            }
        }

        return null;
    }

    public Worksheet getWorksheetContents(SheetWrapper wrapper) {
        try {
            return wrapper.getWorksheet().getContents();
        } catch (Docx4JException e) {
            throw new RuntimeException("Unable to get worksheet contents", e);
        }
    }

    public String getSheetName(Worksheet worksheet) {
        for (SheetWrapper sheetWrapper : worksheets) {
            if (worksheet == getWorksheetContents(sheetWrapper)) {
                return sheetWrapper.getName();
            }
        }

        return null;
    }

    public String getCellValue(Cell cell) {
        if (cell.getV() == null) return null;
        if (cell.getV().isEmpty()) return cell.getV();
        if (cell.getT().equals(STCellType.S)) {
            CTSst jaxbElement;
            try {
                jaxbElement = sharedStrings.getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException("Unable to get strings contents", e);
            }

            CTRst ctRst = jaxbElement.getSi().get(Integer.parseInt(cell.getV()));
            String value = null;

            if (ctRst.getT() != null) {
                value = ctRst.getT().getValue();
            } else {
                if (ctRst.getR() != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (CTRElt ctrElt : ctRst.getR()) {
                        if (ctrElt.getT() != null) {
                            stringBuilder.append(ctrElt.getT().getValue());
                        }
                    }

                    value = stringBuilder.toString();
                }
            }

            return value;
        } else {
            return cell.getV();
        }
    }

    public CTDefinedName getDefinedName(String name) {
        List<CTDefinedName> definedName = workbook.getDefinedNames().getDefinedName();
        CTDefinedName targetRange = null;
        for (CTDefinedName namedRange : definedName) {
            if (name.equals(namedRange.getName())) {
                targetRange = namedRange;
            }
        }

        return targetRange;
    }

    public Map<CellReference, Cell> getCellsByRange(Range range) {
        Worksheet sheet = getSheetByName(range.getSheet());
        SheetData data = sheet.getSheetData();

        Map<CellReference, Cell> result = new LinkedHashMap<>();
        for (int i = 1; i <= data.getRow().size(); i++) {
            Row row = data.getRow().get(i - 1);
            if (range.getFirstRow() <= row.getR() && row.getR() <= range.getLastRow()) {
                List<Cell> c = row.getC();

                for (Cell cell : c) {
                    CellReference cellReference = new CellReference(range.getSheet(), cell.getR());
                    if (range.getFirstColumn() <= cellReference.getColumn() && cellReference.getColumn() <= range.getLastColumn()) {
                        result.put(cellReference, cell);
                    }
                }
            }
        }
        return result;
    }

    public Col getColumnForCell(String sheetName, CellReference cellReference) {
        Worksheet sheet = getSheetByName(sheetName);
        List<Cols> colsList = sheet.getCols();
        for (Cols cols : colsList) {
            for (Col col : cols.getCol()) {
                if (col.getMin() <= cellReference.getColumn() && cellReference.getColumn() <= col.getMax()) {
                    return col;
                }
            }
        }

        return null;
    }

    public StyleSheet.CellXfs getCellStyle(Cell cell) {
        return styleSheet.getCellStyle(cell.getS());
    }

    @Nullable
    public StyleSheet.Font getCellFont(Cell cell) {
        StyleSheet.CellXfs cellStyle = styleSheet.getCellStyle(cell.getS());
        if (cellStyle == null || cellStyle.getFontId() == null) {
            return null;
        }
        return styleSheet.getFont(cellStyle.getFontId());
    }

    @Nullable
    public Double getColumnWidth(CellReference cellReference) {
        Worksheet sheet = getSheetByName(cellReference.getSheet());
        return sheet.getCols()
                .stream()
                .map(Cols::getCol)
                .flatMap(Collection::stream)
                .filter(col -> col.getMin() <= cellReference.getColumn() && cellReference.getColumn() <= col.getMax())
                .findFirst()
                .map(Col::getWidth)
                .orElse(sheet.getSheetFormatPr() != null ? sheet.getSheetFormatPr().getDefaultColWidth() : null);
    }

    public StyleSheet.Font getDefaultFont() {
        return styleSheet.getFont(0L);
    }

    public StyleSheet getStyleSheet() {
        return styleSheet;
    }

    private void traverse(Part parent, RelationshipsPart rp) {
        int chartNum = 0;
        for (Relationship r : rp.getRelationships().getRelationship()) {
            Part part = rp.getPart(r);
            if (handled.contains(part)) {
                continue;
            }

            if (part instanceof JaxbXmlPart) {
                Object o;
                try {
                    o = ((JaxbXmlPart) part).getContents();
                } catch (Docx4JException e) {
                    throw new RuntimeException("Unable to get part contents", e);
                }

                if (o instanceof CTChartSpace) {
                    Drawing drawing = (Drawing) parent;
                    CTDrawing ctDrawing;
                    try {
                        ctDrawing = drawing.getContents();
                    } catch (Docx4JException e) {
                        throw new RuntimeException("Unable to get drawing contents", e);
                    }
                    Object anchorObj = ctDrawing.getEGAnchor().get(chartNum++);

                    Range range = null;
                    if (anchorObj != null) {
                        CTMarker from = null;
                        CTMarker to = null;
                        Child specificAnchor = null;
                        if (anchorObj instanceof CTOneCellAnchor ctOneCellAnchor) {
                            from = ctOneCellAnchor.getFrom();
                            to = ctOneCellAnchor.getFrom();
                            specificAnchor = ctOneCellAnchor;
                        } else if (anchorObj instanceof CTTwoCellAnchor ctTwoCellAnchor) {
                            from = ctTwoCellAnchor.getFrom();
                            to = ctTwoCellAnchor.getTo();
                            specificAnchor = ctTwoCellAnchor;
                        }

                        if (from != null && to != null) {
                            String sheetName = worksheets.get(worksheets.size() - 1).name;
                            range = new Range(sheetName, from.getCol() + 1, from.getRow() + 1, to.getCol() + 1, to.getRow() + 1);
                            chartSpaces.put(range, new ChartWrapper((CTChartSpace) o, drawing, specificAnchor));
                        }
                    } else {
                        chartSpaces.put(range, new ChartWrapper((CTChartSpace) o, drawing, null));
                    }
                }

                if (o instanceof CTStylesheet) {
                    styleSheet = new StyleSheet((CTStylesheet) o);

                }

                if (o instanceof Workbook) {
                    workbook = (Workbook) o;
                }
            }

            if (part instanceof WorksheetPart) {
                for (Relationship relationship : part.getSourceRelationships()) {
                    if (relationship.getType().endsWith("worksheet")) {
                        String sheetId = relationship.getId();
                        for (Sheet sheet : workbook.getSheets().getSheet()) {
                            if (sheet.getId().equals(sheetId)) {
                                worksheets.add(new SheetWrapper((WorksheetPart) part, sheet.getName()));
                            }
                        }

                    }
                }
            } else if (part instanceof SharedStrings) {
                sharedStrings = (SharedStrings) part;
            } else if (part instanceof PivotCacheDefinition) {
                pivotCacheDefinitions.add((PivotCacheDefinition) part);
            }

            handled.add(part);

            if (part.getRelationshipsPart() != null) {
                traverse(part, part.getRelationshipsPart());
            }
        }
    }

    public void clearWorkbook() {
        for (SheetWrapper sheet : worksheets) {
            getWorksheetContents(sheet).getSheetData().getRow().clear();
            CTMergeCells mergeCells = getWorksheetContents(sheet).getMergeCells();
            if (mergeCells != null && mergeCells.getMergeCell() != null) {
                mergeCells.getMergeCell().clear();
            }
            try {
                CTPageBreak rowBreaks = sheet.worksheet.getContents().getRowBreaks();
                if (rowBreaks != null && rowBreaks.getBrk() != null) {
                    rowBreaks.getBrk().clear();
                }
            } catch (Docx4JException e) {
                throw new ReportFormattingException("An error occurred while clearing docx4j workbook", e);
            }
        }
    }

    /**
     * Method clears defined names associated with band data and leaves all other defined names in the workbook
     */
    public void clearBandDefinedNames(BandData rootBand) {
        workbook.getDefinedNames().getDefinedName().removeIf(
                ctDefinedName -> rootBand.findBandRecursively(ctDefinedName.getName()) != null
        );
    }

    public static class SheetWrapper {
        private WorksheetPart worksheet;
        private String name;

        public SheetWrapper(WorksheetPart worksheet, String name) {
            this.worksheet = worksheet;
            this.name = name;
        }

        public WorksheetPart getWorksheet() {
            return worksheet;
        }

        public String getName() {
            return name;
        }
    }

    public static class ChartWrapper {
        private final CTChartSpace chartSpace;
        private final Drawing drawing;
        private final Child anchor;

        public ChartWrapper(CTChartSpace chartSpace, Drawing drawing, Child anchor) {
            this.chartSpace = chartSpace;
            this.drawing = drawing;
            this.anchor = anchor;
        }

        public CTChartSpace getChartSpace() {
            return chartSpace;
        }

        public Drawing getDrawing() {
            return drawing;
        }

        public Child getAnchor() {
            return anchor;
        }
    }
}