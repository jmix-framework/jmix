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
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.slider.JmixDecimalSlider
import io.jmix.flowui.data.value.BufferedContainerValueSource
import io.jmix.flowui.exception.ValidationException
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
    UiComponents uiComponents

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

    def "DecimalSlider with required=true fails validation when the property value is absent"() {
        given: "The SliderView with a required DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The property value is absent"
        view.orderDc.item.total = null
        slider.executeValidators()

        then: "Validation fails"
        def e = thrown(ValidationException)
        e.detailsMessage == "requiredMessageString"

        and: "Component is not valid and shows the required message"
        slider.invalid
        slider.errorMessage == "requiredMessageString"
    }

    def "DecimalSlider with required=true passes validation when the property value is present"() {
        given: "The SliderView with a required DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The property value is present"
        view.orderDc.item.total = 42d
        slider.executeValidators()

        then: "Validation passes"
        noExceptionThrown()

        and: "Component is valid"
        !slider.invalid
    }

    def "DecimalSlider with required=true becomes valid when the property value is set to the minimum value"() {
        given: "The SliderView with a required DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The property value is absent"
        view.orderDc.item.total = null

        then: "Component is not valid"
        slider.invalid

        when: "The property value is set to the value the component already shows"
        view.orderDc.item.total = slider.min

        then: "Component is valid"
        !slider.invalid
    }

    def "DecimalSlider with required=true becomes invalid when the property value is cleared at the minimum value"() {
        given: "The SliderView with a required DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The property value is the minimum value the component already shows"
        view.orderDc.item.total = slider.min

        then: "Component is valid"
        !slider.invalid

        when: "The property value is cleared without changing the component value"
        view.orderDc.item.total = null

        then: "Component is not valid and shows the required message"
        slider.invalid
        slider.errorMessage == "requiredMessageString"
    }

    def "DecimalSlider with required=true and no value source fails validation until a value is set"() {
        given: "The SliderView with a required DecimalSlider that has no value source"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderUnboundRequiredId

        when: "No value has been set to the component"
        slider.executeValidators()

        then: "Validation fails although the component shows the minimum value"
        thrown(ValidationException)
        slider.invalid
        slider.errorMessage == "requiredMessageString"

        when: "A value is set to the component"
        slider.value = 55d
        slider.executeValidators()

        then: "Validation passes"
        noExceptionThrown()

        and: "Component is valid"
        !slider.invalid
    }

    def "DecimalSlider with required=true becomes valid when a value is set to the component"() {
        given: "The SliderView with a required DecimalSlider whose property value is absent"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId
        view.orderDc.item.total = null

        when: "A value is set to the component rather than to the property"
        slider.value = 42d

        then: "The property is updated and the required error is gone"
        view.orderDc.item.total == 42d
        !slider.invalid
    }

    def "DecimalSlider clears the property value instead of writing the minimum value"() {
        given: "The SliderView with a required DecimalSlider bound to a property that has a value"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The component is cleared"
        slider.clear()

        then: "The property value is absent and the component is empty"
        view.orderDc.item.total == null
        slider.isEmpty()
        slider.value == slider.min

        and: "Component is not valid"
        slider.invalid
    }

    def "DecimalSlider with required=true stays required while its value source is inactive"() {
        given: "The SliderView with a required DecimalSlider bound to a property"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId

        when: "The container has no item, so the value source cannot be written to"
        view.orderDc.setItem(null)

        then: "The component is empty and flagged, as any other required field bound to it would be"
        slider.isEmpty()
        slider.invalid
    }

    def "DecimalSlider passes the absent value to validators instead of the minimum value"() {
        given: "The SliderView with an empty DecimalSlider that has a positive validator"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderWithValidatorsId

        expect: "The component is empty although it shows the minimum value"
        slider.isEmpty()

        when: "Validators are executed"
        slider.executeValidators()

        then: "The absent value is valid, the substituted minimum value is not validated"
        noExceptionThrown()
        !slider.invalid
    }

    def "DecimalSlider writes the minimum value to the property although the component value does not change"() {
        given: "The SliderView with a required DecimalSlider whose property value is absent"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderRequiredId
        view.orderDc.item.total = null

        when: "The minimum value the component already shows is set to it"
        slider.value = slider.min

        then: "The property is updated and the required error is gone"
        view.orderDc.item.total == slider.min
        !slider.isEmpty()
        !slider.invalid
    }

    def "DecimalSlider re-runs validators when it becomes empty"() {
        given: "The SliderView with a DecimalSlider bound to a property and a validator rejecting the absent value"
        def view = navigateToView(SliderView)
        def slider = view.decimalSliderWithValueId
        slider.addValidator({ value ->
            if (value == null) {
                throw new ValidationException("absentValueMessage")
            }
        })

        when: "The component becomes empty without changing the value it shows"
        slider.clear()

        then: "The component is invalid, the validator has seen the absent value"
        slider.isEmpty()
        slider.invalid
        slider.errorMessage == "absentValueMessage"
    }

    def "DecimalSlider does not re-enter its binding with a value source that echoes writes"() {
        given: "A DecimalSlider bound to a buffered value source, which fires a value change event on every write"
        def view = navigateToView(SliderView)
        def slider = uiComponents.create(JmixDecimalSlider)
        slider.setMin(0d)
        slider.setMax(1000d)
        slider.setValueSource(new BufferedContainerValueSource<Order, Double>(view.orderDc, "total", true))

        when: "A value is set to the component"
        slider.value = 42d

        then: "The write is not echoed back into the binding"
        slider.value == 42d
    }
}
