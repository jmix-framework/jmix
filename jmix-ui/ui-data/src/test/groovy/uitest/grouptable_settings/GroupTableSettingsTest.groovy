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

package uitest.grouptable_settings

import io.jmix.core.DataManager
import io.jmix.core.security.CurrentAuthentication
import io.jmix.ui.component.GroupTable
import io.jmix.ui.settings.SettingsHelper
import io.jmix.ui.settings.component.GroupTableSettings
import io.jmix.ui.settings.component.TableSettings
import io.jmix.ui.settings.component.binder.GroupTableSettingsBinder
import io.jmix.ui.widget.JmixGroupTable
import io.jmix.uidata.entity.UiTablePresentation
import io.jmix.ui.settings.ScreenSettings
import org.springframework.beans.factory.annotation.Autowired
import test_support.UiDataTestSpecification
import uitest.grouptable_settings.screen.GroupTableSettingsTestScreen

class GroupTableSettingsTest extends UiDataTestSpecification {

    @Autowired
    CurrentAuthentication authentication

    @Autowired
    GroupTableSettingsBinder tableBinder

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["uitest.grouptable_settings.screen"])
    }

    def "Save GroupTable settings test"() {
        showTestMainScreen()

        when: "Open and close screen"
        def screen = createAndShow(GroupTableSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings should be saved"
        screen.facet.settings
                .getSettings(screen.projectsTable.id, TableSettings)
                .present

        when: "Make name column grouped"
        screen = createAndShow(GroupTableSettingsTestScreen)
        screen.projectsTable.groupBy(screen.getColumnId("name"))

        screen.closeWithDefaultAction()

        then: "Changes should be saved"
        def reopenedScreen = createAndShow(GroupTableSettingsTestScreen)
        def screenSettings = reopenedScreen.facet.settings

        def tableOpt = screenSettings.getSettings(reopenedScreen.projectsTable.id, GroupTableSettings)
        tableOpt.present

        tableOpt.get().groupProperties.size() == 1
        tableOpt.get().groupProperties.get(0) == "name"
    }

    def "Apply GroupTable settings test"() {
        showTestMainScreen()

        when: "Make name and active columns are grouped"
        def screen = createAndShow(GroupTableSettingsTestScreen)
        screen.projectsTable.groupByColumns("name", "active")

        screen.closeWithDefaultAction()

        then: "Changes should be applied"
        def reopenedScreen = createAndShow(GroupTableSettingsTestScreen)

        reopenedScreen.projectsTable.withUnwrapped(JmixGroupTable, { table ->
            assert table.groupProperties.size() == 2
        })
    }

    def "Apply GroupTable presentation test"() {
        showTestMainScreen()

        when: "Open screen and save presentation "
        def screen = createAndShow(GroupTableSettingsTestScreen)
        def presentation = persistTablePresentation(screen.projectsTable)

        screen.projectsTable.withUnwrapped(JmixGroupTable, { table ->
            assert table.groupProperties.size() == 0
        })

        then: "Apply presentation"
        screen.projectsTable.applyPresentation(presentation.id)

        screen.projectsTable.withUnwrapped(JmixGroupTable, { table ->
            assert table.groupProperties.size() == 2
        })
    }

    UiTablePresentation persistTablePresentation(GroupTable table) {
        UiTablePresentation presentation = metadata.create(UiTablePresentation)
        presentation.componentId = table.id
        presentation.username = authentication.user.username
        presentation.name = "testPresentation"

        def groupTableSettings = tableBinder.getSettings(table)
        groupTableSettings.setGroupProperties(["name", "active"])

        presentation.settings = SettingsHelper.toSettingsString(groupTableSettings)

        table.presentations.add(presentation)
        dataManager.save(presentation)
    }
}
