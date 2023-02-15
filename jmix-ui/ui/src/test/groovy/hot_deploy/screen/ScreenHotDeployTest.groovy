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

package hot_deploy.screen

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.NoSuchScreenException
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ScreenHotDeployTest extends ScreenSpecification {

    def 'Hot deploy supports screens created at runtime'() {
        /*
         * The class that will be "hot-deployed" is placed in /test/java/hot_deploy/screen/HotDeployTestScreen.
         * Due to manually "hot-deploying" this file we can place only source (not compiled) to the "jmix.core.confDir"
         * and io.jmix.core.ClassManager cannot find this class. So in the test "HotDeployTestScreen" will be
         * compiled but not registered as a screen.
         */

        showTestMainScreen()

        when: '''Trying to open not hot-deployed screen'''
        screens.create('HotDeployTestScreen', OpenMode.NEW_TAB)

        then: 'NoSuchScreenException is thrown'
        thrown NoSuchScreenException

        // trigger hot deploy

        windowConfig.loadScreenClass('hot_deploy.screen.HotDeployTestScreen')

        when: 'Trying to open hot-deployed screen'
        def screen = screens.create('HotDeployTestScreen', OpenMode.NEW_TAB)
        screen.show()

        then: 'Screen is successfully opened'
        screens.getOpenedScreens().getActiveScreens().iterator().next() == screen
    }
}
