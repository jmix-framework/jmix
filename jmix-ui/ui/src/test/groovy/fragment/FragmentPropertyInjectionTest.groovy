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

package fragment

import fragment.screen.PropertyInjectionTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Label
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FragmentPropertyInjectionTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["fragment"])
    }

    def 'Primitives and references are injected into Fragments'() {
        showTestMainScreen()

        def screen = screens.create(PropertyInjectionTestScreen)

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
