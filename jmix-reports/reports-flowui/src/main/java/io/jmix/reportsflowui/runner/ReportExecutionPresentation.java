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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Strategy interface that encapsulates how a particular output channel (download, spreadsheet viewer,
 * table viewer, …) selects templates, output types, and decides whether user input is required
 * before a report can be run.
 * <p>
 * Implementations are registered as Spring beans and collected by {@link ReportPresentationRegistry}.
 * Use {@link ReportExecutionPresentationIds} for the well-known built-in identifiers.
 */
@Internal
@NullMarked
public interface ReportExecutionPresentation {

    /**
     * @return unique identifier of this presentation (see {@link ReportExecutionPresentationIds})
     */
    String getId();

    /**
     * @return {@code true} if the report has at least one template and output type compatible with this presentation
     */
    boolean supportsReport(@Nullable Report report);

    /**
     * @return templates of the report that are compatible with this presentation
     */
    List<ReportTemplate> getAvailableTemplates(@Nullable Report report);

    /**
     * @return the template that should be used by default for this presentation,
     *         preferring {@code selectedTemplate} if it is compatible
     */
    @Nullable
    ReportTemplate resolveDefaultTemplate(@Nullable Report report, @Nullable ReportTemplate selectedTemplate);

    /**
     * @return output types for the template that are compatible with this presentation
     */
    List<ReportOutputType> getAvailableOutputTypes(@Nullable Report report, @Nullable ReportTemplate template);

    /**
     * Resolves the output type to run the report with.
     * <p>
     * An explicitly requested {@code selectedOutputType}, e.g. the one set through the report runner API, must not
     * be silently replaced: an implementation may return another type only if the requested one cannot be produced,
     * either because this presentation is unable to render it or because the template cannot be converted to it.
     * Restrictions on what the user is allowed to choose belong to
     * {@link #getAvailableOutputTypes(Report, ReportTemplate)} instead.
     *
     * @return the output type to run the report with, or {@code null} if this presentation cannot handle the template
     */
    @Nullable
    ReportOutputType resolveDefaultOutputType(@Nullable Report report, @Nullable ReportTemplate template,
                                              @Nullable ReportOutputType selectedOutputType);

    /**
     * A selection passed in the arguments does not automatically remove the need to ask: the built-in
     * presentations still let the user choose between several compatible templates, but do not ask for an output
     * type that has already been chosen.
     *
     * @return {@code true} if the user must choose a template or an output type before the report can be run
     *         with this presentation
     */
    boolean requiresUserChoice(@Nullable Report report, @Nullable ReportTemplate selectedTemplate,
                               @Nullable ReportOutputType selectedOutputType);
}
