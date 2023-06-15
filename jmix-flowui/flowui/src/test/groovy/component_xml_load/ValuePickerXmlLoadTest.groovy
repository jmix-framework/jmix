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

import com.vaadin.flow.component.shared.Tooltip
import component_xml_load.screen.ComponentView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.kit.component.valuepicker.ValuePickerBase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ValuePickerXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def saveContext = new SaveContext()

        def order = dataManager.create(Order)
        def orderLine1 = dataManager.create(OrderLine)
        def orderLine2 = dataManager.create(OrderLine)
        def customer = dataManager.create(Customer)

        orderLine1.setOrder(order)
        orderLine2.setOrder(order)
        order.number = "valuePicker"
        order.orderLines = List.of(orderLine1, orderLine2)

        saveContext.saving(orderLine1, orderLine2, customer, order)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE")
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load #picker component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

        then: "#picker component will be loaded"
        verifyAll(componentView."${picker}Id" as ValuePickerBase) {
            id.get() == "${picker}Id"
            autofocus
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            placeholder == "placeholderString"
            readOnly
            requiredIndicatorVisible
            tabIndex == 3
            themeNames.containsAll(["theme1", "theme2"])
            title == "titleString"
            visible
            width == "100px"
            getAction("action1") != null
            getAction("action2") != null

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }

        where:
        picker << ["valuePicker", "valuesPicker", "entityPicker"]
    }

    def "Load valuePicker component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "ValuePicker component will be loaded with the value of the property"
        verifyAll(componentView.valuePickerId) {
            id.get() == "valuePickerId"
            allowCustomValue
            errorMessage == "errorMessageString"
            !invalid
            required
            requiredMessage == "requiredMessageString"
            value == order.number
        }
    }

    def "Load valuesPicker component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the ComponentView and load data"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "ValuesPicker component will be loaded with the value of the property"
        verifyAll(componentView.valuesPickerId) {
            id.get() == "valuesPickerId"
            allowCustomValue
            errorMessage == "errorMessageString"
            !invalid
            required
            requiredMessage == "requiredMessageString"
            value.size() == 2
            value == order.orderLines
        }
    }

    def "Load entityPicker component with datasource from XML"() {
        given: "An entity with some property"
        def customer = dataManager.load(Order).all().one().customer

        when: "Open the ComponentView and load data"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "EntityPicker component will be loaded with the value of the property"
        verifyAll(componentView.entityPickerId) {
            id.get() == "entityPickerId"
            allowCustomValue
            errorMessage == "errorMessageString"
            !invalid
            required
            requiredMessage == "requiredMessageString"
            value == customer
        }

        when: "metaClassComboBoxId is loaded"
        def metaClassEntityPicker = componentView.metaClassEntityPickerId

        then: "MetaClass is loaded"
        metaClassEntityPicker.metaClass.getJavaClass() == Order.class
    }

    def "Load comboBoxPicker component"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

        then: "ComboBoxPicker component will be loaded"
        verifyAll(componentView.comboBoxPickerId) {
            id.get() == "comboBoxPickerId"
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
            opened
            pageSize == 20
            pattern == "patternString"
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            tabIndex == 3
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            visible
            width == "100px"
            getAction("action1") != null
            getAction("action2") != null

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load entityComboBox component"() {
        given: "An entity with some property"
        def customer = dataManager.load(Order).all().one().customer

        when: "Open the ComponentView and load data"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "EntityComboBox component will be loaded with the value of the property"
        verifyAll(componentView.entityComboBoxId) {
            id.get() == "entityComboBoxId"
            allowCustomValue
            autofocus
            autoOpen
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
            opened
            pageSize == 20
            pattern == "patternString"
            placeholder == "placeholderString"
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            tabIndex == 3
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            visible
            width == "100px"
            value == customer

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }

        when: "metaClassComboBoxId is loaded"
        def metaClassComboBox = componentView.metaClassComboBoxId

        then: "MetaClass is loaded"
        metaClassComboBox.metaClass.getJavaClass() == Order.class
    }
}