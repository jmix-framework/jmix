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
import com.haulmont.cuba.gui.components.CheckBox;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
@Disabled
public class CheckBoxDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setActive(true);
        checkBox.setDatasource(userDs, "active");

        // unbind
        checkBox.setDatasource(null, null);

        Consumer valueChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        checkBox.addValueChangeListener(valueChangeListener);

        user.setActive(false);
        assertEquals(true, checkBox.getValue());
    }

    @Test
    public void testUnsubscribeDsListener() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setActive(true);
        checkBox.setDatasource(userDs, "active");

        // unbind
        checkBox.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        checkBox.setValue(false);
        assertEquals(true, user.getActive());
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setActive(true);
        checkBox.setDatasource(userDs, "active");

        // unbind
        checkBox.setDatasource(null, null);

        // datasource before listener
        checkBox.setDatasource(userDs, "active");
        assertEquals(true, checkBox.getValue());

        boolean[] valueWasChanged = {false};
        Consumer listener = e -> valueWasChanged[0] = true;
        Subscription valueChangeListenerSub = checkBox.addValueChangeListener(listener);

        user.setActive(false);
        assertTrue(valueWasChanged[0]);
        assertEquals(false, checkBox.getValue());

        // reset state
        valueChangeListenerSub.remove();
        checkBox.setDatasource(null, null);
        valueWasChanged[0] = false;
        checkBox.setValue(true);

        // listener before datasource

        checkBox.addValueChangeListener(listener);
        checkBox.setDatasource(userDs, "active");
        assertTrue(valueWasChanged[0]);
        assertEquals(false, checkBox.getValue());
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setActive(true);
        checkBox.setDatasource(userDs, "active");

        // unbind
        checkBox.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);
        checkBox.setDatasource(userDs, "active");

        checkBox.setValue(false);
        assertTrue(valueWasChanged[0]);
        assertEquals(false, user.getActive());
    }

    @Test
    public void testValueChangeListener() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setActive(true);

        // listener before datasource
        Boolean[] valueWasChanged = {false};
        Consumer listener = e -> valueWasChanged[0] = true;
        Subscription valueChangeListenerSub = checkBox.addValueChangeListener(listener);

        checkBox.setDatasource(userDs, "active");
        assertEquals(true, valueWasChanged[0]);

        // reset state
        checkBox.setDatasource(null, null);
        valueChangeListenerSub.remove();
        valueWasChanged[0] = false;

        // datasource before listener
        checkBox.setDatasource(userDs, "active");
        checkBox.addValueChangeListener(listener);

        user.setActive(false);
        assertEquals(true, valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        CheckBox checkBox = uiComponents.create(CheckBox.NAME);

        checkBox.setDatasource(null, null);
        checkBox.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        checkBox.setDatasource(userDs1, "active");
        checkBox.setDatasource(userDs1, "active");

        boolean exceptionWasThrown = false;
        /*try { todo web tests
            checkBox.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        exceptionWasThrown = false;*/
        try {
            checkBox.setDatasource(null, "active");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        userDs1.getItem().setActive(true);
        checkBox.setDatasource(userDs1, "active");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        checkBox.setDatasource(userDs2, "active");

        checkBox.setValue(false);
        assertEquals(true, userDs1.getItem().getActive());
    }
}
