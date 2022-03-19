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

package spec.haulmont.cuba.web.sys.hotdeploy


import io.jmix.ui.NoSuchScreenException
import io.jmix.ui.screen.OpenMode
import org.springframework.core.env.Environment
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

import org.springframework.beans.factory.annotation.Autowired

@Ignore
class ScreenHotDeployTest extends UiScreenSpec {

    @Autowired
    Environment environment

    protected static final String SCREEN_TO_HOT_DEPLOY = '''
package spec.haulmont.cuba.web.sys.hotdeploy.screens;

import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;

@UiController("HotDeployTestScreen")
public class HotDeployTestScreen extends Screen {
}
'''

    def 'Hot deploy supports screens created at runtime'() {
        def screens = vaadinUi.screens

        showMainScreen()

        def confDirLocation = environment.getProperty('jmix.core.conf-dir')
        def confDir = new File(confDirLocation)
        if (!confDir.exists()) {
            confDir.mkdirs()
        }

        def classConfDirLocation = new File(confDir, 'spec/cuba/web/sys/hotdeploy/screens/')
        if (!classConfDirLocation.exists()) {
            classConfDirLocation.mkdirs()
        }

        def classConfLocation = new File(classConfDirLocation, 'HotDeployTestScreen.java')

        classConfLocation.withWriter {
            it.write(SCREEN_TO_HOT_DEPLOY)
        }

        when: 'Trying to open not hot-deployed screen'
        screens.create('HotDeployTestScreen', OpenMode.NEW_TAB)

        then: 'NoSuchScreenException is thrown'
        thrown NoSuchScreenException

        // trigger hot deploy

        // windowConfig.loadScreenClass('spec.haulmont.cuba.web.sys.hotdeploy.screens.HotDeployTestScreen') todo port

        when: 'Trying to open hot-deployed screen'
        def screen = screens.create('HotDeployTestScreen', OpenMode.NEW_TAB)
        screen.show()

        then: 'Screen is successfully opened'
        screens.getOpenedScreens().getActiveScreens().iterator().next() == screen
    }
}
