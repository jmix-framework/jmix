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

package io.jmix.security.role;

/**
 * Thrown when two roles of the same type (resource or row-level) share the same {@code code}.
 * A role's code must be unique within its type across all sources (annotated design-time roles
 * and database runtime roles), otherwise role lookup by code becomes ambiguous.
 */
public class DuplicateRoleCodeException extends RuntimeException {

    private final String code;

    public DuplicateRoleCodeException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
