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

package uitest.pagination_settings

import io.jmix.core.DataManager
import io.jmix.ui.component.impl.AbstractPagination
import io.jmix.ui.settings.component.PaginationSettings
import org.springframework.beans.factory.annotation.Autowired
import test_support.UiDataTestSpecification
import test_support.entity.Project
import uitest.pagination_settings.screen.PaginationSettingsTestScreen

class PaginationSettingsTest extends UiDataTestSpecification {

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["uitest.pagination_settings.screen"])

        10.times { dataManager.save(dataManager.create(Project)) }
    }

    void cleanup() {
        jdbcTemplate.update("delete from TEST_UIDATA_PROJECT")
    }

    def "Pagination save settings"() {
        showTestMainScreen()

        when: "Open and close screen with Pagination"
        def screen = createAndShow(PaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        then: "Settings of Pagination should be saved"
        def settingsOpt = screen.facet.settings
                .getSettings("pagination", PaginationSettings)

        settingsOpt.get().itemsPerPageValue == 5

        when: "Open screen again, change itemsPerPage value, close screen"
        screen = createAndShow(PaginationSettingsTestScreen)

        def savedValue = 3;
        ((AbstractPagination) screen.pagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: "itemsPerPage of the Pagination should be saved"
        def settingsOpt1 = screen.facet.settings
                .getSettings("pagination", PaginationSettings)

        settingsOpt1.get().itemsPerPageValue == savedValue
    }

    def "Pagination apply settings"() {
        showTestMainScreen()

        when: "Open screen and change Pagination itemsPerPage value."
        def screen = createAndShow(PaginationSettingsTestScreen)
        def savedValue = 3;
        ((AbstractPagination) screen.pagination).itemsPerPageValue = savedValue

        screen.closeWithDefaultAction()

        then: """
              Open screen again. It should load amount of items according
              to the saved value.
              """
        def screen1 = createAndShow(PaginationSettingsTestScreen)

        ((AbstractPagination) screen1.pagination).itemsPerPageValue == savedValue
        screen1.projectsDc.items.size() == savedValue
    }

    def "Pagination save null option"() {
        showTestMainScreen()

        when: """
              Open and close screen. The default value will be saved.
              Change itemsPerPage value to NULL and close the screen.
              """
        def screen = createAndShow(PaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        screen = createAndShow(PaginationSettingsTestScreen)
        ((AbstractPagination) screen.pagination).itemsPerPageValue = null
        screen.closeWithDefaultAction()

        then: "Null value should be saved. It means flag 'isItemsPerPageNullOption' must be true."

        def settingsOpt = screen.facet
                .settings.getSettings("pagination", PaginationSettings)

        settingsOpt.get().itemsPerPageValue == null
        settingsOpt.get().isItemsPerPageUnlimitedOption
    }

    def "Pagination apply null option"() {
        showTestMainScreen()

        when: """
              Open and close screen. The default value will be saved.
              Change itemsPerPage value to NULL and close the screen.
              """
        def screen = createAndShow(PaginationSettingsTestScreen)
        screen.closeWithDefaultAction()

        screen = createAndShow(PaginationSettingsTestScreen)
        ((AbstractPagination) screen.pagination).itemsPerPageValue = null
        screen.closeWithDefaultAction()

        then: """
              Open screen. Null option must be selected and loader should
              load maximum amount of items (10 in this case).
              """
        def screen1 = createAndShow(PaginationSettingsTestScreen)
        ((AbstractPagination) screen1.pagination).itemsPerPageValue == null
        screen1.projectsDc.items.size() == 10
    }
}
