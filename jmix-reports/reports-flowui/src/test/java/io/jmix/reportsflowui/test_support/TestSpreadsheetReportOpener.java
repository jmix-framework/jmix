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

package io.jmix.reportsflowui.test_support;

import io.jmix.core.FileRef;
import io.jmix.flowui.view.View;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.runner.SpreadsheetReportOpener;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class TestSpreadsheetReportOpener implements SpreadsheetReportOpener {

    protected Set<String> supportedExtensions = new LinkedHashSet<>(Set.of("xls", "xlsx"));
    protected int openedDocumentCount;
    protected int openedFileRefCount;
    protected ReportOutputDocument lastDocument;
    protected String lastDocumentName;
    protected FileRef lastFileRef;

    @Override
    public boolean supportsExtension(@NonNull String extension) {
        return supportedExtensions.contains(extension.toLowerCase(Locale.ROOT));
    }

    @Override
    public void open(@NonNull View<?> owner, @NonNull ReportOutputDocument document, String documentName) {
        openedDocumentCount++;
        lastDocument = document;
        lastDocumentName = documentName;
    }

    @Override
    public void open(@NonNull View<?> owner, @NonNull FileRef fileRef) {
        openedFileRefCount++;
        lastFileRef = fileRef;
    }

    public void reset() {
        supportedExtensions = new LinkedHashSet<>(Set.of("xls", "xlsx"));
        openedDocumentCount = 0;
        openedFileRefCount = 0;
        lastDocument = null;
        lastDocumentName = null;
        lastFileRef = null;
    }

    public void setSupportedExtensions(Set<String> supportedExtensions) {
        this.supportedExtensions = new LinkedHashSet<>(supportedExtensions);
    }

    public int getOpenedDocumentCount() {
        return openedDocumentCount;
    }

    public int getOpenedFileRefCount() {
        return openedFileRefCount;
    }

    public ReportOutputDocument getLastDocument() {
        return lastDocument;
    }

    public String getLastDocumentName() {
        return lastDocumentName;
    }

    public FileRef getLastFileRef() {
        return lastFileRef;
    }
}
