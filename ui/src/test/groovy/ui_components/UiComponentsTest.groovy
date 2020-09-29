/*
 * Copyright 2020 Haulmont.
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

package ui_components

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.ui.Facets
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.*
import io.jmix.ui.component.mainwindow.*
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Unroll
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration, UiTestConfiguration])
class UiComponentsTest extends ScreenSpecification {

    @Autowired
    Facets facets

    @Unroll
    def "create standard UI component: '#name' with UiComponents"() {
        expect:
        uiComponents.create(name) != null

        where:
        name << [
                RootWindow.NAME,
                TabWindow.NAME,
                DialogWindow.NAME,
                Fragment.NAME,

                HBoxLayout.NAME,
                VBoxLayout.NAME,
                GridLayout.NAME,
                ScrollBoxLayout.NAME,
                HtmlBoxLayout.NAME,
                FlowBoxLayout.NAME,
                CssLayout.NAME,

                Button.NAME,
                LinkButton.NAME,
                Label.NAME,
                Link.NAME,
                CheckBox.NAME,
                GroupBoxLayout.NAME,
                SourceCodeEditor.NAME,
                TextField.NAME,
                PasswordField.NAME,

                ResizableTextArea.NAME,
                TextArea.NAME,
                RichTextArea.NAME,
                MaskedField.NAME,

                Table.NAME,
                TreeTable.NAME,
                GroupTable.NAME,
                DataGrid.NAME,
                TreeDataGrid.NAME,
                DateField.NAME,
                TimeField.NAME,
                ComboBox.NAME,
                EntityPicker.NAME,
                SuggestionField.NAME,
                EntitySuggestionField.NAME,
                ColorPicker.NAME,
                EntityComboBox.NAME,
                CheckBoxGroup.NAME,
                RadioButtonGroup.NAME,
                MultiSelectList.NAME,
                SingleSelectList.NAME,
                FileUploadField.NAME,
                FileMultiUploadField.NAME,
                CurrencyField.NAME,
                SplitPanel.NAME,
                Tree.NAME,
                TabSheet.NAME,
                Accordion.NAME,
                Calendar.NAME,
                Image.NAME,
                BrowserFrame.NAME,
                ButtonsPanel.NAME,
                PopupButton.NAME,
                PopupView.NAME,

                TokenList.NAME,
                TwinColumn.NAME,
                ProgressBar.NAME,
                Pagination.NAME,
                TablePagination.NAME,
                DatePicker.NAME,
                CapsLockIndicator.NAME,
                Form.NAME,

                EntityLinkField.NAME,

                AppMenu.NAME,
                AppWorkArea.NAME,
                LogoutButton.NAME,
                NewWindowButton.NAME,
                UserIndicator.NAME,
                UserActionsButton.NAME,
                TimeZoneIndicator.NAME,
                SideMenu.NAME

                // JavaScriptComponent.NAME todo javaScriptComponent
        ]
    }

    @Unroll
    def "create standard facet: '#facet' with Facets"() {
        given:

        expect:

        facets.create(facet) != null

        where:

        facet << [
                ClipboardTrigger,
                DataLoadCoordinator,
                Timer,

                EditorScreenFacet,
                ScreenFacet,
                LookupScreenFacet,

                InputDialogFacet,
                MessageDialogFacet,
                OptionDialogFacet,

                NotificationFacet,
        ]
    }
}