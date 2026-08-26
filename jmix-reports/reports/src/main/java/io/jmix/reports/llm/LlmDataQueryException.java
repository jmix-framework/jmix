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

import java.io.Serial;

/**
 * Thrown when a query cannot be generated, read back from its stored form, or executed.
 */
public class LlmDataQueryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4192843871512268137L;

    public LlmDataQueryException(String message) {
        super(message);
    }

    public LlmDataQueryException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
