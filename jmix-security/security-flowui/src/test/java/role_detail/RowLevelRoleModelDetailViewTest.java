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

import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;
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

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;
import static test_support.TestRoleLocalizedValues.clearValue;
import static test_support.TestRoleLocalizedValues.click;
import static test_support.TestRoleLocalizedValues.editLocalizedValues;
import static test_support.TestRoleLocalizedValues.enterValue;
import static test_support.TestRoleLocalizedValues.getDialogFields;

/**
 * The test roles are German by default and English for the user of the tests. What the fields of the localized values
 * show, the dialog and the sorting of the child roles are tested on the resource role detail view, which declares and
 * installs them the same way.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RowLevelRoleModelDetailViewTest {

    static final String PARENT_ROLE_CODE = "database-parent";
    static final String TRANSLATED_ROLE_CODE = "database-translated";

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
        parentRole.setChildRoles(Set.of(TestArchiveRowLevelRole.CODE, TestBranchRowLevelRole.CODE));

        RowLevelRoleEntity translatedRole = dataManager.create(RowLevelRoleEntity.class);
        translatedRole.setCode(TRANSLATED_ROLE_CODE);
        translatedRole.setName("Archiv");
        translatedRole.setLocalizedNames("en=Archive\nde=Archivrolle");
        translatedRole.setLocalizedDescriptions("en=Sees the archive");

        dataManager.save(parentRole, translatedRole);
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
        DataGrid<RowLevelRoleModel> childRolesTable = UiTestUtils.getComponent(view, "childRolesTable");
        CollectionContainer<RowLevelRoleModel> childRolesDc =
                ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
        RowLevelRoleModel branch = getRoleModel(childRolesDc.getItems(), TestBranchRowLevelRole.CODE);

        assertThat(getShownText(childRolesTable, "name", branch)).isEqualTo("Branch records");
    }

    @Test
    void localizedValueFields_designTimeRole_areHidden() {
        RowLevelRoleModelDetailView view = openView(TestBranchRowLevelRole.CODE);

        assertThat(getLocalizedNamesField(view).isVisible()).isFalse();
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isFalse();
    }

    @Test
    void editLocalizedNamesAndClearDescriptions_save_viewStoresThem() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);
        assertThat(localizedNamesField.isVisible()).isTrue();

        RoleLocalizedValuesView dialog = editLocalizedValues(localizedNamesField);
        enterValue(getDialogFields(dialog).get(1), "Records");
        click(dialog, "saveAndCloseBtn");
        clearValue(getLocalizedDescriptionsField(view));

        // Saved without closing: closing navigates to the parent layout, which the tests do not have.
        view.save();

        RowLevelRoleEntity role = dataManager.load(RowLevelRoleEntity.class)
                .query("e.code = :code")
                .parameter("code", TRANSLATED_ROLE_CODE)
                .one();
        assertThat(RoleLocalizedValuesUtils.read(role.getLocalizedNames()))
                .isEqualTo(Map.of("de", "Archivrolle", "en", "Records"));
        assertThat(role.getLocalizedDescriptions()).isNull();
    }

    RowLevelRoleModelDetailView openView(String roleCode) {
        viewNavigationSupport.navigate(RowLevelRoleModelDetailView.class, new RouteParameters(
                RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(roleCode)));
        return UiTestUtils.getCurrentView();
    }

    JmixValuePicker<String> getLocalizedNamesField(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedNamesField");
    }

    JmixValuePicker<String> getLocalizedDescriptionsField(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedDescriptionsField");
    }
}
