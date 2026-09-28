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

package role_lookup;

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelLookupView;
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

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ResourceRoleModelLookupViewTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;

    @Test
    void roleModelsTable_showsTranslatedName() {
        ResourceRoleModelLookupView view = openView();
        DataGrid<ResourceRoleModel> roleModelsTable = getRoleModelsTable(view);
        ResourceRoleModel bookkeeper = getRoleModel(getRoleModelsDc(view).getItems(), TestBookkeeperRole.CODE);

        assertThat(getShownText(roleModelsTable, "name", bookkeeper)).isEqualTo("Bookkeeper");
    }

    @Test
    void loadRoles_ordersByTranslatedNameIgnoringCase() {
        ResourceRoleModelLookupView view = openView();

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE,
                        TestManagerRole.CODE, TestFullAccessRole.CODE);
    }

    @Test
    void roleFilter_matchesTranslatedName() {
        ResourceRoleModelLookupView view = openView();

        enterName(view, "keep");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).containsExactly(TestBookkeeperRole.CODE);

        // A part of the German name that the English one lacks.
        enterName(view, "halter");

        assertThat(getTestRoleCodes(getRoleModelsDc(view).getItems())).isEmpty();
    }

    ResourceRoleModelLookupView openView() {
        viewNavigationSupport.navigate(ResourceRoleModelLookupView.class);
        return UiTestUtils.getCurrentView();
    }

    DataGrid<ResourceRoleModel> getRoleModelsTable(ResourceRoleModelLookupView view) {
        return UiTestUtils.getComponent(view, "roleModelsTable");
    }

    CollectionContainer<ResourceRoleModel> getRoleModelsDc(ResourceRoleModelLookupView view) {
        return ViewControllerUtils.getViewData(view).getContainer("roleModelsDc");
    }

}
