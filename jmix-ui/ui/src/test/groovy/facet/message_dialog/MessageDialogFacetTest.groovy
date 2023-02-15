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

package facet.message_dialog

import facet.message_dialog.screen.MessageDialogFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.GuiDevelopmentException
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.WindowMode
import io.jmix.ui.component.impl.ButtonImpl
import io.jmix.ui.component.impl.MessageDialogFacetImpl
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class MessageDialogFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.message_dialog'])
    }

    def 'MessageDialog attributes are correctly loaded'() {
        showTestMainScreen()

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

    def 'Declarative MessageDialog subscription on Action'() {
        showTestMainScreen()

        def screen = screens.create(MessageDialogFacetTestScreen)
        screen.show()

        when: 'Dialog target action performed'

        screen.dialogAction.actionPerform(screen.dialogButton)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Action Subscription' }
    }

    def 'Declarative MessageDialog subscription on Button'() {
        showTestMainScreen()

        def screen = screens.create(MessageDialogFacetTestScreen)

        when: 'Dialog target button is clicked'

        ((ButtonImpl) screen.dialogButton).buttonClicked(null)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Button Subscription' }
    }

    def 'MessageDialog should be bound to frame'() {
        def dialog = new MessageDialogFacetImpl()

        when: 'Trying to show Dialog not bound to frame'

        dialog.show()

        then: 'Exception is thrown'

        thrown IllegalStateException

        when: 'Trying to setup declarative subscription without frame'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown IllegalStateException
    }

    def 'MessageDialog should have single subscription'() {
        showTestMainScreen()

        def screen = screens.create(MessageDialogFacetTestScreen)

        def dialog = new MessageDialogFacetImpl()
        dialog.setOwner(screen.getWindow())
        dialog.setActionTarget('actionId')
        dialog.setButtonTarget('buttonId')

        when: 'Both action and button are set as Dialog target'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }

    def 'MessageDialog target should not be missing'() {
        showTestMainScreen()

        def screen = screens.create(MessageDialogFacetTestScreen)

        def dialog = new MessageDialogFacetImpl()
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
