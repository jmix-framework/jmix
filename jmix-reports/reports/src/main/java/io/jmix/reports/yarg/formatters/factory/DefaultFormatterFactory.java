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
package io.jmix.reports.yarg.formatters.factory;

import io.jmix.reports.yarg.formatters.factory.inline.DefaultInlinersProvider;
import io.jmix.reports.yarg.formatters.factory.inline.ReportInlinersProvider;
import io.jmix.reports.yarg.formatters.impl.*;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import io.jmix.reports.yarg.formatters.impl.docx.HtmlImportProcessor;
import io.jmix.reports.yarg.formatters.impl.docx.HtmlImportProcessorImpl;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverterImpl;
import io.jmix.reports.yarg.exception.UnsupportedFormatException;
import io.jmix.reports.yarg.formatters.ReportFormatter;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportTemplate;
import io.jmix.reports.yarg.util.groovy.Scripting;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultFormatterFactory implements ReportFormatterFactory {

    protected OfficeIntegrationAPI officeIntegration;
    protected DocumentConverter documentConverter;
    protected DefaultFormatProvider defaultFormatProvider;
    protected HtmlImportProcessor htmlImportProcessor;
    protected HtmlToPdfConverterFactory htmlToPdfConverterFactory;
    protected String fontsDirectory;
    protected Scripting scripting;

    protected Map<String, FormatterCreator> formattersMap = new HashMap<>();

    protected ReportInlinersProvider inlinersProvider;

    protected boolean formulasPostProcessingEvaluationEnabled;

    public DefaultFormatterFactory() {
        htmlImportProcessor = new HtmlImportProcessorImpl();
        htmlToPdfConverterFactory = new HtmlToPdfConverterFactory();
        formulasPostProcessingEvaluationEnabled = false;
        formattersMap.put("xls", factoryInput -> {
            XLSFormatter xlsFormatter = new XLSFormatter(factoryInput);
            xlsFormatter.setDocumentConverter(documentConverter);
            xlsFormatter.setDefaultFormatProvider(defaultFormatProvider);
            xlsFormatter.setScripting(scripting);
            return xlsFormatter;
        });

        FormatterCreator docCreator = factoryInput -> {
            if (officeIntegration == null) {
                throw new UnsupportedFormatException("Could not use doc templates because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
            DocFormatter docFormatter = new DocFormatter(factoryInput, officeIntegration);
            docFormatter.setDefaultFormatProvider(defaultFormatProvider);
            return docFormatter;
        };
        formattersMap.put("odt", docCreator);
        formattersMap.put("doc", docCreator);
        FormatterCreator ftlCreator = factoryInput -> {
            HtmlFormatter htmlFormatter = new HtmlFormatter(factoryInput);
            htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
            htmlFormatter.setFontsDirectory(getFontsDirectory());
            htmlFormatter.setPdfConverterFactory(htmlToPdfConverterFactory);
            htmlFormatter.setScripting(scripting);
            return htmlFormatter;
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);
        formattersMap.put("docx", factoryInput -> {
            DocxFormatter docxFormatter = new DocxFormatter(factoryInput);
            docxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            docxFormatter.setDocumentConverter(documentConverter);
            docxFormatter.setHtmlImportProcessor(htmlImportProcessor);
            docxFormatter.setScripting(scripting);
            return docxFormatter;
        });
        FormatterCreator xlsxCreator = factoryInput -> {
            XlsxFormatter xlsxFormatter = new XlsxFormatter(factoryInput);
            xlsxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            xlsxFormatter.setDocumentConverter(documentConverter);
            xlsxFormatter.setScripting(scripting);
            xlsxFormatter.setFormulasPostProcessingEvaluationEnabled(formulasPostProcessingEvaluationEnabled);
            return xlsxFormatter;
        };
        formattersMap.put("xlsx", xlsxCreator);
        formattersMap.put("xlsm", xlsxCreator);

        formattersMap.put("csv", CsvFormatter::new);

        FormatterCreator jasperCreator = JasperFormatter::new;
        formattersMap.put("jasper", jasperCreator);
        formattersMap.put("jrxml", jasperCreator);

        if (inlinersProvider == null)
            setDefaultInlinersProvider();
    }

    public void setOfficeIntegration(OfficeIntegrationAPI officeIntegrationAPI) {
        this.officeIntegration = officeIntegrationAPI;
        this.documentConverter = new DocumentConverterImpl(officeIntegrationAPI);
    }

    public void setHtmlImportProcessor(HtmlImportProcessor htmlImportProcessor) {
        this.htmlImportProcessor = htmlImportProcessor;
    }

    public void setDefaultFormatProvider(DefaultFormatProvider defaultFormatProvider) {
        this.defaultFormatProvider = defaultFormatProvider;
    }

    public String getFontsDirectory() {
        return fontsDirectory;
    }

    public void setFontsDirectory(String fontsDirectory) {
        this.fontsDirectory = fontsDirectory;
    }

    public void setInlinersProvider(ReportInlinersProvider inlinersProvider) {
        this.inlinersProvider = inlinersProvider;
    }

    public HtmlToPdfConverterFactory getHtmlToPdfConverterFactory() {
        return htmlToPdfConverterFactory;
    }

    public void setHtmlToPdfConverterFactory(HtmlToPdfConverterFactory htmlToPdfConverterFactory) {
        this.htmlToPdfConverterFactory = htmlToPdfConverterFactory;
    }

    public Scripting getScripting() {
        return scripting;
    }

    public void setScripting(Scripting scripting) {
        this.scripting = scripting;
    }

    public boolean isFormulasPostProcessingEvaluationEnabled() {
        return formulasPostProcessingEvaluationEnabled;
    }

    public void setFormulasPostProcessingEvaluationEnabled(boolean formulasPostProcessingEvaluationEnabled) {
        this.formulasPostProcessingEvaluationEnabled = formulasPostProcessingEvaluationEnabled;
    }

    public ReportFormatter createFormatter(FormatterFactoryInput factoryInput) {
        String templateExtension = factoryInput.templateExtension;
        BandData rootBand = factoryInput.rootBand;
        ReportTemplate reportTemplate = factoryInput.reportTemplate;
        OutputStream outputStream = factoryInput.outputStream;

        FormatterCreator formatterCreator = formattersMap.get(templateExtension);
        if (formatterCreator == null) {
            throw new UnsupportedFormatException(String.format("Unsupported template extension [%s]", templateExtension));
        }

        ReportFormatter reportFormatter = formatterCreator.create(factoryInput);
        if (reportFormatter instanceof AbstractFormatter) {
            ((AbstractFormatter) reportFormatter).setContentInliners(new ArrayList<>(inlinersProvider.getContentInliners()));
        }

        return reportFormatter;
    }

    protected void setDefaultInlinersProvider() {
        inlinersProvider = new DefaultInlinersProvider();
    }

    protected static interface FormatterCreator {
        ReportFormatter create(FormatterFactoryInput formatterFactoryInput);
    }
}
