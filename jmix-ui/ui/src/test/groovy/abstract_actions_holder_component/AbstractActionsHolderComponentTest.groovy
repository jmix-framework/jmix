/*
 * Copyright 2021 Haulmont.
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

package abstract_actions_holder_component

import abstract_actions_holder_component.screen.WaahcTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.BaseAction
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class AbstractActionsHolderComponentTest extends ScreenSpecification {

    void setup() {
        exportScreensPackages(['abstract_actions_holder_component.screen'])
    }

    def "Add a new action to the table when having updatable captions of actions"() {
        showTestMainScreen()

        def waahcScreen = screens.create(WaahcTestScreen)
        waahcScreen.show()

        def table = waahcScreen.table
        def actionsCount = table.actions.size();

        def actionToAdd = new BaseAction("hello")
                .withCaption("Some caption")

        when: 'Some of action has update its caption and new action added to the table'
        waahcScreen.test.setCaption("Some caption1")
        table.addAction(actionToAdd)

        then: 'No exception must be thrown'
        noExceptionThrown()

        and: 'Count of actions should be incremented'
        waahcScreen.table.actions.size() == actionsCount + 1
    }
}
