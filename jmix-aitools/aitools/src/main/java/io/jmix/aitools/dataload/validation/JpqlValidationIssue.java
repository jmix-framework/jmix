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

public class JpqlValidationIssue {

    protected String code;
    protected String message;
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

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

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
