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

package fragment

import fragment.screen.AttachListenerTestFragment
import fragment.screen.FragmentControllerTestScreen
import fragment.screen.FragmentXmlTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Fragment
import io.jmix.ui.screen.ScreenFragment
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FragmentAttachTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["fragment"])
    }

    def "open screen with declarative fragment"() {
        showTestMainScreen()

        when:

        def screen = screens.create(FragmentXmlTestScreen)
        screen.show()

        then:

        def fragment = screen.getWindow().getComponent(0) as Fragment
        fragment != null
        def controller = fragment.frameOwner as AttachListenerTestFragment
        controller != null

        controller.eventLog == [ScreenFragment.InitEvent, ScreenFragment.AfterInitEvent, ScreenFragment.AttachEvent]
    }

    def "open screen with programmatically added fragment"() {
        showTestMainScreen()

        when:

        def screen = screens.create(FragmentControllerTestScreen)
        screen.show()

        then:

        def fragment = screen.getWindow().getComponent(0) as Fragment
        fragment != null
        def controller = fragment.frameOwner as AttachListenerTestFragment
        controller != null

        controller.eventLog == [ScreenFragment.InitEvent, ScreenFragment.AfterInitEvent, ScreenFragment.AttachEvent]
    }
}
