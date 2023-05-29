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

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.components.PickerField;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
@Disabled
public class PickerFieldDsTest extends AbstractComponentDsTest {

    @Autowired
    protected UiComponents uiComponents;

    @Test
    public void testUnsubscribeWithComponentListener() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = metadata.create(Group.class);
        group.setName("Test group");
        userDs.getItem().setGroup(group);
        pickerField.setDatasource(userDs, "group");

        // unbind
        pickerField.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        pickerField.addValueChangeListener(listener);
        userDs.getItem().setGroup(metadata.create(Group.class));
    }

    @Test
    public void testUnsubscribeWithDsListener() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = metadata.create(Group.class);
        group.setName("Test group");
        userDs.getItem().setGroup(group);
        pickerField.setDatasource(userDs, "group");

        // unbind
        pickerField.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(listener);
        pickerField.setValue(null);
    }

    @Test
    public void testValueChangeListener() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);

        User user = userDs.getItem();
        Group group = metadata.create(Group.class);
        group.setName("Test group");
        user.setGroup(group);

        // datasource before listener

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = pickerField.addValueChangeListener(listener);

        pickerField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);

        // reset state
        pickerField.setDatasource(null, null);
        subscription.remove();
        valueWasChanged[0] = false;
        pickerField.setValue(null);

        // listener before datasource
        pickerField.addValueChangeListener(listener);
        pickerField.setDatasource(userDs, "group");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);

        User user = userDs.getItem();
        Group group = metadata.create(Group.class);
        group.setName("Test group");
        user.setGroup(group);

        pickerField.setDatasource(userDs, "group");

        // unbind
        pickerField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;

        // datasource before listener

        pickerField.setDatasource(userDs, "group");
        Subscription subscription = pickerField.addValueChangeListener(listener);

        user.setGroup(null);
        assertTrue(valueWasChanged[0]);

        // reset state
        pickerField.setDatasource(null, null);
        subscription.remove();
        valueWasChanged[0] = false;
        user.setGroup(metadata.create(Group.class));

        // listener before datasource
        pickerField.addValueChangeListener(listener);
        pickerField.setDatasource(userDs, "group");

        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        Group group = metadata.create(Group.class);
        group.setName("Test group");
        userDs.getItem().setGroup(group);
        pickerField.setDatasource(userDs, "group");

        // unbind
        pickerField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);
        pickerField.setDatasource(userDs, "group");

        pickerField.setValue(null);
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        PickerField pickerField = uiComponents.create(PickerField.NAME);

        pickerField.setDatasource(null, null);
        pickerField.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        /*try { todo web tests
            pickerField.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);
        exceptionWasThrown = false;*/

        try {
            pickerField.setDatasource(null, "group");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        pickerField.setDatasource(userDs1, "group");
        pickerField.setDatasource(userDs1, "group");

        userDs1.getItem().setGroup(metadata.create(Group.class));
        pickerField.setDatasource(userDs1, "group");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        pickerField.setDatasource(userDs2, "group");

        pickerField.setValue(null);
        assertNotNull(userDs1.getItem().getGroup());
    }
}
