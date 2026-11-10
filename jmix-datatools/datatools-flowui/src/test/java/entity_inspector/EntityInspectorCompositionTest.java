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
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.datatoolsflowui.action.EntityInspectorCreateAction;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.EntityInspectorUiTestConfiguration;
import test_support.TestFullAccessUiAuthenticator;
import test_support.entity.InspectorCompositionItem;
import test_support.entity.InspectorCompositionMaster;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UiTest(authenticator = TestFullAccessUiAuthenticator.class)
@SpringBootTest(classes = EntityInspectorUiTestConfiguration.class)
public class EntityInspectorCompositionTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_INSPECTOR_COMPOSITION_ITEM");
        jdbcTemplate.update("delete from TEST_INSPECTOR_COMPOSITION_MASTER");
    }

    @Test
    void testNewCompositionItemOfNewMasterIsSavedWithMaster() {
        EntityInspectorDetailView masterView = navigateToNewMaster();
        ((InspectorCompositionMaster) masterView.getEditedEntity()).setName("master");

        createItem(findItemsDataGrid(masterView), "new item");
        masterView.save();

        InspectorCompositionItem savedItem = dataManager.load(InspectorCompositionItem.class)
                .query("select e from test_InspectorCompositionItem e where e.master.name = :masterName")
                .parameter("masterName", "master")
                .one();
        assertEquals("new item", savedItem.getName());
    }

    private EntityInspectorDetailView navigateToNewMaster() {
        String entityName = metadata.getClass(InspectorCompositionMaster.class).getName();
        RouteParameters routeParameters = new RouteParameters(ImmutableMap.of(
                EntityInspectorDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(entityName),
                EntityInspectorDetailView.ROUTE_PARAM_ID, "new"
        ));
        navigationSupport.navigate(EntityInspectorDetailView.class, routeParameters);

        return UiTestUtils.getCurrentView();
    }

    private void createItem(DataGrid<Object> itemsDataGrid, String name) {
        CreateAction<?> createAction = (CreateAction<?>) itemsDataGrid.getAction(EntityInspectorCreateAction.ID);
        assertNotNull(createAction, "The create action is not created in the entity inspector");
        createAction.execute();

        EntityInspectorDetailView newItemView = (EntityInspectorDetailView) applicationContext
                .getBean(OpenedDialogWindows.class)
                .getCurrentDialog()
                .orElseThrow();
        ((InspectorCompositionItem) newItemView.getEditedEntity()).setName(name);
        newItemView.closeWithSave();
    }

    @SuppressWarnings("unchecked")
    private DataGrid<Object> findItemsDataGrid(EntityInspectorDetailView masterView) {
        DataGrid<Object> dataGrid = (DataGrid<Object>) findDataGrid(masterView.getContent()).orElse(null);
        assertNotNull(dataGrid, "The 'items' data grid is not created in the entity inspector");

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
