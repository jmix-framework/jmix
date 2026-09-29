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

import io.jmix.core.MetadataTools;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;

/**
 * The texts of a model are resolved again when it changes, and a new model has texts of its own even when the container
 * takes it in with its events muted. The role here is a database role without translations: a model of such a role
 * keeps the id of the role, so the models of one role are equal.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LocalizedRoleColumnsInvalidationTest {

    static final String ROLE_CODE = "database-renamed";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ResourceRoleRepository resourceRoleRepository;

    @BeforeEach
    void setUp() {
        ResourceRoleEntity role = dataManager.create(ResourceRoleEntity.class);
        role.setCode(ROLE_CODE);
        role.setName("Altname");
        dataManager.save(role);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        resourceRoleRepository.invalidateCache();
    }

    @Test
    void install_itemChanged_showsNewTexts() {
        View<?> view = openListView();
        DataGrid<ResourceRoleModel> roleModelsTable = UiTestUtils.getComponent(view, "roleModelsTable");
        ResourceRoleModel roleModel = getRoleModel(getRoleModelsDc(view).getItems(), ROLE_CODE);
        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Altname");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEmpty();

        roleModel.setName("Neuname");
        roleModel.setDescription("Neue Beschreibung");

        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Neuname");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEqualTo("Neue Beschreibung");
    }

    @Test
    void install_itemsReplacedWhileMuted_showsTextsOfNewItems() {
        View<?> view = openListView();
        DataGrid<ResourceRoleModel> roleModelsTable = UiTestUtils.getComponent(view, "roleModelsTable");
        CollectionContainer<ResourceRoleModel> roleModelsDc = getRoleModelsDc(view);
        ResourceRoleModel roleModel = getRoleModel(roleModelsDc.getItems(), ROLE_CODE);
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

    View<?> openListView() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        return UiTestUtils.getCurrentView();
    }

    CollectionContainer<ResourceRoleModel> getRoleModelsDc(View<?> view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }
}
