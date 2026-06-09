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

package io.jmix.aitools.dataload.validation;

import org.jspecify.annotations.Nullable;

/**
 * A single problem found while validating a generated JPQL draft.
 */
public class JpqlValidationIssue {

    protected String code;
    protected String message;
    @Nullable
    protected String guidance;

    public JpqlValidationIssue(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public JpqlValidationIssue(String code, String message, @Nullable String guidance) {
        this.code = code;
        this.message = message;
        this.guidance = guidance;
    }

    /**
     * Returns the stable issue code, for example {@code "jpql.blank"}.
     *
     * @return issue code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the human-readable description of the problem.
     *
     * @return issue message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns a hint on how to fix the issue, fed back to the model during repair.
     *
     * @return repair guidance, or {@code null} if none
     */
    @Nullable
    public String getGuidance() {
        return guidance;
    }

    @Override
    public String toString() {
        return "JpqlValidationIssue{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", guidance='" + guidance + '\'' +
                '}';
    }
}
