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

import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.components.DateField;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
@Disabled
public class DateFieldDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        DateField dateField = uiComponents.create(DateField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();

        Date updateTs = new Date();
        user.setUpdateTs(updateTs);
        dateField.setDatasource(userDs, "updateTs");
        dateField.setResolution(DateField.Resolution.SEC);

        // unbind
        dateField.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> valueChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        dateField.addValueChangeListener(valueChangeListener);

        user.setUpdateTs(new Date());
        assertEquals(updateTs, dateField.getValue());
    }

    @Test
    public void testUnsubscribeDsListener() {
        DateField dateField = uiComponents.create(DateField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        Date updateTs = new Date();
        user.setUpdateTs(updateTs);
        dateField.setDatasource(userDs, "updateTs");

        // unbind
        dateField.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        dateField.setValue(new Date());
        assertEquals(updateTs, user.getUpdateTs());
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        DateField dateField = uiComponents.create(DateField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        Date updateTs = new Date();
        user.setUpdateTs(updateTs);
        dateField.setDatasource(userDs, "updateTs");

        // unbind
        dateField.setDatasource(null, null);

        // datasource before listener
        dateField.setDatasource(userDs, "updateTs");
        dateField.setResolution(DateField.Resolution.SEC);
        assertEquals(updateTs, dateField.getValue());

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = dateField.addValueChangeListener(listener);

        Date updateTs2 = new Date();
        user.setUpdateTs(updateTs2);
        assertTrue(valueWasChanged[0]);
        assertEquals(updateTs2, dateField.getValue());

        // reset state
        subscription.remove();
        dateField.setDatasource(null, null);
        valueWasChanged[0] = false;
        dateField.setValue(updateTs);

        // listener before datasource

        dateField.addValueChangeListener(listener);
        dateField.setDatasource(userDs, "updateTs");
        dateField.setResolution(DateField.Resolution.SEC);
        assertTrue(valueWasChanged[0]);
        // assertEquals(updateTs2, dateField.getValue()); todo web tests
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        DateField dateField = uiComponents.create(DateField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        Date updateTs = new Date();
        user.setUpdateTs(updateTs);
        dateField.setDatasource(userDs, "updateTs");

        // unbind
        dateField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);
        dateField.setDatasource(userDs, "updateTs");
        dateField.setResolution(DateField.Resolution.SEC);

        Date updateTs2 = new Date();
        dateField.setValue(updateTs2);
        assertTrue(valueWasChanged[0]);
        assertEquals(updateTs2, user.getUpdateTs());
    }

    @Test
    public void testValueChangeListener() {
        DateField dateField = uiComponents.create(DateField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setUpdateTs(new Date());

        // listener before datasource
        Boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = dateField.addValueChangeListener(listener);

        dateField.setDatasource(userDs, "updateTs");
        assertEquals(true, valueWasChanged[0]);

        // reset state
        dateField.setDatasource(null, null);
        subscription.remove();
        valueWasChanged[0] = false;

        // datasource before listener
        dateField.setDatasource(userDs, "updateTs");
        dateField.addValueChangeListener(listener);

        user.setUpdateTs(new Date());
        assertEquals(true, valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        DateField dateField = uiComponents.create(DateField.NAME);

        dateField.setDatasource(null, null);
        dateField.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        dateField.setDatasource(userDs1, "updateTs");
        dateField.setDatasource(userDs1, "updateTs");

        boolean exceptionWasThrown = false;
        /*try { todo web tests
            dateField.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        exceptionWasThrown = false;*/
        try {
            dateField.setDatasource(null, "updateTs");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        Date updateTs = new Date();
        userDs1.getItem().setUpdateTs(updateTs);
        dateField.setDatasource(userDs1, "updateTs");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        dateField.setDatasource(userDs2, "updateTs");

        dateField.setValue(new Date());
        assertEquals(updateTs, userDs1.getItem().getUpdateTs());
    }
}
