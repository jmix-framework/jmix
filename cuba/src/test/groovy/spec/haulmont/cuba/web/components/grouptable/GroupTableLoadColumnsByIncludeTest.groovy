/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.components.grouptable

import io.jmix.ui.component.GroupTable
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.grouptable.screens.GroupTableLoadColumnsByIncludeScreen

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class GroupTableLoadColumnsByIncludeTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.grouptable.screens'])
    }

    def "load columns by includeAll"() {
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableAll") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 4
    }

    def "load columns by includeAll with system properties"() {
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableSystem") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 12
    }

    def "exclude columns"() {
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
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
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
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
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
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
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableNonPersist") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 5

        groupTable.getColumn("isFragile") == null
    }

    def "load columns without view"() {
        showMainScreen()

        def groupTableScreen = screens.create(GroupTableLoadColumnsByIncludeScreen)
        groupTableScreen.show()

        when:
        def groupTable = groupTableScreen.getWindow().getComponentNN("groupTableWithoutView") as GroupTable
        def columnList = groupTable.getColumns()

        then:
        columnList.size() == 12
    }
}