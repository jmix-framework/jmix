/*
 * Copyright 2022 Haulmont.
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

import io.jmix.flowui.UiEventPublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import ui_events.screen.UiEventsTestScreen

@SpringBootTest
class UiEventsTest extends FlowuiTestSpecification {

    @Autowired
    UiEventPublisher uiEventPublisher

    void setup() {
        registerScreenBasePackages("ui_events.screen")
    }

    def "screen receives an event"() {
        def screen = openScreen(UiEventsTestScreen)

        when: "Fire application event"
        def event = new TestUiEvent(this, "eventMessage")
        uiEventPublisher.publishEvent(event)

        then: "Screen's application listener receives the event"

        screen.eventMessage == event.getMessage()
    }
}
