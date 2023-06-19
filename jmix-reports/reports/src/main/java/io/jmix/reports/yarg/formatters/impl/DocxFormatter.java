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

import io.jmix.reports.yarg.formatters.impl.docx.*;
import io.jmix.reports.yarg.formatters.impl.inline.ContentInliner;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.formatters.impl.docx.DocumentWrapper;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportFieldFormat;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPartAltChunkHost;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocFinder;
import org.docx4j.toc.TocGenerator;
import org.docx4j.utils.AltChunkFinder;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * * Document formatter for '.docx' file types
 */
public class DocxFormatter extends AbstractFormatter {
    protected static final Logger log = LoggerFactory.getLogger(DocxFormatter.class);

    protected WordprocessingMLPackage wordprocessingMLPackage;
    protected DocumentWrapper documentWrapper;
    protected DocumentConverter documentConverter;
    protected HtmlImportProcessor htmlImportProcessor;

    public DocxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.docx);
        supportedOutputTypes.add(ReportOutputType.pdf);
    }

    public void setDocumentConverter(DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    public void setHtmlImportProcessor(HtmlImportProcessor htmlImportProcessor) {
        this.htmlImportProcessor = htmlImportProcessor;
    }

    @Override
    public void renderDocument() {
        loadDocument();

        fillTables();

        replaceAllAliasesInDocument();

        handleUrls();

        updateTableOfContents();

        saveAndClose();
    }

    protected void updateTableOfContents() {
        try {
            MainDocumentPart documentPart = wordprocessingMLPackage.getMainDocumentPart();
            Document wmlDocumentEl;
            try {
                wmlDocumentEl = documentPart.getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException("Unable to get document content", e);
            }
            Body body = wmlDocumentEl.getBody();

            TocFinder finder = new TocFinder();
            new TraversalUtil(body.getContent(), finder);
            SdtBlock structuredDocumentPart = finder.getTocSDT();

            if (structuredDocumentPart != null) {
                TocGenerator tocGenerator = new TocGenerator(wordprocessingMLPackage);
                tocGenerator.updateToc(false);
            }
        } catch (TocException e) {
            log.error("An error occurred during updating the Table Of Contents", e);
        }
    }

    protected void handleUrls() {
        UrlVisitor urlVisitor = new UrlVisitor(new io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate(this), wordprocessingMLPackage.getMainDocumentPart());
        new TraversalUtil(wordprocessingMLPackage.getMainDocumentPart(), urlVisitor);
    }

    protected void loadDocument() {
        if (reportTemplate == null)
            throw new NullPointerException("Template file can't be null.");
        try {
            wordprocessingMLPackage = WordprocessingMLPackage.load(reportTemplate.getDocumentContent());
            documentWrapper = new DocumentWrapper(new DocxFormatterDelegate(this), wordprocessingMLPackage);
        } catch (Docx4JException e) {
            throw wrapWithReportingException(String.format("An error occurred while reading docx template. File name [%s]", reportTemplate.getDocumentName()), e);
        }
    }

    protected void saveAndClose() {
        try {
            checkThreadInterrupted();
            if (ReportOutputType.docx.equals(outputType)) {
                convertAltChunks();
                writeToOutputStream(wordprocessingMLPackage, outputStream);
                outputStream.flush();
            } else if (ReportOutputType.pdf.equals(outputType)) {
                convertAltChunks();
                if (documentConverter != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    writeToOutputStream(wordprocessingMLPackage, bos);
                    documentConverter.convertToPdf(DocumentConverter.FileType.DOCUMENT, bos.toByteArray(), outputStream);
                    outputStream.flush();
                } else {
                    Docx4J.toPDF(wordprocessingMLPackage, outputStream);
                    outputStream.flush();
                }
            } else if (ReportOutputType.html.equals(outputType)) {
                convertAltChunks();
                if (documentConverter != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    writeToOutputStream(wordprocessingMLPackage, bos);
                    documentConverter.convertToHtml(DocumentConverter.FileType.DOCUMENT, bos.toByteArray(), outputStream);
                    outputStream.flush();
                } else {
                    HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
                    htmlSettings.setOpcPackage(wordprocessingMLPackage);
                    Docx4J.toHTML(htmlSettings, outputStream, Docx4J.FLAG_NONE);
                    outputStream.flush();
                }
            } else {
                throw new UnsupportedOperationException(String.format("DocxFormatter could not output file with type [%s]", outputType));
            }
        } catch (Docx4JException e) {
            throw wrapWithReportingException("An error occurred while saving result report", e);
        } catch (IOException e) {
            throw wrapWithReportingException("An error occurred while saving result report to PDF", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected void replaceAllAliasesInDocument() {
        for (TextWrapper text : documentWrapper.getTexts()) {
            text.fillTextWithBandData();
        }
    }

    protected void fillTables() {
        for (TableManager resultingTable : documentWrapper.getTables()) {
            checkThreadInterrupted();
            Tr rowWithAliases = resultingTable.getRowWithAliases();
            if (rowWithAliases != null) {
                List<BandData> bands = rootBand.findBandsRecursively(resultingTable.getBandName());

                if (bands.size() > 1) {
                    for (final BandData band : bands) {
                        Tr newRow = resultingTable.copyRow(rowWithAliases);
                        resultingTable.fillRowFromBand(newRow, band);
                    }
                    resultingTable.getTable().getContent().remove(rowWithAliases);
                } else if (bands.size() == 1) {
                    resultingTable.fillRowFromBand(rowWithAliases, bands.get(0));
                } else if (bands.size() == 0) {
                    if (resultingTable.noHeader()) {
                        resultingTable.getTable().getContent().clear();
                    } else {
                        resultingTable.getTable().getContent().remove(rowWithAliases);
                    }
                }
            }
        }
    }

    protected boolean tryToApplyInliners(String fullParameterName, Object paramValue, Text text) {
        Map<String, ReportFieldFormat> valueFormats = rootBand.getReportFieldFormats();
        if (paramValue != null && valueFormats != null && valueFormats.containsKey(fullParameterName)) {
            String format = valueFormats.get(fullParameterName).getFormat();
            for (ContentInliner contentInliner : DocxFormatter.this.contentInliners) {
                Matcher contentMatcher = contentInliner.getTagPattern().matcher(format);
                if (contentMatcher.find()) {
                    contentInliner.inlineToDocx(wordprocessingMLPackage, text, paramValue, contentMatcher);
                    return true;
                }
            }
        }
        return false;
    }


    protected void writeToOutputStream(WordprocessingMLPackage mlPackage, OutputStream outputStream) throws Docx4JException {
        SaveToZipFile saver = new SaveToZipFile(mlPackage);
        saver.save(outputStream);
    }

    @SuppressWarnings("unchecked")
    public void convertAltChunks() throws Docx4JException {
        JaxbXmlPartAltChunkHost mainDocumentPart = wordprocessingMLPackage.getMainDocumentPart();
        List<Object> contentList = ((ContentAccessor) mainDocumentPart).getContent();

        AltChunkFinder bf = new AltChunkFinder();
        new TraversalUtil(contentList, bf);

        for (AltChunkFinder.LocatedChunk locatedChunk : bf.getAltChunks()) {
            CTAltChunk altChunk = locatedChunk.getAltChunk();
            AlternativeFormatInputPart part
                    = (AlternativeFormatInputPart) mainDocumentPart.getRelationshipsPart().getPart(
                    altChunk.getId());
            if (part.getAltChunkType().equals(AltChunkType.Xhtml)) {
                try {
                    XHTMLImporter xHTMLImporter = new XHTMLImporterImpl(wordprocessingMLPackage);
                    List results = xHTMLImporter.convert(
                            htmlImportProcessor.processHtml(toString(part.getBuffer())), null);
                    locatedChunk.getContentList().remove(locatedChunk.getIndex());
                    Object chunkParent = locatedChunk.getAltChunk().getParent();
                    R run = (R) chunkParent;//always should be R
                    P paragraph = (P) run.getParent();
                    ContentAccessor paragraphParent = (ContentAccessor) paragraph.getParent();

                    if (paragraphParent instanceof ArrayListWml) {
                        ArrayListWml parent = (ArrayListWml) paragraph.getParent();
                        parent.addAll(parent.indexOf(paragraph), results);
                        if (results.get(0) instanceof P) {
                            P resultParagraph = (P) results.get(0);
                            resultParagraph.setPPr(paragraph.getPPr());
                        }
                        parent.remove(paragraph);
                    } else {
                        List<Object> destinationContent = paragraphParent.getContent();
                        int indexToAdd = destinationContent.indexOf(paragraph);
                        destinationContent.remove(indexToAdd);
                        for (Object result : results) {
                            if (result instanceof P) {
                                P resultParagraph = (P) result;
                                destinationContent.add(indexToAdd++, resultParagraph);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("An error occurred while converting HTML parts of DOCX document:", e);
                }
            }
        }
    }

    private String toString(ByteBuffer bb) throws UnsupportedEncodingException {
        byte[] bytes = new byte[bb.limit()];
        bb.get(bytes);
        return new String(bytes, "UTF-8");
    }
}
