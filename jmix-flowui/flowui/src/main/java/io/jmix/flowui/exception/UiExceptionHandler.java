/*
 * Copyright 2019 Haulmont.
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
package io.jmix.flowui.exception;

/**
 * Interface to be implemented by exception handlers
 */
public interface UiExceptionHandler {
    /**
     * Handle an exception. Implementation class should either handle the exception and return {@code true}, or return
     * {@code false} to delegate execution to the next handler in the chain of responsibility.
     *
     * @param exception throwable instance
     * @return {@code true} if the exception has been successfully handled, {@code false} if not
     */
    boolean handle(Throwable exception);
}
