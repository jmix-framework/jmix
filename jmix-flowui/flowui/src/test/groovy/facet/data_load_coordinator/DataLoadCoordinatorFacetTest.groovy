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
import facet.data_load_coordinator.screen.DlcManualNoParamTestScreen
import facet.data_load_coordinator.screen.DlcManualTestScreen
import io.jmix.core.Metadata
import io.jmix.flowui.facet.DataLoadCoordinator
import io.jmix.flowui.view.UiControllerUtils
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
        registerScreenBasePackages("facet.data_load_coordinator")
    }

    @Unroll
    def "manual configuration"() {
        when: "show screen"

        def screen = openScreen(screenClass)
        def screenFacets = UiControllerUtils.getViewFacets(screen);

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

    def "auto configuration"() {
        when: "show screen"

        def screen = openScreen(DlcAutoTestScreen)
        def screenFacets = UiControllerUtils.getViewFacets(screen);

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
    }
}
