/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.web.components;

import com.haulmont.cuba.gui.components.OptionsGroup;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import io.jmix.core.FetchPlan;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
@Disabled
public class OptionsGroupTest extends AbstractComponentTest {

    @Test
    public void testNew() {
        Component component = create(OptionsGroup.NAME);
        assertNotNull(component);
        assertTrue(component instanceof OptionsGroup);
    }

    @Test
    public void testGetSetValue() {
        OptionsGroup component = create(OptionsGroup.class);

        assertNull(component.getValue());

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")));
        component.setValue("One");

        assertEquals("One", component.getValue());
    }

    @Test
    public void testSetToReadonly() {
        OptionsGroup component = create(OptionsGroup.class);

        component.setEditable(false);
        assertFalse(component.isEditable());

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")));
        component.setValue("One");

        assertEquals("One", component.getValue());
        assertFalse(component.isEditable());
    }

    @Test
    public void testSetToReadonlyFromValueListener() {
        final OptionsGroup component = create(OptionsGroup.class);

        assertTrue(component.isEditable());

        component.addValueChangeListener(e -> component.setEditable(false));

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")));
        component.setValue("One");

        assertEquals("One", component.getValue());
        assertFalse(component.isEditable());
    }

    @Test
    public void testDatasource() {
        OptionsGroup component = create(OptionsGroup.class);

        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource();

        testDs.setItem(new User());
        ((DatasourceImpl) testDs).valid();

        assertNull(component.getValue());

        component.setDatasource(testDs, "group");
        assertNotNull(component.getDatasource());
    }

    @Test
    public void testOptionsDatasource() {
        OptionsGroup component = create(OptionsGroup.class);

        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource();

        testDs.setItem(new User());
        ((DatasourceImpl) testDs).valid();

        assertNull(component.getValue());
        Group g = new Group();
        g.setName("Group 0");
        testDs.getItem().setGroup(g);

        //noinspection unchecked
        CollectionDatasource<Group, UUID> groupsDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(Group.class)
                .setView(viewRepository.getFetchPlan(Group.class, FetchPlan.LOCAL))
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .setAllowCommit(false)
                .buildCollectionDatasource();

        Group g1 = new Group();
        g1.setName("Group 1");
        groupsDs.includeItem(g1);
        Group g2 = new Group();
        g2.setName("Group 2");
        groupsDs.includeItem(g2);

        component.setOptionsDatasource(groupsDs);
        component.setValue(g2);

        assertEquals(g2, component.getValue());

        component.setDatasource(testDs, "group");
        component.setValue(g);
        assertEquals(g, testDs.getItem().getGroup());

        component.setValue(g1);
        assertEquals(g1, testDs.getItem().getGroup());

        testDs.getItem().setGroup(g2);
        assertEquals(g2, component.getValue());
    }

    @Test
    public void testValueChangeListener() {
        OptionsGroup component = create(OptionsGroup.class);

        final AtomicInteger counter = new AtomicInteger(0);

        assertNull(component.getValue());

        Consumer<HasValue.ValueChangeEvent> listener1 = e -> {
            assertNull(e.getPrevValue());
            assertEquals("Two", e.getValue());

            counter.addAndGet(1);
        };
        Subscription subscription = component.addValueChangeListener(listener1);

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")));
        component.setValue("Two");

        subscription.remove();

        assertEquals(1, counter.get());

        Consumer<HasValue.ValueChangeEvent> listener2 = e -> {
            assertEquals("Two", e.getPrevValue());
            assertEquals("One", e.getValue());

            counter.addAndGet(1);
        };
        subscription = component.addValueChangeListener(listener2);

        component.setValue("One");
        assertEquals("One", component.getValue());

        assertEquals(2, counter.get());

        subscription.remove();
        Consumer<HasValue.ValueChangeEvent> listener3 = e -> {
            assertEquals("One", e.getPrevValue());
            assertEquals("Three", e.getValue());

            counter.addAndGet(1);
        };
        component.addValueChangeListener(listener3);

        component.setValue("Three");
        assertEquals(3, counter.get());
    }
}
