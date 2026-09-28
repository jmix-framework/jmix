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

package localized_role_columns;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.MetadataTools;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelLookupView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;

/**
 * The texts of a model are resolved again when the container of the grid refreshes, replaces, adds or changes it, and a
 * new model has texts of its own even when the container takes it in with its events muted. The roles here are
 * database roles without translations: a model of such a role keeps the id of the role, so the models of one role are
 * equal.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LocalizedRoleColumnsInvalidationTest {

    static final String PARENT_ROLE_CODE = "database-parent";
    static final String RENAMED_ROLE_CODE = "database-renamed";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ResourceRoleRepository resourceRoleRepository;

    ResourceRoleEntity renamedRole;

    @BeforeEach
    void setUp() {
        renamedRole = saveRole(RENAMED_ROLE_CODE, "Altname", Set.of());
        saveRole(PARENT_ROLE_CODE, "Elternrolle", Set.of(RENAMED_ROLE_CODE));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        resourceRoleRepository.invalidateCache();
    }

    @Test
    void install_itemReplaced_showsTextOfNewItem() {
        View<?> view = openListView();
        ResourceRoleModel roleModel = getRenamedRoleModel(getRoleModelsDc(view));
        assertThat(getShownText(getGrid(view, "roleModelsTable"), "name", roleModel)).isEqualTo("Altname");

        ResourceRoleModel editedRoleModel = metadataTools.copy(roleModel);
        editedRoleModel.setName("Neuname");
        getRoleModelsDc(view).replaceItem(editedRoleModel);

        assertThat(getShownText(getGrid(view, "roleModelsTable"), "name", editedRoleModel)).isEqualTo("Neuname");
    }

    @Test
    void install_itemChanged_showsNewTexts() {
        View<?> view = openListView();
        DataGrid<ResourceRoleModel> roleModelsTable = getGrid(view, "roleModelsTable");
        ResourceRoleModel roleModel = getRenamedRoleModel(getRoleModelsDc(view));
        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Altname");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEmpty();

        roleModel.setName("Neuname");
        roleModel.setDescription("Neue Beschreibung");

        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Neuname");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEqualTo("Neue Beschreibung");
    }

    @Test
    void install_itemsRefreshed_showsTextsOfNewItems() {
        View<?> view = openListView();
        ResourceRoleModel roleModel = getRenamedRoleModel(getRoleModelsDc(view));
        assertThat(getShownText(getGrid(view, "roleModelsTable"), "name", roleModel)).isEqualTo("Altname");

        ResourceRoleModel newRoleModel = metadataTools.copy(roleModel);
        newRoleModel.setName("Neuname");
        getRoleModelsDc(view).setItems(List.of(newRoleModel));

        assertThat(getShownText(getGrid(view, "roleModelsTable"), "name", newRoleModel)).isEqualTo("Neuname");
    }

    @Test
    void install_itemsReplacedWhileMuted_showsTextsOfNewItems() {
        View<?> view = openListView();
        DataGrid<ResourceRoleModel> roleModelsTable = getGrid(view, "roleModelsTable");
        CollectionContainer<ResourceRoleModel> roleModelsDc = getRoleModelsDc(view);
        ResourceRoleModel roleModel = getRenamedRoleModel(roleModelsDc);
        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Altname");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEmpty();

        // As a detail view reloads its child roles when it is shown again.
        ResourceRoleModel newRoleModel = metadataTools.copy(roleModel);
        newRoleModel.setName("Neuname");
        newRoleModel.setDescription("Neue Beschreibung");
        roleModelsDc.mute();
        roleModelsDc.setItems(List.of(newRoleModel));
        roleModelsDc.unmute();

        assertThat(getShownText(roleModelsTable, "name", newRoleModel)).isEqualTo("Neuname");
        assertThat(getShownText(roleModelsTable, "description", newRoleModel)).isEqualTo("Neue Beschreibung");
    }

    @Test
    void install_childRoleRemovedRenamedAndAddedAgain_showsNewName() {
        View<?> view = openDetailView();
        showChildRolesTab(view);
        DataGrid<ResourceRoleModel> childRolesTable = getGrid(view, "childRolesTable");
        CollectionContainer<ResourceRoleModel> childRolesDc =
                ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
        ResourceRoleModel childRoleModel = getRenamedRoleModel(childRolesDc);
        assertThat(getShownText(childRolesTable, "name", childRoleModel)).isEqualTo("Altname");

        childRolesTable.select(childRoleModel);
        performAction(childRolesTable, "remove");

        renamedRole = dataManager.load(ResourceRoleEntity.class).id(renamedRole.getId()).one();
        renamedRole.setName("Neuname");
        dataManager.save(renamedRole);

        performAction(childRolesTable, "add");
        ResourceRoleModelLookupView lookupView = Objects.requireNonNull(UiTestUtils.getLastOpenedViewDialog());
        DataGrid<ResourceRoleModel> lookupTable = getGrid(lookupView, "roleModelsTable");
        CollectionContainer<ResourceRoleModel> lookupDc = ViewControllerUtils.getViewData(lookupView)
                .getContainer("roleModelsDc");
        lookupTable.select(getRenamedRoleModel(lookupDc));
        Action selectAction = ViewControllerUtils.getViewActions(lookupView).getAction("selectAction");
        Objects.requireNonNull(selectAction).actionPerform(lookupTable);

        assertThat(getShownText(childRolesTable, "name", getRenamedRoleModel(childRolesDc))).isEqualTo("Neuname");
    }

    ResourceRoleEntity saveRole(String code, String name, Set<String> childRoles) {
        ResourceRoleEntity role = dataManager.create(ResourceRoleEntity.class);
        role.setCode(code);
        role.setName(name);
        role.setChildRoles(childRoles);
        return dataManager.save(role);
    }

    View<?> openListView() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        return UiTestUtils.getCurrentView();
    }

    View<?> openDetailView() {
        String serializedCode = urlParamSerializer.serialize(PARENT_ROLE_CODE);
        viewNavigationSupport.navigate(ResourceRoleModelDetailView.class,
                new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode));
        return UiTestUtils.getCurrentView();
    }

    /**
     * Selects the tab of the child roles: the content of a tab that is not selected is not attached to the view, so the
     * actions of its grid cannot find the view.
     */
    void showChildRolesTab(View<?> view) {
        JmixTabSheet tabSheet = UiTestUtils.getComponent(view, "tabSheet");
        tabSheet.setSelectedTab(UiTestUtils.getComponent(view, "childRolesTab"));
    }

    DataGrid<ResourceRoleModel> getGrid(View<?> view, String gridId) {
        return UiTestUtils.getComponent(view, gridId);
    }

    CollectionContainer<ResourceRoleModel> getRoleModelsDc(View<?> view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }

    ResourceRoleModel getRenamedRoleModel(CollectionContainer<ResourceRoleModel> container) {
        return getRoleModel(container.getItems(), RENAMED_ROLE_CODE);
    }

    void performAction(DataGrid<ResourceRoleModel> grid, String actionId) {
        Objects.requireNonNull(grid.getAction(actionId)).actionPerform(grid);
    }
}
