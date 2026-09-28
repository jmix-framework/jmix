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
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.role.TestAuditorRole;
import test_support.role.TestBookkeeperRole;
import test_support.role.TestFullAccessRole;
import test_support.role.TestManagerRole;

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
public class ResourceRoleModelListViewTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;

    @Test
    void roleModelsTable_showsTranslatedNameAndDescription() {
        ResourceRoleModelListView view = openView();
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(view);
        ResourceRoleModel bookkeeper = getRoleModel(getRoleModelsDc(view).getItems(), TestBookkeeperRole.CODE);

        assertThat(getShownText(roleModelsTable, "name", bookkeeper)).isEqualTo("Bookkeeper");
        assertThat(getShownText(roleModelsTable, "description", bookkeeper)).isEqualTo("Keeps the books");
    }

    @Test
    void loadRoles_ordersByTranslatedNameIgnoringCase() {
        ResourceRoleModelListView view = openView();

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE,
                        TestManagerRole.CODE, TestFullAccessRole.CODE);
    }

    @Test
    void sortByNameColumn_ordersByTranslatedName() {
        ResourceRoleModelListView view = openView();
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(view);

        sort(roleModelsTable, "name", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestFullAccessRole.CODE, TestManagerRole.CODE,
                        TestBookkeeperRole.CODE, TestAuditorRole.CODE);

        sort(roleModelsTable, "name", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE,
                        TestManagerRole.CODE, TestFullAccessRole.CODE);
    }

    @Test
    void sortByDescriptionColumn_ordersByTranslatedDescription() {
        ResourceRoleModelListView view = openView();
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(view);

        sort(roleModelsTable, "description", SortDirection.ASCENDING);

        // The full access role has no description.
        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestFullAccessRole.CODE, TestManagerRole.CODE, TestAuditorRole.CODE,
                        TestBookkeeperRole.CODE);

        sort(roleModelsTable, "description", SortDirection.DESCENDING);

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestBookkeeperRole.CODE, TestAuditorRole.CODE, TestManagerRole.CODE,
                        TestFullAccessRole.CODE);
    }

    @Test
    void roleFilter_matchesTranslatedName() {
        ResourceRoleModelListView view = openView();

        enterName(view, "keep");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).containsExactly(TestBookkeeperRole.CODE);

        // A part of the German name that the English one lacks.
        enterName(view, "halter");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).isEmpty();
    }

    ResourceRoleModelListView openView() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        return UiTestUtils.getCurrentView();
    }

    DataGrid<ResourceRoleModel> getRoleModelsTable(ResourceRoleModelListView view) {
        return UiTestUtils.getComponent(view, "roleModelsTable");
    }

    CollectionContainer<ResourceRoleModel> getRoleModelsDc(ResourceRoleModelListView view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }

}
