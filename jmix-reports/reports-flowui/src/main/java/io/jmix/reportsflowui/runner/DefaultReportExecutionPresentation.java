/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.runner;

import io.jmix.core.annotation.Internal;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static io.jmix.reports.util.ReportTemplateUtils.containsAlterableTemplate;
import static io.jmix.reports.util.ReportTemplateUtils.supportAlterableForTemplate;

@Internal
@NullMarked
@Component("report_DefaultReportExecutionPresentation")
public class DefaultReportExecutionPresentation implements ReportExecutionPresentation {

    @Override
    public String getId() {
        return ReportExecutionPresentationIds.DEFAULT;
    }

    @Override
    public boolean supportsReport(@Nullable Report report) {
        return report != null;
    }

    @Override
    public List<ReportTemplate> getAvailableTemplates(@Nullable Report report) {
        return report == null || report.getTemplates() == null
                ? Collections.emptyList()
                : report.getTemplates();
    }

    @Nullable
    @Override
    public ReportTemplate resolveDefaultTemplate(@Nullable Report report, @Nullable ReportTemplate selectedTemplate) {
        List<ReportTemplate> templates = getAvailableTemplates(report);
        if (selectedTemplate != null && templates.contains(selectedTemplate)) {
            return selectedTemplate;
        }

        if (report != null && report.getDefaultTemplate() != null && templates.contains(report.getDefaultTemplate())) {
            return report.getDefaultTemplate();
        }

        return templates.isEmpty() ? null : templates.get(0);
    }

    @Override
    public List<ReportOutputType> getAvailableOutputTypes(@Nullable Report report, @Nullable ReportTemplate template) {
        if (template == null || !supportAlterableForTemplate(template)) {
            return Collections.emptyList();
        }

        return ReportPrintHelper.getInputOutputTypesMapping()
                .getOrDefault(template.getExt(), Collections.emptyList());
    }

    @Nullable
    @Override
    public ReportOutputType resolveDefaultOutputType(@Nullable Report report, @Nullable ReportTemplate template,
                                                     @Nullable ReportOutputType selectedOutputType) {
        if (template == null) {
            return null;
        }

        if (selectedOutputType != null) {
            // The "alterable output" flag restricts the output type selection in the input parameters dialog only,
            // so an output type requested explicitly, e.g. by the report runner API, is always applied. A type the
            // template cannot be rendered to is rejected by the formatter with a message naming both of them.
            return selectedOutputType;
        }

        List<ReportOutputType> outputTypes = getAvailableOutputTypes(report, template);
        if (outputTypes.isEmpty()) {
            return template.getReportOutputType();
        }

        ReportOutputType defaultOutputType = template.getReportOutputType();
        return defaultOutputType != null && outputTypes.contains(defaultOutputType)
                ? defaultOutputType
                : outputTypes.get(0);
    }

    @Override
    public boolean requiresUserChoice(@Nullable Report report, @Nullable ReportTemplate selectedTemplate,
                                      @Nullable ReportOutputType selectedOutputType) {
        if (report == null) {
            return false;
        }

        if (getAvailableTemplates(report).size() > 1) {
            return true;
        }

        // The output type is the only thing the dialog can ask for, so there is no reason to show it
        // when the output type is already chosen, e.g. by the report runner API.
        return selectedOutputType == null && containsAlterableTemplate(report);
    }
}
