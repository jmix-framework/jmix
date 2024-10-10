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

package notification.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "notifications-test-view")
@ViewController
public class NotificationsTestView extends StandardView {

    @Autowired
    protected Notifications notifications;

    public void showDefaultNotification() {
        notifications.show("Default notification");
    }

    public void showSuccessNotification() {
        notifications.create("Success notification")
                .withType(Notifications.Type.SUCCESS)
                .show();
    }

    public void showErrorNotification() {
        // closeable by type
        notifications.create("Error notification")
                .withType(Notifications.Type.ERROR)
                .show();
    }

    public void showWarningNotification() {
        // closeable by type
        notifications.create("Warning notification")
                .withType(Notifications.Type.WARNING)
                .show();
    }

    public void showSystemNotification() {
        // closeable by type
        notifications.create("System notification")
                .withType(Notifications.Type.SYSTEM)
                .show();
    }

    public void showTitleMessageNotification() {
        notifications.create("Title", "Message")
                .show();
    }

    public void showComponentNotification() {
        notifications.create(new Span("Component notification"))
                .show();
    }
}
