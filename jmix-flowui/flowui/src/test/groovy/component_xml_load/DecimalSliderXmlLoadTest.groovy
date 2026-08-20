/*
 * Copyright 2026 Haulmont.
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
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.SliderView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DecimalSliderXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def orderLine = dataManager.create(OrderLine)
        orderLine.quantity = 39

        def order = dataManager.create(Order)
        order.total = 96

        dataManager.save(orderLine, order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER_LINE")
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load decimalSlider component from XML"() {
        when: "Open the SliderView"
        def view = navigateToView(SliderView)

        then: "DecimalSlider attributes will be loaded"
        verifyAll(view.decimalSliderId) {
            id.get() == "decimalSliderId"
            ariaLabel.get() == "ariaLabelString"
            ariaLabelledBy.get() == "ariaLabelledByString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            max == 10.5d
            maxHeight == "55px"
            maxWidth == "120px"
            min == 0.5d
            minHeight == "40px"
            minWidth == "80px"
            minMaxVisible
            readOnly
            required
            requiredMessage == "requiredMessageString"
            step == 0.5d
            tabIndex == 3
            value == 2.5d
            valueAlwaysVisible
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

    def "Load decimalSlider component with datasource from XML"() {
        given: "An entity with some property"
        def order = dataManager.load(Order).all().one()

        when: "Open the SliderView and load data"
        def view = navigateToView(SliderView)

        then: "DecimalSlider will be loaded with the value of the property"
        verifyAll(view.decimalSliderWithValueId) {
            id.get() == "decimalSliderWithValueId"
            value == order.total
        }
    }

    def "DecimalSlider falls back to the minimum value if the property value is absent"() {
        given: "The SliderView with a DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderWithValueId

        when: "The property value is absent"
        view.orderDc.item.total = null

        then: "The component value is the minimum value and the property value is not changed"
        slider.value == slider.min
        view.orderDc.item.total == null
    }

    def "DecimalSlider validates value against min and max values"() {
        given: "The SliderView with a DecimalSlider with min and max values"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderWithValidatorsId

        when: "Set a value that is greater than the max value"
        slider.value = 200d

        then: "Component is not valid"
        slider.invalid

        when: "Set a value between the min and max values"
        slider.value = 50d

        then: "Component is valid"
        !slider.invalid
    }

    def "Load decimalSlider validators from XML"() {
        given: "The SliderView with a DecimalSlider with validators"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderWithValidatorsId

        when: "Set a valid value"
        slider.value = 5d

        then: "Component is valid"
        !slider.invalid

        when: "Set an invalid value"
        slider.value = 0d

        then: "Component is not valid"
        slider.invalid
        slider.errorMessage == "errorMessageStringForPositive"
    }
}
