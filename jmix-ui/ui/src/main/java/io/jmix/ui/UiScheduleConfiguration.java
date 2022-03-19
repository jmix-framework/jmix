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

package io.jmix.ui;

import io.jmix.ui.executor.WatchDog;
import io.jmix.ui.executor.impl.WebTasksWatchDog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;

@Configuration
public class UiScheduleConfiguration {

    @Autowired
    protected UiProperties uiProperties;

    @Bean("ui_ThreadPoolTaskScheduler")
    public TaskScheduler threadPoolTaskScheduler() {
        CustomizableThreadFactory threadFactory =
                new CustomizableThreadFactory("ui_backgroundScheduler");
        threadFactory.setDaemon(true);

        ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler(
                Executors.newSingleThreadScheduledExecutor(threadFactory));

        configureTasks(taskScheduler);

        return taskScheduler;
    }

    protected void configureTasks(TaskScheduler scheduler) {
        scheduler.scheduleWithFixedDelay(() -> watchDog().cleanupTasks(),
                uiProperties.getBackgroundTaskTimeoutCheckInterval());
    }

    @Bean("ui_BackgroundWorker_WatchDog")
    public WatchDog watchDog() {
        return new WebTasksWatchDog();
    }
}
