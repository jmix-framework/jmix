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

package screen

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.FrameOwner
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.screen.Screen
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class AfterShowCloseScreenEventTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['test_support.entity.sales.screen'])
    }

    def 'AfterShowEvent is fired'() {
        showTestMainScreen()

        when: 'Screen is opened'
        def eventIsFired = false

        Screen screen = screens.create('test_Order.browse', OpenMode.NEW_TAB)
        screen.addAfterShowListener({ e -> eventIsFired = true })
        screen.show()

        then: 'AfterShowEvent is fired'
        eventIsFired
    }

    def 'AfterCloseEvent is fired'() {
        showTestMainScreen()

        when: 'Screen is opened and then closed'
        def eventIsFired = false

        def screen = screens.create('test_Order.browse', OpenMode.NEW_TAB)
        screen.addAfterCloseListener({ e -> eventIsFired = true })
        screen.show()
        screen.close(FrameOwner.WINDOW_CLOSE_ACTION)

        then: 'AfterCloseEvent is fired'
        eventIsFired
    }
}
