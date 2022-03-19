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

package screen_settings

import io.jmix.ui.settings.component.TableSettings
import io.jmix.ui.settings.ScreenSettings
import test_support.UiDataTestSpecification

class ScreenSettingsTest extends UiDataTestSpecification {

    def "ScreenSettings save and load primitives"() {
        setup: "Put primitives to the ScreenSettings and save it"
        def compId = "compId"

        def screenSettings = loadScreenSettings()
        screenSettings
                .put(compId, "string", "string")
                .put(compId, "int", 1)
                .put(compId, "long", 1l)
                .put(compId, "double", 1d)
                .put(compId, "boolean", true)

        saveSettings(screenSettings)

        when: "Load ScreenSettings"
        screenSettings = loadScreenSettings()

        then: "Values should be the same"
        screenSettings.getString(compId, "string").get() == "string"
        screenSettings.getInteger(compId, "int").get() == 1
        screenSettings.getLong(compId, "long").get() == 1l
        screenSettings.getDouble(compId, "double").get() == 1d
        screenSettings.getBoolean(compId, "boolean").get()
    }

    def "ScreenSettings save and load component settings class"() {
        setup: "Put component settings to ScreenSettings and save it"
        def screenSettings = loadScreenSettings()

        TableSettings settings = new TableSettings()
        settings.setId("table")
        settings.textSelection = true

        screenSettings.put(settings)

        saveSettings(screenSettings)

        when: "Load ScreenSettings"
        screenSettings = applicationContext.getBean(ScreenSettings, "screenId")

        then: "Component settings should be the same"
        def settingsOpt = screenSettings.getSettings("table", TableSettings)

        settingsOpt.get().textSelection
    }

    def "ScreenSettings remove component settings"() {
        def primId = 'primitiveId'
        def compId = 'componentId'

        setup: "Save primitive and component settings"
        def screenSettings = loadScreenSettings()
        screenSettings.put(primId, "string", "string")

        def compSettings = new TableSettings()
        compSettings.id = compId
        screenSettings.put(compSettings)

        saveSettings(screenSettings)

        when: "Remove settings"
        screenSettings = loadScreenSettings()
        def primitiveOpt = screenSettings.getString(primId, "string")
        assert primitiveOpt.get() == "string"

        def componentOpt = screenSettings.getSettings(compId, TableSettings)
        assert componentOpt.get().id == compId

        screenSettings.remove(primId)
        screenSettings.remove(compId)

        saveSettings(screenSettings)

        then: "Settings of primitiveId should not exist"
        def screenSettings1 = loadScreenSettings()

        !screenSettings1.getString(primId, "string").present
        !screenSettings1.getSettings(compId, TableSettings).present
    }

    def "ScreenSettings remove component property settings"() {
        def primId = 'primitiveId'

        setup: "Save primitive and component settings"
        def screenSettings = loadScreenSettings()
        screenSettings.put(primId, "string", "string")

        saveSettings(screenSettings)

        when: "Remove property"
        screenSettings = loadScreenSettings()

        screenSettings.remove(primId, "string")

        saveSettings(screenSettings)

        then: "Property should not exist"
        !loadScreenSettings()
                .getString(primId, "string")
                .present
    }

    ScreenSettings loadScreenSettings() {
        applicationContext.getBean(ScreenSettings, "screenId")
    }

    def saveSettings(ScreenSettings screenSettings) {
        settingsManager.saveSettings(Collections.emptyList(), screenSettings)
    }
}
