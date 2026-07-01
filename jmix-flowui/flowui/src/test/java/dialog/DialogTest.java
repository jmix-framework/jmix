/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.component.HasText;
import dialog.view.DialogsTestView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = "dialog.view")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class DialogTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @ParameterizedTest
    @MethodSource("provideDialogsNamesAndMethods")
    @DisplayName("Open dialogs with different types")
    public void openDialogsWithDifferentTypesTest(String typeName, Consumer<DialogsTestView> openMethod) {
        // Navigate to DialogTestView
        DialogsTestView view = navigateToDialogsTestView();

        // open the dialog with corresponding type
        openMethod.accept(view);

        // dialog will be shown
        DialogInfo lastOpenedDialog = UiTestUtils.getLastOpenedDialog();

        assertNotNull(lastOpenedDialog);
        assertTrue(lastOpenedDialog.getDialog().isOpened());
        assertEquals(((HasText) lastOpenedDialog.getContent()).getText(), typeName);
    }

    @Test
    @DisplayName("Open multiple dialogs")
    public void showMultipleDialogsTest() {
        // Navigate to DialogTestView
        DialogsTestView view = navigateToDialogsTestView();

        // Open three closeable dialogs
        view.openMessageDialog();
        view.openMessageDialog();
        view.openMessageDialog();

        // Three dialogs will be opened
        assertEquals(3, UiTestUtils.getOpenedDialogs().size());

        // One dialog will be closed using API method
        UiTestUtils.getOpenedDialogs().get(0).getDialog().close();

        // Only two dialogs will be opened
        assertEquals(2, UiTestUtils.getOpenedDialogs().size());

        // One dialog will be closed using button
        UiTestUtils.getOpenedDialogs().get(0).getButtons().get(0).click();

        // Only one dialog will be opened
        assertEquals(1, UiTestUtils.getOpenedDialogs().size());
    }

    @ParameterizedTest
    @MethodSource("provideOptionDialogButtonIdsAndCheckClickFunction")
    @DisplayName("Open option dialog with action buttons")
    public void openOptionDialogWithButtonsTest(String buttonId, Function<DialogsTestView, Boolean> checkClickFunction) {
        // Navigate to DialogTestView
        DialogsTestView view = navigateToDialogsTestView();

        // Open the OptionDialog with buttons
        view.openOptionDialog();

        // The dialog will be opened
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();

        assertNotNull(dialogInfo);
        assertTrue(dialogInfo.getDialog().isOpened());
        assertEquals("Option", ((HasText) dialogInfo.getContent()).getText());

        // Click the corresponding button
        dialogInfo.getButtons().stream()
                .filter(button -> buttonId.equals(button.getId().orElseThrow()))
                .findAny()
                .orElseThrow()
                .click();

        // Click will be detected and dialog will be closed
        assertTrue(checkClickFunction.apply(view));
        assertFalse(dialogInfo.getDialog().isOpened());
    }

    protected static Stream<Arguments> provideDialogsNamesAndMethods() {
        return Stream.of(
                Arguments.of("Message", (Consumer<DialogsTestView>) DialogsTestView::openMessageDialog),
                Arguments.of("Option", (Consumer<DialogsTestView>) DialogsTestView::openOptionDialog)
        );
    }

    protected static Stream<Arguments> provideOptionDialogButtonIdsAndCheckClickFunction() {
        return Stream.of(
                Arguments.of("yes", (Function<DialogsTestView, Boolean>) dialog -> dialog.yesPressed),
                Arguments.of("no", (Function<DialogsTestView, Boolean>) dialog -> dialog.noPressed)
        );
    }

    protected <T extends View<?>> T navigateToDialogsTestView() {
        //noinspection unchecked
        navigationSupport.navigate((Class<T>) DialogsTestView.class);
        return UiTestUtils.getCurrentView();
    }
}
