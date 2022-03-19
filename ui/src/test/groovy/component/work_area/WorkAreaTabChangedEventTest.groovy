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

package component.work_area

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.FrameOwner
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.bean.TestWebBean

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class WorkAreaTabChangedEventTest extends ScreenSpecification {

    @Autowired
    TestWebBean testWebBean

    @Override
    void setup() {
        exportScreensPackages(['test_support.entity.sales.screen', 'browser_editor_interaction'])
    }

    def 'WorkAreaTabChangedEvent is fired when screen is opened or closed'() {
        showTestMainScreen()

        when: 'Screen is opened'
        def screen = screens.create('test_Order.browse', OpenMode.NEW_TAB)
        screen.show()

        then: 'WorkAreaTabChangedEvent is fired'
        testWebBean.workAreaTabChangedEventHandled.get()

        when: 'Screen is closed then'
        // reset flag
        testWebBean.workAreaTabChangedEventHandled.set(false)
        // one more screen required to trigger tab switching
        screens.create('test_Customer.browse', OpenMode.NEW_TAB)
                .show()
        screen.close(FrameOwner.WINDOW_CLOSE_ACTION)

        then: 'WorkAreaTabChangedEvent is fired'
        testWebBean.workAreaTabChangedEventHandled.get()
    }
}
