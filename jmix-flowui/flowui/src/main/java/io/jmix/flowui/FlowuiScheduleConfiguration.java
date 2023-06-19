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

package io.jmix.flowui;

import io.jmix.core.TimeSource;
import io.jmix.flowui.backgroundtask.UiBackgroundTaskProperties;
import io.jmix.flowui.backgroundtask.BackgroundTaskWatchDog;
import io.jmix.flowui.backgroundtask.impl.BackgroundTaskWatchDogImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
public class FlowuiScheduleConfiguration {

    @Bean("flowui_ThreadPoolTaskScheduler")
    public TaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("flowui_backgroundScheduler-");
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setDaemon(true);
        return threadPoolTaskScheduler;
    }

    @Bean("flowui_BackgroundWorkerWatchDog")
    public BackgroundTaskWatchDog watchDog(UiBackgroundTaskProperties properties, TimeSource timeSource) {
        return new BackgroundTaskWatchDogImpl(properties, timeSource);
    }
}
