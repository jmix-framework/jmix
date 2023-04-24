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

package facet.lookup_screen

import facet.lookup_screen.screen.LookupScreenFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class LookupScreenFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.lookup_screen', 'test_support.entity.sales.screen'])
    }

    def 'LookupScreenFacet is loaded from XML'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(LookupScreenFacetTestScreen)

        when: 'Screen with LookupScreenFacet is opened'

        screenWithFacet.show()

        def lookupScreenFacet = screenWithFacet.lookupScreen
        def tableLookupScreenFacet = screenWithFacet.tableLookupScreen
        def fieldLookupScreenFacet = screenWithFacet.fieldLookupScreen

        then: 'All LookupScreenFacet settings are correctly loaded'

        lookupScreenFacet.id == 'lookupScreen'
        lookupScreenFacet.entityClass == Order
        lookupScreenFacet.openMode == OpenMode.DIALOG
        lookupScreenFacet.actionTarget == 'action'
        lookupScreenFacet.entityPicker == screenWithFacet.orderPicker
        lookupScreenFacet.container == screenWithFacet.ordersDc
        lookupScreenFacet.listComponent == screenWithFacet.ordersTable

        tableLookupScreenFacet.id == 'tableLookupScreen'
        tableLookupScreenFacet.listComponent == screenWithFacet.ordersTable
        tableLookupScreenFacet.buttonTarget == 'button'

        fieldLookupScreenFacet.id == 'fieldLookupScreen'
        fieldLookupScreenFacet.entityPicker == screenWithFacet.orderPicker
    }

    def 'LookupScreenFacet opens lookup by entity class'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(LookupScreenFacetTestScreen)
        screenWithFacet.show()

        def lookupScreenFacet = screenWithFacet.lookupScreen

        when: 'LookupScreenFacet opens lookup by entity class'

        def orderBrowser = lookupScreenFacet.show()

        then: 'Lookup is correctly opened'

        screens.openedScreens.all.contains(orderBrowser)
    }

    def 'LookupScreenFacet opens lookup by list component'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(LookupScreenFacetTestScreen)
        screenWithFacet.show()

        def tableLookupScreenFacet = screenWithFacet.tableLookupScreen

        when: 'LookupScreenFacet opens lookup by list component'

        def orderBrowser = tableLookupScreenFacet.show()

        then: 'Lookup is correctly opened'

        screens.openedScreens.all.contains(orderBrowser)
    }

    def 'LookupScreenFacet opens lookup by PickerField'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(LookupScreenFacetTestScreen)
        screenWithFacet.show()

        def fieldLookupScreenFacet = screenWithFacet.fieldLookupScreen

        when: 'LookupScreenFacet opens lookup by list component'

        def orderBrowser = fieldLookupScreenFacet.show()

        then: 'Lookup is correctly opened'

        screens.openedScreens.all.contains(orderBrowser)
    }

    def 'Delegates are correctly installed into LookupScreenFacet'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(LookupScreenFacetTestScreen)

        when: 'Screen with LookupScreenFacet is opened'

        screenWithFacet.show()

        def lookupScreenFacet = screenWithFacet.lookupScreen

        then: 'Delegates are installed'

        lookupScreenFacet.selectHandler
        lookupScreenFacet.selectValidator
        lookupScreenFacet.transformation
    }
}
