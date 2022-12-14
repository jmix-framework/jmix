/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reports.libintegration;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.impl.DocxFormatter;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;
import com.haulmont.yarg.formatters.impl.XlsxFormatter;
import io.jmix.reports.ReportsProperties;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JmixFormatterFactory extends DefaultFormatterFactory {

    @Autowired
    protected BeanFactory beanFactory;
    @Autowired
    protected ReportsProperties reportsProperties;

    protected boolean useOfficeForDocumentConversion = true;

    public JmixFormatterFactory() {
        super();
        FormatterCreator ftlCreator = factoryInput -> {
            HtmlFormatter htmlFormatter = beanFactory.getBean(JmixHtmlFormatter.class, factoryInput);
            htmlFormatter.setDefaultFormatProvider(defaultFormatProvider);
            htmlFormatter.setScripting(scripting);
            return htmlFormatter;
        };
        formattersMap.put("ftl", ftlCreator);
        formattersMap.put("html", ftlCreator);

        FormatterCreator docxCreator = factoryInput -> {
            DocxFormatter docxFormatter = new DocxFormatter(factoryInput);
            docxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            if (useOfficeForDocumentConversion) {
                docxFormatter.setDocumentConverter(documentConverter);
            }
            docxFormatter.setHtmlImportProcessor(htmlImportProcessor);
            docxFormatter.setScripting(scripting);
            return docxFormatter;
        };

        formattersMap.put("docx", docxCreator);
        formattersMap.put("chart", factoryInput -> beanFactory.getBean(ChartFormatter.class, factoryInput));
        formattersMap.put("pivot", factoryInput -> beanFactory.getBean(PivotTableFormatter.class, factoryInput));

        FormatterCreator xlsxCreator = factoryInput -> {
            XlsxFormatter xlsxFormatter = beanFactory.getBean(JmixXlsxFormatter.class, factoryInput);
            xlsxFormatter.setDefaultFormatProvider(defaultFormatProvider);
            xlsxFormatter.setDocumentConverter(documentConverter);
            xlsxFormatter.setScripting(scripting);
            xlsxFormatter.setFormulasPostProcessingEvaluationEnabled(reportsProperties.isFormulasPostProcessingEvaluationEnabled());
            return xlsxFormatter;
        };
        formattersMap.put("xlsx", xlsxCreator);

        formattersMap.put("table", factoryInput -> beanFactory.getBean(JmixTableFormatter.class, factoryInput));
    }

    public boolean isUseOfficeForDocumentConversion() {
        return useOfficeForDocumentConversion;
    }

    public void setUseOfficeForDocumentConversion(boolean useOfficeForDocumentConversion) {
        this.useOfficeForDocumentConversion = useOfficeForDocumentConversion;
    }
}