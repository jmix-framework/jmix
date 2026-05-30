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

package io.jmix.aitools.dataload;

import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Outcome of {@link AiDataLoadService#loadData(String)}: the original user request together
 * with the LLM-generated query, its validation result and the fetched rows.
 * <p>
 * Always returned - inspect {@link #isExecuted()} and {@link #getExecutionError()} to tell
 * a successful load from a generation/validation/execution failure.
 */
public class EntityDataLoadResult {

    protected String userText;

    protected EntityDataLoadQuery queryDraft;
    protected JpqlValidationResult validationResult;
    protected List<Map<String, Object>> rows;

    protected boolean hasMore;
    protected boolean executed;

    protected String executionError;

    public EntityDataLoadResult(String userText,
                                EntityDataLoadQuery queryDraft,
                                JpqlValidationResult validationResult,
                                List<Map<String, Object>> rows,
                                boolean hasMore,
                                boolean executed,
                                @Nullable String executionError) {
        this.userText = userText;
        this.queryDraft = queryDraft;
        this.validationResult = validationResult;
        this.rows = rows;
        this.hasMore = hasMore;
        this.executed = executed;
        this.executionError = executionError;
    }

    /**
     * Returns the LLM-generated query draft that was attempted: JPQL, parameters,
     * result-property names, explanation and warnings.
     *
     * @return the LLM-generated query draft
     */
    public EntityDataLoadQuery getQuery() {
        return queryDraft;
    }

    /**
     * Returns the original natural-language request the query was generated from.
     *
     * @return the original natural-language request
     */
    public String getUserText() {
        return userText;
    }

    /**
     * Returns the validation outcome for {@link #getQuery()}: parse/semantic issues found
     * before execution. When {@link JpqlValidationResult#isValid()} is {@code false} the
     * query is not executed.
     *
     * @return the validation outcome
     */
    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Returns whether more rows are available in the database beyond the returned page.
     * The caller can re-issue the request with an increased
     * {@link EntityDataLoadQuery#getFirstResult() firstResult}.
     *
     * @return {@code true} if more rows are available, {@code false} otherwise
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Returns the fetched rows; each map is keyed by the names from
     * {@link EntityDataLoadQuery#getResultProperties()}. Empty when the query was not
     * executed or produced no rows.
     *
     * @return the fetched rows
     */
    public List<Map<String, Object>> getRows() {
        return rows;
    }

    /**
     * Returns whether the query was actually executed against the database. {@code false}
     * means generation failed, validation rejected the query, or execution threw — see
     * {@link #getExecutionError()}.
     *
     * @return {@code true} if the query was executed, {@code false} otherwise
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * Returns the execution error message, or {@code null} if the query was executed
     * successfully or never attempted.
     *
     * @return the execution error message, or {@code null}
     */
    @Nullable
    public String getExecutionError() {
        return executionError;
    }
}
