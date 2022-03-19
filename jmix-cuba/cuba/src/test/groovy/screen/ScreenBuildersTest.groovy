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

package screen

import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.gui.ScreenBuilders
import com.haulmont.cuba.gui.WindowContext
import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.screen.OpenMode
import com.haulmont.cuba.web.screens.UserBrowser
import com.haulmont.cuba.web.screens.UserEditor
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.web.UiScreenSpec

class ScreenBuildersTest extends UiScreenSpec {

    @Autowired
    ScreenBuilders screenBuilders

    @Override
    void setup() {
        exportScreensPackages(['com.haulmont.cuba.web.screens'])
    }

    def "create a screen using legacy ScreenBuilders"() {
        def mainScreen = showTestMainScreen()

        when:
        def screen = screenBuilders.screen(mainScreen)
                .withScreenClass(UserBrowser)
                .withLaunchMode(OpenMode.DIALOG)
                .show()

        then:
        screen != null

        when:
        def context = screen.getWindow().getContext() as WindowContext

        then:
        context.getLaunchMode() == OpenMode.DIALOG
        context.getOpenMode() == io.jmix.ui.screen.OpenMode.DIALOG
    }

    def "create a lookup screen using legacy ScreenBuilders"() {
        def mainScreen = showTestMainScreen()

        when:
        def screen = screenBuilders.lookup(User, mainScreen)
                .withScreenClass(UserBrowser)
                .withLaunchMode(OpenMode.DIALOG)
                .show()

        then:
        screen != null

        when:
        def context = screen.getWindow().getContext() as WindowContext

        then:
        context.getLaunchMode() == OpenMode.DIALOG
        context.getOpenMode() == io.jmix.ui.screen.OpenMode.DIALOG
    }

    def "create an edit screen using legacy ScreenBuilders"() {
        def mainScreen = showTestMainScreen()

        when:
        def screen = screenBuilders.editor(User, mainScreen)
                .newEntity()
                .withScreenClass(UserEditor)
                .withLaunchMode(OpenMode.DIALOG)
                .show()

        then:
        screen != null

        when:
        def context = screen.getWindow().getContext() as WindowContext

        then:
        context.getLaunchMode() == OpenMode.DIALOG
        context.getOpenMode() == io.jmix.ui.screen.OpenMode.DIALOG
    }

    def "build a legacy edit screen using legacy ScreenBuilders"() {
        def mainScreen = showTestMainScreen()

        when:
        AbstractEditor screen = screenBuilders.editor(User, mainScreen)
                .newEntity()
                .withScreenId('test_LegacyUserEditor')
                .show() as AbstractEditor

        then:
        screen != null

        and:
        screen.getEditedEntity() != null
    }
}
