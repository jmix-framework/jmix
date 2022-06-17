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
import io.jmix.core.SaveContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.entity.sec.User
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PasswordFieldXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def saveContext = new SaveContext()
        def order = dataManager.create(Order)
        def user = dataManager.create(User)

        user.password = "password"
        order.user = user

        saveContext.saving(user, order)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
        jdbcTemplate.execute("delete from SEC_USER")
    }

    def "Load passwordField component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "PasswordField attributes will be loaded"
        verifyAll(componentView.passwordFieldId) {
            id.get() == "passwordFieldId"
            autocapitalize == Autocapitalize.SENTENCES
            autocomplete == Autocomplete.ADDITIONAL_NAME
            autocorrect
            autofocus
            autoselect
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            pattern == "patternString"
            placeholder == "placeholderString"
            preventInvalidInput
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            value == "password"
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"
        }
    }

    def "Load passwordField component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "PasswordField will be loaded with the value of the property"
        verifyAll(componentView.passwordFieldWithValueId) {
            id.get() == "passwordFieldWithValueId"
            value == order.user.password
        }
    }
}
