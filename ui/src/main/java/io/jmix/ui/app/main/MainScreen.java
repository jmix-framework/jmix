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

package io.jmix.ui.app.main;

import io.jmix.ui.component.Button;
import io.jmix.ui.Notifications;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;

/**
 * Base class for a controller of application Main screen.
 */
@UiDescriptor("main-screen.xml")
@UiController("main")
public class MainScreen extends Screen {
    @Inject
    protected Notifications notifications;

    @Subscribe
    protected void afterShow(AfterShowEvent event) {
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption("Welcome to JMIX!")
                .show();
    }

    @Subscribe("hiBtn")
    protected void hiBtnClick(Button.ClickEvent event) {
        notifications.create(Notifications.NotificationType.SYSTEM)
                .withCaption("Still works...")
                .show();
    }
}