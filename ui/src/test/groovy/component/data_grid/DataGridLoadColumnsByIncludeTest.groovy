/*
 * Copyright (c) 2020 Haulmont.
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

package component.data_grid

import component.data_grid.screen.DataGridLoadColumnsByIncludeTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DataGrid
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DataGridLoadColumnsByIncludeTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.data_grid"])
    }

    def "load columns by includeAll"() {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridScreen.show()

        when:
        def dataGrid = dataGridScreen.getWindow().getComponentNN("dataGridAll") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 11
    }

    def "load columns by includeAll with custom fetch plan"() {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridScreen.show()

        when:
        def dataGrid = dataGridScreen.getWindow().getComponentNN("dataGridLocalWithAddress") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 12
    }

    def "exclude system and other properties"() {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridScreen.show()

        when:
        def dataGrid = dataGridScreen.getWindow().getComponentNN("dataGridExclude") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 10

        dataGrid.getColumn("address") == null
        dataGrid.getColumn("createTs") == null
    }

    def "entity with embedded property"() {
        showTestMainScreen()

        def dataGridScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridScreen.show()

        when:
        def dataGrid = dataGridScreen.getWindow().getComponentNN("dataGridEmb") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 14

        dataGrid.getColumn("address.city") != null
        dataGrid.getColumn("address.zip") != null
        dataGrid.getColumn("address") != null
    }

    def "overriding columns"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def dataGrid = groupTableScreen.getWindow().getComponentNN("dataGridOverriding") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 12

        !dataGrid.getColumn("name").isSortable()
    }

    def "includeAll with non-persistent entity"() {
        showTestMainScreen()

        def dataGridTableScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridTableScreen.show()

        when:
        def dataGrid = dataGridTableScreen.getWindow().getComponentNN("dataGridNonPersist") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 4

        dataGrid.getColumn("isFragile") == null
    }

    def "load columns without fetch plan"() {
        showTestMainScreen()

        def dataGridTableScreen = screens.create(DataGridLoadColumnsByIncludeTestScreen)
        dataGridTableScreen.show()

        when:
        def dataGrid = dataGridTableScreen.getWindow().getComponentNN("dataGridWithoutFetchPlan") as DataGrid
        def columnList = dataGrid.getColumns()

        then:
        columnList.size() == 11
    }
}
