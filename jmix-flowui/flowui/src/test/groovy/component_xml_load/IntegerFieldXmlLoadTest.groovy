/*
 * Copyright 2022 Haulmont.
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

/*
 * Copyright 2022 Haulmont.
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

/*
 * Copyright 2022 Haulmont.
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

package component_xml_load

import com.vaadin.flow.component.textfield.Autocapitalize
import com.vaadin.flow.component.textfield.Autocomplete
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.IntegerFieldView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class IntegerFieldXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def order = dataManager.create(OrderLine)
        order.quantity = 39

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE")
    }

    def "Load integerField component with from XML"() {
        when: "Open the ComponentView"
        def view = openScreen(IntegerFieldView)

        then: "IntegerField attributes will be loaded"
        verifyAll(view.integerFieldId) {
            id.get() == "integerFieldId"
            !invalid
            autocapitalize == Autocapitalize.SENTENCES
            autocomplete == Autocomplete.ADDITIONAL_NAME
            autocorrect
            autofocus
            autoselect
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            enabled
            errorMessage == "errorMessageString"
            hasControls()
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            max == 500
            maxHeight == "55px"
            maxWidth == "120px"
            min == -500
            minHeight == "40px"
            minWidth == "80px"
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            step == 10
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            value == 50
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"
        }
    }

    def "Load IntegerField component with datasource from XML"() {
        given: "An entity with some property"
        def orderLine = dataManager.load(OrderLine).all().one()

        when: "Open the ComponentView and load data"
        def view = openScreen(IntegerFieldView)

        then: "IntegerField will be loaded with the value of the property"
        verifyAll(view.integerFieldWithValueId) {
            id.get() == "integerFieldWithValueId"
            value == orderLine.quantity
        }
    }
}
