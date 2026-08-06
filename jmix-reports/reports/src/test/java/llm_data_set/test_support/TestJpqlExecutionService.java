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

package llm_data_set.test_support;

import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionRequest;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import io.jmix.aitools.dataload.execution.JpqlExecutionService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Records the request the add-on would execute and returns a configurable outcome, so no database and no
 * validation pipeline are involved.
 */
public class TestJpqlExecutionService extends JpqlExecutionService {

    protected JpqlExecutionRequest lastRequest = new JpqlExecutionRequest();

    protected List<Map<String, Object>> rows = List.of();
    protected boolean executed = true;
    protected boolean valid = true;

    @Nullable
    protected String executionError;

    @Nullable
    protected String issueMessage;

    @Override
    public JpqlExecutionResult execute(JpqlExecutionRequest request) {
        lastRequest = request;

        GeneratedJpqlResult generatedResult = new GeneratedJpqlResult(request.getJpql(), List.of(), "", List.of(),
                request.getMaxResults(), request.getFirstResult());
        List<JpqlValidationIssue> issues = issueMessage == null
                ? List.of()
                : List.of(new JpqlValidationIssue("test.issue", issueMessage));

        return new JpqlExecutionResult(generatedResult, new JpqlValidationResult(valid, issues), rows,
                request.getMaxResults(), request.getFirstResult(), false, false, executed, executionError);
    }

    public JpqlExecutionRequest getLastRequest() {
        return lastRequest;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setExecutionError(String executionError) {
        this.executionError = executionError;
    }

    public void setIssueMessage(String issueMessage) {
        this.issueMessage = issueMessage;
    }
}
