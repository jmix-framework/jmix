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

import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

import java.util.function.Consumer

@Ignore
@SuppressWarnings("GroovyAccessibility")
class LoginScreenTest extends UiScreenSpec {

    def "Open login window"() {
        def screens = vaadinUi.screens

        def beforeShowListener = Mock(Consumer)
        def afterShowListener = Mock(Consumer)

        when:
        def loginWindow = screens.create('loginWindow', OpenMode.ROOT)

        then:
        loginWindow != null

        when:
        loginWindow.addBeforeShowListener(beforeShowListener)
        loginWindow.addAfterShowListener(afterShowListener)
        screens.show(loginWindow)

        then:
        vaadinUi.topLevelWindow == loginWindow.window
        1 * beforeShowListener.accept(_)
        1 * afterShowListener.accept(_)
    }
}