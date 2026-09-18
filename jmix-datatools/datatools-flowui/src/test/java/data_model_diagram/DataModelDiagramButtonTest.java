/*
 * Copyright 2026 Haulmont.
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

package data_model_diagram;

import io.jmix.datatoolsflowui.view.datamodel.DataModelListView;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import test_support.DataModelDiagramUiTestConfiguration;
import test_support.TestDiagramEngine;
import test_support.TestFullAccessUiAuthenticator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UiTest(authenticator = TestFullAccessUiAuthenticator.class)
@SpringBootTest(classes = DataModelDiagramUiTestConfiguration.class)
public class DataModelDiagramButtonTest {

    @Autowired
    TestDiagramEngine diagramEngine;
    @Autowired
    ViewNavigationSupport navigationSupport;

    @BeforeEach
    void resetDiagramEngine() {
        diagramEngine.setServiceReachable(true);
        diagramEngine.setPingFailure(null);
    }

    @Test
    void diagramButtonClick_serviceUnreachable_showsServiceUnavailableNotification() {
        diagramEngine.setServiceReachable(false);

        assertDoesNotThrow(() -> diagramButton().click());

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertNotNull(notification);
        assertEquals("Remote diagramming service is unavailable", notification.getText());
        assertEquals(Notifications.Type.ERROR, notification.getType());
    }

    @Test
    void diagramButtonClick_pingThrows_showsNotificationInsteadOfFailingTheClick() {
        diagramEngine.setPingFailure(HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                "Not Found", HttpHeaders.EMPTY, new byte[0], null));

        assertDoesNotThrow(() -> diagramButton().click());

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertNotNull(notification);
        assertEquals(Notifications.Type.ERROR, notification.getType());
    }

    JmixButton diagramButton() {
        navigationSupport.navigate(DataModelListView.class);
        DataModelListView view = UiTestUtils.getCurrentView();
        return UiTestUtils.getComponent(view, "diagramButton");
    }
}
