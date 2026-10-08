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
import io.jmix.aitools.dataload.execution.JpqlValidationAndRepairService;
import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Stands in for the add-on's validate-and-repair step, recording the request it was given and answering with
 * a preconfigured outcome: the query left as it is, a repaired one, or a failure of the repair itself.
 */
public class TestJpqlValidationAndRepairService extends JpqlValidationAndRepairService {

    protected final List<JpqlExecutionRequest> requests = new ArrayList<>();

    @Nullable
    protected GeneratedJpqlResult repairedResult;

    @Nullable
    protected RuntimeException failure;

    protected List<String> remainingIssues = List.of();

    @Override
    public OperationResult validateAndRepair(JpqlExecutionRequest request) {
        requests.add(request);
        if (failure != null) {
            throw failure;
        }

        JpqlValidationResult validation = new JpqlValidationResult(remainingIssues.isEmpty(),
                remainingIssues.stream()
                        .map(issue -> new JpqlValidationIssue(issue, issue))
                        .toList());

        if (repairedResult == null) {
            GeneratedJpqlResult asGenerated =
                    new GeneratedJpqlResult(request.getJpql(), List.of(), "", List.of());
            return remainingIssues.isEmpty()
                    ? OperationResult.success(request, asGenerated, validation, null)
                    : OperationResult.failed(request, asGenerated, validation, null);
        }

        JpqlRepairResult repair = new JpqlRepairResult(repairedResult, validation, 1, true);
        return remainingIssues.isEmpty()
                ? OperationResult.success(request, repairedResult, validation, repair)
                : OperationResult.failed(request, repairedResult, validation, repair);
    }

    /**
     * Makes the next repair answer with this query instead of the one it was given.
     */
    public void setRepairedResult(@Nullable GeneratedJpqlResult repairedResult) {
        this.repairedResult = repairedResult;
    }

    /**
     * Makes the next repair fail, as it does when the model is unreachable.
     */
    public void setFailure(@Nullable RuntimeException failure) {
        this.failure = failure;
    }

    /**
     * Makes the outcome report these problems, as it does when a repair did not help.
     */
    public void setRemainingIssues(List<String> remainingIssues) {
        this.remainingIssues = remainingIssues;
    }

    public JpqlExecutionRequest getLastRequest() {
        if (requests.isEmpty()) {
            throw new IllegalStateException("Validate and repair was not called");
        }
        return requests.get(requests.size() - 1);
    }
}
