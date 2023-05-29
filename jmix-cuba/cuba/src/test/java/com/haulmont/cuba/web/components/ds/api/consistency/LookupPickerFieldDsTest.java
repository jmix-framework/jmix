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
import com.haulmont.cuba.gui.components.LookupPickerField;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
@Disabled
public class LookupPickerFieldDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        // unbind
        component.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        component.addValueChangeListener(listener);

        user.setGroup(metadata.create(Group.class));
    }

    @Test
    public void testUnsubscribeDsListener() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        // unbind
        component.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(listener);

        component.setValue(metadata.create(Group.class));
    }

    @Test
    public void testOptionsDsUnsubscribe() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        //unbind
        component.setOptionsDatasource(null);

        Datasource.ItemChangeListener<Group> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        groupsDs.addItemChangeListener(listener);

        component.setValue(groups.get(1));
    }

    @Test
    public void testSetValueWithoutOptions() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);

        User user = userDs.getItem();
        Group group = metadata.create(Group.class);
        group.setName("group #0");
        user.setGroup(group);

        component.setDatasource(userDs, "group");

        assertNotNull(component.getValue());
    }

    @Test
    public void testValueChangeListener() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        // listener before datasource

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = component.addValueChangeListener(listener);

        component.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);

        // reset state
        valueWasChanged[0] = false;
        subscription.remove();
        component.setDatasource(null, null);

        // datasource before listener

        component.setDatasource(userDs, "group");
        component.addValueChangeListener(listener);
        user.setGroup(null);
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        // unbind
        component.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;

        // datasource before listener

        component.setDatasource(userDs, "group");
        Subscription subscription = component.addValueChangeListener(listener);
        user.setGroup(metadata.create(Group.class));
        assertTrue(valueWasChanged[0]);

        // reset state
        component.setDatasource(null, null);
        subscription.remove();
        valueWasChanged[0] = false;
        component.setValue(null);

        // listener before datasource
        component.addValueChangeListener(listener);
        component.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        // unbind
        component.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);
        component.setDatasource(userDs, "group");

        component.setValue(null);
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeOptions() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        component.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        component.setDatasource(userDs, "group");

        //unbind
        component.setOptionsDatasource(null);

        Datasource.ItemChangeListener<Group> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        groupsDs.addItemChangeListener(listener);

        component.setValue(groups.get(1));

        //bind
        component.setOptionsDatasource(groupsDs);

        groupsDs.removeItemChangeListener(listener);
        boolean[] valueWasChanged = {false};
        listener = e -> valueWasChanged[0] = true;
        groupsDs.addItemChangeListener(listener);

        component.setValue(groups.get(2));
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        LookupPickerField component = uiComponents.create(LookupPickerField.NAME);

        component.setDatasource(null, null);
        component.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        /*try { todo web tests
            component.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
        exceptionWasThrown = false;*/
        try {
            component.setDatasource(null, "group");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        component.setDatasource(userDs1, "group");
        component.setDatasource(userDs1, "group");

        userDs1.getItem().setGroup(metadata.create(Group.class));
        component.setDatasource(userDs1, "group");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        component.setDatasource(userDs2, "group");

        component.setValue(null);
        assertNotNull(userDs1.getItem().getGroup());
    }
}
