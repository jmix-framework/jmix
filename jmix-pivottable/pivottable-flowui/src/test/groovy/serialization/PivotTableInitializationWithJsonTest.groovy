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

package serialization


import io.jmix.pivottableflowui.kit.component.JmixPivotTable
import io.jmix.pivottableflowui.kit.component.model.AggregationMode
import io.jmix.pivottableflowui.kit.component.model.Order
import io.jmix.pivottableflowui.kit.component.model.Renderer
import spock.lang.Specification

class PivotTableInitializationWithJsonTest extends Specification {

    def "Initialize PivotTable with json"() {
        when: "Set json to the PivotTable"

        def pivotTable = new JmixPivotTable<>()
        pivotTable.setJsonOptions(
                """
                {
                    "showUI": "true",
                    "renderer": "barChart",
                    "autoSortUnusedProperties": "true",
                    "columnOrder": "key_a_to_z",
                    "rowOrder": "value_z_to_a",
                    "emptyDataMessage": "Empty data msg",
                    "menuLimit": "10",
                    "showColumnTotals": "true",
                    "showRowTotals": "false",
                    "unusedPropertiesVertical": "true",
                    "renderers": {
                        "selected": "treemap",
                        "renderers": ["barChart", "table", "treemap"]
                    },
                    "derivedProperties": {
                        "properties": {
                            "fahrenheit" : "function(record){return record.temperature * 1.8 + 32;}"
                        }
                    },
                    "aggregation": {
                        "caption": "custom",
                        "mode": "maximum",
                        "custom": "true",
                        "function":  "function(){return \$.pivotUtilities.aggregatorTemplates.count()();}"
                    },
                    "aggregationProperties": ["month"],
                    "aggregations": {
                        "selected": "lowerBound80",
                        "aggregations": [
                            {
                                "caption": "MAXIMUM",
                                "mode": "maximum",
                                "custom": "false"
                            },
                            {
                                "caption": "CUSTOM",
                                "custom": "true",
                                "function": "function(){return \$.pivotUtilities.aggregatorTemplates.count()();}"
                            }
                        ]
                    },
                    "rendererOptions": {
                        "c3": {
                            "size": {
                                "width": "200",
                                "height": "300"
                            }
                        },
                        "heatmap": {
                            "colorScaleGeneratorFunction": "function(values) { return \\"rgb(0, 255, 0)\\"; }"
                        }
                    },
                    "filterFunction": "function(property) { return false; }",
                    "hiddenFromAggregations": ["city"],
                    "hiddenFromDragDrop": ["temperature"],
                    "hiddenProperties": ["month"],
                    "sortersFunction": "function(property) {if (property == \\"%s\\") {return \$.pivotUtilities.sortAs([6,5,4,3,2,1]);}}",
                    "properties": {
                        "temperature": "Temperature",
                        "month" : "Month",
                        "city" : "City"
                    },
                    "rows": ["month"],
                    "columns": ["temperature"],
                    "inclusions": {
                        "month" : ["December"]
                    },
                    "exclusions": {
                        "temperature" : ["-20"]
                    }
                }
                """
        )

        then: "All component properties are initialized"

        pivotTable.isShowUI()
        pivotTable.renderer == Renderer.BAR_CHART
        pivotTable.autoSortUnusedProperties
        pivotTable.rowOrder == Order.VALUES_DESCENDING
        pivotTable.columnOrder == Order.KEYS_ASCENDING
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
