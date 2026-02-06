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

package side_dialog;

import io.jmix.flowui.component.sidedialog.SideDialog;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.sidedialog.JmixSideDialogOverlay;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import side_dialog.view.SideDialogTestView;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = "side_dialog.view")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class SideDialogTest {

    @Autowired
    private ViewNavigationSupport navigationSupport;

    @Test
    @DisplayName("Open and close SideDialog")
    public void openCloseSideDialog() {
        navigationSupport.navigate(SideDialogTestView.class);

        SideDialogTestView view = UiTestUtils.getCurrentView();

        /*
         * Open SideDialog.
         */

        JmixButton openBtn = UiTestUtils.getComponent(view, "openSideDialogBtn");
        openBtn.click();

        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();

        /*
         * Check that SideDialog was opened.
         */

        Assertions.assertNotNull(dialogInfo);
        Assertions.assertTrue(dialogInfo.getDialog().isOpened());
        Assertions.assertInstanceOf(JmixSideDialogOverlay.class, dialogInfo.getDialog());
        Assertions.assertInstanceOf(SideDialog.class, dialogInfo.getDialogComponent());

        Assertions.assertEquals(1, dialogInfo.getHeaderComponents().size());
        Assertions.assertEquals(1, dialogInfo.getContentComponents().size());
        Assertions.assertEquals(1, dialogInfo.getFooterComponents().size());
        Assertions.assertNotNull(dialogInfo.getContent());

        /*
         * Close SideDialog.
         */

        JmixButton closeBtn = UiTestUtils.getComponent(view, "closeSideDialogBtn");
        closeBtn.click();

        dialogInfo = UiTestUtils.getLastOpenedDialog();

        /*
         * SideDialog should be closed.
         */

        Assertions.assertNull(dialogInfo);
    }
}
