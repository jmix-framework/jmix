/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package spec.haulmont.cuba.web.clipboardtrigger

import com.haulmont.cuba.web.app.main.MainScreen
import io.jmix.ui.component.ClipboardTrigger
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.clipboardtrigger.screens.ScreenWithClipboardTrigger
import spock.lang.Ignore

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class ClipboardTriggerFacetTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.clipboardtrigger.screens'])
    }

    @Ignore
    def "open screen with ClipboardTrigger"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create(MainScreen, OpenMode.ROOT)
        screens.show(mainWindow)

        when:

        def screen = screens.create(ScreenWithClipboardTrigger)
        screen.show()

        then:

        screen.window.getFacet('copyTrigger') instanceof ClipboardTrigger
        screen.window.facets.count() == 1

        screen.copyTrigger != null
        screen.copyTrigger.button != null
        screen.copyTrigger.input != null
    }
}