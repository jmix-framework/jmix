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
 */

package com.haulmont.cuba.web.components.ds.api.consistency;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.components.LookupField;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
@Disabled
public class LookupFieldDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        User user = userDs.getItem();
        user.setGroup(group);
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        lookupField.addValueChangeListener(listener);

        user.setGroup(metadata.create(Group.class));
    }

    @Test
    public void testUnsubscribeDsListener() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        userDs.getItem().setGroup(group);
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        lookupField.setValue(metadata.create(Group.class));
    }

    @Test
    public void testOptionsDsUnsubscribe() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setGroup(groups.get(0));
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setOptionsDatasource(null);

        Datasource.ItemChangeListener<Group> itemChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        groupsDs.addItemChangeListener(itemChangeListener)  ;
        lookupField.setValue(groups.get(1));
    }

    @Test
    public void testSetValueWithoutOptions() {
        LookupField lookupField = uiComponents.create(LookupField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);

        User user = userDs.getItem();
        user.setName("Test name");

        lookupField.setDatasource(userDs, "name");
        assertEquals("Test name", lookupField.getValue());
    }

    @Test
    public void testValueChangeListener() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        lookupField.addValueChangeListener(listener);

        lookupField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        User user = userDs.getItem();
        user.setGroup(group);
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;

        // datasource before listener
        lookupField.setDatasource(userDs, "group");
        Subscription subscription = lookupField.addValueChangeListener(listener);

        user.setGroup(metadata.create(Group.class));
        assertTrue(valueWasChanged[0]);

        // reset state

        valueWasChanged[0] = false;
        lookupField.setDatasource(null, null);
        subscription.remove();
        lookupField.setValue(null);

        // listener before datasource

        lookupField.addValueChangeListener(listener);
        lookupField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        userDs.getItem().setGroup(group);
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);

        lookupField.setDatasource(userDs, "group");
        lookupField.setValue(null);
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeOptions() {
        LookupField lookupField = uiComponents.create(LookupField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        lookupField.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setGroup(groups.get(0));
        lookupField.setDatasource(userDs, "group");

        // unbind
        lookupField.setOptionsDatasource(null);

        Datasource.ItemChangeListener<Group> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        groupsDs.addItemChangeListener(listener);
        lookupField.setValue(groups.get(1));

        // setup
        groupsDs.removeItemChangeListener(listener);
        boolean[] valueWasChanged = {false};
        listener = e -> valueWasChanged[0] = true;
        groupsDs.addItemChangeListener(listener);
        lookupField.setOptionsDatasource(groupsDs);

        lookupField.setValue(groups.get(2));
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        LookupField lookupField = uiComponents.create(LookupField.NAME);

        lookupField.setDatasource(null, null);
        lookupField.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        /*try { todo web tests
            lookupField.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);*/
        exceptionWasThrown = false;
        try {
            lookupField.setDatasource(null, "name");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        lookupField.setDatasource(userDs1, "name");
        lookupField.setDatasource(userDs1, "name");

        userDs1.getItem().setName("Test name");
        lookupField.setDatasource(userDs1, "name");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        lookupField.setDatasource(userDs2, "name");

        lookupField.setValue(null);
        assertEquals("Test name", userDs1.getItem().getName());
    }
}
