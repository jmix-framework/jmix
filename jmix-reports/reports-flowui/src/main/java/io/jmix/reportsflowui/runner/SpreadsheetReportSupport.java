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

import io.jmix.core.FileRef;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Internal support bean for spreadsheet-specific report execution and opening logic.
 * <p>
 * The bean keeps spreadsheet viewer behavior out of the general OSS runner API while still allowing built-in
 * reports UI and premium modules to opt into spreadsheet rendering.
 */
@Internal
@Component("report_SpreadsheetReportSupport")
public class SpreadsheetReportSupport {

    @Autowired
    protected ObjectProvider<SpreadsheetReportOpener> spreadsheetReportOpenerProvider;

    /**
     * Returns whether a spreadsheet opener bridge is available on the classpath.
     */
    public boolean isAvailable() {
        return spreadsheetReportOpenerProvider.getIfAvailable() != null;
    }

    /**
     * Returns whether the given report output type can be opened in a spreadsheet viewer.
     */
    public boolean supportsOutputType(@Nullable ReportOutputType outputType) {
        return outputType != null
                && isSpreadsheetOutputType(outputType)
                && supportsExtension(outputType.getOutputType().getId());
    }

    /**
     * Returns whether the report default template can be opened in a spreadsheet viewer.
     */
    public boolean supportsDefaultOutput(@Nullable Report report) {
        if (report == null) {
            return false;
        }

        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        return defaultTemplate != null && supportsOutputType(defaultTemplate.getReportOutputType());
    }

    /**
     * Returns whether the stored report output can be opened in a spreadsheet viewer.
     */
    public boolean supportsFileRef(@Nullable FileRef fileRef) {
        return fileRef != null && supportsDocumentName(fileRef.getFileName());
    }

    /**
     * Returns whether a report document with the given name can be opened in a spreadsheet viewer.
     */
    public boolean supportsDocumentName(@Nullable String documentName) {
        return supportsExtension(getExtension(documentName));
    }

    /**
     * Returns whether the specified file extension can be opened in a spreadsheet viewer.
     */
    public boolean supportsExtension(@Nullable String extension) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        String normalizedExtension = normalizeExtension(extension);
        return spreadsheetReportOpener != null
                && normalizedExtension != null
                && isSpreadsheetExtension(normalizedExtension)
                && spreadsheetReportOpener.supportsExtension(normalizedExtension);
    }

    /**
     * Opens a generated report document in a spreadsheet viewer.
     */
    public boolean open(@Nullable View<?> owner, ReportOutputDocument document, @Nullable String documentName) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        View<?> resolvedOwner = resolveOwner(owner);
        if (spreadsheetReportOpener == null || resolvedOwner == null) {
            return false;
        }

        spreadsheetReportOpener.open(resolvedOwner, document, documentName);
        return true;
    }

    /**
     * Opens a stored report document in a spreadsheet viewer.
     */
    public boolean open(@Nullable View<?> owner, FileRef fileRef) {
        SpreadsheetReportOpener spreadsheetReportOpener = spreadsheetReportOpenerProvider.getIfAvailable();
        View<?> resolvedOwner = resolveOwner(owner);
        if (spreadsheetReportOpener == null || resolvedOwner == null) {
            return false;
        }

        spreadsheetReportOpener.open(resolvedOwner, fileRef);
        return true;
    }

    /**
     * Wraps a regular report run context with an internal spreadsheet marker.
     */
    public UiReportRunContext createRunContext(UiReportRunContext sourceContext) {
        return new SpreadsheetUiReportRunContext(sourceContext);
    }

    /**
     * Returns whether the given run context was explicitly marked for spreadsheet rendering.
     */
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
