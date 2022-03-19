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

package uitest.component_settings

import io.jmix.ui.component.SizeUnit
import io.jmix.ui.settings.component.GroupBoxSettings
import io.jmix.ui.settings.component.ResizableTextAreaSettings
import io.jmix.ui.settings.component.SplitPanelSettings
import test_support.UiDataTestSpecification
import uitest.component_settings.screen.ComponentSettingsTestScreen

class ComponentSettingsTest extends UiDataTestSpecification {

    void setup() {
        exportScreensPackages(["uitest.component_settings.screen"])
    }

    def "GroupBox save settings test"() {
        showTestMainScreen()

        when: "Open and close screen with GroupBox"
        def screen = createAndShow(ComponentSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of GroupBox should be saved"
        def settingsOpt = screen.facet.settings
                .getSettings("groupBox", GroupBoxSettings.class)

        settingsOpt.get().expanded

        when: "Open screen again, collapse GroupBox and close screen"
        screen = createAndShow(ComponentSettingsTestScreen)

        screen.groupBox.expanded = false

        screen.closeWithDefaultAction()

        then: "Collapse state should be saved"
        def settingsOpt1 = screen.facet.settings
                .getSettings("groupBox", GroupBoxSettings.class)

        !settingsOpt1.get().expanded
    }

    def "GroupBox apply settings test"() {
        showTestMainScreen()

        when: "Open screen with GroupBox, change collapse state and reopen screen"
        def screen = createAndShow(ComponentSettingsTestScreen)

        screen.groupBox.expanded = false // default value is true

        screen.closeWithDefaultAction()

        screen = createAndShow(ComponentSettingsTestScreen)

        then: "GroupBox should be collapsed"
        !screen.groupBox.expanded
    }

    def "SplitPanel save settings test"() {
        showTestMainScreen()

        when: "Open and close screen with SplitPanel"
        def screen = createAndShow(ComponentSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "SplitPanel settings should be saved"
        def settingsOpt = screen.facet.settings
                .getSettings("splitPanel", SplitPanelSettings.class)

        settingsOpt.get().positionUnit == SizeUnit.PERCENTAGE.symbol
        settingsOpt.get().positionValue == 50f

        when: "Open screen again, change splitter position and close screen"
        screen = createAndShow(ComponentSettingsTestScreen)

        screen.splitPanel.setSplitPosition(99, SizeUnit.PERCENTAGE)

        screen.closeWithDefaultAction()

        then: "Splitter position should be saved"
        def settingsOpt1 = screen.facet.settings
                .getSettings("splitPanel", SplitPanelSettings.class)

        settingsOpt1.get().positionValue == 99f
        settingsOpt1.get().positionUnit == SizeUnit.PERCENTAGE.symbol
    }

    def "SplitPanel apply settings test"() {
        showTestMainScreen()

        when: "Open screen with SplitPanel, change position and reopen screen"
        def screen = createAndShow(ComponentSettingsTestScreen)

        screen.splitPanel.setSplitPosition(1, SizeUnit.PERCENTAGE)

        screen.closeWithDefaultAction()

        screen = createAndShow(ComponentSettingsTestScreen)

        then: "SplitPanel should have saved position"
        screen.splitPanel.splitPosition == 1f
        screen.splitPanel.splitPositionSizeUnit == SizeUnit.PERCENTAGE
    }

    def "ResizableTextArea save settings test"() {
        showTestMainScreen()

        when: "Open and close screen with ResizableTextArea"
        def screen = createAndShow(ComponentSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of ResizableTextArea should be saved"
        def settingsOpt = screen.facet.settings
                .getSettings("rta", ResizableTextAreaSettings)

        settingsOpt.get().height == "-1.0px"
        settingsOpt.get().width == "-1.0px"

        when: "Open screen again, change width and height, close screen"
        screen = createAndShow(ComponentSettingsTestScreen)

        screen.rta.setWidth(1f + SizeUnit.PERCENTAGE.toString())
        screen.rta.setHeight(1f + SizeUnit.PERCENTAGE.toString())

        screen.closeWithDefaultAction()

        then: "Width and height of ResizableTextArea should be saved"
        def settingsOpt1 = screen.facet.settings
                .getSettings("rta", ResizableTextAreaSettings)

        settingsOpt1.get().width == 1f + SizeUnit.PERCENTAGE.toString()
        settingsOpt1.get().height == 1f + SizeUnit.PERCENTAGE.toString()
    }

    def "ResizableTextArea apply settings test"() {
        showTestMainScreen()

        when: "Open screen with ResizableTextArea, change width and height, reopen screen"
        def screen = createAndShow(ComponentSettingsTestScreen)

        screen.rta.setWidth(99f + SizeUnit.PERCENTAGE.toString())
        screen.rta.setHeight(99f + SizeUnit.PERCENTAGE.toString())

        screen.closeWithDefaultAction()

        screen = createAndShow(ComponentSettingsTestScreen)

        then: "ResizableTextArea should have saved width and height"
        screen.rta.width == 99f
        screen.rta.widthSizeUnit == SizeUnit.PERCENTAGE

        screen.rta.height == 99f
        screen.rta.heightSizeUnit == SizeUnit.PERCENTAGE
    }
}
