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

import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.JmixUiTestExtension;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.view.initial.InitialView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Checks the extension registered through an instance field, which JUnit does not pass to
 * {@code TestInstancePostProcessor}, so the extension has to initialize itself before each test.
 */
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class UiTestInstanceFieldExtensionTest {

    @RegisterExtension
    JmixUiTestExtension extension = new JmixUiTestExtension();

    @Test
    void beforeEach_initializesExtensionAndOpensInitialView() {
        assertInstanceOf(InitialView.class, UiTestUtils.getCurrentView());
    }
}
