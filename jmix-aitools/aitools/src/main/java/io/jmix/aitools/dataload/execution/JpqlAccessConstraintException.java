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

package io.jmix.aitools.dataload.execution;

import io.jmix.core.security.AccessDeniedException;

/**
 * Signals that the current user's access constraints cannot be applied to a query, so the query is refused rather
 * than executed unfiltered. This happens when the query selects an entity whole, passes a parameter the application
 * supplies itself, or reads an entity whose row-level conditions cannot be applied where it is used. Unlike
 * {@link AccessDeniedException}, the user may read the data, but the query has to be written differently. The
 * message says how.
 */
public class JpqlAccessConstraintException extends RuntimeException {

    /**
     * Creates the exception.
     *
     * @param message what cannot be narrowed and how to rewrite the query; it is shown to the model
     */
    public JpqlAccessConstraintException(String message) {
        super(message);
    }
}
