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

import io.jmix.flowui.backgroundtask.impl.TaskHandlerImpl;

@SuppressWarnings("unused")
public interface BackgroundTaskWatchDog {

    /**
     * Adds task under {@link BackgroundTaskWatchDog} control.
     *
     * @param taskHandler task handler
     */
    void manageTask(TaskHandlerImpl taskHandler);

    /**
     * Task completed, remove it from watches.
     *
     * @param taskHandler task handler
     */
    void removeTask(TaskHandlerImpl taskHandler);

    /**
     * Removes finished, canceled or hangup tasks.
     */
    void cleanupTasks();

    /**
     * Stops execution of all background tasks.
     */
    void stopTasks();

    /**
     * @return active tasks count
     */
    int getActiveTasksCount();
}