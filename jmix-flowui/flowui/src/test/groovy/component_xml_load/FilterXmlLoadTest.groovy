/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.shared.Tooltip
import component_xml_load.screen.FilterView
import io.jmix.flowui.component.SupportsLabelPosition
import io.jmix.flowui.component.SupportsResponsiveSteps
import io.jmix.flowui.component.jpqlfilter.JpqlFilter
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.component.textarea.JmixTextArea
import io.jmix.flowui.view.ViewControllerUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FilterXmlLoadTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load PropertyFilter component from XML"() {
        when: "Open the FilterView"
        def filterView = navigateToView(FilterView)

        then: "PropertyFilter attributes will be loaded"
        verifyAll(filterView.propertyFilterId) {
            id.get() == "propertyFilterId"

            property == "number"
            operation == PropertyFilter.Operation.EQUAL
            dataLoader == ViewControllerUtils.getViewData(filterView).getLoader("ordersDl")

            autoApply
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            value == "1337"
            !enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            labelPosition == SupportsLabelPosition.LabelPosition.TOP
            labelVisible
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            !operationEditable
            operationTextVisible
            parameterName == "parameterNameRcftvgy"
            !readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            tabIndex == 3
            themeNames.containsAll(["themeNames1", "themeNames2"])
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

    def "Load PropertyFilter component with custom value component from XML"() {
        when: "Open the FilterView"
        def filterView = navigateToView(FilterView)

        then: "PropertyFilter and custom value component attributes will be loaded"
        verifyAll(filterView.propertyFilterWithCustomValueComponentId) {
            id.get() == "propertyFilterWithCustomValueComponentId"

            property == "number"
            operation == PropertyFilter.Operation.EQUAL
            dataLoader == ViewControllerUtils.getViewData(filterView).getLoader("ordersDl")

            getValueComponent() instanceof JmixTextArea
            (getValueComponent() as Component).id.orElse("")
                    == "propertyFilterWithCustomValueComponentId_valueComponent"
            getValueComponent().value == "1337"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load JpqlFilter component from XML"() {
        when: "Open the FilterView"
        def filterView = navigateToView(FilterView)

        then: "JpqlFilter attributes will be loaded"
        verifyAll(filterView.jpqlFilterId) {
            id.get() == "jpqlFilterId"

            parameterClass == Void.class
            dataLoader == ViewControllerUtils.getViewData(filterView).getLoader("ordersDl")

            autoApply
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            //noinspection GrEqualsBetweenInconvertibleTypes
            value == true
            !enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            labelPosition == SupportsLabelPosition.LabelPosition.TOP
            labelVisible
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            parameterName == "parameterNameAsdsd"
            !readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            tabIndex == 3
            themeNames.containsAll(["themeNames1", "themeNames2"])
            visible
            width == "100px"

            queryCondition.where == "{E}.number = '1337'"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load JpqlFilter component with custom value component from XML"() {
        when: "Open the FilterView"
        def filterView = navigateToView(FilterView)

        then: "JpqlFilter and custom value component attributes will be loaded"
        verifyAll(filterView.jpqlFilterWithCustomValueComponentId) {
            id.get() == "jpqlFilterWithCustomValueComponentId"

            parameterClass == String.class
            dataLoader == ViewControllerUtils.getViewData(filterView).getLoader("ordersDl")

            getValueComponent() instanceof JmixTextArea
            (getValueComponent() as Component).id.orElse("")
                    == "jpqlFilterWithCustomValueComponentId_valueComponent"
            getValueComponent().getValue() == "1337"

            queryCondition.where == "{E}.number = :parameterNameAsdasda"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load GroupFilter component from XML"() {
        when: "Open the FilterView"
        def filterView = navigateToView(FilterView)

        then: "JpqlFilter attributes will be loaded"
        verifyAll(filterView.groupFilterId) {
            id.get() == "groupFilterId"

            operation == LogicalFilterComponent.Operation.AND
            dataLoader == ViewControllerUtils.getViewData(filterView).getLoader("ordersDl")

            autoApply
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            !enabled
            operationTextVisible
            summaryText == "summaryTextString"
            visible

            filterComponents.get(0) instanceof PropertyFilter
            (filterComponents.get(0) as PropertyFilter).property == "number"
            (filterComponents.get(0) as PropertyFilter).operation == PropertyFilter.Operation.EQUAL
            (filterComponents.get(0) as PropertyFilter).value == "1337"
            (filterComponents.get(0) as PropertyFilter).dataLoader == dataLoader

            filterComponents.get(1) instanceof JpqlFilter
            (filterComponents.get(1) as JpqlFilter).parameterClass == Void.class
            (filterComponents.get(1) as JpqlFilter).where == "{E}.number = '1337'"

            responsiveSteps.get(0).getColumns() == 5
            responsiveSteps.get(0).getLabelsPosition() == SupportsResponsiveSteps.ResponsiveStep.LabelsPosition.TOP
            responsiveSteps.get(0).getMinWidth() == "100px"
        }
    }
}
