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

package facet.input_dialog

import facet.input_dialog.screen.InputDialogFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.Messages
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.TextArea
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class InputDialogFacetTest extends ScreenSpecification {

    @Autowired
    Messages messages;

    @Override
    void setup() {
        exportScreensPackages(['facet.input_dialog', 'io.jmix.ui.app.inputdialog'])
    }

    def 'InputDialog parameters are initialized'() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'InputDialog is shown'

        def inputDialog = inputDialogFacetScreen.inputDialog.show()

        then: 'All parameters are present'

        def paramIds = inputDialog.getParameters()
                .stream()
                .map({ param -> param.id })
                .collect(Collectors.toList())

        paramIds.contains('booleanParam')
        paramIds.contains('intParam')
        paramIds.contains('stringParam')
        paramIds.contains('decimalParam')
        paramIds.contains('enumParam')
        paramIds.contains('entityParam')
        paramIds.contains('dateTime')
        paramIds.contains('localTime')
        paramIds.contains('localDate')
        paramIds.contains('localDateTime')
        paramIds.contains('offsetDateTime')
        paramIds.contains('offsetTime')
    }

    def "Specific parameters ara propagated"() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'InputDialog is shown'

        def inputDialog = inputDialogFacetScreen.inputDialog.show()


        then:

        def dateTimeParam = inputDialog.getParameters().find { (it.getId() == "dateTime") }
        dateTimeParam.getTimeZone().getID() == messages.getMessage("facet.input_dialog.screen/input-dialog-facet-test-screen.timeZoneId")
        dateTimeParam.useUserTimeZone

        def offsetDateTimeParam = inputDialog.getParameters().find { (it.getId() == "offsetDateTime") }
        offsetDateTimeParam.getTimeZone().getID() == messages.getMessage("facet.input_dialog.screen/input-dialog-facet-test-screen.timeZoneId")
        offsetDateTimeParam.useUserTimeZone
    }

    def 'InputDialog parameter default values are propagated'() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'InputDialog is shown'

        def inputDialog = inputDialogFacetScreen.inputDialog.show()

        then:

        inputDialog.getValue('booleanParam')
        inputDialog.getValue('intParam') == 42
        inputDialog.getValue('stringParam') == 'Hello world!'
        inputDialog.getValue('decimalParam') == 1234567890
    }

    def 'InputDialog custom actions are propagated'() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'Custom actions declared for InputDialog'

        def inputDialog = inputDialogFacetScreen.inputDialogCustomActions.show()

        def actions = inputDialog.actions

        then: 'Actions are propagated into InputDialog'

        actions.find { action -> action.id == 'ok' } != null
        actions.find { action -> action.id == 'cancel' } != null
    }

    def "InputDialog can be shown multiple times"() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'InputDialog is shown'

        def inputDialog = inputDialogFacetScreen.inputDialog.show()
        inputDialog.getWindow().getComponentNN("actionsLayout")

        then:
        noExceptionThrown()

        when:
        inputDialog.closeWithDefaultAction()
        inputDialog = inputDialogFacetScreen.inputDialog.show()
        inputDialog.getWindow().getComponentNN("actionsLayout")

        then:
        noExceptionThrown()
    }

    def 'InputDialog parameter custom fields are set'() {
        showTestMainScreen()

        def inputDialogFacetScreen = screens.create(InputDialogFacetTestScreen)
        inputDialogFacetScreen.show()

        when: 'InputDialog is shown'

        def inputDialog = inputDialogFacetScreen.inputDialogCustomFields.show()

        def parameter = inputDialog.getParameters().get(0)
        def field = parameter.getField().get()

        then:

        parameter.getId() == 'customStringParam'
        field instanceof TextArea
        field.getId() == 'customStringParam'
        field.getCaption() == 'Custom string param'
        field.isRequired()
    }
}
