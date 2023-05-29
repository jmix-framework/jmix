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

package spec.haulmont.cuba.web.facets.optiondialog

import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.WindowMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.facets.optiondialog.screens.OptionDialogFacetTestScreen

class OptionDialogFacetTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.facets.optiondialog.screens'])
    }

    def 'OptionDialog attributes are correctly loaded'() {
        showMainScreen()

        when: 'OptionDialog is configured in XML'

        def screenWithDialog = screens.create(OptionDialogFacetTestScreen)
        def optionDialog = screenWithDialog.optionDialog

        then: 'Attribute values are propagated to OptionDialog facet'

        optionDialog.id == 'optionDialog'
        optionDialog.caption == 'OptionDialog Facet'
        optionDialog.message == 'OptionDialog Test'
        optionDialog.contentMode == ContentMode.HTML
        optionDialog.height == 200
        optionDialog.width == 350
        optionDialog.styleName == 'opt-dialog-style'
        optionDialog.windowMode == WindowMode.MAXIMIZED

        when: 'OptionDialog is shown'

        optionDialog.show()

        then: 'UI has this dialog window'

        vaadinUi.windows.any { window ->
            window.caption == 'OptionDialog Facet'
        }
    }
}
