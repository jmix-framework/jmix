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

import com.vaadin.flow.component.shared.Tooltip
import component_xml_load.screen.GenericFilterView
import io.jmix.flowui.action.genericfilter.GenericFilterCopyAction
import io.jmix.flowui.component.SupportsResponsiveSteps
import io.jmix.flowui.component.genericfilter.inspector.FilterPropertiesInspector
import io.jmix.flowui.component.jpqlfilter.JpqlFilter
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.view.ViewControllerUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class GenericFilterXmlLoadTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load GenericFilter component from XML"() {
        when: "Open the GenericFilterView"
        def genericFilterView = navigateToView(GenericFilterView)

        then: "GenericFilter attributes will be loaded"
        verifyAll(genericFilterView.genericFilterId) {
            id.get() == "genericFilterId"

            dataLoader == ViewControllerUtils.getViewData(genericFilterView).getLoader("ordersDl")

            autoApply
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            !enabled
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            !opened
            summaryText == "summaryTextString"
            themeNames.containsAll(["small", "filled"])
            visible
            width == "100px"

            actions.size() == 3
        }
    }

    def "Load GenericFilter inner elements from XML"() {
        when: "Open the GenericFilterView"
        def genericFilterView = navigateToView(GenericFilterView)

        then: "GenericFilter inner attributes will be loaded"
        verifyAll(genericFilterView.genericFilterWithInnerElementsId) {
            id.get() == "genericFilterWithInnerElementsId"

            dataLoader == ViewControllerUtils.getViewData(genericFilterView).getLoader("ordersDl")

            (propertyFiltersPredicate as FilterPropertiesInspector).includedPropertiesRegexp == ".*"
            (propertyFiltersPredicate as FilterPropertiesInspector).excludedProperties.containsAll(["time", "date"])

            responsiveSteps.get(0).columns == 5
            responsiveSteps.get(0).labelsPosition == SupportsResponsiveSteps.ResponsiveStep.LabelsPosition.TOP
            responsiveSteps.get(0).minWidth == "100px"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM

            actions.size() == 1
            actions[0] instanceof GenericFilterCopyAction
        }
    }

    def "Load GenericFilter with conditions from XML"() {
        when: "Open the GenericFilterView"
        def genericFilterView = navigateToView(GenericFilterView)

        then: "GenericFilter conditions will be loaded"
        verifyAll(genericFilterView.genericFilterWithConditionsId) {
            id.get() == "genericFilterWithConditionsId"

            dataLoader == ViewControllerUtils.getViewData(genericFilterView).getLoader("ordersDl")

            conditions.get(0) instanceof PropertyFilter
            (conditions.get(0) as PropertyFilter).dataLoader == dataLoader
            (conditions.get(0) as PropertyFilter).property == "number"
            (conditions.get(0) as PropertyFilter).operation == PropertyFilter.Operation.EQUAL
            (conditions.get(0) as PropertyFilter).value == "1337"

            conditions.get(1) instanceof JpqlFilter
            (conditions.get(1) as JpqlFilter).dataLoader == dataLoader
            (conditions.get(1) as JpqlFilter).parameterClass == Void.class
            (conditions.get(1) as JpqlFilter).where == "{E}.number = '1337'"

            conditions.get(2) instanceof GroupFilter
            (conditions.get(2) as GroupFilter).getId().get() == "groupFilterId"
            (conditions.get(2) as GroupFilter).dataLoader == dataLoader
            (conditions.get(2) as GroupFilter).operation == LogicalFilterComponent.Operation.OR

            ((conditions.get(2) as GroupFilter).filterComponents.get(0) as JpqlFilter).parameterClass == String.class
            ((conditions.get(2) as GroupFilter).filterComponents.get(0) as JpqlFilter).dataLoader == dataLoader
            ((conditions.get(2) as GroupFilter).filterComponents.get(0) as JpqlFilter).parameterName == "parameterNameAsds"
            ((conditions.get(2) as GroupFilter).filterComponents.get(0) as JpqlFilter).value == "1337"
            ((conditions.get(2) as GroupFilter).filterComponents.get(0) as JpqlFilter).queryCondition.where
                    == "{E}.number = :parameterNameAsds"
        }
    }

    def "Load GenericFilter with configurations from XML"() {
        when: "Open the GenericFilterView"
        def genericFilterView = navigateToView(GenericFilterView)

        then: "GenericFilter conditions will be loaded"
        verifyAll(genericFilterView.genericFilterWithConfigurationsId) {
            id.get() == "genericFilterWithConfigurationsId"

            dataLoader == ViewControllerUtils.getViewData(genericFilterView).getLoader("ordersDl")

            configurations.get(0).id == "firstConfig"
            configurations.get(0).name == "firstConfigName"
            //verify 'default' attribute
            currentConfiguration == configurations.get(0)
            configurations.get(0).rootLogicalFilterComponent.operation == LogicalFilterComponent.Operation.OR
            configurations.get(0).rootLogicalFilterComponent.filterComponents.size() == 2

            configurations.get(1).id == "secondConfig"
            configurations.get(1).name == "secondConfigName"
            configurations.get(1).rootLogicalFilterComponent.operation == LogicalFilterComponent.Operation.AND
            configurations.get(1).rootLogicalFilterComponent.filterComponents.get(0) instanceof GroupFilter
        }
    }
}
