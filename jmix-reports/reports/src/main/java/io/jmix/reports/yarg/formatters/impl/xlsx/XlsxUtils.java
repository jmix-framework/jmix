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
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.xmlbeans.XmlCursor;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.spreadsheetdrawing.CTAnchorClientData;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
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

public final class XlsxUtils {

    private static final long PX_PER_INCH = 96;
    private static final long EMU_PER_INCH = 914400;

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
}