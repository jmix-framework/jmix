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

package io.jmix.reportsflowui.runner.impl;

import io.jmix.core.FileRef;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.SpreadsheetReportOpener;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("report_SpreadsheetReportInternalSupport")
public class SpreadsheetReportInternalSupport {

    @Autowired
    protected ObjectProvider<SpreadsheetReportOpener> spreadsheetReportOpenerProvider;

    public boolean isAvailable() {
        return spreadsheetReportOpenerProvider.getIfAvailable() != null;
    }

    public boolean supportsOutputType(@Nullable ReportOutputType outputType) {
        return outputType != null
                && isSpreadsheetOutputType(outputType)
                && supportsExtension(outputType.getOutputType().getId());
    }

    public boolean supportsDefaultOutput(@Nullable Report report) {
        if (report == null) {
            return false;
        }

        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        return defaultTemplate != null && supportsOutputType(defaultTemplate.getReportOutputType());
    }

    public boolean supportsFileRef(@Nullable FileRef fileRef) {
        return fileRef != null && supportsDocumentName(fileRef.getFileName());
    }

    public boolean supportsDocumentName(@Nullable String documentName) {
        return supportsExtension(getExtension(documentName));
    }

    public boolean supportsExtension(@Nullable String extension) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        String normalizedExtension = normalizeExtension(extension);
        return spreadsheetReportOpener != null
                && normalizedExtension != null
                && isSpreadsheetExtension(normalizedExtension)
                && spreadsheetReportOpener.supportsExtension(normalizedExtension);
    }

    public boolean open(@Nullable View<?> owner, ReportOutputDocument document, String documentName) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        View<?> resolvedOwner = resolveOwner(owner);
        if (spreadsheetReportOpener == null || resolvedOwner == null) {
            return false;
        }

        spreadsheetReportOpener.open(resolvedOwner, document, documentName);
        return true;
    }

    public boolean open(@Nullable View<?> owner, FileRef fileRef) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        View<?> resolvedOwner = resolveOwner(owner);
        if (spreadsheetReportOpener == null || resolvedOwner == null) {
            return false;
        }

        spreadsheetReportOpener.open(resolvedOwner, fileRef);
        return true;
    }

    public UiReportRunContext createRunContext(FluentUiReportRunner fluentRunner) {
        return createRunContext(fluentRunner.buildContext());
    }

    public UiReportRunContext createRunContext(UiReportRunContext sourceContext) {
        return new SpreadsheetUiReportRunContext(sourceContext);
    }

    public boolean isSpreadsheetRunContext(UiReportRunContext context) {
        return context instanceof SpreadsheetUiReportRunContext;
    }

    @Nullable
    protected View<?> resolveOwner(@Nullable View<?> owner) {
        if (owner != null) {
            return owner;
        }

        try {
            return UiComponentUtils.getCurrentView();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Nullable
    protected String getExtension(@Nullable String documentName) {
        if (StringUtils.isBlank(documentName) || !documentName.contains(".")) {
            return null;
        }

        return normalizeExtension(StringUtils.substringAfterLast(documentName, "."));
    }

    @Nullable
    protected String normalizeExtension(@Nullable String extension) {
        if (StringUtils.isBlank(extension)) {
            return null;
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    protected boolean isSpreadsheetOutputType(ReportOutputType outputType) {
        return outputType == ReportOutputType.XLS || outputType == ReportOutputType.XLSX;
    }

    protected boolean isSpreadsheetExtension(String extension) {
        return "xls".equals(extension) || "xlsx".equals(extension);
    }
}
