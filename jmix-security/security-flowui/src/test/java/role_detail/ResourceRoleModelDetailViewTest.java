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

package role_detail;

import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.role.TestAuditorRole;
import test_support.role.TestBookkeeperRole;
import test_support.role.TestManagerRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;
import static test_support.TestRoleGrids.getTestRoleCodes;
import static test_support.TestRoleGrids.sort;

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ResourceRoleModelDetailViewTest {

    static final String PARENT_ROLE_CODE = "database-parent";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ResourceRoleRepository resourceRoleRepository;

    @BeforeEach
    void setUp() {
        ResourceRoleEntity parentRole = dataManager.create(ResourceRoleEntity.class);
        parentRole.setCode(PARENT_ROLE_CODE);
        parentRole.setName("Übergeordnete Rolle");
        parentRole.setChildRoles(Set.of(TestAuditorRole.CODE, TestBookkeeperRole.CODE, TestManagerRole.CODE));
        dataManager.save(parentRole);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        resourceRoleRepository.invalidateCache();
    }

    @Test
    void childRolesTable_showsTranslatedName() {
        ResourceRoleModelDetailView view = openView(PARENT_ROLE_CODE);
        ResourceRoleModel bookkeeper = getRoleModel(getChildRolesDc(view).getItems(), TestBookkeeperRole.CODE);

        assertThat(getShownText(getChildRolesTable(view), "name", bookkeeper))
                .isEqualTo("Bookkeeper");
    }

    @Test
    void sortByChildRolesNameColumn_ordersByTranslatedName() {
        ResourceRoleModelDetailView view = openView(PARENT_ROLE_CODE);
        DataGrid<ResourceRoleModel> childRolesTable = getChildRolesTable(view);

        sort(childRolesTable, "name", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getChildRolesDc(view).getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE, TestManagerRole.CODE);

        sort(childRolesTable, "name", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getChildRolesDc(view).getItems()))
                .containsExactly(TestManagerRole.CODE, TestBookkeeperRole.CODE, TestAuditorRole.CODE);
    }

    @Test
    void nameField_showsStoredText() {
        ResourceRoleModelDetailView view = openView(TestBookkeeperRole.CODE);
        TypedTextField<String> nameField = UiTestUtils.getComponent(view, "nameField");

        assertThat(nameField.getValue()).isEqualTo("Buchhalter");
    }

    ResourceRoleModelDetailView openView(String roleCode) {
        String serializedCode = urlParamSerializer.serialize(roleCode);
        viewNavigationSupport.navigate(ResourceRoleModelDetailView.class,
                new RouteParameters(ResourceRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode));
        return UiTestUtils.getCurrentView();
    }

    DataGrid<ResourceRoleModel> getChildRolesTable(ResourceRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "childRolesTable");
    }

    CollectionContainer<ResourceRoleModel> getChildRolesDc(ResourceRoleModelDetailView view) {
        return ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
    }
}
