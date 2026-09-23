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
import io.jmix.flowui.action.inputdialog.InputDialogAction;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies which predefined {@link InputDialog} actions get the input dialog shortcuts, and that the input dialog
 * properties take precedence over the view properties.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app.inputdialog", "dialog.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = {
                "jmix.ui.component.input-dialog-confirm-shortcut = CONTROL-ENTER",
                "jmix.ui.component.input-dialog-cancel-shortcut = ALT-C",
                "jmix.ui.view.save-shortcut = ALT-S",
                "jmix.ui.view.close-shortcut = ESCAPE"
        })
public class InputDialogShortcutsTest {

    static final KeyCombination CONFIRM_SHORTCUT = KeyCombination.create("CONTROL-ENTER");
    static final KeyCombination CANCEL_SHORTCUT = KeyCombination.create("ALT-C");

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Dialogs dialogs;

    @Test
    void ok_confirmShortcutIsBoundToOk() {
        InputDialog dialog = openInputDialog(DialogActions.OK);

        assertShortcut(CONFIRM_SHORTCUT, getAction(dialog, "ok"));
    }

    @Test
    void okCancel_ownShortcutsOverrideViewShortcuts() {
        InputDialog dialog = openInputDialog(DialogActions.OK_CANCEL);

        assertShortcut(CONFIRM_SHORTCUT, getAction(dialog, "ok"));
        assertShortcut(CANCEL_SHORTCUT, getAction(dialog, "cancel"));
    }

    @Test
    void yesNo_cancelShortcutIsBoundToNo() {
        InputDialog dialog = openInputDialog(DialogActions.YES_NO);

        assertShortcut(CONFIRM_SHORTCUT, getAction(dialog, "yes"));
        assertShortcut(CANCEL_SHORTCUT, getAction(dialog, "no"));
    }

    @Test
    void yesNoCancel_cancelShortcutIsBoundToCancelOnly() {
        InputDialog dialog = openInputDialog(DialogActions.YES_NO_CANCEL);

        assertShortcut(CONFIRM_SHORTCUT, getAction(dialog, "yes"));
        assertNull(getAction(dialog, "no").getShortcutCombination());
        assertShortcut(CANCEL_SHORTCUT, getAction(dialog, "cancel"));
    }

    @Test
    void customActions_shortcutsAreNotChanged() {
        navigateToDialogsTestView();

        KeyCombination customShortcut = KeyCombination.create("ALT-A");
        InputDialog dialog = dialogs.createInputDialog(UiTestUtils.getCurrentView())
                .withParameter(InputParameter.stringParameter("name"))
                .withActions(
                        InputDialogAction.action("ok"),
                        InputDialogAction.action("apply")
                                .withShortcutCombination(customShortcut)
                )
                .open();

        assertNull(getAction(dialog, "ok").getShortcutCombination());
        assertEquals(customShortcut, getAction(dialog, "apply").getShortcutCombination());
    }

    InputDialog openInputDialog(DialogActions actions) {
        navigateToDialogsTestView();

        return dialogs.createInputDialog(UiTestUtils.getCurrentView())
                .withParameter(InputParameter.stringParameter("name"))
                .withActions(actions)
                .open();
    }

    static Action getAction(InputDialog dialog, String id) {
        Action action = ViewControllerUtils.getViewActions(dialog).getAction(id);
        assertNotNull(action, "Action not found: " + id);
        return action;
    }

    static void assertShortcut(KeyCombination expected, Action action) {
        KeyCombination actual = action.getShortcutCombination();
        assertNotNull(actual, "No shortcut of action: " + action.getId());
        assertEquals(expected, actual, "Shortcut of action: " + action.getId());
        // KeyCombination#equals does not take this flag into account.
        assertTrue(actual.isResetFocusOnActiveElement(), "Reset focus of action: " + action.getId());
    }

    <T extends View<?>> void navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
    }
}
