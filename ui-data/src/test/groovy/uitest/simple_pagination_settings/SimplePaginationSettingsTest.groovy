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

package uitest.simple_pagination_settings

import io.jmix.core.DataManager
import io.jmix.ui.component.impl.AbstractPagination
import io.jmix.ui.settings.component.SimplePaginationSettings
import org.springframework.beans.factory.annotation.Autowired
import test_support.UiDataTestSpecification
import test_support.entity.Project
import uitest.simple_pagination_settings.screen.SimplePaginationSettingsTestScreen

class SimplePaginationSettingsTest extends UiDataTestSpecification {

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["uitest.simple_pagination_settings.screen"])

        10.times { dataManager.save(dataManager.create(Project)) }
    }

    void cleanup() {
        jdbcTemplate.update("delete from TEST_UIDATA_PROJECT")
    }

    def "SimplePagination save settings"() {
        showTestMainScreen()

        when: "Open and close screen with SimplePagination"

        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of SimplePagination should be saved"

        checkSavedValue(screen, "simplePagination", 5)

        when: "Open screen again, change itemsPerPage value, close screen"
        screen = createAndShow(SimplePaginationSettingsTestScreen)

        def savedValue = 3;
        ((AbstractPagination) screen.simplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: "itemsPerPage of the SimplePagination should be saved"

        checkSavedValue(screen, "simplePagination", savedValue)
    }

    def "SimplePagination apply settings"() {
        showTestMainScreen()

        when: "Open screen and change SimplePagination itemsPerPage value."

        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.simplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: """
              Open screen again. It should load amount of items according
              to the saved value.
              """
        def screen1 = createAndShow(SimplePaginationSettingsTestScreen)

        ((AbstractPagination) screen1.simplePagination).itemsPerPageValue == savedValue
        screen1.projectsDc.items.size() == savedValue
    }

    def "SimplePagination save settings in the Table"() {
        showTestMainScreen()

        when: "Open and close screen with SimplePagination in the Table"

        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of SimplePagination in the Table should be saved"

        checkSavedValue(screen, "tableSimplePagination", 5)

        when: "Open screen again, change itemsPerPage value, close screen"

        screen = createAndShow(SimplePaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.tableSimplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: "itemsPerPage of the SimplePagination in the Table should be saved"

        checkSavedValue(screen, "tableSimplePagination", savedValue)
    }

    def "SimplePagination apply settings in the Table"() {
        showTestMainScreen()

        when: "Open screen and change SimplePagination itemsPerPage value in the Table."

        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.tableSimplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: """
              Open screen again. It should load amount of items according
              to the saved value.
              """
        def screen1 = createAndShow(SimplePaginationSettingsTestScreen)

        ((AbstractPagination) screen1.tableSimplePagination).itemsPerPageValue == savedValue
        screen1.projectsTableDc.items.size() == savedValue
    }

    def "SimplePagination save settings in the DataGrid"() {
        showTestMainScreen()

        when: "Open and close screen with SimplePagination in the DataGrid"

        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of SimplePagination in the DataGrid should be saved"

        checkSavedValue(screen, "dataGridSimplePagination", 5)

        when: "Open screen again, change itemsPerPage value, close screen"

        screen = createAndShow(SimplePaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.dataGridSimplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: "itemsPerPage of the SimplePagination in the DataGrid should be saved"

        checkSavedValue(screen, "dataGridSimplePagination", savedValue)
    }

    def "SimplePagination apply settings in the DataGrid"() {
        showTestMainScreen()

        when: "Open screen and change SimplePagination itemsPerPage value in the DataGrid."
        def screen = createAndShow(SimplePaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.dataGridSimplePagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: """
              Open screen again. It should load amount of items according
              to the saved value.
              """
        def screen1 = createAndShow(SimplePaginationSettingsTestScreen)

        ((AbstractPagination) screen1.dataGridSimplePagination).itemsPerPageValue == savedValue
        screen1.projectsDataGridDc.items.size() == savedValue
    }

    protected void checkSavedValue(SimplePaginationSettingsTestScreen screen,
                                   String compId,
                                   Integer savedValue) {
        def settingsOpt = screen.facet.settings
                .getSettings(compId, SimplePaginationSettings)

        assert settingsOpt.get().itemsPerPageValue == savedValue
    }
}
