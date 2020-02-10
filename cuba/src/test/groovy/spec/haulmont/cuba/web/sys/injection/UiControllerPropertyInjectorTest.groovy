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

package spec.haulmont.cuba.web.sys.injection

import io.jmix.ui.components.Label
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.sys.injection.screens.PropsInjectionTestScreen
import spock.lang.Ignore

@Ignore
@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class UiControllerPropertyInjectorTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['com.haulmont.cuba.web.app.main',
                               'spec.haulmont.cuba.web.sys.injection.screens'])
    }

    /**
     * See properties injection tests for Screens in {@code MenuItemCommandsTest}
     */
    def 'Primitives and references are injected into Fragments'() {
        def screens = vaadinUi.screens

        showMainScreen()

        def screen = screens.create(PropsInjectionTestScreen)

        when: "Screen with Fragment is shown"
        screen.show()

        then: "Declared properties are injected"
        screen.testFragment.boolProp
        screen.testFragment.intProp == 42
        screen.testFragment.doubleProp == 3.14159d

        screen.testFragment.labelProp instanceof Label
        screen.testFragment.dcProp instanceof InstanceContainer
    }
}
