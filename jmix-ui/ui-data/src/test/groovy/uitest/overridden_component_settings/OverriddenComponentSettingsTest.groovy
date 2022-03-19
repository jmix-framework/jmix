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

package uitest.overridden_component_settings

import test_support.UiDataTestSpecification
import test_support.custom_settings.TestResizableTextAreaSettings
import uitest.overridden_component_settings.screen.OverriddenComponentSettingsTestScreen

class OverriddenComponentSettingsTest extends UiDataTestSpecification {

    @Override
    void setup() {
        exportScreensPackages(["uitest.overridden_component_settings.screen"])
    }

    def "Save overridden component settings test"() {
        showTestMainScreen()

        def text = "Settings text"

        when: "Set value and close screen"
        def screen = createAndShow(OverriddenComponentSettingsTestScreen)
        screen.rta.setValue(text)

        screen.closeWithDefaultAction()

        then: "Settings should contain saved text"
        createAndShow(OverriddenComponentSettingsTestScreen)
                .facet.settings.getSettings("rta", TestResizableTextAreaSettings)
                .get().text == text
    }

    def "Apply overridden component settings test"() {
        showTestMainScreen()

        def text = "Settings text"

        when: "Set value and close screen"
        def screen = createAndShow(OverriddenComponentSettingsTestScreen)
        screen.rta.setValue(text)

        screen.closeWithDefaultAction()

        then: "Field should have value from settings"
        createAndShow(OverriddenComponentSettingsTestScreen)
                .rta.value == text
    }
}
