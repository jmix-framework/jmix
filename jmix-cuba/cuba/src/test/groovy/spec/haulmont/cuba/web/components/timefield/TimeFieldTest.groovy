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

package spec.haulmont.cuba.web.components.timefield


import io.jmix.ui.component.TimeField
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.timefield.screens.TimeFieldTestScreen

@SuppressWarnings('GroovyAccessibility')
class TimeFieldTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.timefield.screens', 'com.haulmont.cuba.web.app.main'])
    }

    def 'load "showSeconds" XML attribute'() {
        showMainScreen()

        when: 'Screen is loaded'

        TimeFieldTestScreen screen = screens.create(TimeFieldTestScreen).show()
        def defaultTimeField = screen.defaultTimeField
        def secondTimeField = screen.secondTimeField
        def minuteTimeField = screen.minuteTimeField

        then: 'Attribute value is applied'

        defaultTimeField != null
        !defaultTimeField.getShowSeconds()
        defaultTimeField.resolution == TimeField.Resolution.MIN

        secondTimeField != null
        secondTimeField.getShowSeconds()
        secondTimeField.resolution == TimeField.Resolution.SEC

        minuteTimeField != null
        !minuteTimeField.getShowSeconds()
        minuteTimeField.resolution == TimeField.Resolution.MIN
    }
}
