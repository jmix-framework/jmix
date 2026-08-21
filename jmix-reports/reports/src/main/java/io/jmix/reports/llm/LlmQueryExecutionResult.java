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

package io.jmix.reports.llm;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * What executing a generated query produced: the rows and whether the add-on's own row cap cut them
 * short.
 *
 * @see LlmDataQueryService#execute(LlmQueryExecutionRequest)
 */
public class LlmQueryExecutionResult {

    protected List<Map<String, @Nullable Object>> rows;
    protected boolean truncated;

    public LlmQueryExecutionResult(List<Map<String, @Nullable Object>> rows, boolean truncated) {
        checkNotNullArgument(rows, "rows is null");

        this.rows = List.copyOf(rows);
        this.truncated = truncated;
    }

    /**
     * Returns the fetched rows, keyed by {@link LlmDataQuery#getResultProperties()}. A value is {@code null}
     * when the query returned no value for that column, which an empty string does not stand for.
     *
     * @return the fetched rows
     */
    public List<Map<String, @Nullable Object>> getRows() {
        return rows;
    }

    /**
     * Tells whether the query had more rows to give and the add-on's own cap stopped it. A data set states no
     * row limit of its own, so this reports what the add-on's properties did.
     *
     * @return {@code true} if rows were left behind
     */
    public boolean isTruncated() {
        return truncated;
    }
}
