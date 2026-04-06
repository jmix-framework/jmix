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

package component_xml_load;

import component_xml_load.view.PivotTableXmlLoadTestView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.pivottableflowui.data.ContainerPivotTableItems;
import io.jmix.pivottableflowui.kit.component.model.Aggregation;
import io.jmix.pivottableflowui.kit.component.model.AggregationMode;
import io.jmix.pivottableflowui.kit.component.model.Aggregations;
import io.jmix.pivottableflowui.kit.component.model.DerivedProperties;
import io.jmix.pivottableflowui.kit.component.model.Order;
import io.jmix.pivottableflowui.kit.component.model.Renderer;
import io.jmix.pivottableflowui.kit.component.model.RendererOptions;
import io.jmix.pivottableflowui.kit.component.model.Renderers;
import io.jmix.pivottableflowui.kit.component.model.UnusedPropertiesVertical;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.PivotTableFlowuiTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UiTest(viewBasePackages = "component_xml_load.view")
@SpringBootTest(classes = PivotTableFlowuiTestConfiguration.class)
public class PivotTableXmlLoadTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Test
    @DisplayName("Load PivotTable from XML")
    void loadPivotTableFromXmlTest() {
        /*
         * Navigate to view
         */
        viewNavigators.view(UiTestUtils.getCurrentView(), PivotTableXmlLoadTestView.class)
                .navigate();
        PivotTableXmlLoadTestView view = UiTestUtils.getCurrentView();
        var pivotTable = view.temperatureDataPivotTable;

        /*
         * All PivotTable attributes should be loaded
         */
        assertEquals("temperatureDataPivotTable", pivotTable.getId().orElse(null));
        assertInstanceOf(ContainerPivotTableItems.class, pivotTable.getItems());
        assertEquals(Boolean.TRUE, pivotTable.isShowUI());
        assertEquals(Renderer.BAR_CHART, pivotTable.getRenderer());
        assertEquals(Boolean.TRUE, pivotTable.getAutoSortUnusedProperties());
        assertEquals(Order.VALUES_ASCENDING, pivotTable.getRowOrder());
        assertEquals(Order.VALUES_DESCENDING, pivotTable.getColumnOrder());
        assertEquals("Empty data msg", pivotTable.getEmptyDataMessage());
        assertEquals(10, pivotTable.getMenuLimit());
        assertEquals(Boolean.TRUE, pivotTable.isShowColumnTotals());
        assertEquals(Boolean.FALSE, pivotTable.isShowRowTotals());

        UnusedPropertiesVertical unusedPropertiesVertical = pivotTable.getUnusedPropertiesVertical();
        assertNotNull(unusedPropertiesVertical);
        assertEquals(Boolean.TRUE, unusedPropertiesVertical.getBoolVal());

        Renderers renderers = pivotTable.getRenderers();
        assertNotNull(renderers);
        assertEquals(Renderer.TREEMAP, renderers.getSelectedRenderer());
        assertNotNull(renderers.getRenderers());
        assertEquals(3, renderers.getRenderers().size());
        assertEquals(Renderer.BAR_CHART, renderers.getRenderers().get(0));
        assertEquals(Renderer.TABLE, renderers.getRenderers().get(1));
        assertEquals(Renderer.TREEMAP, renderers.getRenderers().get(2));

        DerivedProperties derivedProperties = pivotTable.getDerivedProperties();
        assertNotNull(derivedProperties);
        assertNotNull(derivedProperties.getProperties());
        assertEquals(1, derivedProperties.getProperties().size());
        var fahrenheit = derivedProperties.getProperties().get("fahrenheit");
        assertNotNull(fahrenheit);
        assertEquals("function(record){return record.temperature * 1.8 + 32;}", fahrenheit.getCode());

        Aggregation aggregation = pivotTable.getAggregation();
        assertNotNull(aggregation);
        assertEquals("custom", aggregation.getCaption());
        assertEquals(Boolean.TRUE, aggregation.getCustom());
        assertEquals(AggregationMode.MAXIMUM, aggregation.getMode());
        assertNotNull(aggregation.getFunction());
        assertEquals("function(){return $.pivotUtilities.aggregatorTemplates.count()();}",
                aggregation.getFunction().getCode());

        List<String> aggregationProperties = pivotTable.getAggregationProperties();
        assertNotNull(aggregationProperties);
        assertEquals(1, aggregationProperties.size());
        assertEquals("month", aggregationProperties.get(0));

        Aggregations aggregations = pivotTable.getAggregations();
        assertNotNull(aggregations);
        assertEquals(AggregationMode.LOWER_BOUND_80, aggregations.getSelectedAggregation());
        assertNotNull(aggregations.getAggregations());
        assertEquals(2, aggregations.getAggregations().size());
        assertEquals("MAXIMUM", aggregations.getAggregations().get(0).getCaption());
        assertEquals(AggregationMode.MAXIMUM, aggregations.getAggregations().get(0).getMode());
        assertFalse(aggregations.getAggregations().get(0).getCustom());
        assertEquals("CUSTOM", aggregations.getAggregations().get(1).getCaption());
        assertNull(aggregations.getAggregations().get(1).getMode());
        assertTrue(aggregations.getAggregations().get(1).getCustom());
        assertNotNull(aggregations.getAggregations().get(1).getFunction());
        assertEquals("function(){return $.pivotUtilities.aggregatorTemplates.count()();}",
                aggregations.getAggregations().get(1).getFunction().getCode());

        RendererOptions rendererOptions = pivotTable.getRendererOptions();
        assertNotNull(rendererOptions);
        assertNotNull(rendererOptions.getC3());
        assertNotNull(rendererOptions.getC3().getSize());
        assertEquals(200D, rendererOptions.getC3().getSize().getWidth());
        assertEquals(300D, rendererOptions.getC3().getSize().getHeight());
        assertNotNull(rendererOptions.getHeatmap());
        assertNotNull(rendererOptions.getHeatmap().getColorScaleGeneratorFunction());
        assertEquals("function(values) { return \"rgb(0, 255, 0)\"; }",
                rendererOptions.getHeatmap().getColorScaleGeneratorFunction().getCode());

        assertNotNull(pivotTable.getFilterFunction());
        assertEquals("function(property) { return false; }", pivotTable.getFilterFunction().getCode());

        List<String> hiddenFromAggregations = pivotTable.getHiddenFromAggregations();
        assertNotNull(hiddenFromAggregations);
        assertEquals(1, hiddenFromAggregations.size());
        assertEquals("city", hiddenFromAggregations.get(0));

        List<String> hiddenFromDragDrop = pivotTable.getHiddenFromDragDrop();
        assertNotNull(hiddenFromDragDrop);
        assertEquals(1, hiddenFromDragDrop.size());
        assertEquals("temperature", hiddenFromDragDrop.get(0));

        List<String> hiddenProperties = pivotTable.getHiddenProperties();
        assertNotNull(hiddenProperties);
        assertEquals(1, hiddenProperties.size());
        assertEquals("month", hiddenProperties.get(0));

        assertNotNull(pivotTable.getSortersFunction());
        String sortersFunction = "function(property) {if (property == \"%s\") {return $.pivotUtilities.sortAs([6,5,4,3,2,1]);}}";
        assertEquals(sortersFunction, pivotTable.getSortersFunction().getCode());

        assertNotNull(pivotTable.getProperties());
        assertNotNull(pivotTable.getProperties().get("temperature"));
        assertNotNull(pivotTable.getProperties().get("month"));
        assertNotNull(pivotTable.getProperties().get("city"));

        List<String> rows = pivotTable.getRows();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        assertEquals("month", rows.get(0));

        List<String> columns = pivotTable.getColumns();
        assertNotNull(columns);
        assertEquals(1, columns.size());
        assertEquals("temperature", columns.get(0));

        Map<String, List<String>> inclusions = pivotTable.getInclusions();
        assertNotNull(inclusions);
        List<String> monthValues = inclusions.get("month");
        assertNotNull(monthValues);
        assertEquals(1, monthValues.size());
        assertEquals("December", monthValues.get(0));

        Map<String, List<String>> exclusions = pivotTable.getExclusions();
        assertNotNull(exclusions);
        List<String> temperatureValues = exclusions.get("temperature");
        assertNotNull(temperatureValues);
        assertEquals(1, temperatureValues.size());
        assertEquals("-20", temperatureValues.get(0));
    }
}
