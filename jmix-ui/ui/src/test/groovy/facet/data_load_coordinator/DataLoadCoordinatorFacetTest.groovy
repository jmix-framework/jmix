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

package facet.data_load_coordinator


import facet.data_load_coordinator.screen.DlcAutoTestScreen
import facet.data_load_coordinator.screen.DlcAutoWithFragmentTestScreen
import facet.data_load_coordinator.screen.DlcManualNoParamTestScreen
import facet.data_load_coordinator.screen.DlcManualTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DataLoadCoordinator
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import spock.lang.Unroll
import test_support.UiTestConfiguration
import test_support.entity.petclinic.Address
import test_support.entity.petclinic.Owner
import test_support.entity.petclinic.OwnerCategory

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DataLoadCoordinatorFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.data_load_coordinator'])
    }

    @Unroll
    def "manual configuration"() {
        showTestMainScreen()

        when: "show screen"

        def screen = screens.create(screenClass)
        screen.show()

        then: "facet is created and injected"

        screen.window.getFacet('dlc') instanceof DataLoadCoordinator
        screen.dlc != null

        and: "master loader is triggered once"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters.isEmpty()

        when: "master item is selected"

        screen.events.clear()
        screen.ownersDc.setItem(screen.ownersDc.getItems()[0])

        then: "slave loader is triggered"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters.size() == 1
        screen.events[0].loadContext.query.parameters['owner'] == screen.ownersDc.getItem()

        when: "entity filter field value is set"

        screen.events.clear()
        def category = new OwnerCategory()
        screen.categoryFilterField.setValue(category)

        then: "master loader is triggered, its item is cleared so slave is triggered too"

        screen.events.size() == 2
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['category'] == category
        screen.events[1].loader == 'petsDl'

        when: "string filter field is set"

        screen.events.clear()
        screen.nameFilterField.setValue("o")

        then: "slave is triggered and parameter is wrapped for case-insensitive like"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['name'] == '(?i)%o%'

        where:

        screenClass << [DlcManualTestScreen, DlcManualNoParamTestScreen]
    }

    def "auto configuration"() {
        showTestMainScreen()

        when: "show screen"

        def screen = screens.create(DlcAutoTestScreen)
        screen.show()

        then: "facet is created and injected"

        screen.window.getFacet('dlc') instanceof DataLoadCoordinator
        screen.dlc != null

        and: "master loader is triggered once"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters.isEmpty()

        when: "master item is selected"

        screen.events.clear()
        screen.ownersDc.setItem(screen.ownersDc.getItems()[0])

        then: "slave loader is triggered"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters.size() == 1
        screen.events[0].loadContext.query.parameters['container_ownersDc'] == screen.ownersDc.getItem()

        when: "entity filter field value is set"

        screen.events.clear()
        def category = metadata.create(OwnerCategory)
        screen.categoryFilterField.setValue(category)

        then: "master loader is triggered, its item is cleared so slave is triggered too"

        screen.events.size() == 2
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_categoryFilterField'] == category
        screen.events[1].loader == 'petsDl'

        when: "string filter field is set"

        screen.events.clear()
        screen.nameFilterField.setValue("o")

        then: "slave is triggered and parameter is wrapped for case-insensitive like"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_nameFilterField'] == '(?i)%o%'
    }

    def "auto configuration of screen with fragment"() {
        showTestMainScreen()

        when: "show screen"

        def screen = screens.create(DlcAutoWithFragmentTestScreen)
        def owner = metadata.create(Owner)
        owner.setName('Joe')
        def address = metadata.create(Address)
        address.setPostcode('123')
        owner.setAddress(address)

        screen.setEntityToEdit(owner)
        screen.show()

        then: "fragment's facet is created and injected"

        def screenFragment = screen.addressFragment
        screenFragment.getFragment().getFacet('addressDlc') != null
        screenFragment.addressDlc != null

        and: "master loader is triggered once"

        screenFragment.events.size() == 1
        screenFragment.events[0].loader == 'countriesDl'
        screenFragment.events[0].loadContext.query.parameters.isEmpty()

        when: "master item is selected"

        screenFragment.events.clear()
        screenFragment.countriesDc.setItem(screenFragment.countriesDc.getItems()[0])

        then: "slave loader is triggered"

        screenFragment.events.size() == 1
        screenFragment.events[0].loader == 'citiesDl'
        screenFragment.events[0].loadContext.query.parameters.size() == 1
        screenFragment.events[0].loadContext.query.parameters['container_countriesDc'] == screenFragment.countriesDc.getItem()
    }
}
