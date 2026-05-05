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


import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.docx.*;
import io.jmix.reports.yarg.formatters.impl.inline.ContentInliner;
import io.jmix.reports.yarg.formatters.impl.inline.HtmlContentInliner;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportFieldFormat;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
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
import org.jsoup.nodes.Entities;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * * Document formatter for '.docx' file types
 */
public class DocxFormatter extends AbstractFormatter {
    protected static final Logger log = LoggerFactory.getLogger(DocxFormatter.class);

    protected WordprocessingMLPackage wordprocessingMLPackage;
    protected DocumentWrapper documentWrapper;
    protected DocumentConverter documentConverter;
    protected HtmlImportProcessor htmlImportProcessor;
    protected Map<P, String> originalParagraphTexts = new IdentityHashMap<>();
    protected Set<P> htmlInlinedParagraphs = Collections.newSetFromMap(new IdentityHashMap<>());

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
        originalParagraphTexts.clear();
        htmlInlinedParagraphs.clear();
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
        UrlVisitor urlVisitor = new UrlVisitor(new DocxFormatterDelegate(this), wordprocessingMLPackage.getMainDocumentPart());
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
        return tryToApplyInliners(fullParameterName, paramValue, text, null);
    }

    protected boolean tryToApplyInliners(String fullParameterName, Object paramValue, Text text, String alias) {
        return tryToApplyInliners(fullParameterName, paramValue, text, alias, null);
    }

    protected boolean tryToApplyInliners(String fullParameterName, Object paramValue, Text text, String alias, BandData tableBand) {
        Map<String, ReportFieldFormat> valueFormats = rootBand.getReportFieldFormats();
        if (paramValue != null && valueFormats != null && valueFormats.containsKey(fullParameterName)) {
            String format = valueFormats.get(fullParameterName).getFormat();
            for (ContentInliner contentInliner : DocxFormatter.this.contentInliners) {
                Matcher contentMatcher = contentInliner.getTagPattern().matcher(format);
                if (contentMatcher.find()) {
                    P paragraph = findParentParagraph(text);
                    String paragraphText = paragraph != null ? getOriginalParagraphText(paragraph) : null;
                    Object valueToInline = paramValue;
                    if (alias != null && contentInliner instanceof HtmlContentInliner
                            && paragraph != null
                            && !htmlInlinedParagraphs.contains(paragraph)
                            && !hasOtherInlinerAlias(paragraphText, alias, tableBand)) {
                        String textValue = tableBand == null
                                ? inlineBandAliasesExcept(paragraphText, alias)
                                : inlineTableBandAliasesExcept(tableBand, paragraphText, alias);
                        valueToInline = inlineHtmlParameterValue(textValue, alias, paramValue);
                    }

                    contentInliner.inlineToDocx(wordprocessingMLPackage, text, valueToInline, contentMatcher);
                    if (contentInliner instanceof HtmlContentInliner && paragraph != null) {
                        htmlInlinedParagraphs.add(paragraph);
                    }
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
                        if (!results.isEmpty() && results.get(0) instanceof P) {
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

    protected String inlineHtmlParameterValue(String textValue, String alias, Object htmlValue) {
        Pattern aliasPattern = Pattern.compile("\\$\\{" + Pattern.quote(alias) + " *" + STRING_FUNCTION_GROUP + "?\\}");
        Matcher aliasMatcher = aliasPattern.matcher(textValue);
        if (!aliasMatcher.find()) {
            return inlineParameterValue(Entities.escape(textValue), alias, String.valueOf(htmlValue));
        }

        return Entities.escape(textValue.substring(0, aliasMatcher.start()))
                + htmlValue
                + Entities.escape(textValue.substring(aliasMatcher.end()));
    }

    protected String getOriginalParagraphText(P paragraph) {
        if (!originalParagraphTexts.containsKey(paragraph)) {
            originalParagraphTexts.put(paragraph, getElementText(paragraph));
        }

        return originalParagraphTexts.get(paragraph);
    }

    protected String getElementText(Object element) {
        StringWriter writer = new StringWriter();
        try {
            TextUtils.extractText(element, writer);
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while rendering docx template.", e);
        }

        return writer.toString();
    }

    protected P findParentParagraph(Text text) {
        Object current = text;
        while (current instanceof Child) {
            Object parent = XmlUtils.unwrap(((Child) current).getParent());
            if (parent instanceof P) {
                return (P) parent;
            }

            current = parent;
        }

        return null;
    }

    protected boolean hasOtherInlinerAlias(String textValue, String excludedAlias, BandData tableBand) {
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(textValue);
        boolean skippedExcludedAlias = false;
        while (matcher.find()) {
            String alias = matcher.group(1);
            if (alias.equals(excludedAlias)) {
                if (skippedExcludedAlias) {
                    return true;
                }

                skippedExcludedAlias = true;
                continue;
            }

            AliasReference aliasReference = getAliasReference(alias, tableBand);
            if (aliasReference != null && hasInlinerFormat(aliasReference.parameterName, aliasReference.fullParameterName)) {
                return true;
            }
        }

        return false;
    }

    protected AliasReference getAliasReference(String alias, BandData tableBand) {
        if (tableBand != null) {
            if (!alias.contains(".")) {
                return new AliasReference(alias, tableBand.getName() + "." + alias);
            }

            BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);
            if (getTableBandPath(tableBand).equals(bandAndParameter.getBandPath())
                    && !StringUtils.isBlank(bandAndParameter.getParameterName())) {
                String parameterName = bandAndParameter.getParameterName();
                return new AliasReference(parameterName, tableBand.getName() + "." + parameterName);
            }
        }

        BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);
        if (StringUtils.isBlank(bandAndParameter.getBandPath())
                || StringUtils.isBlank(bandAndParameter.getParameterName())) {
            return null;
        }
        BandData band = findBandByPath(bandAndParameter.getBandPath());
        return band != null
                ? new AliasReference(bandAndParameter.getParameterName(),
                band.getName() + "." + bandAndParameter.getParameterName())
                : null;
    }

    protected boolean hasInlinerFormat(String parameterName, String fullParameterName) {
        String format = getFormatString(parameterName, fullParameterName);
        if (format == null) {
            return false;
        }

        for (ContentInliner contentInliner : DocxFormatter.this.contentInliners) {
            if (contentInliner.getTagPattern().matcher(format).find()) {
                return true;
            }
        }

        return false;
    }

    protected static class AliasReference {
        protected final String parameterName;
        protected final String fullParameterName;

        protected AliasReference(String parameterName, String fullParameterName) {
            this.parameterName = parameterName;
            this.fullParameterName = fullParameterName;
        }
    }

    protected String inlineBandAliasesExcept(String template, String excludedAlias) {
        return inlineAliasesExcept(template, excludedAlias, null);
    }

    protected String inlineTableBandAliasesExcept(BandData tableBand, String template, String excludedAlias) {
        return inlineAliasesExcept(template, excludedAlias, tableBand);
    }

    protected String inlineAliasesExcept(String template, String excludedAlias, BandData tableBand) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = ALIAS_WITH_BAND_NAME_PATTERN.matcher(template);
        while (matcher.find()) {
            String alias = matcher.group(1);
            if (alias.equals(excludedAlias)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }

            String value = formatAliasValue(alias, matcher.group(2), tableBand);
            if (value == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    protected String formatAliasValue(String alias, String stringFunction, BandData tableBand) {
        if (tableBand != null) {
            String tableBandPath = getTableBandPath(tableBand);
            if (!alias.contains(".")) {
                return formatValue(tableBand.getParameterValue(alias), alias, tableBand.getName() + "." + alias, stringFunction);
            }

            BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);
            if (tableBandPath.equals(bandAndParameter.getBandPath())
                    && !StringUtils.isBlank(bandAndParameter.getParameterName())) {
                String parameterName = bandAndParameter.getParameterName();
                return formatValue(tableBand.getParameterValue(parameterName), parameterName,
                        tableBand.getName() + "." + parameterName, stringFunction);
            }
        }

        BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);
        if (StringUtils.isBlank(bandAndParameter.getBandPath())
                || StringUtils.isBlank(bandAndParameter.getParameterName())) {
            return null;
        }

        BandData band = findBandByPath(bandAndParameter.getBandPath());
        if (band == null) {
            throw wrapWithReportingException(String.format("No band for alias [%s] found", alias));
        }

        String fullParameterName = band.getName() + "." + bandAndParameter.getParameterName();
        Object parameterValue = band.getParameterValue(bandAndParameter.getParameterName());
        return formatValue(parameterValue, bandAndParameter.getParameterName(), fullParameterName, stringFunction);
    }

    protected String getTableBandPath(BandData tableBand) {
        return tableBand.getFullName();
    }

    private String toString(ByteBuffer bb) throws UnsupportedEncodingException {
        byte[] bytes = new byte[bb.limit()];
        bb.get(bytes);
        return new String(bytes, "UTF-8");
    }
}
