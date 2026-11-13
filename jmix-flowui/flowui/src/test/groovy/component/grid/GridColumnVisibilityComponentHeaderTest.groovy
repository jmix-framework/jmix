/*
 * Copyright 2026 Haulmont.
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

package component.grid

import component.grid.view.GridColumnVisibilityComponentHeaderTestView
import io.jmix.core.MessageTools
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sec.Group
import test_support.spec.FlowuiTestSpecification

/**
 * A column header that is a component without text falls back to the attribute caption
 * in the {@code gridColumnVisibility} menu.
 */
@SpringBootTest
class GridColumnVisibilityComponentHeaderTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    MessageTools messageTools

    @Override
    void setup() {
        registerViewBasePackages("component.grid", "io.jmix.flowui.app")
    }

    def "dataGrid menu item of a column with a component header shows the attribute caption"() {
        def view = navigateToView(GridColumnVisibilityComponentHeaderTestView)

        expect:
        view.dataGridColumnVisibility.menuItems*.text == expectedMenuItemTexts()
    }

    def "treeDataGrid menu item of a column with a component header shows the attribute caption"() {
        def view = navigateToView(GridColumnVisibilityComponentHeaderTestView)

        expect:
        view.treeDataGridColumnVisibility.menuItems*.text == expectedMenuItemTexts()
    }

    private List<String> expectedMenuItemTexts() {
        def groupMetaClass = metadata.getClass(Group)
        return [messageTools.getPropertyCaption(groupMetaClass, "name"),
                messageTools.getPropertyCaption(groupMetaClass, "parent")]
    }
}
