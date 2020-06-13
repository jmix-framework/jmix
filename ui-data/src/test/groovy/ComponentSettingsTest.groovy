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

import io.jmix.ui.component.GroupBoxLayout
import io.jmix.ui.component.ResizableTextArea
import io.jmix.ui.component.SizeUnit
import io.jmix.ui.component.SplitPanel
import io.jmix.ui.component.VBoxLayout
import io.jmix.ui.settings.component.GroupBoxSettings
import io.jmix.ui.settings.component.ResizableTextAreaSettings
import io.jmix.ui.settings.component.SettingsWrapperImpl
import io.jmix.ui.settings.component.SplitPanelSettings
import io.jmix.ui.settings.component.binder.GroupBoxSettingsBinder
import io.jmix.ui.settings.component.binder.ResizableTextAreaSettingsBinder
import io.jmix.ui.settings.component.binder.SplitPanelSettingsBinder

import javax.inject.Inject

class ComponentSettingsTest extends UiDataTestSpec {

    @Inject
    GroupBoxSettingsBinder groupBoxBinder

    @Inject
    SplitPanelSettingsBinder splitBinder

    @Inject
    ResizableTextAreaSettingsBinder resTextAreaBinder

    def "GroupBox settings test"() {
        given: "Creates GroupBox"
        def groupBox = uiComponents.create(GroupBoxLayout)
        groupBox.setId("groupBoxId")

        when: "collapse GroupBox and save its state to the ScreenSettings"
        groupBox.setCollapsable(true)
        groupBox.setExpanded(false) // true default value

        settingsManager.saveSettings([groupBox], settings)

        and: "reload ScreenSettings like Screen is closed and Opened again"
        reloadScreenSettings()
        groupBox.setExpanded(true) // set default value

        then: "apply saved settings to the GroupBox"
        Optional<GroupBoxSettings> settingsOpt = settings.getSettings("groupBoxId", GroupBoxSettings)
        settingsOpt.isPresent()

        groupBoxBinder.applySettings(groupBox, new SettingsWrapperImpl(settingsOpt.get()))

        !groupBox.isExpanded()
    }

    def "SplitPanel settings test"() {
        given: "Creates SplitPanel"
        def split = uiComponents.create(SplitPanel)
        split.setId("splitId")
        split.add(uiComponents.create(VBoxLayout.NAME))
        split.add(uiComponents.create(VBoxLayout.NAME))

        def defaultPos = (int) split.getSplitPosition()
        def defaultUnit = split.getSplitPositionSizeUnit()

        when: "set position to the SplitPanel and save its state to the ScreenSettings"
        split.setSplitPosition(defaultPos + 10, SizeUnit.PIXELS) // default position is 50.0 and unit is %

        settingsManager.saveSettings([split], settings)

        and: "reload ScreenSettings like Screen is closed and Opened again"
        reloadScreenSettings()
        split.setSplitPosition(defaultPos, defaultUnit) // set default value

        then: "apply saved settings to the SplitPanel"
        Optional<SplitPanelSettings> settingsOpt = settings.getSettings("splitId", SplitPanelSettings)
        settingsOpt.isPresent()

        splitBinder.applySettings(split, new SettingsWrapperImpl(settingsOpt.get()))

        def pos = (int) split.getSplitPosition()
        def unit = split.getSplitPositionSizeUnit()

        pos == defaultPos + 10
        unit == SizeUnit.PIXELS
    }

    def "ResizableTextArea settings test"() {
        given: "Creates ResizableTextArea"
        def resTextArea = uiComponents.create(ResizableTextArea)
        resTextArea.setId("resTextAreaId")

        def defWidth = resTextArea.getWidth()
        def defWidthUnit = resTextArea.getWidthSizeUnit()

        def defHeight = resTextArea.getHeight()
        def defHeightUnit = resTextArea.getHeightSizeUnit()

        when: "resize width and height of the ResizableTextArea and save its state to the ScreenSettings"
        resTextArea.setWidth((defHeight + 11f) + SizeUnit.PERCENTAGE.toString())
        resTextArea.setHeight((defHeight + 11f) + SizeUnit.PERCENTAGE.toString())

        settingsManager.saveSettings([resTextArea], settings)

        and: "reload ScreenSettings like Screen is closed and Opened again"
        reloadScreenSettings()
        resTextArea.setWidth(defWidth + defWidthUnit.toString()) // set default value
        resTextArea.setHeight(defHeight + defHeightUnit.toString()) // set default value

        then: "apply saved settings to the ResizableTextArea"
        Optional<ResizableTextAreaSettings> settingsOpt = settings.getSettings("resTextAreaId", ResizableTextAreaSettings)
        settingsOpt.isPresent()

        resTextAreaBinder.applySettings(resTextArea, new SettingsWrapperImpl(settingsOpt.get()))

        (float) resTextArea.getHeight() == defHeight + 11f
        resTextArea.getHeightSizeUnit() == SizeUnit.PERCENTAGE

        (float) resTextArea.getWidth() == defWidth + 11f
        resTextArea.getWidthSizeUnit() == SizeUnit.PERCENTAGE
    }
}
