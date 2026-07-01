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

package entity_states;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchMode;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.sales.Order;
import test_support.app.entity.sales.OrderLine;
import test_support.app.entity.sales.OrderLineA;
import test_support.app.entity.sales.Product;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
class EntityStatesCurrentFetchPlanTest {

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private Metadata metadata;

    /**
     * Regression test for Jmix issue 5238: repeated references to the same entity instance must not erase loaded
     * properties from the current fetch plan.
     */
    @Test
    void currentFetchPlanDoesNotEraseLoadedPropertiesForRepeatedEntityInstance() {
        Order order = metadata.create(Order.class);

        Product product = createProduct();

        OrderLine firstLine = metadata.create(OrderLine.class);
        firstLine.setProduct(product);
        firstLine.setQuantity(1);

        OrderLine secondLine = metadata.create(OrderLine.class);
        secondLine.setProduct(product);
        secondLine.setQuantity(2);

        order.setOrderLines(Arrays.asList(firstLine, secondLine));

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        assertProductNameIncluded(fetchPlan);
    }

    /**
     * Covers two Java instances representing the same entity id but having different loaded state. The more complete
     * clone must still contribute its loaded properties to the resulting fetch plan.
     */
    @Test
    void currentFetchPlanIncludesPropertiesFromMoreCompleteCloneWithSameEntityId() {
        Order order = metadata.create(Order.class);

        Product product = createProduct();

        OrderLine firstLine = metadata.create(OrderLine.class);
        firstLine.setQuantity(1);

        OrderLine secondLine = metadata.create(OrderLine.class);
        secondLine.setId(firstLine.getId());
        secondLine.setProduct(product);
        secondLine.setQuantity(2);

        order.setOrderLines(Arrays.asList(firstLine, secondLine));

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        assertProductNameIncluded(fetchPlan);
    }

    @Test
    void currentFetchPlanCutsCycleWithEmptyBackReferencePlan() {
        Order order = metadata.create(Order.class);

        OrderLine line = metadata.create(OrderLine.class);
        line.setOrder(order);
        line.setProduct(createProduct());
        line.setQuantity(1);

        order.setOrderLines(Collections.singletonList(line));

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        FetchPlan orderLinesFetchPlan = fetchPlan.getProperty("orderLines").getFetchPlan();
        assertNotNull(orderLinesFetchPlan);

        FetchPlan backReferenceFetchPlan = orderLinesFetchPlan.getProperty("order").getFetchPlan();
        assertNotNull(backReferenceFetchPlan);
        assertEquals(Order.class, backReferenceFetchPlan.getEntityClass());
        assertTrue(backReferenceFetchPlan.getProperties().isEmpty());

        assertProductNameIncluded(fetchPlan);
    }

    @Test
    void currentFetchPlanUsesDeclaredCollectionElementClassForSubclassItems() {
        Order order = metadata.create(Order.class);

        OrderLineA line = metadata.create(OrderLineA.class);
        line.setQuantity(1);
        line.setParam1("p1");

        order.setOrderLines(Collections.singletonList(line));

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        FetchPlan orderLinesFetchPlan = fetchPlan.getProperty("orderLines").getFetchPlan();
        assertNotNull(orderLinesFetchPlan);
        assertEquals(OrderLine.class, orderLinesFetchPlan.getEntityClass());
        assertTrue(orderLinesFetchPlan.containsProperty("quantity"));
        assertFalse(orderLinesFetchPlan.containsProperty("param1"));
    }

    @Test
    void currentFetchPlanIncludesLoadedEmptyCollectionAsEmptyNestedPlan() {
        Order order = metadata.create(Order.class);
        order.setOrderLines(Collections.emptyList());

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        FetchPlan orderLinesFetchPlan = fetchPlan.getProperty("orderLines").getFetchPlan();
        assertNotNull(orderLinesFetchPlan);
        assertEquals(OrderLine.class, orderLinesFetchPlan.getEntityClass());
        assertTrue(orderLinesFetchPlan.getProperties().isEmpty());
    }

    @Test
    void currentFetchPlanUsesUndefinedFetchModeForNestedProperties() {
        Order order = metadata.create(Order.class);

        OrderLine line = metadata.create(OrderLine.class);
        line.setProduct(createProduct());
        line.setQuantity(1);

        order.setOrderLines(Collections.singletonList(line));

        FetchPlan fetchPlan = entityStates.getCurrentFetchPlan(order);

        assertEquals(FetchMode.UNDEFINED, fetchPlan.getProperty("orderLines").getFetchMode());

        FetchPlan orderLinesFetchPlan = fetchPlan.getProperty("orderLines").getFetchPlan();
        assertNotNull(orderLinesFetchPlan);
        assertEquals(FetchMode.UNDEFINED, orderLinesFetchPlan.getProperty("product").getFetchMode());
    }

    private Product createProduct() {
        Product product = metadata.create(Product.class);
        product.setName("Product #1");
        product.setQuantity(10);
        return product;
    }

    private void assertProductNameIncluded(FetchPlan fetchPlan) {
        FetchPlan orderLinesFetchPlan = fetchPlan.getProperty("orderLines").getFetchPlan();
        assertNotNull(orderLinesFetchPlan);
        assertTrue(orderLinesFetchPlan.containsProperty("product"),
                "Current fetch plan must contain OrderLine.product");

        FetchPlan productFetchPlan = orderLinesFetchPlan.getProperty("product").getFetchPlan();
        assertNotNull(productFetchPlan);
        assertTrue(productFetchPlan.containsProperty("name"),
                "Current fetch plan must contain loaded Product.name");
    }
}
