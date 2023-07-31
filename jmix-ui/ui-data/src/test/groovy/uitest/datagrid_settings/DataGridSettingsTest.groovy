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

package uitest.datagrid_settings

import io.jmix.ui.component.DataGrid
import io.jmix.ui.settings.component.DataGridSettings
import test_support.UiDataTestSpecification
import uitest.datagrid_settings.screen.DataGridSettingsTestScreen

class DataGridSettingsTest extends UiDataTestSpecification {

    @Override
    void setup() {
        exportScreensPackages(["uitest.datagrid_settings.screen"])
    }

    def "Save DataGrid settings test"() {
        showTestMainScreen()

        when: "Open and close screen"
        def screen = createAndShow(DataGridSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "DataGrid settings should be saved"
        screen.facet.settings
                .getSettings(screen.projectsDataGrid.id, DataGridSettings)
                .present

        when: "Change settings of DataGrid"
        screen = createAndShow(DataGridSettingsTestScreen)

        def budgetColumn = screen.getColumn("budget")
        screen.projectsDataGrid.removeColumn(budgetColumn)
        screen.projectsDataGrid.addColumn(budgetColumn)
        screen.getColumn("active").collapsed = true
        screen.projectsDataGrid.sort("name", DataGrid.SortDirection.ASCENDING)

        screen.closeWithDefaultAction()

        then: "DataGrid settings should be saved"
        def reopenedScreen = createAndShow(DataGridSettingsTestScreen)
        def settingsOpt = reopenedScreen.facet.settings
                .getSettings(screen.projectsDataGrid.id, DataGridSettings)

        settingsOpt.get().getSortedColumnMap().get("name") == DataGrid.SortDirection.ASCENDING
        settingsOpt.get().columns.get(4).id == "budget"
        settingsOpt.get().columns.get(3).collapsed
    }

    def "Apply DataGrid settings test"() {
        showTestMainScreen()

        setup: "Change settings of DataGrid"
        def screen = createAndShow(DataGridSettingsTestScreen)

        def budgetColumn = screen.getColumn("budget")
        screen.projectsDataGrid.removeColumn(budgetColumn)
        screen.projectsDataGrid.addColumn(budgetColumn)
        screen.projectsDataGrid.sort("name", DataGrid.SortDirection.ASCENDING)
        screen.getColumn("active").collapsed = true

        screen.closeWithDefaultAction()

        when: "Open reopen screen"
        screen = createAndShow(DataGridSettingsTestScreen)

        then: "DataGrid settings should be saved"
        screen.projectsDataGrid.sortOrder.get(0).columnId == "name"
        screen.projectsDataGrid.sortOrder.get(0).direction == DataGrid.SortDirection.ASCENDING
        screen.getColumn("active").collapsed

        screen.projectsDataGrid.getColumns().get(4).id == "budget"
    }
}
