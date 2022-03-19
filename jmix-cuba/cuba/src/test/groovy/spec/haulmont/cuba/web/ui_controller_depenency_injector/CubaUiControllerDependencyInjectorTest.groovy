/*
 * Copyright 2021 Haulmont.
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

package spec.haulmont.cuba.web.ui_controller_depenency_injector

import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.ui_controller_depenency_injector.screen.AutowireLegacyParamsScreen

class CubaUiControllerDependencyInjectorTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.ui_controller_dependency_injector.screen'])
    }

    def "autowire legacy params"() {
        showMainScreen()

        when: "Open legacy screen"
        def screen = (AutowireLegacyParamsScreen) screens.create('autowireLegacyParamsScreen', OpenMode.NEW_TAB)
        screen.show()

        then: "Legacy params should be autowired"
        screen.citiesDs != null
        screen.dataSupplier != null
        screen.schedulingConfig != null
        screen.dsContext != null
        screen.window != null
        screen.logger != null
    }
}
