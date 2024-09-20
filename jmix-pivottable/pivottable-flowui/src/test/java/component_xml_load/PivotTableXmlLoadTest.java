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

package component_xml_load;

import component_xml_load.view.PivotTableXmlLoadTestView;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.pivottableflowui.component.PivotTable;
import io.jmix.pivottableflowui.data.ContainerPivotTableItems;
import io.jmix.pivottableflowui.kit.component.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.PivotTableFlowuiTestConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"component_xml_load.view"})
@SpringBootTest(classes = {PivotTableFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class PivotTableXmlLoadTest {

    @Autowired
    protected ViewNavigationSupport navigationSupport;

    protected <T extends View<?>> T navigateTo(Class<T> view) {
        navigationSupport.navigate(view);
        return UiTestUtils.getCurrentView();
    }

    @Test
    @DisplayName("Load PivotTable from XML")
    public void loadPivotTableFromXmlTest() {
        PivotTable<?> pivotTable = navigateTo(PivotTableXmlLoadTestView.class).temperatureDataPivotTable;
        assertEquals(pivotTable.getId().orElse(null), "temperatureDataPivotTable");
        assertInstanceOf(ContainerPivotTableItems.class, pivotTable.getItems());
        assertTrue(pivotTable.isShowUI());
        assertEquals(Renderer.BAR_CHART, pivotTable.getRenderer());
        assertTrue(pivotTable.getAutoSortUnusedProperties());
        assertEquals(Order.VALUES_ASCENDING, pivotTable.getRowOrder());
        assertEquals(Order.VALUES_DESCENDING, pivotTable.getColumnOrder());
        assertEquals("Empty data msg", pivotTable.getEmptyDataMessage());
        assertEquals(10, pivotTable.getMenuLimit());
        assertTrue(pivotTable.isShowColumnTotals());
        assertFalse(pivotTable.isShowRowTotals());
        UnusedPropertiesVertical unusedPropertiesVertical = pivotTable.getUnusedPropertiesVertical();
        assertNotNull(unusedPropertiesVertical);
        assertTrue(unusedPropertiesVertical.getBoolVal());
        Renderers renderers = pivotTable.getRenderers();
        assertNotNull(renderers);
        assertEquals(Renderer.TREEMAP, renderers.getSelectedRenderer());
        assertEquals(3, renderers.getRenderers().size());
        assertEquals(Renderer.BAR_CHART, renderers.getRenderers().get(0));
        assertEquals(Renderer.TABLE, renderers.getRenderers().get(1));
        assertEquals(Renderer.TREEMAP, renderers.getRenderers().get(2));
        DerivedProperties derivedProperties = pivotTable.getDerivedProperties();
        assertNotNull(derivedProperties);
        assertEquals(1, derivedProperties.getProperties().entrySet().size());
        JsFunction fahrenheit = derivedProperties.getProperties().get("fahrenheit");
        assertNotNull(fahrenheit);
        assertEquals("function(record){return record.temperature * 1.8 + 32;}", fahrenheit.getCode());
        Aggregation aggregation = pivotTable.getAggregation();
        assertNotNull(aggregation);
        assertEquals("custom", aggregation.getCaption());
        assertTrue(aggregation.getCustom());
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
        assertEquals(2, aggregations.getAggregations().size());
        assertEquals("MAXIMUM", aggregations.getAggregations().get(0).getCaption());
        assertEquals(AggregationMode.MAXIMUM, aggregations.getAggregations().get(0).getMode());
        assertEquals(false, aggregations.getAggregations().get(0).getCustom());
        assertEquals("CUSTOM", aggregations.getAggregations().get(1).getCaption());
        assertNull(aggregations.getAggregations().get(1).getMode());
        assertEquals(true, aggregations.getAggregations().get(1).getCustom());
        assertNotNull(aggregations.getAggregations().get(1).getFunction());
        assertEquals("function(){return $.pivotUtilities.aggregatorTemplates.count()();}",
                aggregations.getAggregations().get(1).getFunction().getCode());
        RendererOptions rendererOptions = pivotTable.getRendererOptions();
        assertNotNull(rendererOptions);
        assertNotNull(rendererOptions.getC3());
        assertNotNull(rendererOptions.getC3().getSize());
        assertEquals(200, rendererOptions.getC3().getSize().getWidth());
        assertEquals(300, rendererOptions.getC3().getSize().getHeight());
        assertNotNull(rendererOptions.getHeatmap());
        assertNotNull(rendererOptions.getHeatmap().getColorScaleGeneratorFunction());
        assertEquals("function(values) { return \"rgb(0, 255, 0)\"; }",
                rendererOptions.getHeatmap().getColorScaleGeneratorFunction().getCode());
        assertNotNull(pivotTable.getFilterFunction());
        assertEquals("function(property) { return false; }", pivotTable.getFilterFunction().getCode());
        assertNotNull(pivotTable.getHiddenFromAggregations());
        assertEquals(1, pivotTable.getHiddenFromAggregations().size());
        assertEquals("city", pivotTable.getHiddenFromAggregations().get(0));
        assertNotNull(pivotTable.getHiddenFromDragDrop());
        assertEquals(1, pivotTable.getHiddenFromDragDrop().size());
        assertEquals("temperature", pivotTable.getHiddenFromDragDrop().get(0));
        assertNotNull(pivotTable.getHiddenProperties());
        assertEquals(1, pivotTable.getHiddenProperties().size());
        assertEquals("month", pivotTable.getHiddenProperties().get(0));
        assertNotNull(pivotTable.getSortersFunction());
        String sortersFunction = """
                function(property) {if (property == "%s") {return $.pivotUtilities.sortAs([6,5,4,3,2,1]);}}""";
        assertEquals(sortersFunction, pivotTable.getSortersFunction().getCode());
        assertNotNull(pivotTable.getProperties());
        assertNotNull(pivotTable.getProperties().get("temperature"));
        assertNotNull(pivotTable.getProperties().get("month"));
        assertNotNull(pivotTable.getProperties().get("city"));
        assertNotNull(pivotTable.getRows());
        assertEquals(1, pivotTable.getRows().size());
        assertEquals("month", pivotTable.getRows().get(0));
        assertNotNull(pivotTable.getColumns());
        assertEquals(1, pivotTable.getColumns().size());
        assertEquals("temperature", pivotTable.getColumns().get(0));
    }
}
