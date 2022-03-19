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

package io.jmix.reportsui.screen.report.wizard.template;

import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.screen.report.wizard.template.generators.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("report_TemplateGenerator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplateGenerator {

    @Autowired
    protected ApplicationContext applicationContext;

    public byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException {
        byte[] template;
        try {
            template = createGenerator(templateFileType).generate(reportData);
        } catch (Exception e) {
            throw new TemplateGenerationException(e);
        }
        return template;

    }

    protected Generator createGenerator(TemplateFileType templateFileType) throws TemplateGenerationException {
        Generator generator;
        switch (templateFileType) {
            case DOCX:
                generator = applicationContext.getBean(DocxGenerator.class);
                break;
            case XLSX:
                generator = applicationContext.getBean(XlsxGenerator.class);
                break;
            case HTML:
                generator = applicationContext.getBean(HtmlGenerator.class);
                break;
            case CHART:
                generator = applicationContext.getBean(ChartGenerator.class);
                break;
            case CSV:
                generator = applicationContext.getBean(CsvGenerator.class);
                break;
            case TABLE:
                generator = applicationContext.getBean(TableGenerator.class);
                break;
            default:
                throw new TemplateGenerationException(templateFileType + " format is unsupported yet");
        }
        return generator;
    }
}