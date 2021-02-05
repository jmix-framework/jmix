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

package component.web_components_helper

import component.web_components_helper.screen.WebComponentsHelperTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ComponentsHelper
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class WebComponentsHelperTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.web_components_helper"])
    }

    def 'Find action in frame'() {
        showTestMainScreen()

        def screen = screens.create(WebComponentsHelperTestScreen)
        screen.show()

        def frame = screen.getWindow()

        when: 'Finding screen action'

        def screenAction = ComponentsHelper.findAction(frame, 'screenAction')

        then: 'Action found'

        screenAction

        when: 'Finding Table action'

        def tableAction = ComponentsHelper.findAction(frame, 'table.createAction')

        then: 'Action found'

        tableAction

        when: 'Finding EntityComboBox action'

        def lpfAction = ComponentsHelper.findAction(frame, 'entityComboBox.lpfAction')

        then: 'Action found'

        lpfAction
    }
}
