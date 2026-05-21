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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class JpqlExecutionResult {

    protected GeneratedJpqlResult generatedJpqlResult;
    protected JpqlValidationResult validationResult;

    protected List<Map<String, Object>> rows;
    protected Integer maxResults;
    protected Integer firstResult;
    protected boolean hasMore;

    protected boolean repaired;
    protected boolean executed;
    protected String executionError;

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

    public static JpqlExecutionResult failed(GeneratedJpqlResult generatedJpqlResult,
                                             JpqlValidationResult validationResult,
                                             boolean repaired) {
        return new JpqlExecutionResult(generatedJpqlResult, validationResult, List.of(),
                generatedJpqlResult.getMaxResults(), generatedJpqlResult.getFirstResult(),
                false, repaired, false, null);
    }

    public static JpqlExecutionResult failed(GeneratedJpqlResult generatedJpqlResult,
                                             JpqlValidationResult validationResult,
                                             int maxResults, boolean repaired,
                                             String executionError) {
        return new JpqlExecutionResult(generatedJpqlResult, validationResult, List.of(),
                maxResults, generatedJpqlResult.getFirstResult(), false, repaired, false, executionError);
    }

    public GeneratedJpqlResult getGeneratedJpqlResult() {
        return generatedJpqlResult;
    }

    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    @Nullable
    public Integer getFirstResult() {
        return firstResult;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public boolean isRepaired() {
        return repaired;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Nullable
    public String getExecutionError() {
        return executionError;
    }
}
