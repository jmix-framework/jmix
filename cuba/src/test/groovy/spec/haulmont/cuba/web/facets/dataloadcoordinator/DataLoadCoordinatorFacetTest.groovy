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

package spec.haulmont.cuba.web.facets.dataloadcoordinator

import com.haulmont.cuba.core.model.OwnerCategory
import io.jmix.ui.component.DataLoadCoordinator
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.facets.dataloadcoordinator.screens.DlcManualScreen
import spec.haulmont.cuba.web.facets.dataloadcoordinator.screens.DlcManualWithLoadDataBeforeShowScreen
import spec.haulmont.cuba.web.facets.dataloadcoordinator.screens.DlcManualNoParamScreen
import spock.lang.Unroll

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class DataLoadCoordinatorFacetTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.facets.dataloadcoordinator.screens'])
    }

    @Unroll
    def "manual configuration"() {
        showMainScreen()

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

        screenClass << [DlcManualScreen, DlcManualNoParamScreen, DlcManualWithLoadDataBeforeShowScreen]
    }
}
