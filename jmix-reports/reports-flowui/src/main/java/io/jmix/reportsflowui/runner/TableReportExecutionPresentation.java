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
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * {@link ReportExecutionPresentation} that targets the in-dialog table viewer.
 * <p>
 * Supports only templates whose output type is {@link ReportOutputType#TABLE}.
 */
@Internal
@Component("report_TableReportExecutionPresentation")
public class TableReportExecutionPresentation implements ReportExecutionPresentation {

    /**
     * @return {@link ReportExecutionPresentationIds#TABLE}
     */
    @Override
    public String getId() {
        return ReportExecutionPresentationIds.TABLE;
    }

    /**
     * @return {@code true} if the report has at least one template that produces table output
     */
    @Override
    public boolean supportsReport(@Nullable Report report) {
        return !getAvailableTemplates(report).isEmpty();
    }

    /**
     * @return templates of the report whose output type is {@link ReportOutputType#TABLE}
     */
    @Override
    public List<ReportTemplate> getAvailableTemplates(@Nullable Report report) {
        if (report == null || report.getTemplates() == null) {
            return Collections.emptyList();
        }

        return report.getTemplates().stream()
                .filter(this::supportsTemplate)
                .toList();
    }

    /**
     * @return the template to use by default, preferring {@code selectedTemplate} if it produces table output
     */
    @Nullable
    @Override
    public ReportTemplate resolveDefaultTemplate(@Nullable Report report, @Nullable ReportTemplate selectedTemplate) {
        List<ReportTemplate> templates = getAvailableTemplates(report);
        if (selectedTemplate != null && templates.contains(selectedTemplate)) {
            return selectedTemplate;
        }

        if (report != null && report.getDefaultTemplate() != null && supportsTemplate(report.getDefaultTemplate())) {
            return report.getDefaultTemplate();
        }

        return templates.isEmpty() ? null : templates.get(0);
    }

    /**
     * @return an empty list — the table presentation does not support alterable output types
     */
    @Override
    public List<ReportOutputType> getAvailableOutputTypes(@Nullable Report report, @Nullable ReportTemplate template) {
        return Collections.emptyList();
    }

    /**
     * @return {@link ReportOutputType#TABLE} if the template produces table output, {@code null} otherwise
     */
    @Nullable
    @Override
    public ReportOutputType resolveDefaultOutputType(@Nullable Report report, @Nullable ReportTemplate template,
                                                     @Nullable ReportOutputType selectedOutputType) {
        return supportsTemplate(template) ? ReportOutputType.TABLE : null;
    }

    /**
     * @return {@code true} if the report has more than one table-compatible template and none is pre-selected
     */
    @Override
    public boolean requiresUserChoice(@Nullable Report report, @Nullable ReportTemplate selectedTemplate,
                                      @Nullable ReportOutputType selectedOutputType) {
        if (report == null) {
            return false;
        }

        if (supportsTemplate(selectedTemplate)) {
            return false;
        }

        return getAvailableTemplates(report).size() > 1;
    }

    protected boolean supportsTemplate(@Nullable ReportTemplate template) {
        return template != null && template.getReportOutputType() == ReportOutputType.TABLE;
    }
}
