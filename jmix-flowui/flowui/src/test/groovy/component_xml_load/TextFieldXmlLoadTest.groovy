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
import component_xml_load.screen.ComponentView
import io.jmix.core.DataManager
import io.jmix.core.metamodel.datatype.impl.IntegerDatatype
import io.jmix.core.metamodel.datatype.impl.StringDatatype
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class TextFieldXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)
        order.number = "textFieldValue"

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load textField component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "TextField attributes will be loaded"
        verifyAll(componentView.textFieldId) {
            id.get() == "textFieldId"
            autocapitalize == Autocapitalize.SENTENCES
            autocomplete == Autocomplete.ADDITIONAL_NAME
            autocorrect
            autofocus
            autoselect
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            datatype.class == IntegerDatatype
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            maxHeight == "55px"
            maxLength == 50
            maxWidth == "120px"
            minHeight == "40px"
            minLength == 2
            minWidth == "80px"
            pattern == "patternString"
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            typedValue == 5050
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"
        }
    }

    def "Load textField component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "TextField will be loaded with the value of the property"
        verifyAll(componentView.textFieldWithValueId) {
            id.get() == "textFieldWithValueId"
            datatype.class == StringDatatype
            value == order.number
        }
    }
}
