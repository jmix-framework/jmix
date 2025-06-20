/*
 * Copyright 2022 Haulmont.
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
 * An interface defines the contract for providing dialogs to handle and display exceptions in the user interface.
 */
public interface ExceptionDialogProvider {

    /**
     * Determines whether the current provider supports handling the specified exception.
     *
     * @param throwable the exception that needs to be checked for support
     * @return {@code true} if the provider supports handling the given exception, {@code false} otherwise
     */
    boolean supports(Throwable throwable);

    /**
     * Provides an {@link ExceptionDialog} for handling and displaying a specific
     * exception in the user interface.
     *
     * @param throwable the exception to be handled and displayed
     * @return an {@link ExceptionDialog} instance for the given exception
     */
    ExceptionDialog getExceptionDialogOpener(Throwable throwable);
}
