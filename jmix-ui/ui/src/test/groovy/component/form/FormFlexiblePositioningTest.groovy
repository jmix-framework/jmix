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

package component.form

import component.form.screen.FormFlexiblePositioningTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.TextField
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FormFlexiblePositioningTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.form"])
    }

    def "open a screen with a complex Form layout"() {
        showTestMainScreen()

        def formScreen = screens.create(FormFlexiblePositioningTestScreen)

        when:
        formScreen.show()

        then:
        noExceptionThrown()
    }

    def "get a component by coords"(int col, int row, String expectedId) {
        showTestMainScreen()

        def formScreen = screens.create(FormFlexiblePositioningTestScreen)
        formScreen.show()

        when:
        def component = formScreen.getFormComponent(col, row)

        then:
        expectedId == component.getId()

        where:
        col | row | expectedId
        0   | 0   | "component00"
        1   | 0   | "component00"

        0   | 1   | "component01"
        0   | 2   | "component01"
        0   | 3   | "component01"

        1   | 1   | "component11"
        2   | 1   | "component11"

        1   | 2   | "component12"

        1   | 3   | "component13"

        2   | 0   | "component20"

        2   | 2   | "component22"
        2   | 3   | "component22"
    }

    def "trying overlap existing component"(int col, int row) {
        showTestMainScreen()

        def formScreen = screens.create(FormFlexiblePositioningTestScreen)
        formScreen.show()

        when:
        def componentToAdd = uiComponents.create(TextField.TYPE_STRING)
        formScreen.addComponentToForm(componentToAdd, col, row)

        then:
        thrown(IllegalArgumentException)

        where:
        col | row
        0   | 2
        1   | 0
    }

    def "insert a component"() {
        showTestMainScreen()

        def formScreen = screens.create(FormFlexiblePositioningTestScreen)
        formScreen.show()

        when: "add a component by col and row"
        def componentId = "component01"
        def componentToAdd = uiComponents.create(TextField.TYPE_STRING)
        componentToAdd.setId(componentId)
        formScreen.addComponentToForm(componentToAdd, 0, 1)

        def component = formScreen.getFormComponent(0, 1)

        then:
        componentId == component.getId()

        when: "add a component by col"
        componentId = "component14"
        componentToAdd = uiComponents.create(TextField.TYPE_STRING)
        componentToAdd.setId(componentId)
        formScreen.addComponentToForm(componentToAdd, 1, null)

        component = formScreen.getFormComponent(1, 4)

        then:
        componentId == component.getId()
    }
}
