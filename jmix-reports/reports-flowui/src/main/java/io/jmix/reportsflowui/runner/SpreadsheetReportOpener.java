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
import io.jmix.flowui.view.View;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;

/**
 * Optional bridge contract for opening spreadsheet report output inside a UI viewer instead of downloading a file.
 */
public interface SpreadsheetReportOpener {

    boolean supportsExtension(String extension);

    void open(View<?> owner, ReportOutputDocument document, String documentName);

    void open(View<?> owner, FileRef fileRef);
}
