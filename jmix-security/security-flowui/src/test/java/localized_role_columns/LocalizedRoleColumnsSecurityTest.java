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

import com.vaadin.flow.component.grid.ColumnPathRenderer;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelListView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.SecurityFlowuiTestConfiguration;
import test_support.TestNoRoleDescriptionsUiAuthenticator;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The user may not see the description of a role, so security removes the description column from the grid.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"},
        authenticator = TestNoRoleDescriptionsUiAuthenticator.class)
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LocalizedRoleColumnsSecurityTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;

    @Test
    void install_columnRemovedBySecurity_leavesIt() {
        viewNavigationSupport.navigate(ResourceRoleModelListView.class);
        ResourceRoleModelListView view = UiTestUtils.getCurrentView();
        DataGrid<ResourceRoleModel> roleModelsTable = UiTestUtils.getComponent(view, "roleModelsTable");
        DataGridColumn<ResourceRoleModel> descriptionColumn =
                Objects.requireNonNull(roleModelsTable.getColumnByKey("description"));

        assertThat(roleModelsTable.getColumns()).doesNotContain(descriptionColumn);
        // A renderer of the helper would send the descriptions to the client again.
        assertThat(descriptionColumn.getRenderer()).isInstanceOf(ColumnPathRenderer.class);
    }
}
