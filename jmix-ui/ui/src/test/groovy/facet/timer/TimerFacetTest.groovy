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

package facet.timer

import facet.timer.screen.NestedTimerFragmentTestScreen
import facet.timer.screen.TimerFacetTestFragment
import facet.timer.screen.TimerFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Timer
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.widget.JmixTimer
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class TimerFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.timer'])
    }

    def "open screen with Timer facet"() {
        showTestMainScreen()

        when:

        def screen = screens.create(TimerFacetTestScreen)
        screen.show()

        then:

        screen.window.getFacet('testTimer') instanceof Timer
        screen.timer != null
        screen.timer.repeating == true
        screen.timer.delay == 2000
        screen.window.facets.count() == 1

        when:

        def impl = screen.timer.timerImpl as JmixTimer
        impl.actionListeners.get(0).accept(impl)
        impl.stopListeners.get(0).accept(impl)

        then:

        screen.ticksCounter == 1
        screen.stopped == true
    }

    def "open fragment with Timer facet"() {
        showTestMainScreen()

        when:

        def screen = screens.create(NestedTimerFragmentTestScreen)
        screen.show()

        then:

        screen.testFragment.getFacet('testTimer') instanceof Timer
        def fragmentWithTimer = screen.testFragment.frameOwner as TimerFacetTestFragment
        fragmentWithTimer.timer != null

        when:

        def timer = fragmentWithTimer.timer
        def impl = timer.timerImpl as JmixTimer
        impl.actionListeners.get(0).accept(impl)
        impl.stopListeners.get(0).accept(impl)

        then:

        fragmentWithTimer.ticksCounter == 1
        fragmentWithTimer.stopped == true
    }
}
