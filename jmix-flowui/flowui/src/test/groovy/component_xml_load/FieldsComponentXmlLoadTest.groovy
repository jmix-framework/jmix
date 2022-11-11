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

import com.vaadin.flow.component.radiobutton.RadioGroupVariant
import component_xml_load.screen.ComponentView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.core.metamodel.datatype.impl.DateTimeDatatype
import io.jmix.core.metamodel.datatype.impl.LocalDateDatatype
import io.jmix.core.metamodel.datatype.impl.LocalTimeDatatype
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.entity.sec.User
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@SpringBootTest
class FieldsComponentXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def saveContext = new SaveContext()
        def user = dataManager.create(User)
        def order = dataManager.create(Order)

        user.active = true

        order.user = user
        order.number = "Order number"
        order.date = LocalDate.ofInstant(new Date().toInstant(), ZoneId.systemDefault())
        order.dateTime = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault())
        order.time = LocalTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()).withNano(0)

        saveContext.saving(user, order)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load checkBox component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "CheckBox attributes will be loaded"
        verifyAll(componentView.checkBoxId) {
            id.get() == "checkBoxId"
            autofocus
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            element.getAttribute("aria-label") == "ariaLabelString"
            enabled
            height == "50px"
            indeterminate
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            readOnly
            requiredIndicatorVisible
            value
            visible
            width == "100px"
        }
    }

    def "Load checkBox component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "CheckBox will be will be loaded with the value of the property"
        verifyAll(componentView.checkBoxWithDataId) {
            getId().get() == "checkBoxWithDataId"
            value == order.user.active
        }
    }

    def "Load comboBox component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "ComboBox will be loaded with the value of the property"
        verifyAll(componentView.comboBoxId) {
            id.get() == "comboBoxId"
            allowCustomValue
            autofocus
            autoOpen
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
            pageSize == 20
            pattern == "patternString"
            placeholder == "placeholderString"
            preventInvalidInput
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            themeNames.containsAll(["small", "align-center"])
            value == order.number
            visible
            width == "100px"
        }
    }

    def "Load datePicker component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "DatePicker will be loaded with the value of the property"
        verifyAll(componentView.datePickerId) {
            id.get() == "datePickerId"
            autoOpen
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            datatype.class == LocalDateDatatype
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
            name == "nameString"
            opened
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            themeNames.containsAll(["small", "align-center"])
            typedValue == order.date
            visible
            weekNumbersVisible
            width == "100px"
        }
    }

    def "Load timePicker component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "TimePicker will be loaded with the value of the property"
        verifyAll(componentView.timePickerId) {
            id.get() == "timePickerId"
            autoOpen
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            clearButtonVisible
            datatype.class == LocalTimeDatatype
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
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            typedValue == order.time
            themeNames.containsAll(["small", "align-center"])
            visible
            width == "100px"
        }
    }

    def "Load dateTimePicker component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "DateTimePicker will be loaded with the value of the property"
        verifyAll(componentView.dateTimePickerId) {
            id.get() == "dateTimePickerId"
            autoOpen
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            datatype.class == DateTimeDatatype
            datePlaceholder == "datePlaceholderString"
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
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            themeNames.containsAll(["small", "align-center"])
            timePlaceholder == "timePlaceholderString"
            typedValue == order.dateTime
            visible
            weekNumbersVisible
            width == "100px"
        }
    }

    def "Load radioButtonGroup component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "RadioButtonGroup will be loaded with the value of the property"
        verifyAll(componentView.radioButtonGroupId) {
            classNames.containsAll(["cssClassName1", "cssClassName2"])
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
            !readOnly
            required
            requiredIndicatorVisible
            themeNames.containsAll([RadioGroupVariant.LUMO_VERTICAL.name()])
            value == order.number
            visible
            width == "100px"
        }
    }

    def "Load select component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = openScreen(ComponentView.class)
        componentView.loadData()

        then: "Select will be loaded with the value of the property"
        verifyAll(componentView.selectId) {
            id.get() == "selectId"
            autofocus
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            emptySelectionAllowed
            emptySelectionCaption == "emptySelectionString"
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
            placeholder == "placeholderString"
            readOnly
            requiredIndicatorVisible
            value == order.number
            visible
            width == "100px"
        }
    }
}
