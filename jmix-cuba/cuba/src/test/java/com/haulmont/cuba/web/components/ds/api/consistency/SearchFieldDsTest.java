/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.cuba.web.components.ds.api.consistency;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.components.SearchField;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
@Disabled
public class SearchFieldDsTest extends AbstractComponentDsTest {

    @Autowired
    protected UiComponents uiComponents;

    @Test
    public void testUnsubscribeComponentListener() {
        SearchField searchField = uiComponents.create(SearchField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        searchField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        User user = userDs.getItem();
        user.setGroup(group);
        searchField.setDatasource(userDs, "group");

        // unbind
        searchField.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        searchField.addValueChangeListener(listener);

        user.setGroup(metadata.create(Group.class));
    }

    @Test
    public void testUnsubscribeDsListener() {
        SearchField searchField = uiComponents.create(SearchField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        searchField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        userDs.getItem().setGroup(group);
        searchField.setDatasource(userDs, "group");

        // unbind
        searchField.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(listener);

        searchField.setValue(metadata.create(Group.class));
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        SearchField searchField = uiComponents.create(SearchField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        searchField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        User user = userDs.getItem();
        user.setGroup(group);
        searchField.setDatasource(userDs, "group");

        // unbind
        searchField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;

        // datasource before listener
        searchField.setDatasource(userDs, "group");
        Subscription subscription = searchField.addValueChangeListener(listener);

        user.setGroup(metadata.create(Group.class));
        assertTrue(valueWasChanged[0]);

        // reset state

        valueWasChanged[0] = false;
        searchField.setDatasource(null, null);
        subscription.remove();
        searchField.setValue(null);

        // listener before datasource

        searchField.addValueChangeListener(listener);
        searchField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        SearchField searchField = uiComponents.create(SearchField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        searchField.setOptionsDatasource(groupsDs);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = groupsDs.getItems().iterator().next();
        userDs.getItem().setGroup(group);
        searchField.setDatasource(userDs, "group");

        // unbind
        searchField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);

        searchField.setDatasource(userDs, "group");
        searchField.setValue(null);
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testValueChangeListener() {
        SearchField searchField = uiComponents.create(SearchField.class);

        CollectionDatasource<Group, UUID> groupsDs = getTestCollectionDatasource();
        searchField.setOptionsDatasource(groupsDs);
        List<Group> groups = new ArrayList<>(groupsDs.getItems());

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setGroup(groups.get(0));

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        searchField.addValueChangeListener(listener);

        searchField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        SearchField searchField = uiComponents.create(SearchField.NAME);

        searchField.setDatasource(null, null);
        searchField.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        /*try { todo web tests
            searchField.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);
        exceptionWasThrown = false;*/

        try {
            searchField.setDatasource(null, "name");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        searchField.setDatasource(userDs1, "name");
        searchField.setDatasource(userDs1, "name");

        userDs1.getItem().setName("Test name");
        searchField.setDatasource(userDs1, "name");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        searchField.setDatasource(userDs2, "name");

        searchField.setValue(null);
        assertEquals("Test name", userDs1.getItem().getName());
    }
}
