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

package attribute_security;

import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.chartsflowui.component.serialization.ChartSerializer;
import io.jmix.chartsflowui.data.ContainerChartItems;
import io.jmix.chartsflowui.data.item.EntityDataItem;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.component.serialization.ChartIncrementalChanges;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import io.jmix.chartsflowui.kit.data.chart.ListChartItems;
import io.jmix.core.Metadata;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import test_support.ChartsFlowuiTestConfiguration;
import test_support.TestEntityAttributeViewConstraint;
import test_support.entity.TransportCount;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {ChartsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class DataSetAttributeSecurityTest {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    DataComponents dataComponents;
    @Autowired
    Metadata metadata;
    @Autowired
    TestEntityAttributeViewConstraint attributeViewConstraint;

    @AfterEach
    void tearDown() {
        attributeViewConstraint.clear();
    }

    @Test
    void serializeDataSet_containerItemsWithDeniedFields_sendsNullInPlace() {
        attributeViewConstraint.denyView("TransportCount", "year");
        attributeViewConstraint.denyView("TransportCount", "cars");

        CollectionContainer<TransportCount> container = dataComponents.createCollectionContainer(TransportCount.class);
        container.setItems(List.of(createTransportCount()));

        JsonNode row = serializeDataSet(new ContainerChartItems<>(container)).get(0);

        assertEquals("{\"year\":null,\"cars\":null,\"motorcycles\":5,\"$k\":\"key\"}", row.toString());
    }

    @Test
    void serializeDataSet_listItemsOfEntitiesWithDeniedField_sendsNull() {
        attributeViewConstraint.denyView("TransportCount", "cars");

        ListChartItems<EntityDataItem> items = new ListChartItems<>(new EntityDataItem(createTransportCount()));

        JsonNode row = serializeDataSet(items).get(0);

        assertEquals("{\"year\":2020,\"cars\":null,\"motorcycles\":5,\"$k\":\"key\"}", row.toString());
    }

    @Test
    void serializeChangedItems_addedItemWithDeniedField_sendsNull() {
        attributeViewConstraint.denyView("TransportCount", "cars");

        ChartIncrementalChanges<EntityDataItem> changes = new ChartIncrementalChanges<>();
        changes.setSource(createSource(new ListChartItems<>()));
        changes.addAddedItems(List.of(new EntityDataItem(createTransportCount())));

        JsonNode row = createSerializer().serializeChangedItems(changes).get("add").get(0);

        assertEquals("{\"year\":2020,\"cars\":null,\"motorcycles\":5,\"$k\":\"key\"}", row.toString());
    }

    JsonNode serializeDataSet(DataProvider<EntityDataItem, ?> dataProvider) {
        DataSet dataSet = new DataSet().withSource(createSource(dataProvider));
        return createSerializer().serializeDataSet(dataSet).get("source");
    }

    DataSet.Source<EntityDataItem> createSource(DataProvider<EntityDataItem, ?> dataProvider) {
        return new DataSet.Source<EntityDataItem>()
                .withDataProvider(dataProvider)
                .withCategoryField("year")
                .withValueFields("cars", "motorcycles");
    }

    TransportCount createTransportCount() {
        TransportCount transportCount = metadata.create(TransportCount.class);
        transportCount.setYear(2020);
        transportCount.setCars(10);
        transportCount.setMotorcycles(5);
        transportCount.setBicycles(1);
        return transportCount;
    }

    ChartSerializer createSerializer() {
        Function<DataItem, String> itemKeyMapper = item -> "key";
        return applicationContext.getBean(ChartSerializer.class, itemKeyMapper);
    }
}
