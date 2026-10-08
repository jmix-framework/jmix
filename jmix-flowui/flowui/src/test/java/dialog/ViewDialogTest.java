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
import io.jmix.flowui.app.inputdialog.InputDialog;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that views shown as dialogs (e.g. {@code InputDialog}) are reported through the view-dialog
 * accessors of {@link UiTestUtils} and are kept separate from component dialogs.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app.inputdialog","dialog.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ViewDialogTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Test
    @DisplayName("InputDialog is reported as a view dialog, not as a component dialog")
    public void inputDialogIsReportedAsViewDialogTest() {
        // Navigate to DialogsTestView
        DialogsTestView view = navigateToDialogsTestView();

        // Open an InputDialog (a view shown as a dialog)
        view.openInputDialog();

        // It is available through the view-dialog accessors
        View<?> lastViewDialog = UiTestUtils.getLastOpenedViewDialog();
        assertNotNull(lastViewDialog);
        assertInstanceOf(InputDialog.class, lastViewDialog);
        assertEquals(1, UiTestUtils.getOpenedViewDialogs().size());
        assertSame(lastViewDialog, UiTestUtils.getOpenedViewDialogs().get(0));

        // ...and it is not reported as a component dialog
        assertNull(UiTestUtils.getLastOpenedDialog());
        assertTrue(UiTestUtils.getOpenedDialogs().isEmpty());
    }

    protected <T extends View<?>> T navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
        return UiTestUtils.getCurrentView();
    }
}
