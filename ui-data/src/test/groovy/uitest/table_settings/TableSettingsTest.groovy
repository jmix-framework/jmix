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

package uitest.table_settings

import io.jmix.core.DataManager
import io.jmix.core.security.CurrentAuthentication
import io.jmix.ui.component.Table
import io.jmix.ui.settings.SettingsHelper
import io.jmix.ui.settings.component.TableSettings
import io.jmix.ui.settings.component.binder.TableSettingsBinder
import io.jmix.uidata.entity.UiTablePresentation
import org.springframework.beans.factory.annotation.Autowired
import test_support.UiDataTestSpecification
import uitest.table_settings.screen.TableSettingsTestScreen

class TableSettingsTest extends UiDataTestSpecification {

    @Autowired
    TableSettingsBinder tableBinder

    @Autowired
    CurrentAuthentication authentication

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["uitest.table_settings.screen"])
    }

    def "Save Table settings test"() {
        showTestMainScreen()

        when: "Open and close screen"
        def screen = createAndShow(TableSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings should be saved"
        screen.facet.settings
                .getSettings(screen.projectsTable.id, TableSettings)
                .present

        when: "Change columns order, width and visibility"
        screen = createAndShow(TableSettingsTestScreen)
        screen.getColumn("name").width = 99
        screen.getColumn("description").collapsed = true

        def budgetColumn = screen.getColumn("budget")
        screen.projectsTable.removeColumn(budgetColumn)
        screen.projectsTable.addColumn(budgetColumn)

        screen.closeWithDefaultAction()

        then: "Changes should be saved"
        def reopenedScreen = createAndShow(TableSettingsTestScreen)
        def screenSettings = reopenedScreen.facet.settings

        def tableOpt = screenSettings.getSettings(reopenedScreen.projectsTable.id, TableSettings)
        tableOpt.present

        def columns = tableOpt.get().columns
        for (TableSettings.ColumnSettings column : columns) {
            if (column.id == "name")
                assert column.width == 99

            if (column.id == "description")
                assert !column.visible
        }

        columns.get(columns.size() - 1).id == "budget"
    }

    def "Apply Table settings test"() {
        showTestMainScreen()

        when: "Change columns order, width and visibility"
        def screen = createAndShow(TableSettingsTestScreen)
        screen.getColumn("name").width = 99
        screen.getColumn("description").collapsed = true

        def budgetColumn = screen.getColumn("budget")
        screen.projectsTable.removeColumn(budgetColumn)
        screen.projectsTable.addColumn(budgetColumn)

        screen.closeWithDefaultAction()

        then: "Changes should be applied after reopening screen"
        def reopenedScreen = createAndShow(TableSettingsTestScreen)

        reopenedScreen.projectsTable.getColumn("description").collapsed

        reopenedScreen.projectsTable.withUnwrapped(com.vaadin.v7.ui.Table, { vTable ->
            assert vTable.getVisibleColumns()[4] == reopenedScreen.getColumnId("budget")
            assert vTable.getColumnWidth(reopenedScreen.getColumnId("name")) == 99
        })
    }

    def "Apply Table presentation test"() {
        showTestMainScreen()

        when: "Open screen and save presentation "
        def screen = createAndShow(TableSettingsTestScreen)
        def presentation = persistTablePresentation(screen.projectsTable)

        then: "Apply presentation"
        screen.projectsTable.applyPresentation(presentation.id)

        screen.getColumn("name").collapsed
        screen.projectsTable.textSelectionEnabled

        screen.projectsTable.sortInfo.ascending
        screen.projectsTable.sortInfo.propertyId == screen.getColumnId("description")
    }

    UiTablePresentation persistTablePresentation(Table table) {
        UiTablePresentation presentation = metadata.create(UiTablePresentation)
        presentation.componentId = table.id
        presentation.username = authentication.user.username
        presentation.name = "testPresentation"

        def tableSettings = tableBinder.getSettings(table)
        tableSettings.textSelection = true
        tableSettings.sortAscending = true
        tableSettings.sortProperty = "description"
        tableSettings.getColumns().get(0).visible = false

        presentation.settings = SettingsHelper.toSettingsString(tableSettings)

        table.presentations.add(presentation)
        dataManager.save(presentation)
    }
}
