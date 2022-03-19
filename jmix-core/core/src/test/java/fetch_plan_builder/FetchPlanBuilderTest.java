/*
 * Copyright 2019 Haulmont.
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

package fetch_plan_builder;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.Owner;
import test_support.app.entity.Pet;
import test_support.app.entity.sales.Order;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class FetchPlanBuilderTest {

    @Autowired
    private FetchPlans fetchPlans;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private Metadata metadata;

    @Test
    public void testBuild() {
        FetchPlan view = fetchPlans.builder(Pet.class).build();

        assertNotNull(view);
        assertFalse(containsSystemProperties(view));
        assertFalse(view.containsProperty("name"));
    }

    @Test
    public void testProperty() {
        FetchPlan view = fetchPlans.builder(Pet.class).add("name").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testRefProperty() {
        FetchPlan view = fetchPlans.builder(Pet.class).add("owner").build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
    }

    @Test
    public void testInlineRefProperty() {
        FetchPlan view = fetchPlans.builder(Pet.class)
                .add("owner.name")
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
    }

    @Test
    public void testRefView() {
        FetchPlan view = fetchPlans.builder(Pet.class)
                .add("owner", builder -> builder.add("name"))
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
    }

    @Test
    public void testRefLocalView() {
        FetchPlan view = fetchPlans.builder(Pet.class)
                .add("owner", FetchPlan.LOCAL)
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertTrue(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testProperties() {
        FetchPlan view = fetchPlans.builder(Pet.class).addAll("name", "nick").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testSystem() {
        FetchPlan view = fetchPlans.builder(Pet.class).addSystem().addAll("name").build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        view = fetchPlans.builder(Pet.class).addSystem().addFetchPlan(FetchPlan.LOCAL).build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testInstanceName() {
        FetchPlan view = fetchPlans.builder(Pet.class).addFetchPlan(FetchPlan.INSTANCE_NAME).build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocal() {
        FetchPlan petView = fetchPlans.builder(Pet.class).addFetchPlan(FetchPlan.LOCAL).build();

        assertTrue(containsSystemProperties(petView));
        assertTrue(petView.containsProperty("name"));

        FetchPlan ownerView = fetchPlans.builder(Owner.class).addFetchPlan(FetchPlan.LOCAL).build();
        assertTrue(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testBase() {
        FetchPlan view = fetchPlans.builder(Pet.class).addFetchPlan(FetchPlan.BASE).build();

        assertTrue(containsSystemProperties(view));//all system properties of Pet is local and should be included to BASE View
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocalAndRef() {
        FetchPlan view = fetchPlans.builder(Pet.class)
                .addFetchPlan(FetchPlan.LOCAL)
                .add("owner")
                .build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));

        view = fetchPlans.builder(Pet.class)
                .addFetchPlan(FetchPlan.LOCAL)
                .add("owner.name")
                .add("owner.address.city")
                .build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertTrue(ownerView.containsProperty("address"));

        FetchPlan addressView = ownerView.getProperty("address").getFetchPlan();
        assertTrue(addressView.containsProperty("city"));
    }

    @Test
    public void testAddFetchPlan() {
        FetchPlan view1 = fetchPlans.builder(Pet.class)
                .add("owner", FetchPlan.LOCAL)
                .build();

        FetchPlan view2 = fetchPlans.builder(Pet.class)
                .addFetchPlan(view1)
                .add("name")
                .build();

        FetchPlanProperty ownerProp = view2.getProperty("owner");
        assertTrue(ownerProp != null && ownerProp.getFetchPlan() != null);
        assertTrue(ownerProp.getFetchPlan().containsProperty("name"));
    }

    @Test
    public void testMerge() {
        FetchPlan first = fetchPlans.builder(Pet.class)
                .add("name")
                .add("owner.address.city")
                .build();
        FetchPlan second = fetchPlans.builder(Pet.class)
                .add("nick")
                .add("owner.address.zip")
                .build();
        FetchPlan merged = fetchPlans.builder(first).merge(second).build();

        assertTrue(merged.containsProperty("name"));
        assertTrue(merged.containsProperty("nick"));
        assertTrue(merged.getProperty("owner").getFetchPlan().getProperty("address").getFetchPlan().containsProperty("city"));
        assertTrue(merged.getProperty("owner").getFetchPlan().getProperty("address").getFetchPlan().containsProperty("zip"));

        assertFalse(merged.getProperty("owner").getFetchPlan().containsProperty("name"));
    }

    @Test
    public void testFetchPlanForPropertyPath() {
        FetchPlan orderFP = fetchPlans.builder(Order.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("orderLines", FetchPlan.BASE)
                .add("orderLines.product", FetchPlan.BASE)
                .build();

        FetchPlan orderLinesFP = orderFP.getProperty("orderLines").getFetchPlan();
        assertTrue(containsBaseProperties(orderLinesFP));

        FetchPlan productFP = orderLinesFP.getProperty("product").getFetchPlan();
        assertTrue(containsBaseProperties(productFP));
    }

    @Test
    public void testFetchPlanWithFetchModeForPropertyPath() {
        FetchPlan orderFP = fetchPlans.builder(Order.class)
                .addFetchPlan(FetchPlan.BASE)
                .add("orderLines", FetchPlan.BASE, FetchMode.UNDEFINED)
                .add("orderLines.product", FetchPlan.BASE, FetchMode.UNDEFINED)
                .build();

        assertEquals(FetchMode.UNDEFINED, orderFP.getProperty("orderLines").getFetchMode());
        assertEquals(FetchMode.UNDEFINED, orderFP.getProperty("orderLines").getFetchPlan().getProperty("product").getFetchMode());
    }

    private boolean containsBaseProperties(FetchPlan fetchPlan) {
        Collection<MetaProperty> properties = metadata.getClass(fetchPlan.getEntityClass()).getProperties();
        return properties.stream()
                .filter(metaProperty -> !metaProperty.getRange().isClass())
                .map(MetadataObject::getName)
                .allMatch(fetchPlan::containsProperty);
    }

    private boolean containsSystemProperties(FetchPlan view) {
        List<String> systemProperties = metadataTools.getSystemProperties(metadata.getClass(view.getEntityClass()));
        return systemProperties.stream().allMatch(view::containsProperty);
    }


}
