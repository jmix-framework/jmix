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

import com.vaadin.flow.component.textfield.TextFieldBase;
import io.jmix.core.MessageTools;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.SupportsTrimming;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.SecurityFlowuiTestConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleLocalizedValues.click;
import static test_support.TestRoleLocalizedValues.enterValue;
import static test_support.TestRoleLocalizedValues.getDialogFields;

/**
 * The available locales of the tests are German, the default one, and English.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RoleLocalizedValuesViewTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    DialogWindows dialogWindows;
    @Autowired
    MessageTools messageTools;

    @Test
    void open_showsFieldForEachAvailableLocale() {
        RoleLocalizedValuesView dialog = openDialog("en=Accounting\nfr=Comptabilité", false);

        List<TextFieldBase<?, String>> fields = getDialogFields(dialog);
        assertThat(fields)
                .extracting(TextFieldBase::getLabel)
                .containsExactly(messageTools.getLocaleDisplayName(Locale.GERMAN),
                        messageTools.getLocaleDisplayName(Locale.ENGLISH));
        assertThat(fields)
                .extracting(TextFieldBase::getValue)
                .containsExactly("", "Accounting");
        assertThat(fields).allMatch(TypedTextField.class::isInstance);
    }

    @Test
    void open_multiline_showsTextAreas() {
        RoleLocalizedValuesView dialog = openDialog("en=Keeps the books", true);

        List<TextFieldBase<?, String>> fields = getDialogFields(dialog);
        assertThat(fields).hasSize(2).allMatch(JmixTextArea.class::isInstance);
        assertThat(fields.get(1).getValue()).isEqualTo("Keeps the books");
    }

    @Test
    void save_setsTypedValueRemovesClearedEntryAndKeepsOtherLocales() {
        RoleLocalizedValuesView dialog = openDialog("de=Buchhaltung\nen=Accounting\nfr=Comptabilité", false);
        List<TextFieldBase<?, String>> fields = getDialogFields(dialog);

        enterValue(fields.get(0), "");
        enterValue(fields.get(1), "Bookkeeping");
        click(dialog, "saveAndCloseBtn");

        assertThat(RoleLocalizedValuesUtils.read(dialog.getLocalizedValues()))
                .isEqualTo(Map.of("en", "Bookkeeping", "fr", "Comptabilité"));
    }

    @Test
    void save_multilineFieldsClearedByUser_removeTheirEntries() {
        RoleLocalizedValuesView dialog = openDialog(
                "de=Führt die Bücher\nen=Keeps the books\nfr=Tient les comptes", true);
        List<TextFieldBase<?, String>> fields = getDialogFields(dialog);

        enterValue(fields.get(0), "   ");
        enterValue(fields.get(1), "");
        click(dialog, "saveAndCloseBtn");

        assertThat(RoleLocalizedValuesUtils.read(dialog.getLocalizedValues()))
                .isEqualTo(Map.of("fr", "Tient les comptes"));
    }

    @Test
    void save_noEntryLeft_givesNull() {
        RoleLocalizedValuesView dialog = openDialog("en=Accounting", false);
        TextFieldBase<?, String> englishField = getDialogFields(dialog).get(1);
        // An application may turn trimming off, so blank input reaches the dialog as it is.
        ((SupportsTrimming) englishField).setTrimEnabled(false);

        enterValue(englishField, " ");
        click(dialog, "saveAndCloseBtn");

        assertThat(dialog.getLocalizedValues()).isNull();
    }

    @Test
    void save_nothingChanged_returnsStoredBundle() {
        // Properties.store would write the entries in an order of its own, so a rewritten bundle would differ.
        String storedBundle = "fr=Comptabilité\nen=Accounting\nde=Buchhaltung";
        RoleLocalizedValuesView dialog = openDialog(storedBundle, false);

        click(dialog, "saveAndCloseBtn");

        assertThat(dialog.getLocalizedValues()).isEqualTo(storedBundle);
    }

    @Test
    void save_blankStoredEntryUntouched_returnsStoredBundle() {
        // The blank entry of German opens as an empty field, which removes an entry when the dialog is saved.
        String storedBundle = "de=\nen=Accounting";
        RoleLocalizedValuesView dialog = openDialog(storedBundle, false);

        click(dialog, "saveAndCloseBtn");

        assertThat(dialog.getLocalizedValues()).isEqualTo(storedBundle);
    }

    @Test
    void open_unreadableBundle_showsEmptyFields() {
        RoleLocalizedValuesView dialog = openDialog("en=Accounting\nru=\\u00", false);

        assertThat(getDialogFields(dialog))
                .extracting(TextFieldBase::getValue)
                .containsExactly("", "");
    }

    @Test
    void save_unreadableBundle_givesFieldValues() {
        RoleLocalizedValuesView dialog = openDialog("en=Accounting\nru=\\u00", false);

        click(dialog, "saveAndCloseBtn");
        assertThat(dialog.getLocalizedValues()).isNull();

        RoleLocalizedValuesView typedDialog = openDialog("en=Accounting\nru=\\u00", false);
        enterValue(getDialogFields(typedDialog).get(1), "Bookkeeping");
        click(typedDialog, "saveAndCloseBtn");
        assertThat(RoleLocalizedValuesUtils.read(typedDialog.getLocalizedValues()))
                .isEqualTo(Map.of("en", "Bookkeeping"));
    }

    @Test
    void saveAction_shortcut_commitsFocusedFieldFirst() {
        RoleLocalizedValuesView dialog = openDialog("en=Accounting", false);
        Action saveAction = Objects.requireNonNull(ViewControllerUtils.getViewActions(dialog).getAction("saveAction"));

        // The focus leaves the field before the action runs, so that a value typed just before the shortcut is saved.
        KeyCombination shortcut = Objects.requireNonNull(saveAction.getShortcutCombination());
        assertThat(shortcut.isResetFocusOnActiveElement()).isTrue();
    }

    RoleLocalizedValuesView openDialog(String localizedValues, boolean multiline) {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        return dialogWindows.view(UiTestUtils.getCurrentView(), RoleLocalizedValuesView.class)
                .withViewConfigurer(view -> {
                    view.setLocalizedValues(localizedValues);
                    view.setMultiline(multiline);
                })
                .open()
                .getView();
    }
}
