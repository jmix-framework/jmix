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
import com.haulmont.cuba.gui.components.TextField;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
@Disabled
public class TextFieldDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        TextField textField = uiComponents.create(TextField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        textField.setDatasource(userDs, "name");

        // unbind
        textField.setDatasource(null, null);

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        textField.addValueChangeListener(listener);
        user.setName("anotherName");
    }

    @Test
    public void testUnsubscribeDsListener() {
        TextField textField = uiComponents.create(TextField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setName("testName");
        textField.setDatasource(userDs, "name");

        // unbind
        textField.setDatasource(null, null);

        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        textField.setValue("anotherName");
    }

    @Test
    public void testValueChangeListener() {
        TextField textField = uiComponents.create(TextField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setName("testName");

        // listener before datasource

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = textField.addValueChangeListener(listener);

        textField.setDatasource(userDs, "name");
        assertTrue(valueWasChanged[0]);

        // reset state
        valueWasChanged[0] = false;
        subscription.remove();
        textField.setDatasource(null, null);

        // datasource before listener
        textField.setDatasource(userDs, "name");
        textField.addValueChangeListener(listener);
        userDs.getItem().setName("anotherName");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        TextField textField = uiComponents.create(TextField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        textField.setDatasource(userDs, "name");

        // unbind
        textField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;

        // datasource before listener

        textField.setDatasource(userDs, "name");
        Subscription subscription = textField.addValueChangeListener(listener);
        user.setName("anotherName");
        assertTrue(valueWasChanged[0]);

        // reset state
        subscription.remove();
        textField.setDatasource(null, null);
        valueWasChanged[0] = false;
        textField.setValue("testName");

        // listener before datasource
        textField.addValueChangeListener(listener);
        textField.setDatasource(userDs, "name");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        TextField textField = uiComponents.create(TextField.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        userDs.getItem().setName("testName");
        textField.setDatasource(userDs, "name");

        // unbind
        textField.setDatasource(null, null);

        // setup
        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> listener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(listener);

        textField.setDatasource(userDs, "name");
        textField.setValue("anotherName");
        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        TextField textField = uiComponents.create(TextField.NAME);

        textField.setDatasource(null, null);
        textField.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        /*try { todo web tests
            textField.setDatasource(userDs1, null);
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);
        exceptionWasThrown = false;*/

        try {
            textField.setDatasource(null, "name");
        } catch (Exception e) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        textField.setDatasource(userDs1, "name");
        textField.setDatasource(userDs1, "name");

        userDs1.getItem().setName("Test name");
        textField.setDatasource(userDs1, "name");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        textField.setDatasource(userDs2, "name");
    }
}
