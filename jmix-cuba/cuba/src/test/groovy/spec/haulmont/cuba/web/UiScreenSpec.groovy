/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.web

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.core.sys.AppProperties
import com.haulmont.cuba.gui.UiComponents
import com.haulmont.cuba.web.testsupport.WebTest
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.core.security.UserRepository
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.screen.Screen
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
@WebTest
class UiScreenSpec extends ScreenSpecification {

    @Autowired
    AppProperties appProperties

    @Autowired
    DataManager dataManager

    @Autowired
    UserSessionSource sessionSource

    @Autowired
    UiComponents cubaUiComponents

    @Autowired
    SystemAuthenticator authenticator

    @Override
    void setup() {
        exportScreensPackages(['com.haulmont.cuba.web.app.main'])

        /**
         * In UserIndicatorImpl#refreshUser we load current authenticated user from UserRepository.
         * But UserRepository has empty users list.
         * To prevent exception we add user with username "system".
         */
        def userRepository = applicationContext.getBean(UserRepository)
        if (userRepository instanceof InMemoryUserRepository)  {
            ((InMemoryUserRepository) userRepository).addUser(User.builder()
                    .username("system")
                    .password("")
                    .authorities(Collections.emptyList())
                    .build())
        }
    }

    @Override
    void cleanup() {
        def userRepository = applicationContext.getBean(UserRepository)
        if (userRepository instanceof InMemoryUserRepository) {
            def system = ((InMemoryUserRepository) userRepository)
                    .loadUserByUsername("system")
            ((InMemoryUserRepository) userRepository).removeUser(system)
        }
    }

    @Override
    protected void setupAuthentication() {
        authenticator.begin()
    }

    @Override
    protected void cleanupAuthentication() {
        authenticator.end()
    }

    protected Screen showMainWindow() {
        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)
        mainWindow
    }

    protected Screen showMainScreen() {
        def mainScreen = screens.create("main", OpenMode.ROOT)
        screens.show(mainScreen)
        mainScreen
    }
}
