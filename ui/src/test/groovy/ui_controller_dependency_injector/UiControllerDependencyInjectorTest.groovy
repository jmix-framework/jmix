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

package ui_controller_dependency_injector

import io.jmix.core.CoreConfiguration
import io.jmix.core.Messages
import io.jmix.core.common.util.ParamsMap
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.Screens
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Button
import io.jmix.ui.component.formatter.NumberFormatter
import io.jmix.ui.screen.MapScreenOptions
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import ui_controller_dependency_injector.screen.AutowireFacetDialogActionTestScreen
import ui_controller_dependency_injector.screen.AutowireToFieldsTestScreen
import ui_controller_dependency_injector.screen.AutowireToSettersTestScreen
import ui_controller_dependency_injector.screen.WindowParamTestScreen

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class UiControllerDependencyInjectorTest extends ScreenSpecification {

    @Autowired
    ApplicationContext applicationContext

    @Override
    void setup() {
        exportScreensPackages(["ui_controller_dependency_injector"])
    }

    def "Dependency injector supports fields"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def screen = getScreens().create(AutowireToFieldsTestScreen)
        screen.show()

        then: "All beans should be autowired"

        screen.beanFactory == applicationContext
        screen.messages == applicationContext.getBean(Messages)
        screen.screenBuilders == applicationContext.getBean(ScreenBuilders)
        screen.screens instanceof Screens

        screen.button instanceof Button

        screen.numberFormatterProvider instanceof ObjectProvider
        screen.numberFormatterProvider.getObject() instanceof NumberFormatter
    }

    def "Dependency injector supports setters"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def screen = getScreens().create(AutowireToSettersTestScreen)
        screen.show()

        then: "All beans should be autowired"

        screen.beanFactory == applicationContext
        screen.messages == applicationContext.getBean(Messages)
        screen.screenBuilders == applicationContext.getBean(ScreenBuilders)
        screen.screens instanceof Screens

        screen.button instanceof Button

        screen.numberFormatterProvider instanceof ObjectProvider
        screen.numberFormatterProvider.getObject() instanceof NumberFormatter
    }

    def "Dependency injector supports window parameters"() {
        showTestMainScreen()

        when: "Screen is loaded"

        def params = ParamsMap.of('defaultParam', 'default',
                'requiredParam', 'required',
                'namedParam', 'named')

        WindowParamTestScreen screen = getScreens().create(WindowParamTestScreen,
                OpenMode.NEW_TAB,
                new MapScreenOptions(params))
                .show() as WindowParamTestScreen

        then: "All params should be loaded"

        screen.defaultParam == 'default'
        screen.requiredParam == 'required'
        screen.optionalParam == null
        screen.namedParam == 'named'
    }

    def "Dependency injector supports ActionsAwareDialogFacet DialogAction"() {
        showTestMainScreen()

        when: "Show screen that contains facets and fragment with facets"
        def screen = (AutowireFacetDialogActionTestScreen) screens.create(AutowireFacetDialogActionTestScreen)
        screen.show()

        then: "Actions should be injected to the controller of the screen and to the fragment"

        screen.okInputDialog != null
        screen.okOptionDialog != null

        screen.okFragmentInputDialog != null
        screen.okFragmentOptionDialog != null

        screen.testFragment.okFragmentInputDialog != null
        screen.testFragment.okFragmentOptionDialog != null
    }
}
