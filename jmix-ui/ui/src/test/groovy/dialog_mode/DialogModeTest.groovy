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

package dialog_mode

import dialog_mode.screen.DialogAutoSizeTestScreen
import dialog_mode.screen.DialogSpecifiedSizeTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.DialogWindow
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DialogModeTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['dialog_mode'])
    }

    def 'DialogMode supports AUTO width and height'() {
        showTestMainScreen()

        def screen = screens.create(DialogAutoSizeTestScreen)
        screen.show()

        when:
        def dialogWindow = screen.getWindow() as DialogWindow
        def dialogWidth = dialogWindow.getDialogWidth()
        def dialogHeight = dialogWindow.getDialogHeight()

        then:
        dialogWidth == -1
        dialogHeight == -1
    }

    def 'DialogMode supports specified width and height'() {
        showTestMainScreen()

        def screen = screens.create(DialogSpecifiedSizeTestScreen)
        screen.show()

        when:
        def dialogWindow = screen.getWindow() as DialogWindow
        def dialogWidth = dialogWindow.getDialogWidth()
        def dialogHeight = dialogWindow.getDialogHeight()

        then:
        dialogWidth == 600
        dialogHeight == 400
    }
}
