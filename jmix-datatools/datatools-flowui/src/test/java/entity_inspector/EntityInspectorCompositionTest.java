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
import io.jmix.datatoolsflowui.action.EntityInspectorEditAction;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.EntityInspectorUiTestConfiguration;
import test_support.TestFullAccessUiAuthenticator;
import test_support.entity.InspectorCompositionItem;
import test_support.entity.InspectorCompositionMaster;

import java.util.ArrayList;
import java.util.List;
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
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_INSPECTOR_COMPOSITION_ITEM");
        jdbcTemplate.update("delete from TEST_INSPECTOR_COMPOSITION_MASTER");
    }

    @Test
    void testCompositionItemEditedInDialogKeepsUnsavedValueOnReopen() {
        InspectorCompositionMaster master = metadata.create(InspectorCompositionMaster.class);
        master.setName("master");
        master.setItems(new ArrayList<>());
        dataManager.save(master);

        InspectorCompositionItem item = metadata.create(InspectorCompositionItem.class);
        item.setName("item");
        item.setAmount(15);
        item.setMaster(master);
        dataManager.save(item);

        EntityInspectorDetailView masterView = navigateToInspector(master);
        DataGrid<Object> itemsDataGrid = findItemsDataGrid(masterView);

        // the item is edited in the dialog and the dialog is saved into the master view's data context
        EntityInspectorDetailView itemView = openItemView(itemsDataGrid);
        InspectorCompositionItem itemToEdit = (InspectorCompositionItem) itemView.getEditedEntity();

        assertEquals(15, itemToEdit.getAmount());

        itemToEdit.setAmount(10);
        itemView.closeWithSave();

        // the master view is not saved, so the edit exists only in its data context
        assertEquals(10, getSingleItem(itemsDataGrid).getAmount());

        EntityInspectorDetailView reopenedItemView = openItemView(itemsDataGrid);

        assertEquals(10, ((InspectorCompositionItem) reopenedItemView.getEditedEntity()).getAmount());
    }

    @Test
    void testUnchangedCompositionItemIsReloadedOnReopen() {
        InspectorCompositionMaster master = metadata.create(InspectorCompositionMaster.class);
        master.setName("master");
        master.setItems(new ArrayList<>());
        dataManager.save(master);

        InspectorCompositionItem item = metadata.create(InspectorCompositionItem.class);
        item.setName("item");
        item.setAmount(15);
        item.setMaster(master);
        dataManager.save(item);

        EntityInspectorDetailView masterView = navigateToInspector(master);
        DataGrid<Object> itemsDataGrid = findItemsDataGrid(masterView);

        EntityInspectorDetailView itemView = openItemView(itemsDataGrid);
        itemView.closeWithDiscard();

        jdbcTemplate.update("update TEST_INSPECTOR_COMPOSITION_ITEM set AMOUNT = 20");

        EntityInspectorDetailView reopenedItemView = openItemView(itemsDataGrid);

        assertEquals(20, ((InspectorCompositionItem) reopenedItemView.getEditedEntity()).getAmount());
    }

    private EntityInspectorDetailView navigateToInspector(InspectorCompositionMaster master) {
        String entityName = metadata.getClass(InspectorCompositionMaster.class).getName();
        RouteParameters routeParameters = new RouteParameters(ImmutableMap.of(
                EntityInspectorDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(entityName),
                EntityInspectorDetailView.ROUTE_PARAM_ID, urlParamSerializer.serialize(master.getId())
        ));
        navigationSupport.navigate(EntityInspectorDetailView.class, routeParameters);

        return UiTestUtils.getCurrentView();
    }

    private EntityInspectorDetailView openItemView(DataGrid<Object> itemsDataGrid) {
        itemsDataGrid.select(getSingleItem(itemsDataGrid));

        Action editAction = itemsDataGrid.getAction(EntityInspectorEditAction.ID);
        assertNotNull(editAction, "The edit action is not created in the entity inspector");
        editAction.actionPerform(itemsDataGrid);

        return UiTestUtils.getLastOpenedViewDialog();
    }

    private InspectorCompositionItem getSingleItem(ListDataComponent<Object> itemsDataGrid) {
        List<Object> items = ((ContainerDataUnit<Object>) itemsDataGrid.getItems()).getContainer().getItems();
        assertEquals(1, items.size());

        return (InspectorCompositionItem) items.get(0);
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
