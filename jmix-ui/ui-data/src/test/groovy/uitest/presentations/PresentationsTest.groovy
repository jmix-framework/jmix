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

package uitest.presentations

import io.jmix.core.DataManager
import io.jmix.core.security.CurrentAuthentication
import io.jmix.ui.component.ComponentsHelper
import io.jmix.ui.component.GroupTable
import io.jmix.ui.presentation.model.TablePresentation
import io.jmix.ui.settings.ComponentSettingsRegistry
import io.jmix.ui.settings.SettingsHelper
import io.jmix.ui.settings.component.GroupTableSettings
import io.jmix.ui.settings.component.TableSettings.ColumnSettings
import io.jmix.uidata.entity.UiTablePresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import test_support.UiDataTestSpecification
import uitest.presentations.screen.PresentationsTestScreen
import uitest.presentations.screen.TextSelectionTestScreen

class PresentationsTest extends UiDataTestSpecification {

    @Autowired
    ComponentSettingsRegistry registry

    @Autowired
    CurrentAuthentication authentication

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    void setup() {
        exportScreensPackages(["uitest.presentations.screen"])
    }

    void cleanup() {
        jdbcTemplate.execute("delete from UI_TABLE_PRESENTATION")
    }

    def "apply default presentation"() {
        showTestMainScreen()

        def screen = (PresentationsTestScreen) screens.create(PresentationsTestScreen)
        screen.show()

        when: "Save default presentation"

        saveDefaultPresentation(screen.groupTable)
        screen.closeWithDefaultAction()

        then: "After screen reopening default presentation should be applied"

        def screen1 = (PresentationsTestScreen) screens.create(PresentationsTestScreen)
        screen1.show()

        def settingsBinder = registry.getSettingsBinder(screen1.groupTable.class)
        def settings = (GroupTableSettings) settingsBinder.getSettings(screen1.groupTable)

        def budget = getColumnSettings(settings, "budget")
        budget.width == 100

        settings.groupProperties.contains("name")
    }

    def "reset presentation"() {
        showTestMainScreen()

        def screen = (PresentationsTestScreen) screens.create(PresentationsTestScreen)
        screen.show()

        when: "Save default presentation"

        def defaultSettings = (GroupTableSettings) screen.groupTable.defaultSettings

        saveDefaultPresentation(screen.groupTable)
        screen.closeWithDefaultAction()

        then: """
              Default presentation should be applied after screen reopening.
              And after presentation reset, table should have initial state.
              """

        def screen1 = (PresentationsTestScreen) screens.create(PresentationsTestScreen)
        screen1.show()

        def settingsBinder = registry.getSettingsBinder(screen1.groupTable.class)
        def presentationSettings = (GroupTableSettings) settingsBinder.getSettings(screen1.groupTable)

        defaultSettings.groupProperties != presentationSettings.groupProperties
        def defaultBudget = getColumnSettings(defaultSettings, "budget")
        def presentationBudget = getColumnSettings(presentationSettings, "budget")
        defaultBudget.width != presentationBudget.width

        screen1.groupTable.resetPresentation()

        def settings = (GroupTableSettings) settingsBinder.getSettings(screen1.groupTable)
        defaultSettings.groupProperties == settings.groupProperties
        defaultBudget.width == getColumnSettings(settings, "budget").width
    }

    def "load components"() {
        showTestMainScreen()

        when: "Open screen with custom set of components in the presentations facet"
        def screen = (PresentationsTestScreen) screens.create(PresentationsTestScreen)
        screen.show()

        then: "Components should be loaded from the descriptor"
        screen.componentsPresentations.components.size() == 1
        screen.componentsPresentations.components.contains(screen.groupTable)
    }

    def "textSelectionEnabled should be saved only when Presentations enabled for the table"() {
        showTestMainScreen()

        when: "Open screen with ScreenSettings and Presentations facets"
        def screen = (TextSelectionTestScreen) screens.create(TextSelectionTestScreen)
        screen.show()

        then: "All tables should have textSelectionEnabled = true"
        screen.tableAppliedTextSelection.textSelectionEnabled
        screen.tableNotAppliedTextSelection.textSelectionEnabled

        when: """
              Programmatically disable textSelectionEnabled and close screen to save
              their state in UiSetting.
              """

        screen.tableAppliedTextSelection.textSelectionEnabled = false
        screen.tableNotAppliedTextSelection.textSelectionEnabled = false

        screen.closeWithDefaultAction()

        screen = (TextSelectionTestScreen) screens.create(TextSelectionTestScreen)
        screen.show()

        then: """
              After reopening screen table without Presentations should have enabled
              text selection.
              Table with Presentations should have disabled text selection.
              """

        screen.tableNotAppliedTextSelection.textSelectionEnabled
        !screen.tableAppliedTextSelection.textSelectionEnabled
    }

    protected ColumnSettings getColumnSettings(GroupTableSettings settings, String id) {
        return settings.columns.stream()
                .filter({ it.id == id })
                .findFirst()
                .get()
    }

    protected TablePresentation saveDefaultPresentation(GroupTable groupTable) {
        groupTable.groupByColumns("name")
        groupTable.getColumn("budget").setWidth(100)

        def binder = registry.getSettingsBinder(groupTable.class)
        def settings = binder.getSettings(groupTable)

        UiTablePresentation presentation = dataManager.create(UiTablePresentation)
        presentation.setSettings(SettingsHelper.toSettingsString(settings))
        presentation.setComponentId(ComponentsHelper.getComponentPath(groupTable))
        presentation.setIsDefault(true)
        presentation.setUsername(authentication.user.username)
        presentation.setName("test_pres")

        dataManager.save(presentation)
    }
}
