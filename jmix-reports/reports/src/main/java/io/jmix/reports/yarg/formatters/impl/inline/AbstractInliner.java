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
package io.jmix.reports.yarg.formatters.impl.inline;

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeComponent;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import io.jmix.reports.yarg.formatters.impl.xlsx.CellReference;
import io.jmix.reports.yarg.formatters.impl.xlsx.XlsxUtils;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.XComponentContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.ImageUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.dml.*;
import org.docx4j.dml.spreadsheetdrawing.*;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.xlsx4j.sml.Cell;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;
import static org.apache.poi.util.Units.EMU_PER_PIXEL;

public abstract class AbstractInliner implements ContentInliner {
    private static final String TEXT_GRAPHIC_OBJECT = "com.sun.star.text.TextGraphicObject";
    private static final String GRAPHIC_PROVIDER_OBJECT = "com.sun.star.graphic.GraphicProvider";
    private static final int IMAGE_FACTOR = 27;

    protected Pattern tagPattern;
    protected int docxUniqueId1, docxUniqueId2;

    protected abstract byte[] getContent(Object paramValue);

    @Override
    public void inlineToXlsx(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart, Cell newCell, Object paramValue, Matcher matcher) {
        try {
            Image image = new Image(paramValue, matcher);
            if (image.isValid()) {
                BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(pkg, worksheetPart, image.imageContent);
                CTOneCellAnchor anchor = new CTOneCellAnchor();
                anchor.setFrom(new CTMarker());
                CellReference cellReference = new CellReference("", newCell.getR());
                anchor.getFrom().setCol(cellReference.getColumn() - 1);
                anchor.getFrom().setRow(cellReference.getRow() - 1);
                anchor.setExt(new CTPositiveSize2D());
                anchor.getExt().setCx(XlsxUtils.convertPxToEmu(image.width));
                anchor.getExt().setCy(XlsxUtils.convertPxToEmu(image.height));
                newCell.setV(null);
                putImage(worksheetPart, pkg, imagePart, anchor);
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to xlsx file", e);
        }
    }

    private void putImage(WorksheetPart worksheetPart, SpreadsheetMLPackage pkg, BinaryPartAbstractImage imagePart, CTOneCellAnchor anchor) throws Docx4JException {
        PartName drawingPart = new PartName(StringUtils.replaceIgnoreCase(worksheetPart.getPartName().getName(),
                "worksheets/sheet", "drawings/drawing"));
        String imagePartName = imagePart.getPartName().getName();
        Part part = pkg.getParts().get(drawingPart);
        if (part != null && !(part instanceof Drawing))
            throw new ReportFormattingException("Wrong Class: not a Drawing");
        Drawing drawing = (Drawing) part;
        int currentId = 0;
        if (drawing == null) {
            drawing = new Drawing(drawingPart);
            drawing.setContents(new CTDrawing());
            Relationship relationship = worksheetPart.addTargetPart(drawing);
            org.xlsx4j.sml.CTDrawing smlDrawing = new org.xlsx4j.sml.CTDrawing();
            smlDrawing.setId(relationship.getId());
            smlDrawing.setParent(worksheetPart.getContents());
            worksheetPart.getContents().setDrawing(smlDrawing);
        } else {
            currentId = drawing.getContents().getEGAnchor().size();
        }

        CTPicture picture = new CTPicture();

        CTBlipFillProperties blipFillProperties = new CTBlipFillProperties();
        blipFillProperties.setStretch(new CTStretchInfoProperties());
        blipFillProperties.getStretch().setFillRect(new CTRelativeRect());
        blipFillProperties.setBlip(new CTBlip());
        blipFillProperties.getBlip().setEmbed("rId" + (currentId + 1));
        blipFillProperties.getBlip().setCstate(STBlipCompression.PRINT);

        picture.setBlipFill(blipFillProperties);

        CTNonVisualDrawingProps nonVisualDrawingProps = new CTNonVisualDrawingProps();
        nonVisualDrawingProps.setId(currentId + 2);
        nonVisualDrawingProps.setName(imagePartName.substring(imagePartName.lastIndexOf("/") + 1));
        nonVisualDrawingProps.setDescr(nonVisualDrawingProps.getName());

        CTNonVisualPictureProperties nonVisualPictureProperties = new CTNonVisualPictureProperties();
        nonVisualPictureProperties.setPicLocks(new CTPictureLocking());
        nonVisualPictureProperties.getPicLocks().setNoChangeAspect(true);
        CTPictureNonVisual nonVisualPicture = new CTPictureNonVisual();

        nonVisualPicture.setCNvPr(nonVisualDrawingProps);
        nonVisualPicture.setCNvPicPr(nonVisualPictureProperties);

        picture.setNvPicPr(nonVisualPicture);

        CTShapeProperties shapeProperties = new CTShapeProperties();
        CTTransform2D transform2D = new CTTransform2D();
        transform2D.setOff(new CTPoint2D());
        transform2D.setExt(new CTPositiveSize2D());
        shapeProperties.setXfrm(transform2D);
        shapeProperties.setPrstGeom(new CTPresetGeometry2D());
        shapeProperties.getPrstGeom().setPrst(STShapeType.RECT);
        shapeProperties.getPrstGeom().setAvLst(new CTGeomGuideList());

        picture.setSpPr(shapeProperties);

        anchor.setPic(picture);
        anchor.setClientData(new CTAnchorClientData());

        drawing.getContents().getEGAnchor().add(anchor);

        Relationship rel = new Relationship();
        rel.setId("rId" + (currentId + 1));
        rel.setType(Namespaces.IMAGE);
        rel.setTarget(imagePartName);

        drawing.getRelationshipsPart().addRelationship(rel);
        RelationshipsPart relPart = drawing.getRelationshipsPart();
        pkg.getParts().remove(relPart.getPartName());
        pkg.getParts().put(relPart);
        pkg.getParts().remove(drawing.getPartName());
        pkg.getParts().put(drawing);
    }

    @Override
    public void inlineToDocx(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                Part part = resolveTextPartForDOCX(text, wordPackage);
                BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, part, image.imageContent);
                int originalWidth = imagePart.getImageInfo().getSize().getWidthPx();
                int originalHeight = imagePart.getImageInfo().getSize().getHeightPx();

                double widthScale = (double) image.width / (double) originalWidth;
                double heightScale = (double) image.height  / (double) originalHeight;
                double actualScale = Math.min(widthScale, heightScale);

                long targetWidth = Math.round(originalWidth * actualScale);
                long targetHeight = Math.round(originalHeight * actualScale);

                Inline inline = imagePart.createImageInline("", "", docxUniqueId1++, docxUniqueId2++,
                        XlsxUtils.convertPxToEmu(targetWidth), XlsxUtils.convertPxToEmu(targetHeight), false);

                org.docx4j.wml.Drawing drawing = new org.docx4j.wml.ObjectFactory().createDrawing();
                R run = (R) text.getParent();
                run.getContent().add(drawing);
                drawing.getAnchorOrInline().add(inline);
                text.setValue("");
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to docx file", e);
        }
    }

    @Override
    public void inlineToXls(HSSFPatriarch patriarch, HSSFCell resultCell, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                HSSFSheet sheet = resultCell.getSheet();
                HSSFWorkbook workbook = sheet.getWorkbook();

                int pictureIdx = workbook.addPicture(image.imageContent, Workbook.PICTURE_TYPE_JPEG);

                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(resultCell.getColumnIndex());
                anchor.setRow1(resultCell.getRowIndex());
                anchor.setCol2(resultCell.getColumnIndex());
                anchor.setRow2(resultCell.getRowIndex());
                if (patriarch == null) {
                    throw new IllegalArgumentException(String.format("No HSSFPatriarch object provided. Charts on this sheet could cause this effect. Please check sheet %s", resultCell.getSheet().getSheetName()));
                }
                HSSFPicture picture = patriarch.createPicture(anchor, pictureIdx);
                Dimension size = ImageUtils.getDimensionFromAnchor(picture);
                double actualHeight = size.getHeight() / EMU_PER_PIXEL;
                double actualWidth = size.getWidth() / EMU_PER_PIXEL;
                picture.resize((double) image.width / actualWidth, (double) image.height / actualHeight);
            }
        } catch (IllegalArgumentException e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to xls file", e);
        }
    }

    @Override
    public void inlineToDoc(OfficeComponent officeComponent, XTextRange textRange, XText destination, Object paramValue,
                            Matcher paramsMatcher) throws Exception {
        try {
            if (paramValue != null) {
                Image image = new Image(paramValue, paramsMatcher);

                if (image.isValid()) {
                    XComponent xComponent = officeComponent.getOfficeComponent();
                    insertImage(xComponent, officeComponent.getOfficeResourceProvider(), destination, textRange, image);
                }
            }
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting bitmap to doc file", e);
        }
    }

    protected void insertImage(XComponent document, OfficeResourceProvider officeResourceProvider, XText destination, XTextRange textRange,
                               Image image) throws Exception {
        XMultiServiceFactory xFactory = as(XMultiServiceFactory.class, document);
        XComponentContext xComponentContext = officeResourceProvider.getXComponentContext();
        XMultiComponentFactory serviceManager = xComponentContext.getServiceManager();

        Object oImage = xFactory.createInstance(TEXT_GRAPHIC_OBJECT);
        Object oGraphicProvider = serviceManager.createInstanceWithContext(GRAPHIC_PROVIDER_OBJECT, xComponentContext);

        XGraphicProvider xGraphicProvider = as(XGraphicProvider.class, oGraphicProvider);

        XPropertySet imageProperties = buildImageProperties(xGraphicProvider, oImage, image.imageContent);
        XTextContent xTextContent = as(XTextContent.class, oImage);
        destination.insertTextContent(textRange, xTextContent, true);
        setImageSize(image.width, image.height, oImage, imageProperties);
    }

    protected void setImageSize(int width, int height, Object oImage, XPropertySet imageProperties)
            throws Exception {
        Size aActualSize = (Size) imageProperties.getPropertyValue("ActualSize");
        aActualSize.Height = height * IMAGE_FACTOR;
        aActualSize.Width = width * IMAGE_FACTOR;
        XShape xShape = as(XShape.class, oImage);
        xShape.setSize(aActualSize);
    }

    protected XPropertySet buildImageProperties(XGraphicProvider xGraphicProvider, Object oImage, byte[] imageContent)
            throws Exception {
        XPropertySet imageProperties = as(XPropertySet.class, oImage);

        PropertyValue[] propValues = new PropertyValue[]{new PropertyValue()};
        propValues[0].Name = "InputStream";
        propValues[0].Value = new ByteArrayToXInputStreamAdapter(imageContent);

        XGraphic graphic = xGraphicProvider.queryGraphic(propValues);
        if (graphic != null) {
            imageProperties.setPropertyValue("Graphic", graphic);

            imageProperties.setPropertyValue("HoriOrient", HoriOrientation.NONE);
            imageProperties.setPropertyValue("VertOrient", HoriOrientation.NONE);

            imageProperties.setPropertyValue("HoriOrientPosition", 0);
            imageProperties.setPropertyValue("VertOrientPosition", 0);
        }

        return imageProperties;
    }

    protected class Image {
        byte[] imageContent = null;
        int width = 0;
        int height = 0;

        public Image(Object paramValue, Matcher paramsMatcher) {
            if (paramValue == null) {
                return;
            }

            imageContent = getContent(paramValue);
            if (imageContent.length == 0) {
                imageContent = null;
                return;
            }

            width = Integer.parseInt(paramsMatcher.group(1));
            height = Integer.parseInt(paramsMatcher.group(2));
        }

        boolean isValid() {
            return imageContent != null;
        }
    }

    protected Part resolveTextPartForDOCX(Text text, WordprocessingMLPackage wordPackage) {
        List<SectionWrapper> sectionWrappers = wordPackage.getDocumentModel().getSections();
        for (SectionWrapper sw : sectionWrappers) {
            HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
            List<Part> parts = Arrays.asList(hfp.getFirstHeader(), hfp.getDefaultHeader(), hfp.getEvenHeader(),
                    hfp.getFirstFooter(), hfp.getDefaultFooter(), hfp.getEvenFooter());
            for (Part part : parts) {
                TextMatchCallback callback = new TextMatchCallback(text);
                new TraversalUtil(part, callback);
                if (callback.matched) {
                    return part;
                }
            }
        }
        return wordPackage.getMainDocumentPart();
    }

    protected class TextMatchCallback extends TraversalUtil.CallbackImpl {
        Text text;
        boolean matched;

        public TextMatchCallback(Text text) {
            this.text = text;
        }

        @Override
        public List<Object> apply(Object o) {
            if (text == o) {
                matched = true;
            }
            return null;
        }
    }
}