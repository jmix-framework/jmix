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

    public EntityDataLoadQuery getQuery() {
        return queryDraft;
    }

    public String getUserText() {
        return userText;
    }

    public JpqlValidationResult getValidationResult() {
        return validationResult;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public boolean isExecuted() {
        return executed;
    }

    @Nullable
    public String getExecutionError() {
        return executionError;
    }
}
