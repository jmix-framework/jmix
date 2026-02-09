/*
 * Copyright 2025 Haulmont.
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


import facet.data_load_coordinator.screen.DlcFragmentHostTestView
import io.jmix.core.Metadata
import io.jmix.flowui.facet.FragmentDataLoadCoordinator
import io.jmix.flowui.fragment.FragmentUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Unroll
import test_support.entity.petclinic.OwnerCategory
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FragmentDataLoadCoordinatorFacetTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    @Override
    void setup() {
        registerViewBasePackages("facet.data_load_coordinator")
    }

    @Unroll
    def "Manual configuration for the #fragmentId"() {
        when: "Test view is shown"

        def view = navigateToView DlcFragmentHostTestView
        def fragment = view."${fragmentId}"
        def fragmentFacets = FragmentUtils.getFragmentFacets fragment

        then: "Facet is created and injected"

        fragmentFacets.getFacet('dlc') instanceof FragmentDataLoadCoordinator
        fragment.dlc != null

        and: "Main loader is triggered once"

        fragment.events.size() == 1
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters.isEmpty()

        when: "Main item is selected"

        fragment.events.clear()
        fragment.ownersDc.setItem(fragment.ownersDc.getItems()[0])

        then: "Secondary loader is triggered"

        fragment.events.size() == 1
        fragment.events[0].loader == 'petsDl'
        fragment.events[0].loadContext.query.parameters.size() == 1
        fragment.events[0].loadContext.query.parameters['owner'] == fragment.ownersDc.getItem()

        when: "entity filter field value is set"

        fragment.events.clear()
        def category = new OwnerCategory()
        fragment.categoryFilterField.setValue(category)

        then: "Main loader is triggered, its item is cleared so secondary is triggered too"

        fragment.events.size() == 2
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters['category'] == category
        fragment.events[1].loader == 'petsDl'

        when: "String filter field is set"

        fragment.events.clear()
        fragment.nameFilterField.setValue("o")

        then: "Secondary is triggered and parameter is wrapped for case-insensitive like"

        fragment.events.size() == 1
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters['name'] == '(?i)%o%'

        where:

        fragmentId << ["dlcManualFragment", "dlcManualNoParamFragment"]
    }

    @Unroll
    def "Auto configuration for the #fragmentId"() {
        when: "show screen"

        def view = navigateToView DlcFragmentHostTestView
        def fragment = view."${fragmentId}"
        def fragmentFacets = FragmentUtils.getFragmentFacets fragment

        then: "Facet is created and injected"

        fragmentFacets.getFacet('dlc') instanceof FragmentDataLoadCoordinator
        fragment.dlc != null

        and: "Main loader is triggered once"

        fragment.events.size() == 1
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters.isEmpty()

        when: "Main item is selected"

        fragment.events.clear()
        fragment.ownersDc.setItem(fragment.ownersDc.getItems()[0])

        then: "Secondary loader is triggered"

        fragment.events.size() == 1
        fragment.events[0].loader == 'petsDl'
        fragment.events[0].loadContext.query.parameters.size() == 1
        fragment.events[0].loadContext.query.parameters['container_ownersDc'] == fragment.ownersDc.getItem()

        when: "Entity filter field value is set"

        fragment.events.clear()
        def category = metadata.create(OwnerCategory)
        fragment.categoryFilterField.setValue(category)

        then: "Main loader is triggered, its item is cleared so secondary is triggered too"

        fragment.events.size() == 2
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters['component_categoryFilterField'] == category
        fragment.events[1].loader == 'petsDl'

        when: "String filter field is set"

        fragment.events.clear()
        fragment.nameFilterField.setValue("o")

        then: "Secondary is triggered and parameter is wrapped for case-insensitive like"

        fragment.events.size() == 1
        fragment.events[0].loader == 'ownersDl'
        fragment.events[0].loadContext.query.parameters['component_nameFilterField'] == '(?i)%o%'

        where:

        fragmentId << ["dlcAutoFragment", "dlcAutoProvidedParamFragment"]
    }
}
