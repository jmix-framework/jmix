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
package io.jmix.reports.yarg.structure.xml.impl;

import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.structure.*;
import io.jmix.reports.yarg.structure.xml.XmlWriter;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class DefaultXmlWriter implements XmlWriter {

    @Override
    public String buildXml(Report report) {
        try {
            Document document = DocumentFactory.getInstance().createDocument();
            Element root = document.addElement("report");

            root.addAttribute("name", report.getName());
            writeTemplates(report, root);
            writeInputParameters(report, root);
            writeValueFormats(report, root);
            writeRootBand(report, root);

            StringWriter stringWriter = new StringWriter();
            new XMLWriter(stringWriter, OutputFormat.createPrettyPrint()).write(document);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new ReportingException(e);
        }
    }

    protected void writeRootBand(Report report, Element root) {
        ReportBand rootBandDefinition = report.getRootBand();
        Element rootBandDefinitionElement = root.addElement("rootBand");
        writeBandDefinition(rootBandDefinitionElement, rootBandDefinition);
    }

    protected void writeInputParameters(Report report, Element root) {
        Element reportTemplatesElement = root.addElement("parameters");
        if (report.getReportParameters() != null) {
            for (ReportParameter reportParameter : report.getReportParameters()) {
                Element reportTemplateElement = reportTemplatesElement.addElement("parameter");
                reportTemplateElement.addAttribute("name", reportParameter.getName());
                reportTemplateElement.addAttribute("alias", reportParameter.getAlias());
                reportTemplateElement.addAttribute("required", String.valueOf(reportParameter.getRequired()));
                reportTemplateElement.addAttribute("class", reportParameter.getParameterClass().getCanonicalName());
                if (reportParameter instanceof ReportParameterWithDefaultValue &&
                        ((ReportParameterWithDefaultValue) reportParameter).getDefaultValue() != null) {
                    reportTemplateElement.addAttribute("defaultValue",
                            ((ReportParameterWithDefaultValue) reportParameter).getDefaultValue());
                }
            }
        }
    }

    protected void writeValueFormats(Report report, Element root) {
        Element reportTemplatesElement = root.addElement("formats");
        for (ReportFieldFormat reportFieldFormat : report.getReportFieldFormats()) {
            Element reportTemplateElement = reportTemplatesElement.addElement("format");
            reportTemplateElement.addAttribute("name", reportFieldFormat.getName());
            reportTemplateElement.addAttribute("format", reportFieldFormat.getFormat());
        }
    }

    protected void writeTemplates(Report report, Element root) {
        Map<String, ReportTemplate> reportTemplates = report.getReportTemplates();
        Element reportTemplatesElement = root.addElement("templates");
        for (ReportTemplate reportTemplate : reportTemplates.values()) {
            Element reportTemplateElement = reportTemplatesElement.addElement("template");
            reportTemplateElement.addAttribute("code", reportTemplate.getCode());
            reportTemplateElement.addAttribute("documentName", reportTemplate.getDocumentName());
            reportTemplateElement.addAttribute("documentPath", reportTemplate.getDocumentPath());
            reportTemplateElement.addAttribute("outputType", reportTemplate.getOutputType().getId());
            reportTemplateElement.addAttribute("outputNamePattern", reportTemplate.getOutputNamePattern());
        }
    }

    protected void writeBandDefinition(Element element, ReportBand bandDefinition) {
        element.addAttribute("name", bandDefinition.getName());
        element.addAttribute("orientation", bandDefinition.getBandOrientation().id);
        Element childrenBandsElement = element.addElement("bands");

        Element reportQueriesElement = element.addElement("queries");
        if (bandDefinition.getReportQueries() != null) {
            for (ReportQuery reportQuery : bandDefinition.getReportQueries()) {
                Element reportQueryElement = reportQueriesElement.addElement("query");
                reportQueryElement.addAttribute("name", reportQuery.getName());
                reportQueryElement.addAttribute("type", reportQuery.getLoaderType());
                reportQueryElement.addElement("script").setText(reportQuery.getScript());
            }
        }

        if (bandDefinition.getChildren() != null) {
            for (ReportBand childBandDefinition : bandDefinition.getChildren()) {
                Element childBandElement = childrenBandsElement.addElement("band");
                writeBandDefinition(childBandElement, childBandDefinition);
            }
        }
    }
}