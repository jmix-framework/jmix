/*
 * Copyright 2026 Haulmont.
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

package entity_inspector;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Metadata;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.flowui.component.grid.DataGrid;
import org.springframework.context.ApplicationContext;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorDataGridBuilder;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.CustomDataStore;
import test_support.EntityInspectorUiTestConfiguration;
import test_support.TestFullAccessUiAuthenticator;
import test_support.entity.CustomStoreChild;
import test_support.entity.CustomStoreEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The entity inspector opened on an entity of a store that does not accept a JPQL query string.
 */
@UiTest(authenticator = TestFullAccessUiAuthenticator.class)
@SpringBootTest(classes = EntityInspectorUiTestConfiguration.class)
public class EntityInspectorCustomStoreTest {

    @Autowired
    Metadata metadata;
    @Autowired
    DataComponents dataComponents;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    CustomDataStore customDataStore;
    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;

    @AfterEach
    void cleanup() {
        customDataStore.clear();
    }

    @Test
    void testEntityOfCustomStoreIsEditedById() {
        CustomStoreEntity entity = createEntityWithOneChild();

        EntityInspectorDetailView view = navigateToInspector(entity);

        assertEquals(entity, view.getEditedEntity());
    }

    @Test
    void testCollectionTabShowsOnlyTheChildrenOfTheEditedEntity() {
        CustomStoreEntity entity = createEntityWithOneChild();
        // another child that belongs to no entity: it must not reach the tab
        CustomStoreChild strayChild = metadata.create(CustomStoreChild.class);
        strayChild.setName("stray");
        customDataStore.put(strayChild);

        EntityInspectorDetailView view = navigateToInspector(entity);
        DataGrid<Object> childrenDataGrid = findChildrenDataGrid(view);

        List<Object> items = ((ContainerDataUnit<Object>) childrenDataGrid.getItems()).getContainer().getItems();

        assertEquals(1, items.size());
        assertEquals("child", ((CustomStoreChild) items.get(0)).getName());
    }

    @Test
    void testListGridShowsPlainAttributesOfCustomStoreEntity() {
        CollectionContainer<CustomStoreEntity> container =
                dataComponents.createCollectionContainer(CustomStoreEntity.class);

        DataGrid<?> dataGrid = InspectorDataGridBuilder.from(applicationContext, container)
                .withSystem(true)
                .build();

        List<String> columns = dataGrid.getColumns().stream()
                .map(column -> column.getKey())
                .toList();

        // The entity's own store persists these fields, so the list view must offer them as columns.
        org.junit.jupiter.api.Assertions.assertTrue(columns.contains("name"),
                "expected a 'name' column, got " + columns);
        org.junit.jupiter.api.Assertions.assertTrue(columns.contains("id"),
                "expected an 'id' column, got " + columns);
    }

    private CustomStoreEntity createEntityWithOneChild() {
        CustomStoreChild child = metadata.create(CustomStoreChild.class);
        child.setName("child");
        customDataStore.put(child);

        CustomStoreEntity entity = metadata.create(CustomStoreEntity.class);
        entity.setName("parent");
        entity.setChildren(new ArrayList<>(List.of(child)));
        customDataStore.put(entity);

        return entity;
    }

    private EntityInspectorDetailView navigateToInspector(CustomStoreEntity entity) {
        String entityName = metadata.getClass(CustomStoreEntity.class).getName();
        RouteParameters routeParameters = new RouteParameters(ImmutableMap.of(
                EntityInspectorDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(entityName),
                EntityInspectorDetailView.ROUTE_PARAM_ID, urlParamSerializer.serialize(entity.getId())
        ));
        navigationSupport.navigate(EntityInspectorDetailView.class, routeParameters);

        return UiTestUtils.getCurrentView();
    }

    @SuppressWarnings("unchecked")
    private DataGrid<Object> findChildrenDataGrid(EntityInspectorDetailView view) {
        DataGrid<Object> dataGrid = (DataGrid<Object>) findDataGrid(view.getContent()).orElse(null);
        assertNotNull(dataGrid, "The 'children' data grid is not created in the entity inspector");

        return dataGrid;
    }

    private Optional<Component> findDataGrid(Component component) {
        if (component instanceof DataGrid) {
            return Optional.of(component);
        }
        return component.getChildren()
                .map(this::findDataGrid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
