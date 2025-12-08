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

import com.vaadin.flow.component.Component;

/**
 * Exception to be thrown by field validators.
 */
public class ValidationException extends RuntimeException {

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the detailed message associated with this exception instance.
     *
     * @return the localized message of the exception
     */
    public String getDetailsMessage() {
        return getLocalizedMessage();
    }

    /**
     * Represents a contract for exceptions or objects that are associated
     * with a specific UI {@link Component}.
     */
    public interface HasRelatedComponent {

        /**
         * Returns associated component.
         *
         * @return the component associated with this instance
         */
        Component getComponent();
    }
}