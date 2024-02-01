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

import component_xml_load.view.ChartDataSetTestView;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.data.ContainerChartItems;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.view.ViewControllerUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AbstractXmlLoadTest;
import test_support.ChartsFlowuiTestConfiguration;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"component_xml_load.view"})
@SpringBootTest(classes = {ChartsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ChartDataSetXmlLoadTest extends AbstractXmlLoadTest {

    @Test
    @DisplayName("DataSet with CollectionContainer, categoryField, valueFields")
    public void loadDataSetFromXmlTest() {
        ChartDataSetTestView view = navigateTo(ChartDataSetTestView.class);
        Chart chart = view.dataSetChartId;
        DataSet.Source<?> source = chart.getDataSet().getSource();

        assertNotNull(source);
        assertInstanceOf(ContainerChartItems.class, source.getDataProvider());

        ContainerChartItems<?> containerChartItems = (ContainerChartItems<?>) source.getDataProvider();
        CollectionContainer<?> itemsContainer = containerChartItems.getContainer();

        assertEquals(19, itemsContainer.getItems().size());
        assertEquals(
                ViewControllerUtils.getViewData(view).getContainer("transportDc"),
                itemsContainer
        );

        assertEquals("year", source.getCategoryField());
        assertLinesMatch(Stream.of("cars", "motorcycles", "bicycles"), source.getValueFields().stream());
    }
}
