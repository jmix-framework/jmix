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

package uitest.screen_settings_facet

import io.jmix.ui.component.Table
import io.jmix.ui.screen.Screen
import io.jmix.ui.settings.component.GroupBoxSettings
import io.jmix.ui.settings.component.GroupTableSettings
import test_support.UiDataTestSpecification
import uitest.screen_settings_facet.screen.FacetAutoExcludeTestScreen
import uitest.screen_settings_facet.screen.FacetAutoTestScreen
import uitest.screen_settings_facet.screen.FacetDelegateTestScreen
import uitest.screen_settings_facet.screen.FacetFragmentTableSettingsHostScreen
import uitest.screen_settings_facet.screen.FacetManualTestScreen

import static io.jmix.ui.component.Table.SortDirection.ASCENDING
import static io.jmix.ui.component.Table.SortDirection.DESCENDING

class ScreenSettingsFacetTest extends UiDataTestSpecification {

    @Override
    void setup() {
        exportScreensPackages(["uitest.screen_settings_facet.screen"])
    }

    def "ScreenSettingsFacet with auto mode"() {
        showTestMainScreen()

        when: "Open screen with ScreenSettingsFacet and close it"
        Screen screen = createAndShow(FacetAutoTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of GroupTable should be saved"
        screen.facet.settings
                .getSettings(screen.projectsTable.id, GroupTableSettings.class)
                .isPresent()
    }

    def "ScreenSettingsFacet with manual saving"() {
        showTestMainScreen()

        when: "Open screen with ScreenSettingsFacet and close it"
        Screen screen = createAndShow(FacetManualTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of GroupTable must not be saved"
        !screen.facet.settings
                .getSettings(screen.projectsTable.id, GroupTableSettings.class)
                .isPresent()

        when: "Add GroupTable to facet"
        // we cannot use the same instance, because screen's lifecycle events triggered only once
        screen = screens.create(FacetManualTestScreen)
        screen.facet.addComponentIds(screen.projectsTable.id)

        screen.show()
        screen.closeWithDefaultAction()

        then: "GroupTable's settings should be saved"
        screen.facet.settings
                .getSettings(screen.projectsTable.id, GroupTableSettings.class)
                .isPresent()
    }

    def "ScreenSettingsFacet with auto mode and excluded component"() {
        showTestMainScreen()

        when: "Open screen, change excluded component state"
        FacetAutoExcludeTestScreen screen = createAndShow(FacetAutoExcludeTestScreen)

        then: "Check that facet contains excluded component"
        screen.facet.excludedComponentIds.size() > 0
        screen.facet.excludedComponentIds.contains("groupBox")

        when: "Reopen screen"
        screen.closeWithDefaultAction()
        // we cannot use the same instance, because screen's lifecycle events triggered only once
        screen = createAndShow(FacetAutoExcludeTestScreen)

        then: "Settings for GroupBox shouldn't be presented"

        !screen.facet.settings
                .getSettings(screen.groupBox.id, GroupBoxSettings)
                .isPresent()

        screen.facet.settings
                .getSettings(screen.projectsTable.id, GroupTableSettings)
                .isPresent()
    }

    def "ScreenSettingsFacet lifecycle delegates save, apply, applyDataLoading"() {
        showTestMainScreen()

        when: "Open screen with ScreenSettingsFacet"
        Screen screen = createAndShow(FacetDelegateTestScreen)

        then: "ApplyDataLoading and Apply delegates should be fired"
        screen.calls == 2

        when: "Close screen"
        screen.closeWithDefaultAction()

        then: "Save settings delegate should be fired"
        screen.calls == 3
    }

    def "Table sort settings from first same type fragment are not overwritten by last fragment"() {
        showTestMainScreen()

        when: "Sort table in the first fragment and visit the last fragment"
        def screen = createAndShow(FacetFragmentTableSettingsHostScreen)
        screen.firstFragment.projectsTable.sort("name", ASCENDING)
        screen.tabSheet.setSelectedTab("lastTab")
        screen.closeWithDefaultAction()

        then: "Only first fragment table should restore the saved sort after reopening"
        def reopenedScreen = createAndShow(FacetFragmentTableSettingsHostScreen)
        assertTableSort(reopenedScreen.firstFragment.projectsTable,
                reopenedScreen.firstFragment.getColumnId("name"), true)
        assertNoTableSort(reopenedScreen.lastFragment.projectsTable)
    }

    def "Table sort settings from last same type fragment are not applied to all fragments"() {
        showTestMainScreen()

        when: "Sort table in the last fragment"
        def screen = createAndShow(FacetFragmentTableSettingsHostScreen)
        screen.tabSheet.setSelectedTab("lastTab")
        screen.lastFragment.projectsTable.sort("description", DESCENDING)
        screen.closeWithDefaultAction()

        then: "Only last fragment table should restore the saved sort after reopening"
        def reopenedScreen = createAndShow(FacetFragmentTableSettingsHostScreen)
        assertNoTableSort(reopenedScreen.firstFragment.projectsTable)
        assertTableSort(reopenedScreen.lastFragment.projectsTable,
                reopenedScreen.lastFragment.getColumnId("description"), false)
    }

    def "Legacy simple id table settings are applied to same type fragments"() {
        showTestMainScreen()

        when: "Open screen with legacy settings saved by plain component id"
        def screen = screens.create(FacetFragmentTableSettingsHostScreen)
        settingsCache.setSetting(screen.id, legacyProjectsTableSortSettings())
        screen.show()
        screen.tabSheet.setSelectedTab("lastTab")

        then: "Fragment tables still read legacy settings"
        assertTableSort(screen.firstFragment.projectsTable,
                screen.firstFragment.getColumnId("name"), true)
        assertTableSort(screen.lastFragment.projectsTable,
                screen.lastFragment.getColumnId("name"), true)
    }

    protected void assertTableSort(Table table, Object columnId, boolean ascending) {
        assert table.sortInfo != null
        assert table.sortInfo.propertyId == columnId
        assert table.sortInfo.ascending == ascending
    }

    protected void assertNoTableSort(Table table) {
        assert table.sortInfo == null
    }

    protected String legacyProjectsTableSortSettings() {
        return """
            [{
              "id": "projectsTable",
              "sortProperty": "name",
              "sortAscending": true,
              "columns": [
                {"id": "name", "visible": true},
                {"id": "description", "visible": true},
                {"id": "budget", "visible": true},
                {"id": "startDate", "visible": true},
                {"id": "active", "visible": true}
              ]
            }]
        """
    }
}
