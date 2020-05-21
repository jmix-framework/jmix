/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.executor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.ui.background-task")
@ConstructorBinding
public class UiBackgroundTaskProperties {

    int minThreadsCount;
    int maxActiveTasksCount;
    int timeoutSeconds;

    public UiBackgroundTaskProperties(
            @DefaultValue("4") int minThreadsCount,
            @DefaultValue("100") int maxActiveTasksCount,
            @DefaultValue("60") int timeoutSeconds
    ) {
        this.minThreadsCount = minThreadsCount;
        this.maxActiveTasksCount = maxActiveTasksCount;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Minimum number of background task threads.
     */
    public int getMinThreadsCount() {
        return minThreadsCount;
    }

    /**
     * Maximum number of active background tasks.
     */
    public int getMaxActiveTasksCount() {
        return maxActiveTasksCount;
    }

    /**
     * Tasks that do not update their status are killed after the timeout.
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
