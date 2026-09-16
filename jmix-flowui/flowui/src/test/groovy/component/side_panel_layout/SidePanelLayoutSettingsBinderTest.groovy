/*
 * Copyright 2026 Haulmont.
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

package component.side_panel_layout

import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout
import io.jmix.flowui.facet.settings.component.SidePanelLayoutSettings
import io.jmix.flowui.facet.settings.component.binder.SidePanelLayoutSettingsBinder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SidePanelLayoutSettingsBinderTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    SidePanelLayoutSettingsBinder binder

    def "getSettings reads the current sizes when resizable"() {
        given:
        def layout = uiComponents.create(SidePanelLayout)
        layout.sidePanelResizable = true
        layout.sidePanelHorizontalSize = "320px"

        when:
        def settings = binder.getSettings(layout)

        then:
        settings.horizontalSize == "320px"
    }

    def "applySettings restores the stored size"() {
        given:
        def layout = uiComponents.create(SidePanelLayout)
        layout.sidePanelResizable = true
        def settings = new SidePanelLayoutSettings()
        settings.horizontalSize = "400px"

        when:
        binder.applySettings(layout, settings)

        then:
        layout.sidePanelHorizontalSize == "400px"
    }

    def "saveSettings detects a change and writes it back"() {
        given:
        def layout = uiComponents.create(SidePanelLayout)
        layout.sidePanelResizable = true
        layout.sidePanelHorizontalSize = "300px"
        def settings = new SidePanelLayoutSettings()

        when:
        def changed = binder.saveSettings(layout, settings)

        then:
        changed
        settings.horizontalSize == "300px"
    }

    def "binder is a no-op when the panel is not resizable"() {
        given:
        def layout = uiComponents.create(SidePanelLayout)
        layout.sidePanelResizable = false
        layout.sidePanelHorizontalSize = "300px"
        def settings = new SidePanelLayoutSettings()
        settings.horizontalSize = "999px"

        when:
        def changed = binder.saveSettings(layout, settings)
        binder.applySettings(layout, settings)

        then:
        !changed
        layout.sidePanelHorizontalSize == "300px"
    }
}
