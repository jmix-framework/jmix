/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplates.impl;

import io.jmix.core.Metadata;
import io.jmix.emailtemplates.TemplateConverter;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.emailtemplates.utils.HtmlTemplateUtils;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component("emltmp_TemplateConverter")
public class TemplateConverterImpl implements TemplateConverter {

    @Autowired
    private Metadata metadata;

    @Autowired
    private ReportsSerialization reportsSerialization;

    @Override
    public Report convertToReport(JsonEmailTemplate template) {
        String reportJson = template.getReportJson();
        Report report = null;
        if (StringUtils.isNotBlank(reportJson)) {
            report = reportsSerialization.convertToReport(reportJson);
            report.setXml(reportJson);
        } else {
            report = initReport(template);
        }
        report.setName(template.getName());
        report.setCode(template.getCode());
        updateReportOutputName(report, template);
        report.getDefaultTemplate().setContent(getHtmlReportTemplate(template).getBytes(UTF_8));
        report.setIsTmp(true);
        return report;
    }

    public String getHtmlReportTemplate(JsonEmailTemplate template) {
        String html = template.getHtml();
        if (html == null) {
            html = "";
        } else {
            html = HtmlTemplateUtils.prettyPrintHTML(html);
        }
        return html.replaceAll("\\$\\{([a-zA-Z0-9.]*[^}]*)}", "\\$\\{Root.fields.$1\\}");
    }

    private Report initReport(JsonEmailTemplate jsonTemplate) {
        Report report = metadata.create(Report.class);

        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setCode(ReportTemplate.DEFAULT_TEMPLATE_CODE);
        template.setReportOutputType(ReportOutputType.HTML);
        template.setReport(report);
        template.setName("template.html");
        String html = jsonTemplate.getHtml();
        if (html != null) {
            template.setContent(html.getBytes(UTF_8));
        }
        report.setTemplates(Collections.singletonList(template));
        report.setDefaultTemplate(template);

        BandDefinition rootDefinition = metadata.create(BandDefinition.class);
        rootDefinition.setReport(report);
        rootDefinition.setName("Root");
        rootDefinition.setPosition(0);
        rootDefinition.setDataSets(new ArrayList<>());
        report.setBands(new HashSet<>());
        report.getBands().add(rootDefinition);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setBandDefinition(rootDefinition);
        dataSet.setType(DataSetType.GROOVY);
        dataSet.setName("Root");
        rootDefinition.getDataSets().add(dataSet);

        rootDefinition.setReport(report);


        report.setName(jsonTemplate.getName());
        report.setReportType(ReportType.SIMPLE);
        report.setIsTmp(true);

        report.setXml(reportsSerialization.convertToString(report));
        return report;
    }

    public void updateReportOutputName(Report report, JsonEmailTemplate template) {
        BandDefinition rootBandDefinition = IterableUtils.find(report.getBands(), (Predicate) object -> {
            BandDefinition band = (BandDefinition) object;
            return band.getParentBandDefinition() == null;
        });
        DataSet dataSet = rootBandDefinition.getDataSets().stream()
                .filter(e -> "Root".equals(e.getName()))
                .findFirst()
                .orElse(null);
        if (dataSet == null) {
            dataSet = metadata.create(DataSet.class);
            dataSet.setBandDefinition(rootBandDefinition);
            dataSet.setType(DataSetType.GROOVY);
            dataSet.setName("Root");
            rootBandDefinition.getDataSets().add(dataSet);
        }
        String subject = template.getSubject();
        if (StringUtils.isNotBlank(subject)) {
            subject = subject.replaceAll("\\$\\{([a-zA-Z0-9]*)}", "\"+params[\"$1\"]+\"");
            subject = subject.replaceAll("\\$\\{([a-zA-Z0-9]*).([a-zA-Z0-9.]*)}", "\"+params[\"$1\"].$2+\"");
            dataSet.setText("return [[\"__REPORT_FILE_NAME\": \"" + subject + "\"]]");
        } else {
            dataSet.setText("return []");
        }
    }
}
