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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all {@link ReportExecutionPresentation} implementations available in the application context.
 * <p>
 * Used by the report runner to resolve how a specific output channel (download, spreadsheet viewer,
 * table viewer, …) should prepare and display a report result.
 */
@Internal
@Component("report_ReportPresentationRegistry")
public class ReportPresentationRegistry {

    @Autowired
    protected List<ReportExecutionPresentation> presentations;

    protected final Map<String, ReportExecutionPresentation> presentationsById = new LinkedHashMap<>();

    @PostConstruct
    protected void init() {
        presentations.forEach(p -> presentationsById.put(p.getId(), p));
    }

    /**
     * @return the presentation associated with the run context, falling back to
     *         {@link ReportExecutionPresentationIds#DEFAULT} when the context carries no explicit presentation
     * @throws IllegalArgumentException if the resolved presentation id is not registered
     */
    public ReportExecutionPresentation getPresentation(UiReportRunContext context) {
        if (context instanceof PresentationUiReportRunContext presentationContext) {
            return getPresentation(presentationContext.getPresentationId());
        }
        return getPresentation(ReportExecutionPresentationIds.DEFAULT);
    }

    /**
     * @return the presentation registered under {@code presentationId}
     * @throws IllegalArgumentException if no presentation with the given id is registered
     */
    public ReportExecutionPresentation getPresentation(String presentationId) {
        ReportExecutionPresentation presentation = presentationsById.get(presentationId);
        if (presentation == null) {
            throw new IllegalArgumentException("Unknown report presentation: " + presentationId);
        }
        return presentation;
    }

    /**
     * @return {@code true} if a presentation with the given id is registered
     */
    public boolean hasPresentation(String presentationId) {
        return presentationsById.containsKey(presentationId);
    }

    /**
     * @return {@code true} if the presentation is registered and supports the report;
     *         {@code false} when the presentation is not registered
     */
    public boolean supportsReport(@Nullable Report report, String presentationId) {
        if (!hasPresentation(presentationId)) {
            return false;
        }
        return getPresentation(presentationId).supportsReport(report);
    }

    /**
     * Wraps {@code sourceContext} in a {@link PresentationUiReportRunContext} targeting the given presentation.
     * Returns the original context unchanged when the presentation is {@link ReportExecutionPresentationIds#DEFAULT}
     * and the context is not already presentation-aware.
     */
    public UiReportRunContext createRunContext(UiReportRunContext sourceContext, String presentationId) {
        ReportExecutionPresentation presentation = getPresentation(presentationId);
        if (ReportExecutionPresentationIds.DEFAULT.equals(presentation.getId())) {
            return sourceContext instanceof PresentationUiReportRunContext
                    ? copyContext(sourceContext)
                    : sourceContext;
        }

        if (sourceContext instanceof PresentationUiReportRunContext presentationContext
                && presentationContext.getPresentationId().equals(presentation.getId())) {
            return sourceContext;
        }

        return new PresentationUiReportRunContext(sourceContext, presentation.getId());
    }

    /**
     * @return {@code true} if the run context targets the presentation identified by {@code presentationId}
     */
    public boolean isPresentation(UiReportRunContext context, String presentationId) {
        return getPresentation(context).getId().equals(presentationId);
    }

    /**
     * Resolves the default template and output type for the presentation carried by the context
     * and writes them back into the context's underlying run context.
     */
    public void applyPresentationDefaults(UiReportRunContext context) {
        ReportExecutionPresentation presentation = getPresentation(context);
        Report report = context.getReport();
        ReportTemplate template = presentation.resolveDefaultTemplate(report, context.getReportTemplate());
        if (shouldPreserveMissingDefaultTemplate(context, presentation, report)) {
            template = null;
        }
        context.getReportRunContext().setReportTemplate(template);

        ReportOutputType outputType =
                presentation.resolveDefaultOutputType(report, template, context.getOutputType());
        context.getReportRunContext().setOutputType(outputType);
    }

    protected UiReportRunContext copyContext(UiReportRunContext sourceContext) {
        UiReportRunContext copyContext = new UiReportRunContext()
                .setReportRunContext(sourceContext.getReportRunContext())
                .setInBackground(sourceContext.getInBackground())
                .setParametersDialogShowMode(sourceContext.getParametersDialogShowMode());
        if (sourceContext.getOwner() != null) {
            copyContext.setOwner(sourceContext.getOwner());
        }
        return copyContext;
    }

    protected boolean shouldPreserveMissingDefaultTemplate(UiReportRunContext context,
                                                           ReportExecutionPresentation presentation,
                                                           @Nullable Report report) {
        return ReportExecutionPresentationIds.DEFAULT.equals(presentation.getId())
                && report != null
                && context.getReportTemplate() == null
                && report.getDefaultTemplate() == null
                && !presentation.requiresUserChoice(report, null, context.getOutputType());
    }
}
