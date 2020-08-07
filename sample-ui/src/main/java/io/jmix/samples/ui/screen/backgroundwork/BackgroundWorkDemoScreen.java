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

package io.jmix.samples.ui.screen.backgroundwork;

import io.jmix.ui.Dialogs;
import io.jmix.ui.component.Button;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("backgroundWorkDemo")
@UiDescriptor("background-work-demo.xml")
public class BackgroundWorkDemoScreen extends Screen {

    @Autowired
    private Dialogs dialogs;

    @Subscribe("runBT")
    public void onRunBTClick(Button.ClickEvent event) {
        BackgroundTask<Integer, Void> backgroundTask = createBackgroundTask(10, 1, 3000);

        dialogs.createBackgroundWorkDialog(this, backgroundTask)
                .show();
    }

    @Subscribe("runBT2")
    public void onRunBT2Click(Button.ClickEvent event) {
        int total = 5;
        BackgroundTask<Integer, Void> backgroundTask = createBackgroundTask(10, total, 1000);

        dialogs.createBackgroundWorkDialog(this, backgroundTask)
                .withCancelAllowed(true)
                .withMessage("My Message")
                .withTotal(total)
                .show();
    }


    @Subscribe("runBT3")
    public void onRunBT3Click(Button.ClickEvent event) {
        int total = 5;
        BackgroundTask<Integer, Void> backgroundTask = createBackgroundTask(10, total, 1000);

        dialogs.createBackgroundWorkDialog(this, backgroundTask)
                .withCaption("Task")
                .withMessage("My Task is Running")
                .withTotal(total)
                .withShowProgressInPercentage(true)
                .withCancelAllowed(true)
                .show();
    }

    private BackgroundTask<Integer, Void> createBackgroundTask(int timeoutSeconds, int total, int updateIntervalMillis) {
        return new BackgroundTask<Integer, Void>(timeoutSeconds, this) {
            @Override
            public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
                taskLifeCycle.publish(0);
                for (int i = 0; i < total; i++) {
                    Thread.sleep(updateIntervalMillis);
                    taskLifeCycle.publish(i+1);
                }
                return null;
            }
        };
    }

}
