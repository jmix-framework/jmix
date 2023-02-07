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

package io.jmix.flowui.backgroundtask;

import javax.annotation.Nullable;

/**
 * Task handler for {@link BackgroundTask}.
 *
 * @param <V> type of task's result
 */
public interface BackgroundTaskHandler<V> {

    /**
     * Executes the {@link BackgroundTask}.
     * <br>
     * This method must be called only once for a handler instance.
     */
    @ExecutedOnUIThread
    void execute();

    /**
     * Cancels task.
     *
     * @return {@code true} if canceled, {@code false} if the task was not started or is already stopped
     */
    @ExecutedOnUIThread
    boolean cancel();

    /**
     * Waits for the task completion and return its result.
     *
     * @return task's result returned from {@link BackgroundTask#run(TaskLifeCycle)} method
     */
    @ExecutedOnUIThread
    @Nullable
    V getResult();

    /**
     * @return {@code true} if the task is completed
     */
    boolean isDone();

    /**
     * @return {@code true} if the task has been canceled
     */
    boolean isCancelled();

    /**
     * @return {@code true} if the task is running
     */
    boolean isAlive();
}