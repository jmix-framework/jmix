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

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.*;
import groovy.text.GStringTemplateEngine;
import io.jmix.reports.yarg.formatters.factory.HtmlToPdfConverterFactory;
import io.jmix.reports.yarg.formatters.impl.pdf.HtmlToPdfConverter;
import io.jmix.reports.yarg.formatters.impl.pdf.ITextPdfConverter;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.UnsupportedFormatException;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Document formatter for '.html' and '.ftl' file types
 */
public class HtmlFormatter extends AbstractFormatter {
    private static final Logger log = LoggerFactory.getLogger(HtmlFormatter.class);

    protected BeansWrapper beansWrapper = new BeansWrapper();
    protected ObjectWrapper objectWrapper;
    protected String fontsDirectory;
    protected HtmlToPdfConverterFactory pdfConverterFactory;

    public HtmlFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.custom);
        supportedOutputTypes.add(ReportOutputType.csv);
        supportedOutputTypes.add(ReportOutputType.html);
        supportedOutputTypes.add(ReportOutputType.pdf);
        beansWrapper.setNullModel(TemplateScalarModel.EMPTY_STRING);

        objectWrapper = new DefaultObjectWrapper() {
            @Override
            public TemplateModel wrap(Object obj) throws TemplateModelException {
                if (obj instanceof Map) {
                    return new MapModel((Map) obj, beansWrapper);
                }
                return super.wrap(obj);
            }
        };
    }

    @Override
    public void renderDocument() {
        if (ReportOutputType.custom.equals(outputType) || ReportOutputType.csv.equals(outputType) || ReportOutputType.html.equals(outputType)) {
            writeHtmlDocument(rootBand, outputStream);
        } else if (ReportOutputType.pdf.equals(outputType)) {
            ByteArrayOutputStream htmlOutputStream = new ByteArrayOutputStream();
            writeHtmlDocument(rootBand, htmlOutputStream);

            String htmlContent = new String(htmlOutputStream.toByteArray());
            renderPdfDocument(htmlContent, outputStream);

        } else {
            throw new UnsupportedFormatException();
        }
    }

    public void setFontsDirectory(String fontsDirectory) {
        this.fontsDirectory = fontsDirectory;
    }

    public void setPdfConverterFactory(HtmlToPdfConverterFactory pdfConverterFactory) {
        this.pdfConverterFactory = pdfConverterFactory;
    }

    protected void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        File temporaryFile = null;
        try {
            temporaryFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(temporaryFile));
            dataOutputStream.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            dataOutputStream.close();

            HtmlToPdfConverter converter = pdfConverterFactory == null ? new ITextPdfConverter() : pdfConverterFactory.createHtmlToPdfConverter();
            loadFonts(converter);

            String url = temporaryFile.toURI().toURL().toString();
            converter.convert(url, outputStream);

        } catch (Exception e) {
            throw wrapWithReportingException("", e);
        } finally {
            FileUtils.deleteQuietly(temporaryFile);
        }
    }

    /**
     * @deprecated
     * @see #loadFonts(HtmlToPdfConverter)
     */
    @Deprecated
    protected void loadFonts(ITextRenderer renderer) {
        loadFonts(new ITextPdfConverter(renderer));
    }

    /**
     * @deprecated
     * @see #loadFontsFromDirectory(HtmlToPdfConverter, File)
     */
    @Deprecated
    protected void loadFontsFromDirectory(ITextRenderer renderer, File fontsDir) {
        loadFontsFromDirectory(new ITextPdfConverter(renderer), fontsDir);
    }


    protected void loadFonts(HtmlToPdfConverter converter) {
        if (StringUtils.isNotBlank(fontsDirectory)) {
            File systemFontsDir = new File(fontsDirectory);
            loadFontsFromDirectory(converter, systemFontsDir);
        }
    }

    protected void loadFontsFromDirectory(HtmlToPdfConverter converter, File fontsDir) {
        if (fontsDir.exists()) {
            if (fontsDir.isDirectory()) {
                File[] files = fontsDir.listFiles((dir, name) -> {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".otf") || lower.endsWith(".ttf");
                });
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        try {
                            converter.addFont(file);
                        } catch (IOException e) {
                            if (StringUtils.contains(e.getMessage(), "cannot be embedded due to licensing restrictions")) {
                                log.debug(e.getMessage());
                            } else {
                                log.warn(e.getMessage());
                            }
                        }
                    }
                } else {
                    log.debug("Fonts directory is empty: " + fontsDir.getPath());
                }
            } else {
                log.warn(format("File %s is not a directory", fontsDir.getAbsolutePath()));
            }
        } else {
            log.debug("Fonts directory does not exist: " + fontsDir.getPath());
        }
    }

    protected void writeHtmlDocument(BandData rootBand, OutputStream outputStream) {
        Map templateModel = getTemplateModel(rootBand);

        if (reportTemplate.isGroovy()) {
            groovy.text.Template htmlTemplate = getGroovyTemplate();
            Writer htmlWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            try {
                htmlTemplate.make(templateModel).writeTo(htmlWriter);
                htmlWriter.close();
            } catch (ReportingException e) {
                throw e;
            } catch (Exception e) {
                throw wrapWithReportingException("An error occurred while rendering html document.", e);
            }
        } else {
            Template htmlTemplate = getFreemarkerTemplate();
            Writer htmlWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            try {
                htmlTemplate.process(templateModel, htmlWriter);
                htmlWriter.close();
            } catch (TemplateException fmException) {
                throw wrapWithReportingException("FreeMarkerException: " + fmException.getMessage());
            } catch (ReportingException e) {
                throw e;
            } catch (Exception e) {
                throw wrapWithReportingException("An error occurred while rendering html document.", e);
            }
        }
    }

    protected Map getTemplateModel(BandData rootBand) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(rootBand.getName(), getBandModel(rootBand));
        return model;
    }

    protected Map getBandModel(BandData band) {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, Object> bands = new HashMap<String, Object>();
        for (String bandName : band.getChildrenBands().keySet()) {
            List<BandData> subBands = band.getChildrenBands().get(bandName);
            List<Map> bandModels = new ArrayList<Map>();
            for (BandData child : subBands)
                bandModels.add(getBandModel(child));

            bands.put(bandName, bandModels);
        }
        model.put("bands", bands);

        model.put("fields", band.getData());

        return model;
    }

    protected Template getFreemarkerTemplate() {
        try {
            String templateContent = IOUtils.toString(reportTemplate.getDocumentContent(), StandardCharsets.UTF_8);
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            stringLoader.putTemplate(reportTemplate.getDocumentName(), templateContent);

            Configuration fmConfiguration = new Configuration();
            fmConfiguration.setTemplateLoader(stringLoader);
            fmConfiguration.setDefaultEncoding("UTF-8");

            Template htmlTemplate = fmConfiguration.getTemplate(reportTemplate.getDocumentName());
            htmlTemplate.setObjectWrapper(objectWrapper);
            return htmlTemplate;
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while creating freemarker template", e);
        }
    }

    protected groovy.text.Template getGroovyTemplate() {
        try {
            GStringTemplateEngine templateEngine = new GStringTemplateEngine();
            String templateContent = IOUtils.toString(reportTemplate.getDocumentContent(), StandardCharsets.UTF_8);
            groovy.text.Template htmlTemplate = templateEngine.createTemplate(templateContent);
            return htmlTemplate;
        } catch (Exception e) {
            throw wrapWithReportingException("An error occurred while creating groovy template", e);
        }
    }
}
