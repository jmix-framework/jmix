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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.request.RequestContextHolder;
import test_support.FlowuiTestConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the Vaadin environment created by the extension for a test is torn down after it, so that nothing
 * keeps the application context reachable once Spring evicts and closes it.
 */
@UiTest
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class UiTestVaadinTeardownTest {

    static final AtomicBoolean serviceDestroyed = new AtomicBoolean();

    // Assertions run in @AfterAll because the extension tears Vaadin down in afterEach, after the test method.
    @AfterAll
    static void afterAll() {
        assertTrue(serviceDestroyed.get(), "VaadinService created for the test must be destroyed after it");

        assertNull(VaadinService.getCurrent());
        assertNull(VaadinSession.getCurrent());
        assertNull(UI.getCurrent());
        assertNull(RequestContextHolder.getRequestAttributes());
    }

    @Test
    void afterEach_destroysVaadinServiceAndClearsCurrentInstances() {
        VaadinService.getCurrent().addServiceDestroyListener(event -> serviceDestroyed.set(true));
    }
}
