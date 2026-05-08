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

package io.jmix.texttodata.validation;

import java.util.List;

public class JpqlValidationResult {

    protected boolean valid;
    protected List<JpqlValidationIssue> issues;

    public JpqlValidationResult(boolean valid, List<JpqlValidationIssue> issues) {
        this.valid = valid;
        this.issues = issues;
    }

    public boolean isValid() {
        return valid;
    }

    public List<JpqlValidationIssue> getIssues() {
        return issues;
    }

    @Override
    public String toString() {
        return "JpqlValidationResult{" +
                "valid=" + valid +
                ", issues=" + issues +
                '}';
    }
}
