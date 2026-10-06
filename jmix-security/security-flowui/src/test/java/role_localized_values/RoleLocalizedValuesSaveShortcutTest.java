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

package role_localized_values;

import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import test_support.SecurityFlowuiTestConfiguration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The application configures a save shortcut, which the save action of the dialog takes.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
@TestPropertySource(properties = "jmix.ui.view.save-shortcut=CONTROL-ENTER")
public class RoleLocalizedValuesSaveShortcutTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    DialogWindows dialogWindows;

    @Test
    void saveAction_shortcut_commitsFocusedFieldFirst() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        RoleLocalizedValuesView dialog = dialogWindows.view(UiTestUtils.getCurrentView(), RoleLocalizedValuesView.class)
                .open()
                .getView();
        Action saveAction = Objects.requireNonNull(ViewControllerUtils.getViewActions(dialog).getAction("saveAction"));

        // The focus leaves the field before the action runs, so that a value typed just before the shortcut is saved.
        KeyCombination shortcut = Objects.requireNonNull(saveAction.getShortcutCombination());
        assertThat(shortcut.isResetFocusOnActiveElement()).isTrue();
    }
}
