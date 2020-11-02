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

import com.haulmont.cuba.core.global.ViewBuilder;
import com.haulmont.cuba.core.model.Owner;
import com.haulmont.cuba.core.model.Pet;
import com.haulmont.cuba.core.testsupport.CoreTest;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class ViewBuilderTest {

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Test
    public void testBuild() {
        FetchPlan view = ViewBuilder.of(Pet.class).build();

        assertNotNull(view);
        assertFalse(containsSystemProperties(view));
        assertFalse(view.containsProperty("name"));
    }

    @Test
    public void testProperty() {
        FetchPlan view = ViewBuilder.of(Pet.class).add("name").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testRefProperty() {
        FetchPlan view = ViewBuilder.of(Pet.class).add("owner").build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        FetchPlan ownerView = view.getProperty("owner").getFetchPlan();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
    }

    @Test
    public void testInlineRefProperty() {
        FetchPlan view = ViewBuilder.of(Pet.class)
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
        FetchPlan view = ViewBuilder.of(Pet.class)
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
        FetchPlan view = ViewBuilder.of(Pet.class)
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
        FetchPlan view = ViewBuilder.of(Pet.class).addAll("name", "nick").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testSystem() {
        FetchPlan view = ViewBuilder.of(Pet.class).addSystem().addAll("name").build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        view = ViewBuilder.of(Pet.class).addSystem().addView(FetchPlan.LOCAL).build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testMinimal() {
        FetchPlan view = ViewBuilder.of(Pet.class).addView(FetchPlan.INSTANCE_NAME).build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocal() {
        FetchPlan petView = ViewBuilder.of(Pet.class).addView(FetchPlan.LOCAL).build();

        assertTrue(containsSystemProperties(petView));
        assertTrue(petView.containsProperty("name"));

        FetchPlan ownerView = ViewBuilder.of(Owner.class).addView(FetchPlan.LOCAL).build();
        assertTrue(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testBase() {
        FetchPlan view = ViewBuilder.of(Pet.class).addView(FetchPlan.BASE).build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocalAndRef() {
        FetchPlan view = ViewBuilder.of(Pet.class)
                .addView(FetchPlan.LOCAL)
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

        view = ViewBuilder.of(Pet.class)
                .addView(FetchPlan.LOCAL)
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
    public void testMerging() {
        FetchPlan view1 = ViewBuilder.of(Pet.class)
                .add("owner", FetchPlan.LOCAL)
                .build();

        FetchPlan view2 = ViewBuilder.of(Pet.class)
                .addView(view1)
                .add("name")
                .build();

        FetchPlanProperty ownerProp = view2.getProperty("owner");
        assertTrue(ownerProp != null && ownerProp.getFetchPlan() != null);
        assertTrue(ownerProp.getFetchPlan().containsProperty("name"));
    }

    private boolean containsSystemProperties(FetchPlan view) {
        List<String> systemProperties = metadataTools.getSystemProperties(metadata.getClass(view.getEntityClass()));
        return systemProperties.stream().allMatch(view::containsProperty);
    }

}
