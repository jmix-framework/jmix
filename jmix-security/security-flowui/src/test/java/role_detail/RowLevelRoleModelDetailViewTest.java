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
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityflowui.view.rowlevelrole.RowLevelRoleModelDetailView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.role.TestArchiveRowLevelRole;
import test_support.role.TestBranchRowLevelRole;
import test_support.role.TestPartnerRowLevelRole;
import test_support.view.TestExtendedRowLevelRoleModelDetailView;

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
public class RowLevelRoleModelDetailViewTest {

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
    RowLevelRoleRepository rowLevelRoleRepository;

    @BeforeEach
    void setUp() {
        RowLevelRoleEntity parentRole = dataManager.create(RowLevelRoleEntity.class);
        parentRole.setCode(PARENT_ROLE_CODE);
        parentRole.setName("Übergeordnete Zeilenrolle");
        parentRole.setChildRoles(Set.of(TestArchiveRowLevelRole.CODE, TestBranchRowLevelRole.CODE,
                TestPartnerRowLevelRole.CODE));
        dataManager.save(parentRole);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROW_LEVEL_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        rowLevelRoleRepository.invalidateCache();
    }

    @Test
    void childRolesTable_showsTranslatedName() {
        RowLevelRoleModelDetailView view = openView(PARENT_ROLE_CODE);
        RowLevelRoleModel branch = getRoleModel(getChildRolesDc(view).getItems(), TestBranchRowLevelRole.CODE);

        assertThat(getShownText(getChildRolesTable(view), "name", branch))
                .isEqualTo("Branch records");
    }

    @Test
    void sortByChildRolesNameColumn_ordersByTranslatedName() {
        RowLevelRoleModelDetailView view = openView(PARENT_ROLE_CODE);
        DataGrid<RowLevelRoleModel> childRolesTable = getChildRolesTable(view);

        sort(childRolesTable, "name", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getChildRolesDc(view).getItems()))
                .containsExactly(TestArchiveRowLevelRole.CODE, TestPartnerRowLevelRole.CODE,
                        TestBranchRowLevelRole.CODE);

        sort(childRolesTable, "name", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getChildRolesDc(view).getItems()))
                .containsExactly(TestBranchRowLevelRole.CODE, TestPartnerRowLevelRole.CODE,
                        TestArchiveRowLevelRole.CODE);
    }

    @Test
    void nameField_showsStoredText() {
        RowLevelRoleModelDetailView view = openView(TestBranchRowLevelRole.CODE);
        TypedTextField<String> nameField = UiTestUtils.getComponent(view, "nameField");

        assertThat(nameField.getValue()).isEqualTo("Filialdaten");
    }

    @Test
    void childRolesTable_extendingViewWithOwnOnInit_showsTranslatedName() {
        String serializedCode = urlParamSerializer.serialize(PARENT_ROLE_CODE);
        viewNavigationSupport.navigate(TestExtendedRowLevelRoleModelDetailView.class,
                new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode));
        TestExtendedRowLevelRoleModelDetailView view = UiTestUtils.getCurrentView();
        CollectionContainer<RowLevelRoleModel> childRolesDc =
                ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
        RowLevelRoleModel branch = getRoleModel(childRolesDc.getItems(), TestBranchRowLevelRole.CODE);

        assertThat(getShownText(UiTestUtils.getComponent(view, "childRolesTable"), "name", branch))
                .isEqualTo("Branch records");
    }

    RowLevelRoleModelDetailView openView(String roleCode) {
        String serializedCode = urlParamSerializer.serialize(roleCode);
        viewNavigationSupport.navigate(RowLevelRoleModelDetailView.class,
                new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode));
        return UiTestUtils.getCurrentView();
    }

    DataGrid<RowLevelRoleModel> getChildRolesTable(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "childRolesTable");
    }

    CollectionContainer<RowLevelRoleModel> getChildRolesDc(RowLevelRoleModelDetailView view) {
        return ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
    }
}
