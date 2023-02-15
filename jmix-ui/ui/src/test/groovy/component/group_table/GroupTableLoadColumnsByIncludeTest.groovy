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

package component.group_table

import component.group_table.screen.GroupTableLoadColumnsByIncludeTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.GroupTable
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class GroupTableLoadColumnsByIncludeTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.group_table"])
    }

    def "load columns by includeAll"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableAll") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 11
    }

    def "load columns by includeAll with custom fetch plan"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableLocalWithAddress") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 12
    }

    def "exclude columns"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableExclude") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 10

        groupTable.getColumn("address") == null
        groupTable.getColumn("createTs") == null
    }

    def "entity with embedded property"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableEmb") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 14

        groupTable.getColumn("address.city") != null
        groupTable.getColumn("address.zip") != null
        groupTable.getColumn("address") != null
    }

    def "grouping and overriding columns"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableGrouping") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 12

        groupTable.getColumn("address").isGroupAllowed()
        !groupTable.getColumn("name").isSortable()
    }

    def "with non persistent entity"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableNonPersist") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 4

        groupTable.getColumn("isFragile") == null
    }

    def "load columns without fetch plan"() {
        showTestMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeTestScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableWithoutFetchPlan") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 12
    }
}
