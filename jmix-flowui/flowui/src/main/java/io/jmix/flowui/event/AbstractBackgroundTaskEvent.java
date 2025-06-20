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
import org.springframework.context.ApplicationEvent;

/**
 * Base class for events that contain information about {@link BackgroundTask}.
 */
public abstract class AbstractBackgroundTaskEvent extends ApplicationEvent {

    private BackgroundTask<?, ?> task;
    private boolean stopPropagation;

    public AbstractBackgroundTaskEvent(Object source, BackgroundTask<?, ?> task) {
        super(source);
        this.task = task;
    }

    /**
     * Returns the background task associated with this event.
     *
     * @return the {@link BackgroundTask} instance associated with the event
     */
    public BackgroundTask<?, ?> getTask() {
        return task;
    }

    /**
     * Determines whether the propagation of the event should be stopped.
     *
     * @return {@code true} if the propagation of the event should be stopped,
     *         {@code false} otherwise
     */
    public boolean isStopPropagation() {
        return stopPropagation;
    }

    /**
     * Sets whether the propagation of this event should be stopped.
     *
     * @param stopPropagation a boolean value indicating whether to stop the propagation
     *                         of the event. {@code true} to stop propagation, {@code false} otherwise.
     */
    public void setStopPropagation(boolean stopPropagation) {
        this.stopPropagation = stopPropagation;
    }
}
