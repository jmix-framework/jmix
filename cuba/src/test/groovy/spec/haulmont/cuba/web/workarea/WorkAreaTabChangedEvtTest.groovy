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

package spec.haulmont.cuba.web.workarea

import io.jmix.ui.screen.FrameOwner
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.menu.commandtargets.TestWebBean
import spock.lang.Ignore

import javax.inject.Inject

@Ignore
class WorkAreaTabChangedEvtTest extends UiScreenSpec {

    @Inject
    TestWebBean testWebBean

    def 'WorkAreaTabChangedEvent is fired when screen is opened or closed'() {
        showMainScreen()

        when: 'Screen is opened'
        def screen = screens.create('sec$User.browse', OpenMode.NEW_TAB)
        screen.show()

        then: 'WorkAreaTabChangedEvent is fired'
        testWebBean.workAreaTabChangedEventHandled.get()

        when: 'Screen is closed then'
        // reset flag
        testWebBean.workAreaTabChangedEventHandled.set(false)
        // one more screen required to trigger tab switching
        screens.create('sec$Group.browse', OpenMode.NEW_TAB)
                .show()
        screen.close(FrameOwner.WINDOW_CLOSE_ACTION)

        then: 'WorkAreaTabChangedEvent is fired'
        testWebBean.workAreaTabChangedEventHandled.get()
    }
}
