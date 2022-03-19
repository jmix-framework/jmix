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

package spec.haulmont.cuba.web.components.resizabletextarea

import io.jmix.ui.component.ResizableTextArea
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.resizabletextarea.screens.ResizableTextAreaTestScreen

class ResizableTextAreaTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.resizabletextarea.screens',
                               'com.haulmont.cuba.web.app.main'])
    }

    def 'load "resizable" XML attribute'() {
        showMainScreen()

        when: 'Screen is loaded'

        ResizableTextAreaTestScreen screen = screens.create(ResizableTextAreaTestScreen).show()
        def defaultField = screen.defaultField
        def resizableField = screen.resizableField
        def notResizableField = screen.notResizableField

        then: 'Default attribute value is "true"'

        defaultField != null
        defaultField.resizable
        defaultField.resizableDirection == ResizableTextArea.ResizeDirection.BOTH

        and: 'Value "true" from XML is applied for attribute'

        resizableField != null
        resizableField.resizable
        resizableField.resizableDirection == ResizableTextArea.ResizeDirection.BOTH

        and: 'Value "false" from XML is applied for attribute'

        notResizableField != null
        !notResizableField.resizable
        notResizableField.resizableDirection == ResizableTextArea.ResizeDirection.NONE
    }

    def 'load "cols" XML attribute'() {
        showMainScreen()

        when: 'Screen is loaded'

        ResizableTextAreaTestScreen screen = screens.create(ResizableTextAreaTestScreen).show()
        def defaultField = screen.defaultField
        def twoColumnsField = screen.twoColumnsField

        then: 'Default attribute value is "0"'

        defaultField != null
        defaultField.columns == 0

        and: 'Value "2" from XML is applied for attribute'

        twoColumnsField != null
        twoColumnsField.columns == 2
    }
}
