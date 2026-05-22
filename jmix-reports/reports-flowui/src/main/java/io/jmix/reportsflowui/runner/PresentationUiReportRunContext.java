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

import java.util.Objects;

/**
 * Extension of {@link UiReportRunContext} that carries an explicit {@link ReportExecutionPresentation} identifier.
 * <p>
 * Created by {@link ReportPresentationRegistry#createRunContext} when the runner needs to target a specific
 * output channel (spreadsheet viewer, table viewer, …) instead of the default download behaviour.
 */
@Internal
public class PresentationUiReportRunContext extends UiReportRunContext {

    protected final String presentationId;

    /**
     * Creates a copy of {@code sourceContext} tagged with the given {@code presentationId}.
     */
    public PresentationUiReportRunContext(UiReportRunContext sourceContext, String presentationId) {
        setReportRunContext(sourceContext.getReportRunContext());
        if (sourceContext.getOwner() != null) {
            setOwner(sourceContext.getOwner());
        }
        if (sourceContext.getOutputNamePattern() != null) {
            setOutputNamePattern(sourceContext.getOutputNamePattern());
        }
        setInBackground(sourceContext.getInBackground());
        setParametersDialogShowMode(sourceContext.getParametersDialogShowMode());
        this.presentationId = Objects.requireNonNull(presentationId);
    }

    /**
     * @return the identifier of the target {@link ReportExecutionPresentation}
     */
    public String getPresentationId() {
        return presentationId;
    }
}
