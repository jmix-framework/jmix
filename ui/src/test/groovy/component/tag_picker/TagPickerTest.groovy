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

package component.tag_picker

import component.tag_picker.screen.TagPickerTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.RequiredValueMissingException
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class TagPickerTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.tag_picker.screen"])
    }

    def "TagPicker entity ValueSource"() {
        showTestMainScreen()

        def screen = (TagPickerTestScreen) screens.create(TagPickerTestScreen)
        screen.show()

        when: "Set entity collection value"

        def items = screen.optOrderLinesDc.getItems()
        screen.entityTagPicker.setValue(items)

        then: "TagPicker and Container should fire change events"

        screen.entityTagValueChangeCount == 1
        screen.containerPropertyChangeCount == 1
        screen.entityTagPicker.getValue() == items

        when: "Set null value"

        screen.entityTagPicker.setValue(null)

        then: "TagPicker and Container should fire change events"

        screen.entityTagValueChangeCount == 2
        screen.containerPropertyChangeCount == 2
    }

    def "TagPicker with datatype"() {
        showTestMainScreen()

        def screen = (TagPickerTestScreen) screens.create(TagPickerTestScreen)
        screen.show()

        when: "Set collection value"

        def options = screen.tagPicker.getOptions().options.collect()
        screen.tagPicker.setValue(options)

        then: "TagPicker ValueChangeEvent should be fired"

        screen.datatypeTagValueChangeCount == 1;
    }

    def "TagPicker required"() {
        showTestMainScreen()

        def screen = (TagPickerTestScreen) screens.create(TagPickerTestScreen)
        screen.show()

        when: "Validate empty TagPicker"

        screen.requiredTagPicker.validate()

        then: "Exception should be thrown"

        thrown(RequiredValueMissingException)
    }
}
