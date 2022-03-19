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

import io.jmix.ui.component.Label
import io.jmix.ui.screen.Screen
import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

@UiController('UiEventTestScreen')
@UiDescriptor('ui-event-test-screen.xml')
class UiEventTestScreen extends Screen {

    @Autowired
    Label<String> receiverLab

    @EventListener
    void onEvent(TestUiEvent event) {
        receiverLab.setValue(event.getMsg())
    }
}