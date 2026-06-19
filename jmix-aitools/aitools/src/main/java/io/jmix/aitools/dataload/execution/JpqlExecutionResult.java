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

package io.jmix.aitools.dataload.execution;

import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Outcome of a single {@link JpqlExecutionService#execute} call.
 */
@NullMarked
public class JpqlExecutionResult {

    protected GeneratedJpqlResult generatedJpqlResult;
    protected JpqlValidationResult validationResult;

    protected List<Map<String, Object>> rows;
    protected boolean hasMore;

    protected boolean repaired;
    protected boolean executed;

    @Nullable
    protected String executionError;

    @Nullable
    protected Integer maxResults;
    @Nullable
    protected Integer firstResult;

    /**
     * @param generatedJpqlResult generated (and possibly repaired) query draft
     * @param validationResult    validation result of the processed query
     * @param rows                fetched rows, each a property-name-to-value map
     * @param maxResults          effective maximum number of rows
     * @param firstResult         applied row offset
     * @param hasMore             whether more rows are available beyond {@code maxResults}
     * @param repaired            whether the query was repaired before execution
     * @param executed            whether the query was actually executed
     * @param executionError      error message from a failed execution
     */
    public JpqlExecutionResult(GeneratedJpqlResult generatedJpqlResult,
                               JpqlValidationResult validationResult,
                               List<Map<String, Object>> rows,
                               @Nullable Integer maxResults,
                               @Nullable Integer firstResult,
                               boolean hasMore,
                               boolean repaired,
                               boolean executed,
                               @Nullable String executionError) {
        this.generatedJpqlResult = generatedJpqlResult;
        this.validationResult = validationResult;
        this.rows = rows;
        this.maxResults = maxResults;
        this.firstResult = firstResult;
        this.hasMore = hasMore;
        this.repaired = repaired;
        this.executed = executed;
        this.executionError = executionError;
    }

    /**
     * Creates a non-executed result for a query that failed validation.
     *
     * @param generatedJpqlResult generated (and possibly repaired) query draft
     * @param validationResult    validation result of the processed query
     * @param repaired            whether a repair attempt was made before validation failed
     * @return non-executed result describing the validation failure
     */
    public static JpqlExecutionResult failed(GeneratedJpqlResult generatedJpqlResult,
                                             JpqlValidationResult validationResult,
                                             boolean repaired) {
        return new JpqlExecutionResult(generatedJpqlResult, validationResult, List.of(),
                generatedJpqlResult.getMaxResults(), generatedJpqlResult.getFirstResult(),
                false, repaired, false, null);
    }

    /**
     * Creates a non-executed result for a query that passed validation but failed at execution time.
     *
     * @param generatedJpqlResult generated (and possibly repaired) query draft
     * @param validationResult    validation result of the processed query
     * @param maxResults          effective maximum number of rows that was applied
     * @param repaired            whether the query was repaired before execution
     * @param executionError      error message from the failed execution, or {@code null} if unavailable
     * @return non-executed result describing the execution failure
     */
    public static JpqlExecutionResult failed(GeneratedJpqlResult generatedJpqlResult,
                                             JpqlValidationResult validationResult,
                                             int maxResults, boolean repaired,
                                             @Nullable String executionError) {
        return new JpqlExecutionResult(generatedJpqlResult, validationResult, List.of(),
                maxResults, generatedJpqlResult.getFirstResult(), false, repaired, false, executionError);
    }

    /**
     * Returns the generated (and possibly repaired) JPQL draft that was processed.
     *
     * @return generated query draft
     */
    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    /**
     * Returns the validation result of the processed query.
     *
     * @return validation result
     */
    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Returns the fetched rows as an unmodifiable list, each row being a property-name-to-value map.
     * Empty if the query was not executed or returned nothing.
     *
     * @return fetched rows, never {@code null}
     */
    public List<Map<String, Object>> getRows() {
        return Collections.unmodifiableList(rows);
    }

    /**
     * Returns the effective maximum number of rows that was applied.
     *
     * @return maximum number of rows, or {@code null} if unknown
     */
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Returns the row offset that was applied.
     *
     * @return row offset, or {@code null} if no offset was used
     */
    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    /**
     * Returns whether more rows are available beyond {@link #getMaxResults()}.
     *
     * @return {@code true} if more rows are available
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Returns whether the query was repaired before execution.
     *
     * @return {@code true} if the query was repaired
     */
    public boolean isRepaired() {
        return repaired;
    }

    /**
     * Returns whether the query was actually executed against the data store.
     *
     * @return {@code true} if the query was executed
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * Returns the error message from a failed execution.
     *
     * @return execution error message, or {@code null} if execution succeeded or was never attempted
     */
    @Nullable
    public String getExecutionError() {
        return executionError;
    }
}
