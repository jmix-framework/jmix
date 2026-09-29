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

package role_list;

import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityflowui.view.rowlevelrole.RowLevelRoleModelListView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.role.TestArchiveRowLevelRole;
import test_support.role.TestBranchRowLevelRole;
import test_support.role.TestPartnerRowLevelRole;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleFilters.enterName;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;
import static test_support.TestRoleGrids.getTestRoleCodes;
import static test_support.TestRoleGrids.sort;

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RowLevelRoleModelListViewTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_ROW_LEVEL_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        rowLevelRoleRepository.invalidateCache();
    }

    @Test
    void roleModelsTable_showsTranslatedNameAndDescription() {
        RowLevelRoleModelListView view = openView();
        DataGrid<RowLevelRoleModel> roleModelsTable = getRoleModelsTable(view);
        RowLevelRoleModel branch = getRoleModel(getRoleModelsDc(view).getItems(), TestBranchRowLevelRole.CODE);

        assertThat(getShownText(roleModelsTable, "name", branch)).isEqualTo("Branch records");
        assertThat(getShownText(roleModelsTable, "description", branch)).isEqualTo("data of one branch");
    }

    @Test
    void loadRoles_ordersByTranslatedNameIgnoringCase() {
        RowLevelRoleModelListView view = openView();

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestArchiveRowLevelRole.CODE, TestPartnerRowLevelRole.CODE,
                        TestBranchRowLevelRole.CODE);
    }

    @Test
    void sortByNameColumn_ordersByTranslatedName() {
        RowLevelRoleModelListView view = openView();
        DataGrid<RowLevelRoleModel> roleModelsTable = getRoleModelsTable(view);

        sort(roleModelsTable, "name", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestBranchRowLevelRole.CODE, TestPartnerRowLevelRole.CODE,
                        TestArchiveRowLevelRole.CODE);

        sort(roleModelsTable, "name", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestArchiveRowLevelRole.CODE, TestPartnerRowLevelRole.CODE,
                        TestBranchRowLevelRole.CODE);
    }

    @Test
    void sortByDescriptionColumn_ordersByTranslatedDescription() {
        RowLevelRoleModelListView view = openView();
        DataGrid<RowLevelRoleModel> roleModelsTable = getRoleModelsTable(view);

        sort(roleModelsTable, "description", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestBranchRowLevelRole.CODE, TestArchiveRowLevelRole.CODE,
                        TestPartnerRowLevelRole.CODE);

        sort(roleModelsTable, "description", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestPartnerRowLevelRole.CODE, TestArchiveRowLevelRole.CODE,
                        TestBranchRowLevelRole.CODE);
    }

    @Test
    void roleFilter_matchesTranslatedName() {
        RowLevelRoleModelListView view = openView();

        enterName(view, "branch");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).containsExactly(TestBranchRowLevelRole.CODE);

        // A part of the German name that the English one lacks.
        enterName(view, "filial");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).isEmpty();
    }

    @Test
    void roleModelsTable_databaseRole_showsTranslatedNameAndDescription() {
        // Not a test role code, so that the order of the test roles stays as the other tests expect it.
        RowLevelRoleEntity roleEntity = dataManager.create(RowLevelRoleEntity.class);
        roleEntity.setCode("database-translated");
        roleEntity.setName("Archiv");
        roleEntity.setDescription("Sieht das Archiv");
        roleEntity.setLocalizedNames("en=Archive");
        roleEntity.setLocalizedDescriptions("en=Sees the archive");
        dataManager.save(roleEntity);

        RowLevelRoleModelListView view = openView();
        DataGrid<RowLevelRoleModel> roleModelsTable = getRoleModelsTable(view);
        RowLevelRoleModel roleModel = getRoleModel(getRoleModelsDc(view).getItems(), "database-translated");

        assertThat(getShownText(roleModelsTable, "name", roleModel)).isEqualTo("Archive");
        assertThat(getShownText(roleModelsTable, "description", roleModel)).isEqualTo("Sees the archive");
    }

    RowLevelRoleModelListView openView() {
        viewNavigationSupport.navigate(RowLevelRoleModelListView.class);
        return UiTestUtils.getCurrentView();
    }

    DataGrid<RowLevelRoleModel> getRoleModelsTable(RowLevelRoleModelListView view) {
        return UiTestUtils.getComponent(view, "roleModelsTable");
    }

    CollectionContainer<RowLevelRoleModel> getRoleModelsDc(RowLevelRoleModelListView view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }

}
