/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl.builder;

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("report_AnnotatedReportBuilder")
public class AnnotatedReportBuilderImpl implements AnnotatedReportBuilder {

    protected final Metadata metadata;
    protected final MessageTools messageTools;

    public AnnotatedReportBuilderImpl(Metadata metadata, MessageTools messageTools) {
        this.metadata = metadata;
        this.messageTools = messageTools;
    }

    @Override
    public Report createReportFromDefinition(Object reportDefinition) {
        Class<?> reportClass = reportDefinition.getClass();
        ReportDef reportAnnotation = reportClass.getAnnotation(ReportDef.class);

        Report report = metadata.create(Report.class);
        assignReportParameters(report, reportAnnotation);

        report.setInputParameters(extractInputParameters(reportClass));
        // todo bands, templates, value formats, report group
        return report;
    }

    private void assignReportParameters(Report report, ReportDef reportAnnotation) {
        String nameValue = reportAnnotation.name();
        report.setName(messageTools.loadString(nameValue));

        if (nameValue.startsWith(MessageTools.MARK)) {
            report.setLocaleNames(buildLocaleNames(nameValue));
        }

        report.setCode(reportAnnotation.code());
    }

    private String buildLocaleNames(String name) {
        // todo get locales here: io.jmix.core.MessageTools.getAvailableLocalesMap
        //   do the same like io.jmix.reports.util.MsgBundleTools.getLocalizedValue but reversed
        return null;
    }

    private List<ReportInputParameter> extractInputParameters(Class<?> reportClass) {
        return null; // todo
    }
}
