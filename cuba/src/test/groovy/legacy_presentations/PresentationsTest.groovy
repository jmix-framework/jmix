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

package legacy_presentations

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.gui.components.GroupTable
import com.haulmont.cuba.settings.component.CubaGroupTableSettings
import io.jmix.core.DataManager
import io.jmix.core.security.CurrentAuthentication
import io.jmix.ui.component.ComponentsHelper
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.settings.ComponentSettingsRegistry
import io.jmix.ui.settings.UserSettingsTools
import io.jmix.ui.settings.component.TableSettings
import io.jmix.uidata.entity.UiTablePresentation
import legacy_presentations.screens.PresentationsLegacyTestScreen
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.web.UiScreenSpec

class PresentationsTest extends UiScreenSpec {

    @Autowired
    ComponentSettingsRegistry registry

    @Autowired
    UserSettingsTools settingsTools

    @Autowired
    CurrentAuthentication authentication

    @Autowired
    protected Persistence persistence;

    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.getDataSource())
        jdbcTemplate.execute("delete from UI_TABLE_PRESENTATION")
        jdbcTemplate.execute("delete from UI_SETTING")
    }

    def "apply default presentation"() {
        showMainScreen()

        def screen = (PresentationsLegacyTestScreen) screens.create(
                "test_PresentationsLegacyTestScreen", OpenMode.NEW_TAB)
        screen.show()

        when: """
              Save presentation and make it default. 
              Close screen to save default presentation id in settings.
              """

        def presentation = savePresentation(screen.groupTable)
        screen.groupTable.getPresentations().add(presentation)
        screen.groupTable.applyPresentationAsDefault(presentation.getId())

        screen.closeWithDefaultAction()

        then: "Default presentation should be applied after screen reopening."

        def screen1 = (PresentationsLegacyTestScreen) screens.create(
                "test_PresentationsLegacyTestScreen", OpenMode.NEW_TAB)
        screen1.show()

        def settings = (CubaGroupTableSettings) registry.getSettingsBinder(screen1.groupTable.class)
                .getSettings(screen1.groupTable)

        screen1.groupTable.getDefaultPresentationId() == presentation.getId()
        settings.groupProperties.contains("name")
        getColumnSettings(settings, "weight").width == 100
    }

    def "reset presentation"() {
        showMainScreen()

        def screen = (PresentationsLegacyTestScreen) screens.create(
                "test_PresentationsLegacyTestScreen", OpenMode.NEW_TAB)
        screen.show()

        when: """
              Save presentation and make it default. 
              Close screen to save default presentation id in settings.
              """

        def defaultSettings = (CubaGroupTableSettings) screen.groupTable.defaultSettings

        def presentation = savePresentation(screen.groupTable)
        screen.groupTable.getPresentations().add(presentation)
        screen.groupTable.applyPresentationAsDefault(presentation.getId())

        screen.closeWithDefaultAction()

        then: """
              Default presentation should be applied after screen reopening.
              And after presentation reset, table should have initial state.
              """

        def screen1 = (PresentationsLegacyTestScreen) screens.create(
                "test_PresentationsLegacyTestScreen", OpenMode.NEW_TAB)
        screen1.show()

        def settingsBinder = registry.getSettingsBinder(screen1.groupTable.class)
        def presentationSettings = (CubaGroupTableSettings) settingsBinder.getSettings(screen1.groupTable)

        // check that default presentation is applied

        defaultSettings.groupProperties != presentationSettings.groupProperties
        def defaultWeight = getColumnSettings(defaultSettings, "weight")
        def presentationWeight = getColumnSettings(presentationSettings, "weight")
        defaultWeight.width != presentationWeight.width

        screen1.groupTable.resetPresentation()

        // check that reset presentation works

        def settings = (CubaGroupTableSettings) settingsBinder.getSettings(screen1.groupTable)
        defaultSettings.groupProperties == settings.groupProperties
        defaultWeight.width == getColumnSettings(settings, "weight").width
    }

    protected TableSettings.ColumnSettings getColumnSettings(CubaGroupTableSettings settings, String id) {
        return settings.columns.stream()
                .filter({ it.id == id })
                .findFirst()
                .get()
    }

    protected UiTablePresentation savePresentation(GroupTable groupTable) {
        groupTable.groupByColumns("name")
        groupTable.getColumn("weight").setWidth(100)

        UiTablePresentation presentation = applicationContext.getBean(DataManager)
                .create(UiTablePresentation)

        def binder = registry.getSettingsBinder(groupTable.class);
        def settings = (CubaGroupTableSettings) binder.getSettings(groupTable)

        def rawSettings = """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
                <presentation textSelection=\"false\">
                 <columns sortProperty=\"\" sortAscending=\"true\">
                     %s
                 </columns>
                 <groupProperties>
                     %s
                 </groupProperties>
                </presentation>
                """

        def rawColumns = new StringBuilder();
        for (TableSettings.ColumnSettings column : settings.columns) {
            rawColumns.append("<columns id=\"").append(column.getId()).append("\" ")
            rawColumns.append("visible=\"").append(column.getVisible()).append("\" ")
            if (column.getWidth() != null) {
                rawColumns.append("width=\"").append(column.getWidth()).append("\"")
            }
            rawColumns.append("/>\n")
        }

        def rawGroupProperties = new StringBuilder()
        for (String prop : settings.groupProperties) {
            rawGroupProperties.append("<property id=\"").append(prop).append("\"/>\n")
        }

        rawSettings = String.format(rawSettings, rawColumns, rawGroupProperties)

        presentation.setSettings(rawSettings)
        presentation.setComponentId(ComponentsHelper.getComponentPath(groupTable))
        presentation.setUsername(authentication.user.username)
        presentation.setName("test_pres")

        return applicationContext.getBean(DataManager)
                .save(presentation)
    }
}
