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

package ui_events

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.UiEventPublisher
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class UiEventTest extends ScreenSpecification {

    @Autowired
    UiEventPublisher uiEvents

    @Override
    void setup() {
        exportScreensPackages(['ui_events'])
    }

    def "screen receives an event"() {
        showTestMainScreen()

        def screen = screens.create(UiEventTestScreen)
        screen.show()

        when:
        uiEvents.publishEvent(new TestUiEvent(this, 'hello'))

        then:
        screen.receiverLab.value == 'hello'
    }
}
