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
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.spreadsheetdrawing.CTAnchorClientData;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTOneCellAnchor;
import org.docx4j.dml.spreadsheetdrawing.CTPicture;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
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

        double sum = Double.valueOf(0);
        int len = columnLetters.length();
        for (int i = 0; i < len; i++) {
            sum += (columnLetters.charAt(i) - 'A' + 1) * Math.pow(26, len - i - 1);
        }

        return (int) sum;
    }

    public static CTPicture createPicture(String imageRelID) {

        org.docx4j.dml.spreadsheetdrawing.ObjectFactory dmlspreadsheetdrawingObjectFactory = new org.docx4j.dml.spreadsheetdrawing.ObjectFactory();

        CTPicture picture = dmlspreadsheetdrawingObjectFactory.createCTPicture();

        org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

        CTBlipFillProperties blipfillproperties = dmlObjectFactory.createCTBlipFillProperties();
        picture.setBlipFill(blipfillproperties);

        CTBlip blip = dmlObjectFactory.createCTBlip();
        blipfillproperties.setBlip(blip);
        blip.setCstate(org.docx4j.dml.STBlipCompression.NONE);
        blip.setEmbed(imageRelID);

        return picture;
    }

    public static RelationshipsPart attachImageToCell(org.docx4j.openpackaging.parts.DrawingML.Drawing drawing,
                                                      Integer col, Integer row, XlsxImage image, String imageRelID) {
        CTPicture picture = createPicture(imageRelID);
        XSSFClientAnchor srcanchor = (XSSFClientAnchor) image.getPicture().getAnchor();
        if (image.getXssfDrawing().getCTDrawing().getTwoCellAnchorArray().length > 0) {
            CTTwoCellAnchor anchor = new CTTwoCellAnchor();

            anchor.setFrom(new CTMarker());
            anchor.getFrom().setCol(col);
            long fromCollOff = (long) srcanchor.getDx1();
            anchor.getFrom().setColOff(srcanchor.getDx1());
            anchor.getFrom().setRow(row);
            long fromRowOff = (long) srcanchor.getDy1();
            anchor.getFrom().setRowOff(srcanchor.getDy1());

            anchor.setTo(new CTMarker());
            anchor.getTo().setCol(col);
            long toColOff = (long) srcanchor.getDx2();
            anchor.getTo().setColOff(toColOff);
            anchor.getTo().setRow(row);
            long toRowOff = (long) srcanchor.getDy2();
            anchor.getTo().setRowOff(toRowOff);

            anchor.setPic(picture);
            anchor.getPic().setSpPr(picture.getSpPr());
            anchor.setClientData(new CTAnchorClientData());
            drawing.getJaxbElement().getEGAnchor().add(anchor);
        }
        if (image.getXssfDrawing().getCTDrawing().getOneCellAnchorArray().length > 0) {
            CTOneCellAnchor anchor = new CTOneCellAnchor();
            anchor.setFrom(new CTMarker());
            anchor.getFrom().setCol(col);
            long fromCollOff = (long) srcanchor.getDx1();
            anchor.getFrom().setColOff(fromCollOff);
            anchor.getFrom().setRow(row);
            long fromRowOff = (long) srcanchor.getDy1();
            anchor.getFrom().setRowOff(fromRowOff);

            anchor.setExt(new CTPositiveSize2D());
            anchor.getExt().setCx(Math.round(image.getSize().getWidth()));
            anchor.getExt().setCy(Math.round(image.getSize().getHeight()));

            anchor.setPic(picture);
            anchor.setClientData(new CTAnchorClientData());
            drawing.getJaxbElement().getEGAnchor().add(anchor);
        }
        RelationshipsPart relPart = drawing.getRelationshipsPart();
        return relPart;
    }

    public static org.docx4j.openpackaging.parts.DrawingML.Drawing getOrCreateWorksheetDrawing(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart) {
        org.docx4j.openpackaging.parts.DrawingML.Drawing drawing = null;
        try {
            PartName partName = new PartName(StringUtils.replaceIgnoreCase(worksheetPart.getPartName().getName(),
                    "worksheets/sheet", "drawings/drawing"));
            drawing = (org.docx4j.openpackaging.parts.DrawingML.Drawing) pkg.getParts().get(partName);
            if (drawing == null) {
                drawing = new org.docx4j.openpackaging.parts.DrawingML.Drawing(partName);
                drawing.setContents(new CTDrawing());
                Relationship relationship = worksheetPart.addTargetPart(drawing);
                org.xlsx4j.sml.CTDrawing smlDrawing = new org.xlsx4j.sml.CTDrawing();
                smlDrawing.setId(relationship.getId());
                smlDrawing.setParent(worksheetPart.getContents());
                worksheetPart.getContents().setDrawing(smlDrawing);
                worksheetPart.addTargetPart(drawing);
            }
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        return drawing;
    }
}