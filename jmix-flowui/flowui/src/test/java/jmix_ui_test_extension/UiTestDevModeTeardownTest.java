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

package jmix_ui_test_extension;

import com.vaadin.flow.internal.DevModeHandlerManager;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import jmix_ui_test_extension.test_support.TestDevModeConfiguration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Checks that the dev-mode handler, which Vaadin starts for every test when development mode is on, is stopped
 * after the test, so that its threads and watchers do not keep the application context reachable.
 */
@UiTest
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        TestDevModeConfiguration.class},
        properties = "vaadin.productionMode=false")
class UiTestDevModeTeardownTest {

    @Nullable
    static DevModeHandlerManager testDevModeHandlerManager;

    @Autowired
    DevModeHandlerManager devModeHandlerManager;

    // Assertions run in @AfterAll because the extension tears Vaadin down in afterEach, after the test method.
    @AfterAll
    static void afterAll() {
        assertNotNull(testDevModeHandlerManager);
        verify(testDevModeHandlerManager).stopDevModeHandler();
    }

    @Test
    void afterEach_stopsDevModeHandler() throws Exception {
        // Precondition: the extension has started the dev-mode handler for this test and has not stopped it yet
        verify(devModeHandlerManager).initDevModeHandler(any(), any());
        verify(devModeHandlerManager, never()).stopDevModeHandler();

        testDevModeHandlerManager = devModeHandlerManager;
    }
}
