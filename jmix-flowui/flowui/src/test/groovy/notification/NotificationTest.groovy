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

package notification

import com.vaadin.flow.component.html.Span
import io.jmix.flowui.Notifications
import io.jmix.flowui.testassist.UiTestUtils
import notification.view.NotificationsTestView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class NotificationTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("notification.view")
    }

    def "Open notifications with #typeName type"() {
        given: "Opened NotificationsTestView"
        def view = navigateToView NotificationsTestView

        when: "Show the notification with #typeName type"
        view."show${typeName}Notification"()

        then: "Notification with #typeName type will be shown"
        verifyAll(UiTestUtils.lastOpenedNotification) {
            text == "${typeName} notification"
            type == Notifications.Type.valueOf("${typeName}".toUpperCase())
        }

        where:
        typeName << ["Default", "Success", "Error", "Warning", "System"]
    }

    def "Show multiple notifications"() {
        given: "Opened NotificationsTestView"
        def view = navigateToView NotificationsTestView

        when: "Show three closeable notifications"
        view.showErrorNotification()
        view.showWarningNotification()
        view.showSystemNotification()

        then: "Three notifications will be shown"
        UiTestUtils.openedNotifications.size() == 3

        when: "One notification will be closed"
        UiTestUtils.openedNotifications.get(0).notification.close()

        then: "Only two notifications will be shown"
        UiTestUtils.openedNotifications.size() == 2
    }

    def "Open notification with title and message"() {
        given: "Opened NotificationsTestView"
        def view = navigateToView NotificationsTestView

        when: "Show the notification with title and message"
        view.showTitleMessageNotification()

        then: "The notification with title and message will be shown"
        def notification = UiTestUtils.lastOpenedNotification

        notification.notification.opened
        notification.title == "Title"
        notification.message == "Message"
    }

    def "Open notification with custom component"() {
        given: "Opened NotificationsTestView"
        def view = navigateToView NotificationsTestView

        when: "Show the notification with custom component"
        view.showComponentNotification()

        then: "The notification with custom component will be shown"
        def notification = UiTestUtils.lastOpenedNotification

        notification.notification.opened
        notification.component instanceof Span
        (notification.component as Span).text == "Component notification"
    }
}
