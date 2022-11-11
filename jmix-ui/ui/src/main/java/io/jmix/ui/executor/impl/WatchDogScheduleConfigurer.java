/*
 * Copyright 2022 Haulmont.
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

import io.jmix.ui.UiProperties;
import io.jmix.ui.executor.WatchDog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component("ui_WatchDogScheduleConfigurer")
public class WatchDogScheduleConfigurer {

    private TaskScheduler taskScheduler;

    private WatchDog watchDog;

    private UiProperties uiProperties;

    public WatchDogScheduleConfigurer(@Qualifier("ui_ThreadPoolTaskScheduler") TaskScheduler taskScheduler,
                                      WatchDog watchDog, UiProperties uiProperties) {
        this.taskScheduler = taskScheduler;
        this.watchDog = watchDog;
        this.uiProperties = uiProperties;
    }

    @EventListener
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        taskScheduler.scheduleWithFixedDelay(() -> watchDog.cleanupTasks(),
                uiProperties.getBackgroundTaskTimeoutCheckInterval());
    }

}
