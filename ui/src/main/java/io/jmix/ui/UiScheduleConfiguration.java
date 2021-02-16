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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@Configuration
public class UiScheduleConfiguration implements SchedulingConfigurer {

    @Autowired
    protected UiProperties uiProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(threadPoolTaskScheduler());
        taskRegistrar.addFixedDelayTask(new IntervalTask(() -> watchDog().cleanupTasks(),
                uiProperties.getBackgroundTaskTimeoutCheckInterval()));
    }

    @Bean("ui_ThreadPoolTaskScheduler")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setDaemon(true);
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("ui_backgroundScheduler");
        return taskScheduler;
    }

    @Bean("ui_BackgroundWorker_WatchDog")
    public WatchDog watchDog() {
        return new WebTasksWatchDog();
    }
}
