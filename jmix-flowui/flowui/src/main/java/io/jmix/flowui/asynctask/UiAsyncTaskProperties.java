/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.asynctask;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for UI asynchronous tasks.
 */
@ConfigurationProperties(prefix = "jmix.ui.async-task")
public class UiAsyncTaskProperties {

    /**
     * Configuration for the executor service used for UI asynchronous tasks.
     */
    ExecutorServiceConfig executorService;

    /**
     * Default timeout in seconds for produced completable futures. Default value is 300 seconds (5 minutes).
     */
    int defaultTimeoutSec;

    public UiAsyncTaskProperties(
            @DefaultValue ExecutorServiceConfig executorService,
            @DefaultValue("300") int defaultTimeoutSec) {
        this.executorService = executorService;
        this.defaultTimeoutSec = defaultTimeoutSec;
    }

    public ExecutorServiceConfig getExecutorService() {
        return executorService;
    }

    public int getDefaultTimeoutSec() {
        return defaultTimeoutSec;
    }

    public static class ExecutorServiceConfig {

        /**
         * Maximum pool size for the executor service. Default value is 10.
         */
        int maximumPoolSize;

        public ExecutorServiceConfig(
                @DefaultValue("10") int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }
    }
}
