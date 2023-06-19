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

package io.jmix.flowui.backgroundtask;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "jmix.ui.background-task")
public class UiBackgroundTaskProperties {

    /**
     * Number of background task threads.
     */
    int threadsCount;

    /**
     * Tasks that do not update their status are killed after the timeout (task's timeout plus latency timout).
     * If a duration suffix is not specified, seconds will be used.
     */
    Duration taskKillingLatency;

    /**
     * Interval for checking timeout of the {@link BackgroundTask}. If a duration suffix is not specified,
     * milliseconds will be used.
     */
    Duration timeoutExpirationCheckInterval;

    public UiBackgroundTaskProperties(
            @DefaultValue("10") int threadsCount,
            @DefaultValue("60") @DurationUnit(ChronoUnit.SECONDS) Duration taskKillingLatency,
            @DefaultValue("5000") Duration timeoutExpirationCheckInterval
    ) {
        this.threadsCount = threadsCount;
        this.taskKillingLatency = taskKillingLatency;
        this.timeoutExpirationCheckInterval = timeoutExpirationCheckInterval;
    }

    /**
     * @see #threadsCount
     */
    public int getThreadsCount() {
        return threadsCount;
    }

    /**
     * @see #taskKillingLatency
     */
    public Duration getTaskKillingLatency() {
        return taskKillingLatency;
    }

    /**
     * @see #timeoutExpirationCheckInterval
     */
    public Duration getTimeoutExpirationCheckInterval() {
        return timeoutExpirationCheckInterval;
    }
}
