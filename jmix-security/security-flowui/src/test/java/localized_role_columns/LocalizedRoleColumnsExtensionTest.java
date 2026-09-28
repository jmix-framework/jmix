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
import test_support.view.TestExtendedResourceRoleModelListView;
import test_support.view.TestReplacedGridResourceRoleModelLookupView;
import test_support.view.TestReplacedInitResourceRoleModelListView;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;
import static test_support.TestRoleGrids.getTestRoleCodes;
import static test_support.TestRoleGrids.sort;

/**
 * The role screens and the views that extend them keep the configuration they give the columns, and the extending
 * views keep working.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LocalizedRoleColumnsExtensionTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;

    @Test
    void install_extendingViewSuppliesRenderer_keepsIt() {
        TestExtendedResourceRoleModelListView view = openView();
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(view);
        ResourceRoleModel auditor = getRoleModel(getRoleModelsDc(view).getItems(), TestAuditorRole.CODE);

        assertThat(getShownText(roleModelsTable, "name", auditor)).isEqualTo("CUSTOM test-auditor");
        assertThat(getShownText(roleModelsTable, "description", auditor)).isEqualTo("checks the books");
    }

    @Test
    void install_extendingViewSetsComparator_keepsIt() {
        TestExtendedResourceRoleModelListView view = openView();

        sort(getRoleModelsTable(view), "name", SortDirection.ASCENDING);

        // The comparator of the extending view orders the roles by code, descending.
        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestManagerRole.CODE, TestFullAccessRole.CODE, TestBookkeeperRole.CODE,
                        TestAuditorRole.CODE);
    }

    @Test
    void install_extendingDescriptorMakesColumnUnsortable_keepsIt() {
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(openView());

        assertThat(Objects.requireNonNull(roleModelsTable.getColumnByKey("description")).isSortable()).isFalse();
        assertThat(Objects.requireNonNull(roleModelsTable.getColumnByKey("name")).isSortable()).isTrue();
    }

    @Test
    void install_baseListView_keepsColumnsSortable() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        ResourceRoleModelListView view = UiTestUtils.getCurrentView();
        DataGrid<ResourceRoleModel> roleModelsTable = UiTestUtils.getComponent(view, "roleModelsTable");

        assertThat(Objects.requireNonNull(roleModelsTable.getColumnByKey("name")).isSortable()).isTrue();
        assertThat(Objects.requireNonNull(roleModelsTable.getColumnByKey("description")).isSortable()).isTrue();
    }

    @Test
    void listView_extendingViewReplacesOnInit_opensInTranslatedOrder() {
        viewNavigationSupport.navigate(TestReplacedInitResourceRoleModelListView.class);
        TestReplacedInitResourceRoleModelListView view = UiTestUtils.getCurrentView();
        CollectionContainer<ResourceRoleModel> roleModelsDc =
                ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");

        assertThat(getTestRoleCodes(roleModelsDc.getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE, TestManagerRole.CODE,
                        TestFullAccessRole.CODE);
    }

    @Test
    void lookupView_extendingViewRenamesGrid_showsTranslation() {
        viewNavigationSupport.navigate(TestReplacedGridResourceRoleModelLookupView.class);
        TestReplacedGridResourceRoleModelLookupView view = UiTestUtils.getCurrentView();
        DataGrid<ResourceRoleModel> rolesGrid = UiTestUtils.getComponent(view, "rolesGrid");
        CollectionContainer<ResourceRoleModel> roleModelsDc =
                ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
        ResourceRoleModel bookkeeper = getRoleModel(roleModelsDc.getItems(), TestBookkeeperRole.CODE);

        assertThat(getShownText(rolesGrid, "name", bookkeeper)).isEqualTo("Bookkeeper");
    }

    TestExtendedResourceRoleModelListView openView() {
        viewNavigationSupport.navigate(TestExtendedResourceRoleModelListView.class);
        return UiTestUtils.getCurrentView();
    }

    DataGrid<ResourceRoleModel> getRoleModelsTable(TestExtendedResourceRoleModelListView view) {
        return UiTestUtils.getComponent(view, "roleModelsTable");
    }

    CollectionContainer<ResourceRoleModel> getRoleModelsDc(TestExtendedResourceRoleModelListView view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }
}
