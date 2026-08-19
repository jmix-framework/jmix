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
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;

import java.util.List;

/**
 * Answers with a configurable verdict and records what it was asked about, so the seam can be tested without
 * the add-on's validators and without a data model to validate against.
 */
public class TestJpqlValidationService extends JpqlValidationService {

    protected List<String> issueMessages = List.of();

    protected GeneratedJpqlResult lastValidated = new GeneratedJpqlResult("", List.of(), "", List.of());

    protected int validations;

    @Override
    public JpqlValidationResult validate(GeneratedJpqlResult generatedJpqlResult) {
        lastValidated = generatedJpqlResult;
        validations++;

        if (issueMessages.isEmpty()) {
            return new JpqlValidationResult(true, List.of());
        }

        return new JpqlValidationResult(false, issueMessages.stream()
                .map(message -> new JpqlValidationIssue("test.issue", message))
                .toList());
    }

    /**
     * Stands in for the add-on rejecting a query, whatever the query says.
     */
    public void setIssueMessages(List<String> issueMessages) {
        this.issueMessages = issueMessages;
    }

    /**
     * @return how often a query was checked, which tells a check that happened from one that was avoided
     */
    public int getValidations() {
        return validations;
    }

    public GeneratedJpqlResult getLastValidated() {
        return lastValidated;
    }
}
