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

package component_registration

import component_registration.config.ComponentRegistrationTestConfiguration
import component_registration.screen.ExtComponentRegistrationScreen
import component_registration.screen.NewComponentRegistrationScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.impl.ResizableTextAreaImpl
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.xml.layout.LoaderResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiConfiguration, UiTestConfiguration, ComponentRegistrationTestConfiguration])
class ComponentRegistrationTest extends ScreenSpecification {

    @Autowired
    LoaderResolver loaderResolver

    @Override
    void setup() {
        exportScreensPackages(["component_registration.screen"])
    }

    def "Extend button component test"() {
        showTestMainScreen()

        when: "Open screen with overridden button"
        def screen = (ExtComponentRegistrationScreen) screens.create(ExtComponentRegistrationScreen)
        screen.show()

        then:
        noExceptionThrown()
    }

    def "Register new button component test"() {
        showTestMainScreen()

        when: "Open screen with new button"
        def screen = (NewComponentRegistrationScreen) screens.create(NewComponentRegistrationScreen)
        screen.show()

        then:
        noExceptionThrown()
    }

    def "Registration priority test"() {
        when: """
              The configuration contains two ordered definitions of custom Field. The first definition
              provides component class and loader class. The second overrides the first definition and
              provides only component class.
              """
        def customField = uiComponents.create(ComponentRegistrationTestConfiguration.TEST_FIELD_NAME)
        def loaderClass = loaderResolver.getLoader(ComponentRegistrationTestConfiguration.TEST_FIELD_ELEMENT)

        then: """
              As the registration mechanism filter registrations with duplicate names only the component class
              will be overridden (due to @Order) and the loader class will not be registered at all.
              """

        customField.getClass() == ResizableTextAreaImpl
        loaderClass == null
    }
}
