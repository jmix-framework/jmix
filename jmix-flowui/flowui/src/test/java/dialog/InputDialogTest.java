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
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.inputdialog.InputDialogAction;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the public API of {@link InputDialog}.
 */
@UiTest(viewBasePackages = {"io.jmix.flowui.app.inputdialog", "dialog.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class InputDialogTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Dialogs dialogs;

    @Test
    void getActions_isUnmodifiable() {
        InputDialogAction applyAction = InputDialogAction.action("apply");
        InputDialog dialog = createInputDialog();
        dialog.setActions(applyAction);

        assertEquals(List.of(applyAction), dialog.getActions());
        assertThrows(UnsupportedOperationException.class,
                () -> dialog.getActions().add(new DialogAction(DialogAction.Type.OK)));
    }

    @Test
    void getParameters_isUnmodifiable() {
        InputParameter nameParameter = InputParameter.stringParameter("name");
        InputDialog dialog = createInputDialog();
        dialog.setParameter(nameParameter);

        assertEquals(List.of(nameParameter), dialog.getParameters());
        assertThrows(UnsupportedOperationException.class,
                () -> dialog.getParameters().add(InputParameter.stringParameter("comment")));
    }

    InputDialog createInputDialog() {
        navigateToDialogsTestView();

        return dialogs.createInputDialog(UiTestUtils.getCurrentView())
                .build()
                .getView();
    }

    <T extends View<?>> void navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
    }
}
