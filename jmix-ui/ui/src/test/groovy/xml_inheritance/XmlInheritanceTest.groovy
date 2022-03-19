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

package xml_inheritance

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.HBoxLayout
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import xml_inheritance.screen.XmlInheritanceExtBaseTestScreen

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class XmlInheritanceTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["xml_inheritance.screen"])
    }

    def "ext:index in extended screen"() {
        showTestMainScreen()

        when: "Created screen that extends another one"
        def screen = (XmlInheritanceExtBaseTestScreen) screens.create(XmlInheritanceExtBaseTestScreen)
        screen.show()

        then: "Components that use ext:index should change their position"

        def hboxNew = (HBoxLayout) screen.getWindow().getComponent("hboxAddNew")
        hboxNew.getComponent(0).id == "new1"

        def hboxUp = (HBoxLayout) screen.getWindow().getComponent("hboxMoveBaseToUp")
        hboxUp.getComponent(2).id == "up1"

        def hboxDown = (HBoxLayout) screen.getWindow().getComponent("hboxMoveBaseToDown")
        hboxDown.getComponent(1).id == "down3"
    }
}
