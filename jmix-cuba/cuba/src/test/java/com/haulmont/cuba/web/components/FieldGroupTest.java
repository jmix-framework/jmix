/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.web.components;

import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.vaadin.ui.GridLayout;
import io.jmix.core.FetchPlan;
import com.haulmont.cuba.core.model.common.User;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.TextArea;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

// todo rewrite with Spock
@SuppressWarnings({"deprecation", "rawtypes"})
@Disabled
public class FieldGroupTest extends AbstractComponentTest {

    @Test
    public void newFieldGroup() {
        Component component = create(FieldGroup.NAME);
        assertTrue(component instanceof FieldGroup);
    }

    @Test
    public void initFields() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        FieldGroup.FieldConfig fc = fieldGroup.createField("name");
        fc.setProperty("name");
        fc.setDatasource(createTestDs());

        fieldGroup.addField(fc);
        fieldGroup.bind();
    }

    @Test
    public void initFieldsWithProperties() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.bind();

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertEquals(fcName, fieldGroup.getField("name"));
        assertEquals(fcLogin, fieldGroup.getField("login"));
    }

    @Test
    public void initWithCustomFields() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(create(TextField.class));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(create(TextField.class));
        fieldGroup.addField(fcLogin);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));
    }

    @Test
    public void add() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(create(TextField.class));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(create(TextField.class));
        fieldGroup.addField(fcLogin);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(2, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup, 0, 1));
    }

    @Test
    public void addWithColumn() {
        FieldGroup fieldGroup = create(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(create(TextField.class));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(create(TextField.class));
        fieldGroup.addField(fcLogin, 1);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(2, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup, 1, 0));
    }

    @Test
    public void addWithColumnAndRow() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setComponent(create(TextField.class));
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setComponent(create(TextField.class));
        fieldGroup.addField(fcLogin, 0, 0);

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(2, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 1));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup, 0, 0));
    }

    @Test
    public void removeField() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.removeField("login");

        assertEquals(fcName, fieldGroup.getField("name"));
        assertNull(fieldGroup.getField("login"));

        fieldGroup.bind();

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
    }

    @Test
    public void removeBoundField() {
        FieldGroup fieldGroup = create(FieldGroup.class);

        Datasource<User> testDs = createTestDs();

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fcName.setDatasource(testDs);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fcLogin.setDatasource(testDs);
        fieldGroup.addField(fcLogin);

        fieldGroup.bind();

        fieldGroup.removeField("login");

        assertEquals(fcName, fieldGroup.getField("name"));
        assertNull(fieldGroup.getField("login"));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(1, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
    }

    @Test
    public void addWithSet() {
        FieldGroup fieldGroup = create(FieldGroup.class);
        fieldGroup.setColumns(2);

        FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
        fcName.setProperty("name");
        fcName.setEditable(false);
        fieldGroup.addField(fcName);

        FieldGroup.FieldConfig fcLogin = fieldGroup.createField("login");
        fcLogin.setProperty("login");
        fcLogin.setEnabled(false);
        fcLogin.setVisible(false);
        fieldGroup.addField(fcLogin, 1);

        fcName.setComponent(create(TextField.class));
        fcLogin.setComponent(create(TextField.class));

        assertNotNull(fcLogin.getComponent());
        assertNotNull(fcName.getComponent());

        assertNotNull(getComposition(fcName));
        assertNotNull(getComposition(fcLogin));

        assertEquals(2, fieldGroup.getOwnComponents().size());
        assertTrue(fieldGroup.getOwnComponents().contains(fcName.getComponent()));
        assertTrue(fieldGroup.getOwnComponents().contains(fcLogin.getComponent()));

        assertEquals(1, getGridRows(fieldGroup));
        assertEquals(2, getGridColumns(fieldGroup));

        assertEquals(getComposition(fcName), getGridCellComposition(fieldGroup, 0, 0));
        assertEquals(getComposition(fcLogin), getGridCellComposition(fieldGroup, 1, 0));
    }

    @Test
    public void addExistingField() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FieldGroup fieldGroup = create(FieldGroup.class);
            fieldGroup.setColumns(2);

            FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
            fcName.setProperty("name");
            fcName.setEditable(false);
            fieldGroup.addField(fcName);

            fieldGroup.addField(fcName);
        });
    }

    @Test
    public void addIncorrectColumn() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FieldGroup fieldGroup = create(FieldGroup.class);
            fieldGroup.setColumns(2);

            FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
            fcName.setProperty("name");
            fcName.setEditable(false);
            fieldGroup.addField(fcName);

            fieldGroup.addField(fcName, 2);
        });
    }

    @Test
    public void addIncorrectRow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FieldGroup fieldGroup = create(FieldGroup.class);
            fieldGroup.setColumns(2);

            FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
            fcName.setProperty("name");
            fcName.setEditable(false);
            fieldGroup.addField(fcName);

            fieldGroup.addField(fcName, 0, 3);
        });
    }

    @Test
    public void changeBoundComponent() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            FieldGroup fieldGroup = create(FieldGroup.class);
            fieldGroup.setColumns(2);

            FieldGroup.FieldConfig fcName = fieldGroup.createField("name");
            fcName.setProperty("name");
            fcName.setEditable(false);
            fieldGroup.addField(fcName);

            fieldGroup.bind();

            fcName.setComponent(create(TextArea.class));
        });
    }

    @Test
    public void removeNonDefinedField() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FieldGroup fieldGroup = create(FieldGroup.class);
            fieldGroup.setColumns(2);

            fieldGroup.removeField("none");
        });
    }

    protected Object getComposition(FieldGroup.FieldConfig fc) {
        Method getCompositionMethod = MethodUtils.getAccessibleMethod(fc.getClass(), "getComponent");
        Object composition;
        try {
            composition = getCompositionMethod.invoke(fc);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Invoke error", e);
        }
        return ((Component) composition).unwrap(Object.class);
    }

    protected int getGridRows(FieldGroup fieldGroup) {
        return fieldGroup.unwrap(GridLayout.class).getRows();
    }

    protected int getGridColumns(FieldGroup fieldGroup) {
        return fieldGroup.unwrap(GridLayout.class).getColumns();
    }

    protected Object getGridCellComposition(FieldGroup fieldGroup, int col, int row) {
        return fieldGroup.unwrap(GridLayout.class).getComponent(col, row);
    }

    protected Datasource<User> createTestDs() {
        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getFetchPlan(User.class, FetchPlan.LOCAL))
                .buildDatasource();

        testDs.setItem(metadata.create(User.class));
        ((DatasourceImpl) testDs).valid();

        return testDs;
    }
}
