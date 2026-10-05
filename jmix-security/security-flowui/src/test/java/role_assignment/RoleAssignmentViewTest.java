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

package role_assignment;

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.role.assignment.RoleAssignmentModel;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.view.roleassignment.RoleAssignmentView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.entity.TestUser;
import test_support.role.TestBookkeeperRole;
import test_support.role.TestBranchRowLevelRole;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getShownText;

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RoleAssignmentViewTest {

    static final String USERNAME = "alice";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TestUser user = dataManager.create(TestUser.class);
        user.setUsername(USERNAME);
        dataManager.save(user);

        saveRoleAssignment(TestBookkeeperRole.CODE, RoleAssignmentRoleType.RESOURCE);
        saveRoleAssignment(TestBranchRowLevelRole.CODE, RoleAssignmentRoleType.ROW_LEVEL);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT");
        jdbcTemplate.update("delete from TEST_USER");
    }

    @Test
    void roleAssignmentsTables_showTranslatedRoleName() {
        RoleAssignmentView view = openView();

        DataGrid<RoleAssignmentModel> resourceRoleAssignmentsTable =
                UiTestUtils.getComponent(view, "resourceRoleAssignmentsTable");
        assertThat(getShownText(resourceRoleAssignmentsTable, "roleName",
                getRoleAssignment(view, "resourceRoleAssignmentsDc", TestBookkeeperRole.CODE)))
                .isEqualTo("Bookkeeper");

        DataGrid<RoleAssignmentModel> rowLevelRoleAssignmentsTable =
                UiTestUtils.getComponent(view, "rowLevelRoleAssignmentsTable");
        assertThat(getShownText(rowLevelRoleAssignmentsTable, "roleName",
                getRoleAssignment(view, "rowLevelRoleAssignmentsDc", TestBranchRowLevelRole.CODE)))
                .isEqualTo("Branch records");
    }

    void saveRoleAssignment(String roleCode, String roleType) {
        RoleAssignmentEntity roleAssignment = dataManager.create(RoleAssignmentEntity.class);
        roleAssignment.setUsername(USERNAME);
        roleAssignment.setRoleCode(roleCode);
        roleAssignment.setRoleType(roleType);
        dataManager.save(roleAssignment);
    }

    RoleAssignmentView openView() {
        String serializedUsername = urlParamSerializer.serialize(USERNAME);
        viewNavigationSupport.navigate(RoleAssignmentView.class, new RouteParameters("username", serializedUsername));
        return UiTestUtils.getCurrentView();
    }

    RoleAssignmentModel getRoleAssignment(RoleAssignmentView view, String containerId, String roleCode) {
        CollectionContainer<RoleAssignmentModel> container =
                ViewControllerUtils.getViewData(view).getContainer(containerId);
        return container.getItems().stream()
                .filter(roleAssignment -> roleCode.equals(roleAssignment.getRoleCode()))
                .findFirst()
                .orElseThrow();
    }
}
