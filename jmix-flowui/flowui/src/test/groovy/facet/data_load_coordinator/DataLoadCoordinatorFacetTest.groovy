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

package facet.data_load_coordinator

import facet.data_load_coordinator.screen.DlcAutoMultipleParamsTestScreen
import facet.data_load_coordinator.screen.DlcAutoProvidedParamTestScreen
import facet.data_load_coordinator.screen.DlcAutoTestScreen
import facet.data_load_coordinator.screen.DlcManualNoParamTestScreen
import facet.data_load_coordinator.screen.DlcManualTestScreen
import io.jmix.core.Metadata
import io.jmix.flowui.facet.DataLoadCoordinator
import io.jmix.flowui.view.ViewControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll
import test_support.entity.petclinic.OwnerCategory
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataLoadCoordinatorFacetTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    @Override
    void setup() {
        registerViewBasePackages("facet.data_load_coordinator")
    }

    @Unroll
    def "manual configuration"() {
        when: "show screen"

        def screen = navigateToView(screenClass)
        def screenFacets = ViewControllerUtils.getViewFacets(screen);

        then: "facet is created and injected"

        screenFacets.getFacet('dlc') instanceof DataLoadCoordinator
        screen.dlc != null

        and: "main loader is triggered once"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters.isEmpty()

        when: "main item is selected"

        screen.events.clear()
        screen.ownersDc.setItem(screen.ownersDc.getItems()[0])

        then: "secondary loader is triggered"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters.size() == 1
        screen.events[0].loadContext.query.parameters['owner'] == screen.ownersDc.getItem()

        when: "entity filter field value is set"

        screen.events.clear()
        def category = new OwnerCategory()
        screen.categoryFilterField.setValue(category)

        then: "main loader is triggered, its item is cleared so secondary is triggered too"

        screen.events.size() == 2
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['category'] == category
        screen.events[1].loader == 'petsDl'

        when: "string filter field is set"

        screen.events.clear()
        screen.nameFilterField.setValue("o")

        then: "secondary is triggered and parameter is wrapped for case-insensitive like"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['name'] == '(?i)%o%'

        where:

        screenClass << [DlcManualTestScreen, DlcManualNoParamTestScreen]
    }

    @Unroll
    def "auto configuration"() {
        when: "show screen"

        def screen = navigateToView(screenClass)
        def screenFacets = ViewControllerUtils.getViewFacets(screen);

        then: "facet is created and injected"

        screenFacets.getFacet('dlc') instanceof DataLoadCoordinator
        screen.dlc != null

        and: "main loader is triggered once"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters.isEmpty()

        when: "main item is selected"

        screen.events.clear()
        screen.ownersDc.setItem(screen.ownersDc.getItems()[0])

        then: "secondary loader is triggered"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters.size() == 1
        screen.events[0].loadContext.query.parameters['container_ownersDc'] == screen.ownersDc.getItem()

        when: "entity filter field value is set"

        screen.events.clear()
        def category = metadata.create(OwnerCategory)
        screen.categoryFilterField.setValue(category)

        then: "main loader is triggered, its item is cleared so secondary is triggered too"

        screen.events.size() == 2
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_categoryFilterField'] == category
        screen.events[1].loader == 'petsDl'

        when: "string filter field is set"

        screen.events.clear()
        screen.nameFilterField.setValue("o")

        then: "secondary is triggered and parameter is wrapped for case-insensitive like"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_nameFilterField'] == '(?i)%o%'

        where:

        screenClass << [DlcAutoTestScreen, DlcAutoProvidedParamTestScreen]
    }

    def "auto configuration with several parameters in query text"() {
        when: "show screen"

        def screen = navigateToView(DlcAutoMultipleParamsTestScreen)

        then: "loaders with parameters in query text are not triggered"

        screen.events.isEmpty()

        when: "one of two component parameters is set"

        screen.nameFilterField.setValue("o")

        then: "loader gets values of both component parameters"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_nameFilterField'] == '(?i)%o%'
        screen.events[0].loadContext.query.parameters.containsKey('component_categoryFilterField')
        screen.events[0].loadContext.query.parameters['component_categoryFilterField'] == null

        when: "the other component parameter is set"

        screen.events.clear()
        def category = metadata.create(OwnerCategory)
        screen.categoryFilterField.setValue(category)

        then: "loader gets current values of both component parameters"

        screen.events.size() == 1
        screen.events[0].loader == 'ownersDl'
        screen.events[0].loadContext.query.parameters['component_categoryFilterField'] == category
        screen.events[0].loadContext.query.parameters['component_nameFilterField'] == '(?i)%o%'

        when: "container item is selected"

        screen.events.clear()
        screen.ownersDc.setItem(screen.ownersDc.getItems()[0])

        then: "loader gets values of container and component parameters"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters['container_ownersDc'] == screen.ownersDc.getItem()
        screen.events[0].loadContext.query.parameters.containsKey('component_petNameFilterField')
        screen.events[0].loadContext.query.parameters['component_petNameFilterField'] == null

        when: "component parameter of the same loader is set"

        screen.events.clear()
        screen.petNameFilterField.setValue("M")

        then: "loader gets current values of container and component parameters"

        screen.events.size() == 1
        screen.events[0].loader == 'petsDl'
        screen.events[0].loadContext.query.parameters['component_petNameFilterField'] == '(?i)%M%'
        screen.events[0].loadContext.query.parameters['container_ownersDc'] == screen.ownersDc.getItem()
    }
}
