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

import component.image.view.JmixImageTestView;
import component.listmenu.view.ListMenuTestView;
import component.standarddetailview.view.BlankTestView;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.exception.NoSuchViewException;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.view.initial.InitialView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import jmix_ui_test_extension.test_support.CustomUiTestAuthenticator;
import jmix_ui_test_extension.test_support.ExtCustomUiTestAuthenticator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = {"component.standarddetailview.view", "component.image"},
        authenticator = CustomUiTestAuthenticator.class)
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class UiTestNestedClassesTest {

    @Autowired
    private ViewNavigators viewNavigators;
    @Autowired
    ViewNavigationSupport navigationSupport;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Test
    @DisplayName("Use view base packages")
    public void loadJmixImageWithDataContainer() {
        var origin = navigateTo(BlankTestView.class);
        viewNavigators.view(origin, JmixImageTestView.class)
                .navigate();

        JmixImageTestView view = UiTestUtils.getCurrentView();

        Assertions.assertEquals(JmixImageTestView.class, view.getClass());
    }

    @Nested
    class NestedClass {

        @Test
        @DisplayName("Use view base packages from outer class")
        public void loadJmixImageWithDataContainer() {
            var origin = navigateTo(BlankTestView.class);
            viewNavigators.view(origin, JmixImageTestView.class)
                    .navigate();

            JmixImageTestView view = UiTestUtils.getCurrentView();

            Assertions.assertEquals(JmixImageTestView.class, view.getClass());
        }

        @Test
        @DisplayName("Use authentication from outer class")
        public void useAuthenticationFromOuterClass() {
            UserDetails user = currentAuthentication.getUser();

            Assertions.assertEquals(CustomUiTestAuthenticator.username, user.getUsername());
        }

        @Test
        @DisplayName("Use initial view from outer class")
        public void useInitialViewFromOuterClass() {
            Assertions.assertInstanceOf(InitialView.class, UiTestUtils.getCurrentView());
        }
    }

    @UiTest(viewBasePackages = "component.listmenu",
            authenticator = ExtCustomUiTestAuthenticator.class,
            initialView = BlankTestView.class)
    @Nested
    class NestedClassWithUiTestAnnotation {

        @Test
        @DisplayName("Checks that view base packages are not changed")
        public void checkViewBasePackages() {
            var origin = navigateTo(BlankTestView.class);
            Assertions.assertThrows(NoSuchViewException.class, () ->
                    viewNavigators.view(origin, ListMenuTestView.class).navigate());

            viewNavigators.view(origin, JmixImageTestView.class)
                    .navigate();

            JmixImageTestView view = UiTestUtils.getCurrentView();

            Assertions.assertEquals(JmixImageTestView.class, view.getClass());
        }

        @Test
        @DisplayName("Checks that current authentication is not overriden")
        public void checkCurrentAuthentication() {
            UserDetails user = currentAuthentication.getUser();

            Assertions.assertEquals(CustomUiTestAuthenticator.username, user.getUsername());
        }

        @Test
        @DisplayName("Checks that outer class' initial view is not overriden")
        public void checkInitialView() {
            Assertions.assertInstanceOf(InitialView.class, UiTestUtils.getCurrentView());
        }
    }

    protected <T extends View<?>> T navigateTo(Class<T> view) {
        navigationSupport.navigate(view);
        return UiTestUtils.getCurrentView();
    }
}
