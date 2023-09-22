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

import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.component.textfield.Autocapitalize
import com.vaadin.flow.component.textfield.Autocomplete
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.EmailFieldView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class EmailFieldXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def customer = dataManager.create(Customer)
        customer.email = "example@email.com"

        dataManager.save(customer)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_CUSTOMER")
    }

    def "Load emailField component from XML"() {
        when: "Open the ComponentView"
        def emailFieldView = navigateToView(EmailFieldView)

        then: "EmailField attributes will be loaded"
        verifyAll(emailFieldView.emailFieldId) {
            id.get() == "emailFieldId"
            !invalid
            allowedCharPattern == "charPattern"
            autocapitalize == Autocapitalize.SENTENCES
            autocomplete == Autocomplete.ADDITIONAL_NAME
            autocorrect
            autofocus
            autoselect
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            clearButtonVisible
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            maxHeight == "55px"
            maxLength == 10
            maxWidth == "120px"
            minHeight == "40px"
            minLength == 1
            minWidth == "80px"
            pattern == "patternString"
            placeholder == "placeholderString"
            readOnly
            required
            requiredMessage == "requiredMessage"
            requiredIndicatorVisible
            tabIndex == 3
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            value == "example@email.com"
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load emailField component with datasource from XML"() {
        given: "An entity with some property"
        def customer = dataManager.load(Customer).all().one()

        when: "Open the ComponentView and load data"
        def componentView = navigateToView(EmailFieldView)

        then: "EmailField will be loaded with the value of the property"
        verifyAll(componentView.emailFieldWithValueId) {
            id.get() == "emailFieldWithValueId"
            value == customer.email
        }
    }
}
