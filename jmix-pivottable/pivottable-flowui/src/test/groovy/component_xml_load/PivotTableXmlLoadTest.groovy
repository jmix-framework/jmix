/*
 * Copyright 2024 Haulmont.
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

import component_xml_load.view.PivotTableXmlLoadTestView
import io.jmix.pivottableflowui.PivotTableFlowuiConfiguration
import io.jmix.pivottableflowui.data.ContainerPivotTableItems
import io.jmix.pivottableflowui.kit.component.model.AggregationMode
import io.jmix.pivottableflowui.kit.component.model.Order
import io.jmix.pivottableflowui.kit.component.model.Renderer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import test_support.PivotTableFlowuiTestSpecification

@SpringBootTest
@ContextConfiguration(classes = [PivotTableFlowuiConfiguration])
class PivotTableXmlLoadTest extends PivotTableFlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.view")
    }

    def "Load #container from XML"() {
        when: "Open the HtmlView"
        def view = navigateToView(PivotTableXmlLoadTestView.class)
        def pivotTable = view.temperatureDataPivotTable

        then: "All PivotTable attributes will be loaded"
        pivotTable.getId().orElse(null) == "temperatureDataPivotTable"
        pivotTable.items instanceof ContainerPivotTableItems
        pivotTable.isShowUI()
        pivotTable.renderer == Renderer.BAR_CHART
        pivotTable.autoSortUnusedProperties
        pivotTable.rowOrder == Order.VALUES_ASCENDING
        pivotTable.columnOrder == Order.VALUES_DESCENDING
        pivotTable.emptyDataMessage == "Empty data msg"
        pivotTable.menuLimit == 10
        pivotTable.isShowColumnTotals()
        !pivotTable.isShowRowTotals()

        def unusedPropertiesVertical = pivotTable.unusedPropertiesVertical
        unusedPropertiesVertical
        unusedPropertiesVertical.boolVal

        def renderers = pivotTable.renderers
        renderers.selectedRenderer == Renderer.TREEMAP
        renderers.renderers.size() == 3
        renderers.renderers.get(0) == Renderer.BAR_CHART
        renderers.renderers.get(1) == Renderer.TABLE
        renderers.renderers.get(2) == Renderer.TREEMAP

        def derivedProperties = pivotTable.derivedProperties
        derivedProperties
        derivedProperties.properties.entrySet().size() == 1
        def fahrenheit = derivedProperties.properties["fahrenheit"]
        fahrenheit
        fahrenheit.code == "function(record){return record.temperature * 1.8 + 32;}"

        def aggregation = pivotTable.aggregation
        aggregation
        aggregation.caption == "custom"
        aggregation.custom
        aggregation.mode == AggregationMode.MAXIMUM
        aggregation.function
        aggregation.function.code == "function(){return \$.pivotUtilities.aggregatorTemplates.count()();}"

        def aggregationProperties = pivotTable.aggregationProperties
        aggregationProperties
        aggregationProperties.size() == 1
        aggregationProperties.get(0) == "month"

        def aggregations = pivotTable.aggregations
        aggregations
        aggregations.selectedAggregation == AggregationMode.LOWER_BOUND_80
        aggregations.aggregations.size() == 2
        aggregations.aggregations.get(0).caption == "MAXIMUM"
        aggregations.aggregations.get(0).mode == AggregationMode.MAXIMUM
        !aggregations.aggregations.get(0).custom
        aggregations.aggregations.get(1).caption == "CUSTOM"
        aggregations.aggregations.get(1).mode == null
        aggregations.aggregations.get(1).custom
        aggregations.aggregations.get(1).function
        aggregations.aggregations.get(1).function.code ==
                "function(){return \$.pivotUtilities.aggregatorTemplates.count()();}"

        def rendererOptions = pivotTable.rendererOptions
        rendererOptions
        rendererOptions.c3
        rendererOptions.c3.size
        rendererOptions.c3.size.width == 200
        rendererOptions.c3.size.height == 300
        rendererOptions.heatmap
        rendererOptions.heatmap.colorScaleGeneratorFunction
        rendererOptions.heatmap.colorScaleGeneratorFunction.code == "function(values) { return \"rgb(0, 255, 0)\"; }"

        pivotTable.filterFunction
        pivotTable.filterFunction.code == "function(property) { return false; }"

        def hiddenFromAggregations = pivotTable.hiddenFromAggregations
        hiddenFromAggregations
        hiddenFromAggregations.size() == 1
        hiddenFromAggregations.get(0) == "city"

        def hiddenFromDragDrop = pivotTable.hiddenFromDragDrop
        hiddenFromDragDrop
        hiddenFromDragDrop.size() == 1
        hiddenFromDragDrop.get(0) == "temperature"

        def hiddenProperties = pivotTable.hiddenProperties
        hiddenProperties
        hiddenProperties.size() == 1
        hiddenProperties.get(0) == "month"

        pivotTable.sortersFunction
        def sortersFunction = """function(property) {if (property == "%s") {return \$.pivotUtilities.sortAs([6,5,4,3,2,1]);}}"""
        pivotTable.sortersFunction.code == sortersFunction

        pivotTable.properties
        pivotTable.properties["temperature"]
        pivotTable.properties["month"]
        pivotTable.properties["city"]

        def rows = pivotTable.rows
        rows
        rows.size() == 1
        rows.get(0) == "month"

        def columns = pivotTable.columns
        columns
        columns.size() == 1
        columns.get(0) == "temperature"

        def inclusions = pivotTable.getInclusions()
        def monthValues = inclusions.get("month")
        monthValues
        monthValues.size() == 1
        monthValues.get(0) == "December"

        def exclusions = pivotTable.getExclusions()
        def temperatureValues = exclusions.get("temperature")
        temperatureValues
        temperatureValues.size() == 1
        temperatureValues.get(0) == "-20"
    }
}