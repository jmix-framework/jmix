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

package io.jmix.flowui.event;

import io.jmix.flowui.backgroundtask.BackgroundTask;

/**
 * Event is published if the {@link BackgroundTask#done(Object)} throws an exception and executor
 * cannot handle it.
 */
public class BackgroundTaskUnhandledExceptionEvent extends AbstractBackgroundTaskEvent {

    private Exception exception;

    public BackgroundTaskUnhandledExceptionEvent(Object source, BackgroundTask task, Exception exception) {
        super(source, task);
        this.exception = exception;
    }

    /**
     * Returns the exception that occurred during the execution of the background task
     * if the task's {@code done(Object)} method fails and the executor is unable to handle it.
     *
     * @return the exception that caused the failure
     */
    public Exception getException() {
        return exception;
    }
}
