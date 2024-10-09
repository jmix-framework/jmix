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

package dialog

import com.vaadin.flow.component.HasText
import dialog.view.DialogsTestView
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DialogTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("dialog.view")
    }

    def "Open dialogs with #typeName type"() {
        given: "Opened DialogsTestView"
        def view = navigateToView DialogsTestView

        when: "Open the dialog with #typeName type"
        view."open${typeName}Dialog"()

        then: "Dialog with #typeName type will be shown"
        verifyAll(UiTestUtils.lastOpenedDialog) {
            dialog.opened
            (content as HasText).text == "${typeName}"
        }

        where:
        typeName << ["Message", "Option"]
    }

    def "Show multiple dialogs"() {
        given: "Opened DialogsTestView"
        def view = navigateToView DialogsTestView

        when: "Show three closeable dialogs"
        view.openMessageDialog()
        view.openMessageDialog()
        view.openMessageDialog()

        then: "Three dialogs will be opened"
        UiTestUtils.openedDialogs.size() == 3

        when: "One dialog will be closed using API method"
        UiTestUtils.openedDialogs.get(0).dialog.close()

        then: "Only two dialogs will be opened"
        UiTestUtils.openedDialogs.size() == 2

        when: "One dialog will be closed using button"
        UiTestUtils.openedDialogs.get(0).buttons.get(0).click()

        then: "Only one dialog will be opened"
        UiTestUtils.openedDialogs.size() == 1
    }

    def "Open option dialog with #buttonId click"() {
        given: "Opened DialogsTestView"
        def view = navigateToView DialogsTestView

        when: "Open the OptionDialog with buttons"
        view.openOptionDialog()

        then: "The dialog with buttons be opened"
        def dialogInfo = UiTestUtils.lastOpenedDialog

        dialogInfo.dialog.opened
        (dialogInfo.content as HasText).text == "Option"

        when: "The '#buttonId' button will be clicked"
        dialogInfo.buttons.find { ("${buttonId}" == it.id.get()) }.click()

        then: "Click will be detected and dialog will be closed"
        view."${buttonId}Pressed"
        !dialogInfo.dialog.opened

        where:
        buttonId << ["yes", "no"]
    }
}
