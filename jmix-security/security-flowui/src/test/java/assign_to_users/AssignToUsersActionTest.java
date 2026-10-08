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

package assign_to_users;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.entity.TestUser;
import test_support.role.TestBookkeeperRole;
import test_support.view.TestUserListView;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class AssignToUsersActionTest {

    static final String USERNAME = "alice";

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        saveUser(USERNAME);
        saveUser(SecurityFlowuiTestConfiguration.REJECTED_USERNAME);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT");
        jdbcTemplate.update("delete from TEST_USER");
    }

    /**
     * A user the role cannot be assigned to is warned about, the others are reported as assigned.
     */
    @ParameterizedTest
    @CsvSource({USERNAME + ", SUCCESS", SecurityFlowuiTestConfiguration.REJECTED_USERNAME + ", WARNING"})
    void assignToUsers_userSelected_notifiesWithTranslatedRoleName(String username, Notifications.Type type) {
        TestUserListView usersView = assignBookkeeperToUsers();

        selectUser(usersView, username);

        NotificationInfo notification = Objects.requireNonNull(UiTestUtils.getLastOpenedNotification());
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getMessage()).contains("Bookkeeper");
    }

    void saveUser(String username) {
        TestUser user = dataManager.create(TestUser.class);
        user.setUsername(username);
        dataManager.save(user);
    }

    /**
     * Selects the bookkeeper in the list of resource roles and performs {@code assignToUsers}, which opens the lookup
     * of users.
     */
    TestUserListView assignBookkeeperToUsers() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        View<?> roleListView = UiTestUtils.getCurrentView();
        DataGrid<ResourceRoleModel> roleModelsTable = UiTestUtils.getComponent(roleListView, "roleModelsTable");
        CollectionContainer<ResourceRoleModel> roleModelsDc =
                ViewControllerUtils.getViewData(roleListView).getContainer("roleModelsDc");
        roleModelsTable.select(getRoleModel(roleModelsDc.getItems(), TestBookkeeperRole.CODE));

        Objects.requireNonNull(roleModelsTable.getAction("assignToUsers")).actionPerform(roleModelsTable);

        return UiTestUtils.getLastOpenedViewDialog();
    }

    void selectUser(TestUserListView usersView, String username) {
        CollectionContainer<TestUser> usersDc = ViewControllerUtils.getViewData(usersView).getContainer("usersDc");
        DataGrid<TestUser> usersDataGrid = UiTestUtils.getComponent(usersView, "usersDataGrid");
        usersDataGrid.select(usersDc.getItems().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElseThrow());

        Action selectAction = ViewControllerUtils.getViewActions(usersView).getAction("selectAction");
        Objects.requireNonNull(selectAction).actionPerform(usersDataGrid);
    }
}
