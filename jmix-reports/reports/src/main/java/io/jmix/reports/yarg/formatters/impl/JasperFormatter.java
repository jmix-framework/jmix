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

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.impl.jasper.CubaJRFunction;
import io.jmix.reports.yarg.formatters.impl.jasper.JRBandDataDataSource;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.ReportOutputType;
import io.jmix.reports.yarg.structure.ReportTemplate;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;

import java.util.HashMap;
import java.util.Map;

public class JasperFormatter extends AbstractFormatter {

    protected static final String JASPER_EXT = "jasper";
    protected static final String JRXML_EXT = "jrxml";

    protected static final String CSV_DELIMETER = ";";

    private static final String CUBA_PARAM = "REPORTING";

    public JasperFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    public void renderDocument() {
        try {
            switch (getExtension(reportTemplate)) {
                case JASPER_EXT:
                    printReport((JasperReport) JRLoader.loadObject(reportTemplate.getDocumentContent()));
                    break;
                case JRXML_EXT:
                    JasperDesign design = JRXmlLoader.load(reportTemplate.getDocumentContent());
                    if (!design.getParametersMap().containsKey(CUBA_PARAM))
                        design.addParameter(createJRParameter());

                    printReport(JasperCompileManager.compileReport(design));
                    break;
                default:
                    throw new ReportFormattingException("Error handling template extension");
            }
        } catch (JRException e) {
            throw new ReportFormattingException("Error formatting jasper report: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void printReport(JasperReport report) throws JRException {
        JRDataSource dataSource = new JRBandDataDataSource(rootBand);
        Map<String, Object> params = new HashMap<>();
        params.put(CUBA_PARAM, new CubaJRFunction(dataSource));

        JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);

        Exporter exporter = createExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.exportReport();
    }

    @SuppressWarnings("unchecked")
    protected Exporter createExporter() {
        Exporter exporter;
        if (ReportOutputType.pdf == outputType) {
            exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
        } else if (ReportOutputType.html == outputType) {
            exporter = new HtmlExporter();
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleHtmlExporterConfiguration());
        } else if (ReportOutputType.csv == outputType){
            exporter = new JRCsvExporter();
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
            SimpleCsvExporterConfiguration config = new SimpleCsvExporterConfiguration();
            config.setFieldDelimiter(CSV_DELIMETER);
            exporter.setConfiguration(config);
        } else if (ReportOutputType.doc == outputType ){
            exporter = new JRRtfExporter();
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleRtfExporterConfiguration());
        } else if (ReportOutputType.docx == outputType){
            exporter = new JRDocxExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleDocxExporterConfiguration());
        } else if (ReportOutputType.xls == outputType){
            exporter = new JRXlsExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleXlsExporterConfiguration());
        } else if (ReportOutputType.xlsx == outputType){
            exporter = new JRXlsxExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleXlsxExporterConfiguration());
        } else
            throw new ReportFormattingException("Cannot create jasper exporter using defined output type: " + outputType);

        return exporter;
    }

    protected JRParameter createJRParameter() {
        JRDesignParameter jrParameter = new JRDesignParameter();
        jrParameter.setName(CUBA_PARAM);
        jrParameter.setValueClass(CubaJRFunction.class);
        return jrParameter;
    }

    private String getExtension(ReportTemplate reportTemplate) {
        String[] split = reportTemplate.getDocumentName().split("\\.");
        return split[split.length - 1].toLowerCase();
    }
}
