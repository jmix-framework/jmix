/*
 * Copyright 2022 Haulmont.
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

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import jmix_ui_test_extension.test_support.CustomUiTestAuthenticator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = "component.image", authenticator = CustomUiTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class UiTestCustomAuthenticator {

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @DisplayName("Test custom authenticator")
    @Test
    public void testCustomAuthenticator() {
        User principal = (User) currentAuthentication.getAuthentication().getPrincipal();

        Assertions.assertEquals(CustomUiTestAuthenticator.username, principal.getUsername());
    }
}
