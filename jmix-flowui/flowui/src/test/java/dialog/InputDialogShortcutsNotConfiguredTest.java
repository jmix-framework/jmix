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

package dialog;

import dialog.view.DialogsTestView;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static dialog.InputDialogShortcutsTest.getAction;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that the predefined {@link InputDialog} actions get no shortcuts when neither the input dialog
 * properties nor the view properties are set.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app.inputdialog", "dialog.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class InputDialogShortcutsNotConfiguredTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Dialogs dialogs;

    @Test
    void okCancel_noShortcuts() {
        navigateToDialogsTestView();

        InputDialog dialog = dialogs.createInputDialog(UiTestUtils.getCurrentView())
                .withParameter(InputParameter.stringParameter("name"))
                .withActions(DialogActions.OK_CANCEL)
                .open();

        assertNull(getAction(dialog, "ok").getShortcutCombination());
        assertNull(getAction(dialog, "cancel").getShortcutCombination());
    }

    <T extends View<?>> void navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
    }
}
