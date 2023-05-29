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

package spec.haulmont.cuba.web.facets.messagedialog

import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.WindowMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.facets.messagedialog.screens.MessageDialogFacetTestScreen

class MessageDialogFacetTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.facets.messagedialog.screens'])
    }

    def 'MessageDialog attributes are correctly loaded'() {
        showMainScreen()

        when: 'MessageDialog is configured in XML'

        def screenWithDialog = screens.create(MessageDialogFacetTestScreen)
        def messageDialog = screenWithDialog.messageDialog

        then: 'Attribute values are propagated to MessageDialog facet'

        messageDialog.id == 'messageDialog'
        messageDialog.caption == 'MessageDialog Facet'
        messageDialog.message == 'MessageDialog Test'
        messageDialog.contentMode == ContentMode.HTML
        messageDialog.height == 200
        messageDialog.width == 350
        messageDialog.styleName == 'msg-dialog-style'
        messageDialog.modal
        messageDialog.windowMode == WindowMode.MAXIMIZED
        messageDialog.closeOnClickOutside

        when: 'MessageDialog is shown'

        messageDialog.show()

        then: 'UI has this dialog window'

        vaadinUi.windows.any { window ->
            window.caption == 'MessageDialog Facet'
        }
    }
}
