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
import io.jmix.reports.yarg.reporting.ReportOutputDocument;

/**
 * Strategy interface for handling a completed report execution.
 * <p>
 * Implementations are collected by the runner and called in {@link org.springframework.core.annotation.Order @Order}
 * sequence until one returns {@code true}. Implementations should be annotated with
 * {@link org.springframework.stereotype.Component @Component} and {@code @Order}.
 */
@Internal
public interface ReportResultHandler {

    /**
     * Handles the report output document produced by the runner.
     *
     * @param document the generated report document
     * @param context  the run context that produced the document
     * @return {@code true} if the document was handled and further handlers should not be invoked,
     *         {@code false} to pass to the next handler
     */
    boolean handle(ReportOutputDocument document, UiReportRunContext context);
}
