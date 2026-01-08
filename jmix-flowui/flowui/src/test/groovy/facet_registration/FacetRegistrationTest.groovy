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

package facet_registration

import facet_registration.view.FacetRegistrationFragmentHostTestView
import facet_registration.view.FacetRegistrationTestView
import io.jmix.flowui.Facets
import io.jmix.flowui.facet.Timer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = FacetRegistrationTestConfiguration)
class FacetRegistrationTest extends FlowuiTestSpecification {

    @Autowired
    Facets facets

    @Override
    void setup() {
        registerViewBasePackages("facet_registration.view")
    }

    def "Check facet replacement"() {
        when: """
              Facet replacement has the following view:
                TimerImpl <- TestFirstTimerFacet <- TestSecondTimerFacet
                TimerImpl <- TestThirdTimerFacet
              The 'TestThirdTimerFacet' has the priority.
              """
        def defaultTimer = facets.create(Timer.class)
        def firstTimerFacet = facets.create(TestFirstTimerFacet)
        def secondTimerFacet = facets.create(TestSecondTimerFacet)
        def thirdTimerFacet = facets.create(TestThirdTimerFacet)

        then: "Created facets should have types"

        defaultTimer.class == TestThirdTimerFacet
        firstTimerFacet.class == TestSecondTimerFacet
        secondTimerFacet.class == TestSecondTimerFacet
        thirdTimerFacet.class == TestThirdTimerFacet

        when: "Open view with declarative facet"
        def view = navigateToView(FacetRegistrationTestView)

        then: "Loaded facet should have default autostart true that is defined in the extended loader"
        view.timer.autostart
    }

    def "Check facet replacement for the fragment"() {
        when: """
             Facet loader and implementation replacement has the following view:
                FragmentDataLoadCoordinatorFacetLoader <- ExtFragmentDataLoadCoordinatorFacetLoader
                TimerImpl <- TestThirdTimerFacet
                The 'TestThirdTimerFacet' has the priority
             """
        def view = navigateToView(FacetRegistrationFragmentHostTestView)

        then: "Loaded facet should have hardcoded id via loader and TestThirdTimerFacet type"
        def facet = view.fragment.extFragmentDataLoadCoordinatorFacet

        facet != null
        facet.id == 'extFragmentDataLoadCoordinatorFacet'
        facet.class == ExtFragmentDataLoadCoordinatorFacet
    }
}
