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

package component.tag_field

import component.tag_field.screen.TagFieldTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.impl.TagFieldImpl
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import io.jmix.ui.widget.JmixAbstractSuggestionField
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.OrderLine

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class TagFieldTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.tag_field.screen"])
    }

    def "TagFiled entity ValueSource"() {
        showTestMainScreen()

        def screen = (TagFieldTestScreen) screens.create(TagFieldTestScreen)
        screen.show()

        when: "Set new value"
        OrderLine ol = metadata.create(OrderLine)

        screen.tagFieldOrder.setValue(Collections.singletonList(ol))

        then: "ValueChangeEvent should be invoked"
        screen.valueChangeCount == 1
    }

    def "Create new item by enter press"() {
        showTestMainScreen()

        def screen = (TagFieldTestScreen) screens.create(TagFieldTestScreen)
        screen.show()

        when: "Imitate enter press with some value for TagField with Entity type"
        def searchString = "new entity"
        TagFieldImpl field = (TagFieldImpl) screen.tagFieldTagCreation
        JmixAbstractSuggestionField vTagField = (JmixAbstractSuggestionField) field.component
        vTagField.enterActionHandler.accept(searchString)

        then: "New entity should be created with provided search value"
        Collection<OrderLine> orderLines = screen.tagFieldTagCreation.getValue()

        orderLines[0].getDescription() == searchString
        screen.valueChangeCountCreation == 1;
    }
}
