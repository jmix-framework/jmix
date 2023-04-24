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
 * Exception that is thrown in case of incorrect access to a shared data from a thread that does not own necessary lock.
 */
public class IllegalConcurrentAccessException extends RuntimeException {
    public IllegalConcurrentAccessException() {
        super("UI Shared state was accessed from a background thread");
    }

    public IllegalConcurrentAccessException(String message) {
        super(message);
    }
}