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

package io.jmix.ui.executor.impl;

import io.jmix.ui.executor.UiBackgroundTaskProperties;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.TimeUnit;

public class WebTasksWatchDog extends TasksWatchDog {

    @Autowired
    protected UiBackgroundTaskProperties properties;

    @Override
    protected ExecutionStatus getExecutionStatus(long actualTimeMs, TaskHandlerImpl taskHandler) {
        long timeout = taskHandler.getTimeoutMs();
        if (timeout > 0 && (actualTimeMs - taskHandler.getStartTimeStamp()) > timeout) {
            return ExecutionStatus.TIMEOUT_EXCEEDED;
        }

        // kill tasks, which do not update status for latency milliseconds
        long latencyMs = TimeUnit.SECONDS.toMillis(properties.getTimeoutSeconds());
        if (timeout > 0 && (actualTimeMs - taskHandler.getStartTimeStamp()) > timeout + latencyMs) {
            return ExecutionStatus.SHOULD_BE_KILLED;
        }

        return ExecutionStatus.NORMAL;
    }
}