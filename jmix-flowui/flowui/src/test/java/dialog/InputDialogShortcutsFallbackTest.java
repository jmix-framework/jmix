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
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static dialog.InputDialogShortcutsTest.assertShortcut;
import static dialog.InputDialogShortcutsTest.getAction;

/**
 * Verifies that the predefined {@link InputDialog} actions fall back to the view shortcuts when the input dialog
 * properties are unset or blank.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app.inputdialog", "dialog.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = {
                // An escaped space, i.e. a blank value.
                "jmix.ui.component.input-dialog-cancel-shortcut = \\u0020",
                "jmix.ui.view.save-shortcut = ALT-S",
                "jmix.ui.view.close-shortcut = ESCAPE"
        })
public class InputDialogShortcutsFallbackTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Dialogs dialogs;

    @Test
    void okCancel_unsetOrBlankShortcutsFallBackToViewShortcuts() {
        navigateToDialogsTestView();

        InputDialog dialog = dialogs.createInputDialog(UiTestUtils.getCurrentView())
                .withParameter(InputParameter.stringParameter("name"))
                .withActions(DialogActions.OK_CANCEL)
                .open();

        assertShortcut(KeyCombination.create("ALT-S"), getAction(dialog, "ok"));
        assertShortcut(KeyCombination.create("ESCAPE"), getAction(dialog, "cancel"));
    }

    <T extends View<?>> void navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
    }
}
