/*
 * Copyright (c) 2020 Haulmont.
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

package facet.notification.screen;


import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.NotificationFacet;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController
@UiDescriptor("notification-facet-test-screen.xml")
public class NotificationFacetTestScreen extends Screen {

    @Autowired
    public NotificationFacet testNotification;
    @Autowired
    public Action notificationAction;
    @Autowired
    public Button notificationButton;

    public boolean captionProvided = false;
    public boolean descriptionProvided = false;

    public boolean closeEvtFired = false;

    @Install(subject = "captionProvider", to = "testNotification")
    public String getNotificationCaption() {
        captionProvided = true;
        return "Caption from provider";
    }

    @Install(subject = "descriptionProvider", to = "testNotification")
    public String getNotificationDescription() {
        descriptionProvided = true;
        return "Description from provider";
    }

    @Subscribe("testNotification")
    public void onNotificationClosed(Notifications.CloseEvent e) {
        closeEvtFired = true;
    }
}
