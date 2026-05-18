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
 * Internal marker context that tells the UI runner to open a spreadsheet report in the embedded spreadsheet viewer.
 */
@Internal
public class SpreadsheetUiReportRunContext extends UiReportRunContext {

    /**
     * Creates a spreadsheet-marked copy of a regular UI report run context.
     */
    public SpreadsheetUiReportRunContext(UiReportRunContext sourceContext) {
        setReportRunContext(sourceContext.getReportRunContext());
        if (sourceContext.getOwner() != null) {
            setOwner(sourceContext.getOwner());
        }
        setInBackground(sourceContext.getInBackground());
        setParametersDialogShowMode(sourceContext.getParametersDialogShowMode());
    }
}
