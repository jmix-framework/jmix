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

package spec.haulmont.cuba.web.screens


import spec.haulmont.cuba.web.UiScreenSpec

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class UserBrowseTest extends UiScreenSpec {

    /*def setup() { todo port
        def group = new Group(name: 'Company')
        def users = [
                new User(login: 'admin', loginLowerCase: 'admin', group: group, active: true, name: 'Administrator'),
                new User(login: 'anonymous', loginLowerCase: 'anonymous', group: group, active: true, name: 'Anonymous')
        ]

        *//*TestServiceProxy.mock(DataService, Mock(DataService) {
            loadList(_) >> { LoadContext lc ->
                if (lc.entityMetaClass == 'sec$User') {
                    return users
                }

                return []
            }
        })

        TestServiceProxy.mock(SecurityScopesService, Mock(SecurityScopesService) {
            getAvailableSecurityScopes() >> {
                return []
            }
        })*//*
    }

    def "open UserBrowser"() {
        def screens = vaadinUi.screens

        def beforeShowListener = Mock(Consumer)
        def afterShowListener = Mock(Consumer)

        when:
        def mainWindow = screens.create("main", OpenMode.ROOT)
        screens.show(mainWindow)

        then:
        vaadinUi.topLevelWindow == mainWindow.window

        when:
        def userBrowser = screens.create('sec$User.browse', OpenMode.NEW_TAB)
        userBrowser.addBeforeShowListener(beforeShowListener)
        userBrowser.addAfterShowListener(afterShowListener)
        userBrowser.show()

        then:
        1 * beforeShowListener.accept(_)
        1 * afterShowListener.accept(_)

        userBrowser instanceof UserBrowser
        screens.getOpenedScreens().currentBreadcrumbs[0] == userBrowser
        userBrowser.usersTable != null
        userBrowser.window != null
    }*/
}