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

import io.jmix.reports.yarg.exception.ReportingXmlException;
import io.jmix.reports.yarg.structure.*;
import io.jmix.reports.yarg.structure.impl.*;
import io.jmix.reports.yarg.structure.xml.XmlReader;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefaultXmlReader implements XmlReader {

    @Override
    public Report parseXml(String xml) throws IOException {
        try {
            SAXReader reader;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SchemaFactory schemaFactory =
                        SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

                factory.setSchema(schemaFactory.newSchema(
                        new Source[]{new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporting.xsd"))}));

                SAXParser parser = factory.newSAXParser();

                reader = new SAXReader(parser.getXMLReader());
            } catch (SAXException e) {
                throw new ReportingXmlException(String.format("An error occurred during loading reporting xsd. \\n[%s]", xml), e);
            } catch (ParserConfigurationException e) {
                throw new ReportingXmlException(String.format("An error occurred during loading reporting xsd. \\n[%s]", xml), e);
            }

            Document document = reader.read(new StringReader(xml));
            Element rootElement = document.getRootElement();
            Map<String, ReportTemplate> templateMap = parseTemplates(rootElement);
            List<ReportParameter> reportParameters = parseInputParameters(rootElement);
            List<ReportFieldFormat> reportFieldFormats = parseValueFormats(rootElement);
            BandBuilder rootBandDefinitionBuilder = new BandBuilder().name(BandData.ROOT_BAND_NAME);
            parseQueries(rootElement.element("rootBand"), rootBandDefinitionBuilder);
            parseChildBandDefinitions(rootElement.element("rootBand"), rootBandDefinitionBuilder);
            ReportBand rootBandDefinition = rootBandDefinitionBuilder.build();
            String reportName = rootElement.attribute("name").getText();
            ReportImpl report = new ReportImpl(reportName, templateMap, rootBandDefinition, reportParameters, reportFieldFormats);
            return report;
        } catch (DocumentException e) {
            throw new ReportingXmlException(String.format("An error occurred while parsing report xml. \\n[%s]", xml), e);
        } catch (FileNotFoundException e) {
            throw new ReportingXmlException(String.format("Could not find report template. \\n[%s]", xml), e);
        } catch (ClassNotFoundException e) {
            throw new ReportingXmlException(String.format("Report parameter class not found. \\n[%s]", xml), e);
        }
    }

    /**
     * Override this method to load files differently from basic file system way
     *
     * @param documentPath - path to document (file system path or other if overriden)
     * @throws FileNotFoundException
     */
    protected InputStream getDocumentContent(String documentPath) throws FileNotFoundException {
        return new FileInputStream(documentPath);
    }

    protected Map<String, ReportTemplate> parseTemplates(Element rootElement) throws IOException {
        Element templatesElement = rootElement.element("templates");
        List<Element> templates = templatesElement.elements("template");
        Map<String, ReportTemplate> templateMap = new HashMap<String, ReportTemplate>();
        for (Element template : templates) {
            String code = template.attribute("code").getText();
            String documentName = template.attribute("documentName").getText();
            String documentPath = template.attribute("documentPath").getText();
            String outputType = template.attribute("outputType").getText();
            String outputNamePattern = template.attribute("outputNamePattern").getText();

            ReportTemplate reportTemplate = new ReportTemplateBuilder()
                    .code(code)
                    .documentName(documentName)
                    .documentPath(documentPath)
                    .documentContent(getDocumentContent(documentPath))
                    .outputType(ReportOutputType.getOutputTypeById(outputType))
                    .outputNamePattern(outputNamePattern).build();


            templateMap.put(reportTemplate.getCode(), reportTemplate);
        }

        return templateMap;
    }

    protected List<ReportParameter> parseInputParameters(Element rootElement) throws FileNotFoundException, ClassNotFoundException {
        List<ReportParameter> reportParameters = new ArrayList<ReportParameter>();

        Element inputParametersElement = rootElement.element("parameters");
        if (inputParametersElement != null) {
            List<Element> parameters = inputParametersElement.elements("parameter");
            for (Element parameter : parameters) {
                String name = parameter.attribute("name").getText();
                String alias = parameter.attribute("alias").getText();
                Boolean required = Boolean.valueOf(parameter.attribute("required").getText());
                Class type = Class.forName(parameter.attribute("class").getText());
                Attribute defaultValueAttr = parameter.attribute("defaultValue");
                String defaultValue = null;
                if (defaultValueAttr != null) {
                    defaultValue = defaultValueAttr.getText();
                }

                ReportParameterImpl reportParameter = new ReportParameterImpl(name, alias, required, type, defaultValue);
                reportParameters.add(reportParameter);
            }
        }

        return reportParameters;
    }

    protected List<ReportFieldFormat> parseValueFormats(Element rootElement) throws FileNotFoundException, ClassNotFoundException {
        List<ReportFieldFormat> reportParameters = new ArrayList<ReportFieldFormat>();
        Element formatsElement = rootElement.element("formats");

        if (formatsElement != null) {
            List<Element> parameters = formatsElement.elements("format");
            for (Element parameter : parameters) {
                String name = parameter.attribute("name").getText();
                String format = parameter.attribute("format").getText();
                Attribute groovyScriptAttribute = parameter.attribute("groovyScript");
                if (groovyScriptAttribute != null) {
                    Boolean groovyFlag = (Boolean) groovyScriptAttribute.getData();
                    reportParameters.add(new ReportFieldFormatImpl(name, format, groovyFlag));
                } else {
                    reportParameters.add(new ReportFieldFormatImpl(name, format));
                }
            }
        }

        return reportParameters;
    }

    protected void parseChildBandDefinitions(Element bandDefinitionElement, BandBuilder parentBandDefinitionBuilder) throws FileNotFoundException, ClassNotFoundException {
        Element childrenBandsElement = bandDefinitionElement.element("bands");
        if (childrenBandsElement != null) {
            List<Element> childrenBands = childrenBandsElement.elements("band");
            for (Element childBandElement : childrenBands) {
                String childBandName = childBandElement.attribute("name").getText();
                BandOrientation orientation = BandOrientation.fromId(childBandElement.attribute("orientation").getText());
                BandBuilder childBandDefinitionBuilder =
                        new BandBuilder()
                                .name(childBandName)
                                .orientation(orientation);

                parseQueries(childBandElement, childBandDefinitionBuilder);
                parseChildBandDefinitions(childBandElement, childBandDefinitionBuilder);
                ReportBand childBandDefinition = childBandDefinitionBuilder.build();
                parentBandDefinitionBuilder.child(childBandDefinition);
            }
        }
    }

    private void parseQueries(Element bandElement, BandBuilder bandDefinitionBuilder) {
        Element reportQueriesElement = bandElement.element("queries");

        if (reportQueriesElement != null) {
            List<Element> reportQueryElements = reportQueriesElement.elements("query");
            for (Element queryElement : reportQueryElements) {
                String script = queryElement.element("script").getText();
                String type = queryElement.attribute("type").getText();
                String queryName = queryElement.attribute("name").getText();

                bandDefinitionBuilder.query(queryName, script, type);
            }
        }
    }
}