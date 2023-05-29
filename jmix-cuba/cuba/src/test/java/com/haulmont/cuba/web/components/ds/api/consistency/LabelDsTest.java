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
import com.haulmont.cuba.gui.components.Label;
import io.jmix.core.common.event.Subscription;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.HasValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

// todo rewrite with Spock
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
@Disabled
public class LabelDsTest extends AbstractComponentDsTest {

    @Test
    public void testUnsubscribeComponentListener() {
        Label label = uiComponents.create(Label.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        label.setDatasource(userDs, "name");

        // unbind
        label.setDatasource(null, null);
        assertNotNull(label.getValue());

        Consumer<HasValue.ValueChangeEvent> listener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        label.addValueChangeListener(listener);

        user.setName("anotherName");
        assertEquals("testName", label.getValue());
    }

    @Test
    public void testUnsubscribeDsListener() {
        Label label = uiComponents.create(Label.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        label.setDatasource(userDs, "name");

        // unbind
        label.setDatasource(null, null);
        assertNotNull(label.getValue());

        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> {
            throw new RuntimeException("Value was changed externally");
        };
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        label.setValue("anotherName");
        assertEquals("testName", user.getName());
    }

    @Test
    public void testValueChangeListener() {
        Label label = uiComponents.create(Label.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        label.setDatasource(userDs, "name");

        // listener after datasource
        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = label.addValueChangeListener(listener);

        user.setName("anotherName");
        assertTrue(valueWasChanged[0]);

        // reset state
        subscription.remove();
        label.setDatasource(null, null);
        valueWasChanged[0] = false;
        label.setValue("testName");

        // datasource after listener
        label.addValueChangeListener(listener);
        label.setDatasource(userDs, "name");

        assertTrue(valueWasChanged[0]);
    }

    @Test
    public void testUnsubscribeSubscribeComponentListener() {
        Label label = uiComponents.create(Label.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        label.setDatasource(userDs, "name");

        label.setDatasource(null, null);

        // datasource before listener
        label.setDatasource(userDs, "name");
        assertEquals("testName", label.getValue());

        boolean[] valueWasChanged = {false};
        Consumer<HasValue.ValueChangeEvent> listener = e -> valueWasChanged[0] = true;
        Subscription subscription = label.addValueChangeListener(listener);

        user.setName("anotherName");
        assertTrue(valueWasChanged[0]);
        assertEquals("anotherName", label.getValue());

        // reset state
        subscription.remove();
        label.setDatasource(null, null);
        valueWasChanged[0] = false;
        label.setValue("testName");

        // listener before datasource
        label.addValueChangeListener(listener);
        label.setDatasource(userDs, "name");
        assertTrue(valueWasChanged[0]);
        assertEquals("anotherName", label.getValue());
    }

    @Test
    public void testUnsubscribeSubscribeDsListener() {
        Label label = uiComponents.create(Label.NAME);

        Datasource<User> userDs = createTestDatasource(User.class);
        User user = userDs.getItem();
        user.setName("testName");
        label.setDatasource(userDs, "name");

        label.setDatasource(null, null);
        label.setDatasource(userDs, "name");

        boolean[] valueWasChanged = {false};
        Datasource.ItemPropertyChangeListener<User> propertyChangeListener = e -> valueWasChanged[0] = true;
        userDs.addItemPropertyChangeListener(propertyChangeListener);

        label.setValue("anotherName");
        assertTrue(valueWasChanged[0]);
        assertEquals("anotherName", user.getName());
    }

    @Test
    public void testDatasourceRepeatableAssign() {
        Label label = uiComponents.create(Label.NAME);

        label.setDatasource(null, null);
        label.setDatasource(null, null);

        Datasource<User> userDs1 = createTestDatasource(User.class);
        boolean exceptionWasThrown = false;
        try {
            label.setDatasource(userDs1, null);
        } catch (Exception ignored) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
        exceptionWasThrown = false;

        try {
            label.setDatasource(null, "name");
        } catch (Exception ignored) {
            exceptionWasThrown = true;
        }
        assertTrue(exceptionWasThrown);

        label.setDatasource(userDs1, "name");
        label.setDatasource(userDs1, "name");

        userDs1.getItem().setName("Test name");
        label.setDatasource(userDs1, "name");

        Datasource<User> userDs2 = createTestDatasource(User.class);
        label.setDatasource(userDs2, "name");

        label.setValue("Another name");
        assertEquals("Test name", userDs1.getItem().getName());
    }
}
