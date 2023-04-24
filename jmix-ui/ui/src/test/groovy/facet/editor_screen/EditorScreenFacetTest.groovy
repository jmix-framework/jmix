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

package facet.editor_screen

import facet.editor_screen.screen.EditorScreenFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.builder.EditMode
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class EditorScreenFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.editor_screen', 'test_support.entity.sales.screen'])
    }

    def 'EditorScreenFacet is loaded from XML'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)

        when: 'Screen with EditorScreenFacet is opened'

        screenWithFacet.show()

        def editorScreenFacet = screenWithFacet.editorScreenFacet

        then: 'All EditorScreenFacet settings are correctly loaded'

        editorScreenFacet.id == 'editorScreenFacet'
        editorScreenFacet.openMode == OpenMode.DIALOG
        editorScreenFacet.entityClass == Order
        editorScreenFacet.actionTarget == 'action'
        editorScreenFacet.entityPicker == screenWithFacet.orderField
        editorScreenFacet.listComponent == screenWithFacet.ordersTable
        editorScreenFacet.editMode == EditMode.EDIT
        editorScreenFacet.addFirst
    }

    def 'EditorScreenFacet opens editor by entity class'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)
        screenWithFacet.show()

        def editorScreenFacet = screenWithFacet.editorScreenFacet

        when: 'EditorScreenFacet opens editor by entity class'

        def orderEditor = editorScreenFacet.show()

        then: 'Editor is correctly opened'

        screens.openedScreens.all.contains(orderEditor)
    }

    def 'EditorScreenFacet opens editor by list component'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)
        screenWithFacet.show()

        def tableScreenFacet = screenWithFacet.tableScreenFacet

        when: 'EditorScreenFacet opens editor by entity class'

        def orderEditor = tableScreenFacet.show()

        then: 'Editor is correctly opened'

        screens.openedScreens.all.contains(orderEditor)
    }

    def 'EditorScreenFacet opens editor by PickerField'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)
        screenWithFacet.show()

        def fieldScreenFacet = screenWithFacet.fieldScreenFacet

        when: 'EditorScreenFacet opens editor by entity class'

        def orderEditor = fieldScreenFacet.show()

        then: 'Editor is correctly opened'

        screens.openedScreens.all.contains(orderEditor)
    }

    def 'EditorScreenFacet opens editor by entity provider'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)
        screenWithFacet.show()

        def providerEditorFacet = screenWithFacet.editorEntityProvider

        when: 'EditorScreenFacet opens editor by entity class'

        def orderEditor = providerEditorFacet.show()

        then: 'Editor is correctly opened'

        screens.openedScreens.all.contains(orderEditor)

    }

    def 'Delegates are correctly installed into EditorScreenFacet'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(EditorScreenFacetTestScreen)

        when: 'Screen with EditorScreenFacet is opened'

        screenWithFacet.show()

        def editorScreenFacet = screenWithFacet.editorScreenFacet

        then: 'Delegates are installed'

        editorScreenFacet.entityProvider
        editorScreenFacet.initializer
    }
}
