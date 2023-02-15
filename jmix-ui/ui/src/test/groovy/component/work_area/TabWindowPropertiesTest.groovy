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

package component.work_area

import com.vaadin.server.FontIcon
import com.vaadin.ui.TabSheet
import component.work_area.screen.TabbedScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Window
import io.jmix.ui.component.impl.TabWindowImpl
import io.jmix.ui.icon.JmixIcon
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import javax.annotation.Nonnull

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class TabWindowPropertiesTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.work_area"])
    }

    def "change window caption from screen"() {
        showTestMainScreen()

        def screen = screens.create(TabbedScreen)
        def window = screen.getWindow()

        when:
        screen.show()

        then:
        def caption = readCaptionFromTab(window)
        caption == TabbedScreen.SCREEN_INIT_CAPTION

        when:
        window.setCaption('Modified tabbed caption')

        then:
        def modifiedCaption = readCaptionFromTab(window)
        modifiedCaption == 'Modified tabbed caption'

        when:
        window.setCaption("Very very very very long caption")

        then:
        def abbreviatedCaption = readCaptionFromTab(window)
        abbreviatedCaption == 'Very very very very long ...'

        when:
        screen.closeWithDefaultAction()
        window.setCaption('Not attached')

        then:
        window.getCaption() == 'Not attached'
    }

    def "change window description from screen"() {
        showTestMainScreen()

        def screen = screens.create(TabbedScreen)
        def window = screen.getWindow()

        when:
        screen.show()

        then:
        def description = readDescriptionFromTab(window)
        description == null

        when:
        window.setDescription('New description')

        then:
        def modifiedDescription = readDescriptionFromTab(window)
        modifiedDescription == 'Tabbed Screen Caption: New description'

        when:
        screen.closeWithDefaultAction()
        window.setDescription('Not attached')

        then:
        window.getDescription() == 'Not attached'
    }

    @SuppressWarnings("GroovyPointlessBoolean")
    def "change window closeable from screen"() {
        showTestMainScreen()

        def screen = screens.create(TabbedScreen)
        def window = screen.getWindow()

        when:
        screen.show()
        def tab = getWindowTab(window)

        then:
        tab.isClosable() == true

        when:
        window.setCloseable(false)

        then:
        tab.isClosable() == false

        when:
        screen.closeWithDefaultAction()
        window.setCloseable(true)

        then:
        window.isCloseable() == true
    }

    @SuppressWarnings("GroovyPointlessBoolean")
    def "change window icon from screen"() {
        showTestMainScreen()

        def screen = screens.create(TabbedScreen)
        def window = screen.getWindow()

        when:
        screen.show()
        def tab = getWindowTab(window)

        then:
        tab.getIcon() == null

        when:
        window.setIconFromSet(JmixIcon.HOME)

        then:
        tab.getIcon() instanceof FontIcon

        when:
        screen.closeWithDefaultAction()
        window.setIconFromSet(null)

        then:
        window.getIcon() == null
    }

    @SuppressWarnings("GroovyAccessibility")
    private static String readCaptionFromTab(Window window) {
        def tabWindow = window as TabWindowImpl
        def tab = tabWindow.asTabWindow()

        if (tab == null) {
            throw new IllegalStateException("window is not tabbed");
        }

        return tab.getCaption()
    }

    @SuppressWarnings("GroovyAccessibility")
    private static String readDescriptionFromTab(Window window) {
        def tabWindow = window as TabWindowImpl
        def tab = tabWindow.asTabWindow()

        if (tab == null) {
            throw new IllegalStateException("window is not tabbed");
        }

        return tab.getDescription()
    }

    @SuppressWarnings("GroovyAccessibility")
    @Nonnull
    private static TabSheet.Tab getWindowTab(Window window) {
        def tabWindow = window as TabWindowImpl
        def tab = tabWindow.asTabWindow()

        if (tab == null) {
            throw new IllegalStateException("window is not tabbed");
        }

        return tab
    }
}
