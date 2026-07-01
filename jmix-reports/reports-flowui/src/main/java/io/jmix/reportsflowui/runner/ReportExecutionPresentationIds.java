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

/**
 * Well-known identifiers for built-in {@link ReportExecutionPresentation} implementations.
 */
@Internal
public final class ReportExecutionPresentationIds {

    /** Default presentation: downloads the output document or opens a built-in viewer. */
    public static final String DEFAULT = "default";

    /** Spreadsheet presentation: opens XLS/XLSX output in the embedded spreadsheet viewer (premium). */
    public static final String SPREADSHEET = "spreadsheet";

    /** Table presentation: renders JSON output in an in-dialog table view. */
    public static final String TABLE = "table";

    private ReportExecutionPresentationIds() {
    }
}
