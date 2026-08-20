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
import component_xml_load.screen.RangeSliderView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DecimalRangeSliderXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load decimalRangeSlider component from XML"() {
        when: "Open the RangeSliderView"
        def view = navigateToView(RangeSliderView)

        then: "DecimalRangeSlider attributes will be loaded"
        verifyAll(view.decimalRangeSliderId) {
            id.get() == "decimalRangeSliderId"
            accessibleNameStart.get() == "accessibleNameStartString"
            accessibleNameEnd.get() == "accessibleNameEndString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            max == 100d
            maxHeight == "55px"
            maxWidth == "120px"
            min == 0d
            minHeight == "40px"
            minWidth == "80px"
            minMaxVisible
            readOnly
            requiredIndicatorVisible
            step == 0.5d
            tabIndex == 3
            value.start() == 20.5d
            value.end() == 80.5d
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

    def "Load decimalRangeSlider component with the end value only from XML"() {
        when: "Open the RangeSliderView"
        def view = navigateToView(RangeSliderView)

        then: "The start value of DecimalRangeSlider is the min value"
        verifyAll(view.decimalRangeSliderWithEndValueId) {
            id.get() == "decimalRangeSliderWithEndValueId"
            value.start() == 0d
            value.end() == 20d
        }
    }
}
