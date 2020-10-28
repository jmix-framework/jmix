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

package spec.haulmont.cuba.web.facets.screen

import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.facets.screen.screens.ScreenFacetTestScreen
import spec.haulmont.cuba.web.facets.screen.screens.ScreenToOpenWithFacet

class ScreenFacetTest extends UiScreenSpec {

    @Override
    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.facets.screen.screens'])
    }

    def 'ScreenFacet is loaded from XML'() {
        showTestMainScreen()

        def screenWithFacet = screens.create(ScreenFacetTestScreen)

        when: 'Screen with ScreenFacet is opened'

        screenWithFacet.show()

        def screenIdFacet = screenWithFacet.screenIdFacet
        def screenClassFacet = screenWithFacet.screenClassFacet

        then: 'All ScreenFacet settings are correctly loaded'

        screenIdFacet.id == 'screenIdFacet'
        screenIdFacet.screenId == 'test_ScreenToOpenWithFacet'
        screenIdFacet.openMode == OpenMode.NEW_TAB
        screenIdFacet.getLaunchMode() == com.haulmont.cuba.gui.screen.OpenMode.NEW_TAB
        screenIdFacet.actionTarget == 'action'

        screenClassFacet.id == 'screenClassFacet'
        screenClassFacet.screenClass == ScreenToOpenWithFacet
        screenClassFacet.openMode == OpenMode.DIALOG
        screenClassFacet.getLaunchMode() == com.haulmont.cuba.gui.screen.OpenMode.DIALOG
        screenClassFacet.buttonTarget == 'button'
    }
}
