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

import java.util.Map;

/**
 * Lifecycle object that is passed to {@link BackgroundTask#run(TaskLifeCycle)} method to allow working thread to
 * interact with the execution environment.
 *
 * @param <T> task progress measurement unit
 */
public interface TaskLifeCycle<T> {
    /**
     * Publishes changes to show progress.
     *
     * @param changes Changes
     * @throws InterruptedException if task was interrupted by calling {@link BackgroundTaskHandler#cancel()}
     */
    @SuppressWarnings({"unchecked"})
    void publish(T... changes) throws InterruptedException;

    /**
     * @return {@code true} if the working thread has been interrupted
     */
    boolean isInterrupted();

    /**
     * @return {@code true} if a task was interrupted by calling the "cancel" method
     */
    boolean isCancelled();

    /**
     * @return execution parameters that was set by {@link BackgroundTask#getParams()}
     */
    Map<String, Object> getParams();
}