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

package notification;

import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import notification.view.NotificationsTestView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = "notification.view")
@SpringBootTest(classes = {FlowuiTestAssistConfiguration.class, FlowuiTestConfiguration.class})
public class NotificationTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @ParameterizedTest
    @MethodSource("provideNotificationsNamesAndMethods")
    @DisplayName("Show notifications with different types")
    public void showNotificationsWithDifferentTypesTest(String typeName, Consumer<NotificationsTestView> showMethod) {
        // Navigate to NotificationsTestView
        NotificationsTestView view = navigateToNotificationsTestView();

        // Show the notification with corresponding type
        showMethod.accept(view);

        // Notification with corresponding type will be shown
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();

        assertNotNull(lastOpenedNotificationInfo);
        assertEquals("%s notification".formatted(typeName), lastOpenedNotificationInfo.getText());
        assertEquals(Notifications.Type.valueOf(typeName.toUpperCase()), lastOpenedNotificationInfo.getType());
    }

    @Test
    @DisplayName("Show multiple notifications")
    public void showMultipleDialogsTest() {
        // Navigate to NotificationsTestView
        NotificationsTestView view = navigateToNotificationsTestView();

        // Show three closeable notifications
        view.showErrorNotification();
        view.showErrorNotification();
        view.showErrorNotification();

        // Three notifications will be shown
        assertEquals(3, UiTestUtils.getOpenedNotifications().size());

        // One notification will be closed
        UiTestUtils.getOpenedNotifications().get(0).getNotification().close();

        // Only two notifications will be show
        assertEquals(2, UiTestUtils.getOpenedNotifications().size());
    }

    @Test
    @DisplayName("Show notification with title and message")
    public void showNotificationWithTitleAndMessageTest() {
        // Navigate to NotificationsTestView
        NotificationsTestView view = navigateToNotificationsTestView();

        // Show the notification with title and message
        view.showTitleMessageNotification();

        // The notification will be shown
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();

        assertNotNull(lastOpenedNotificationInfo);
        assertTrue(lastOpenedNotificationInfo.getNotification().isOpened());
        assertEquals("Title", lastOpenedNotificationInfo.getTitle());
        assertEquals("Message", lastOpenedNotificationInfo.getMessage());
    }

    @Test
    @DisplayName("Show notification with custom component")
    public void showNotificationWithCustomComponentTest() {
        // Navigate to NotificationsTestView
        NotificationsTestView view = navigateToNotificationsTestView();

        // Show the notification with custom component
        view.showComponentNotification();

        // The notification will be shown
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();

        assertNotNull(lastOpenedNotificationInfo);
        assertTrue(lastOpenedNotificationInfo.getNotification().isOpened());
        assertInstanceOf(Span.class, lastOpenedNotificationInfo.getComponent());
        assertEquals("Component notification", ((Span) lastOpenedNotificationInfo.getComponent()).getText());
    }

    protected static Stream<Arguments> provideNotificationsNamesAndMethods() {
        return Stream.of(
                Arguments.of("Default",
                        (Consumer<NotificationsTestView>) NotificationsTestView::showDefaultNotification),
                Arguments.of("Success",
                        (Consumer<NotificationsTestView>) NotificationsTestView::showSuccessNotification),
                Arguments.of("Error",
                        (Consumer<NotificationsTestView>) NotificationsTestView::showErrorNotification),
                Arguments.of("Warning",
                        (Consumer<NotificationsTestView>) NotificationsTestView::showWarningNotification),
                Arguments.of("System",
                        (Consumer<NotificationsTestView>) NotificationsTestView::showSystemNotification)
        );
    }

    protected <T extends View<?>> T navigateToNotificationsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) NotificationsTestView.class);
        return UiTestUtils.getCurrentView();
    }
}
