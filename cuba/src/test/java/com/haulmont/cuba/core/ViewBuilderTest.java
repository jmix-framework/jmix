/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.Owner;
import com.haulmont.cuba.core.model.Pet;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.FetchPlanProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class ViewBuilderTest {

    @Test
    public void testBuild() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).build();

        assertNotNull(view);
        assertFalse(containsSystemProperties(view));
        assertFalse(view.containsProperty("name"));
    }

    @Test
    public void testProperty() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).add("name").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testRefProperty() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).add("owner").build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
    }

    @Test
    public void testInlineRefProperty() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class)
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
        FetchPlan view = FetchPlanBuilder.of(Pet.class)
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
        FetchPlan view = FetchPlanBuilder.of(Pet.class)
                .add("owner", FetchPlan.LOCAL)
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testProperties() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).addAll("name", "nick").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testSystem() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).addSystem().addAll("name").build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        view = FetchPlanBuilder.of(Pet.class).addSystem().addView(FetchPlan.LOCAL).build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testMinimal() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).addView(FetchPlan.MINIMAL).build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocal() {
        FetchPlan petView = FetchPlanBuilder.of(Pet.class).addView(FetchPlan.LOCAL).build();

        assertFalse(containsSystemProperties(petView));
        assertTrue(petView.containsProperty("name"));

        FetchPlan ownerView = FetchPlanBuilder.of(Owner.class).addView(FetchPlan.LOCAL).build();
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testBase() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class).addView(FetchPlan.BASE).build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocalAndRef() {
        FetchPlan view = FetchPlanBuilder.of(Pet.class)
                .addView(FetchPlan.LOCAL)
                .add("owner")
                .build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));

        view = FetchPlanBuilder.of(Pet.class)
                .addView(FetchPlan.LOCAL)
                .add("owner.name")
                .add("owner.address.city")
                .build();

        assertFalse(containsSystemProperties(view));
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
    public void testMerging() {
        FetchPlan view1 = FetchPlanBuilder.of(Pet.class)
                .add("owner", FetchPlan.LOCAL)
                .build();

        FetchPlan view2 = FetchPlanBuilder.of(Pet.class)
                .addView(view1)
                .add("name")
                .build();

        FetchPlanProperty ownerProp = view2.getProperty("owner");
        assertTrue(ownerProp != null && ownerProp.getFetchPlan() != null);
        assertTrue(ownerProp.getFetchPlan().containsProperty("name"));
    }

    private boolean containsSystemProperties(FetchPlan view) {
        return view.containsProperty("id")
                && view.containsProperty("version")
                && view.containsProperty("deleteTs")
                && view.containsProperty("deletedBy")
                && view.containsProperty("createTs")
                && view.containsProperty("createdBy")
                && view.containsProperty("updateTs")
                && view.containsProperty("updatedBy");
    }

}
