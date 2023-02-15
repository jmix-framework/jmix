/*
 * Copyright (c) 2020 Haulmont.
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

package facet.option_dialog

import facet.option_dialog.screen.OptionDialogFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.GuiDevelopmentException
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.WindowMode
import io.jmix.ui.component.impl.ButtonImpl
import io.jmix.ui.component.impl.OptionDialogFacetImpl
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class OptionDialogFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.option_dialog'])
    }

    def 'OptionDialog attributes are correctly loaded'() {
        showTestMainScreen()

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

    def 'Declarative OptionDialog subscription on Action'() {
        showTestMainScreen()

        def screen = screens.create(OptionDialogFacetTestScreen)
        screen.show()

        when: 'Dialog target action performed'

        screen.dialogAction.actionPerform(screen.dialogButton)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Action Subscription' }
    }

    def 'Declarative OptionDialog subscription on Button'() {
        showTestMainScreen()

        def screen = screens.create(OptionDialogFacetTestScreen)

        when: 'Dialog target button is clicked'

        ((ButtonImpl) screen.dialogButton).buttonClicked(null)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Button Subscription' }
    }

    def 'OptionDialog should be bound to frame'() {
        def dialog = new OptionDialogFacetImpl()

        when: 'Trying to show Dialog not bound to frame'

        dialog.show()

        then: 'Exception is thrown'

        thrown IllegalStateException

        when: 'Trying to setup declarative subscription without frame'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown IllegalStateException
    }

    def 'OptionDialog should have single subscription'() {
        showTestMainScreen()

        def screen = screens.create(OptionDialogFacetTestScreen)

        def dialog = new OptionDialogFacetImpl()
        dialog.setOwner(screen.getWindow())
        dialog.setActionTarget('actionId')
        dialog.setButtonTarget('buttonId')

        when: 'Both action and button are set as Dialog target'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }

    def 'OptionDialog target should not be missing'() {
        showTestMainScreen()

        def screen = screens.create(OptionDialogFacetTestScreen)

        def dialog = new OptionDialogFacetImpl()
        dialog.setOwner(screen.getWindow())

        when: 'Missing action is set as target'

        dialog.setActionTarget('missingAction')
        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException

        when: 'Missing button is set as target'

        dialog.setActionTarget(null)
        dialog.setButtonTarget('missingButton')
        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }
}
