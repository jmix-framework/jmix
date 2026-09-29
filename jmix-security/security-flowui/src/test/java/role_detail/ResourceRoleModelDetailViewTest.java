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

import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securityflowui.view.resourcerole.ResourceRoleModelDetailView;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;
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

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static test_support.TestRoleGrids.getRoleModel;
import static test_support.TestRoleGrids.getShownText;
import static test_support.TestRoleGrids.getTestRoleCodes;
import static test_support.TestRoleGrids.sort;
import static test_support.TestRoleLocalizedValues.clearValue;
import static test_support.TestRoleLocalizedValues.click;
import static test_support.TestRoleLocalizedValues.editLocalizedValues;
import static test_support.TestRoleLocalizedValues.enterValue;
import static test_support.TestRoleLocalizedValues.getCollapsedValue;
import static test_support.TestRoleLocalizedValues.getDialogFields;

/**
 * The test roles are German by default and English for the user of the tests.
 */
@UiTest(viewBasePackages = {"io.jmix.securityflowui.view", "test_support.view"})
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class ResourceRoleModelDetailViewTest {

    static final String PARENT_ROLE_CODE = "database-parent";
    static final String TRANSLATED_ROLE_CODE = "database-translated";
    // Stored in an order other than that of the available locales, with an entry of a locale that is not available and,
    // among the descriptions, a blank one, as an import or an edit outside the views may store it.
    static final String LOCALIZED_NAMES = "fr=Comptabilité\nen=Accounting\nde=Buchhaltungsrolle";
    static final String LOCALIZED_DESCRIPTIONS = "fr=Tient les comptes\nen=Keeps the books\nde=";

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

        ResourceRoleEntity translatedRole = dataManager.create(ResourceRoleEntity.class);
        translatedRole.setCode(TRANSLATED_ROLE_CODE);
        translatedRole.setName("Buchhaltung");
        // The detail view requires a scope to save the role.
        translatedRole.setScopes(Set.of(SecurityScope.UI));
        translatedRole.setLocalizedNames(LOCALIZED_NAMES);
        translatedRole.setLocalizedDescriptions(LOCALIZED_DESCRIPTIONS);

        dataManager.save(parentRole, translatedRole);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from SEC_RESOURCE_ROLE");
        // The repository caches roles by code and does not see a delete through JDBC.
        resourceRoleRepository.invalidateCache();
    }

    @Test
    void childRolesTable_showsAndSortsByTranslatedName() {
        ResourceRoleModelDetailView view = openView(PARENT_ROLE_CODE);
        DataGrid<ResourceRoleModel> childRolesTable = getChildRolesTable(view);
        ResourceRoleModel bookkeeper = getRoleModel(getChildRolesDc(view).getItems(), TestBookkeeperRole.CODE);

        assertThat(getShownText(childRolesTable, "name", bookkeeper)).isEqualTo("Bookkeeper");

        sort(childRolesTable, "name", SortDirection.ASCENDING);

        assertThat(getTestRoleCodes(getChildRolesDc(view).getItems()))
                .containsExactly(TestAuditorRole.CODE, TestBookkeeperRole.CODE, TestManagerRole.CODE);
    }

    @Test
    void localizedValueFields_databaseRole_showLocalesThatHaveValue() {
        ResourceRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        assertThat(getLocalizedNamesField(view).isVisible()).isTrue();
        assertThat(getCollapsedValue(getLocalizedNamesField(view))).isEqualTo("de, en");
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isTrue();
        assertThat(getCollapsedValue(getLocalizedDescriptionsField(view))).isEqualTo("en");
    }

    @Test
    void localizedValueFields_designTimeRole_areHidden() {
        ResourceRoleModelDetailView view = openView(TestBookkeeperRole.CODE);

        assertThat(getLocalizedNamesField(view).isVisible()).isFalse();
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isFalse();
    }

    @Test
    void localizedValueFields_unreadableBundle_showNoLocales() {
        ResourceRoleEntity brokenRole = dataManager.create(ResourceRoleEntity.class);
        brokenRole.setCode("database-broken");
        brokenRole.setName("Kaputt");
        brokenRole.setLocalizedNames("en=Broken\nru=\\u00");
        dataManager.save(brokenRole);

        ResourceRoleModelDetailView view = openView("database-broken");

        assertThat(getCollapsedValue(getLocalizedNamesField(view))).isEmpty();
    }

    @Test
    void editLocalizedNames_save_setsFieldValueAndViewStoresIt() {
        ResourceRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);
        Map<String, String> editedEntries = Map.of("de", "Buchhaltungsrolle", "en", "Bookkeeping",
                "fr", "Comptabilité");

        RoleLocalizedValuesView dialog = editLocalizedValues(localizedNamesField);

        assertThat(dialog.getPageTitle()).isEqualTo(localizedNamesField.getLabel());
        assertThat(getDialogFields(dialog))
                .allMatch(TypedTextField.class::isInstance)
                .extracting(TextFieldBase::getValue)
                .containsExactly("Buchhaltungsrolle", "Accounting");

        enterValue(getDialogFields(dialog).get(1), "Bookkeeping");
        click(dialog, "saveAndCloseBtn");

        assertThat(RoleLocalizedValuesUtils.read(localizedNamesField.getValue())).isEqualTo(editedEntries);

        // Saved without closing: closing navigates to the parent layout, which the tests do not have.
        view.save();

        assertThat(RoleLocalizedValuesUtils.read(loadTranslatedRoleEntity().getLocalizedNames()))
                .isEqualTo(editedEntries);
    }

    @Test
    void editLocalizedDescriptions_opensMultilineDialog() {
        ResourceRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        RoleLocalizedValuesView dialog = editLocalizedValues(getLocalizedDescriptionsField(view));

        assertThat(getDialogFields(dialog))
                .allMatch(JmixTextArea.class::isInstance)
                .extracting(TextFieldBase::getValue)
                .containsExactly("", "Keeps the books");
    }

    @Test
    void clearLocalizedValues_save_viewStoresNone() {
        ResourceRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        clearValue(getLocalizedNamesField(view));
        clearValue(getLocalizedDescriptionsField(view));

        assertThat(getCollapsedValue(getLocalizedNamesField(view))).isEmpty();

        // Saved without closing: closing navigates to the parent layout, which the tests do not have.
        view.save();

        ResourceRoleEntity role = loadTranslatedRoleEntity();
        assertThat(role.getLocalizedNames()).isNull();
        assertThat(role.getLocalizedDescriptions()).isNull();
    }

    ResourceRoleModelDetailView openView(String roleCode) {
        viewNavigationSupport.navigate(ResourceRoleModelDetailView.class, new RouteParameters(
                ResourceRoleModelDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(roleCode)));
        return UiTestUtils.getCurrentView();
    }

    JmixValuePicker<String> getLocalizedNamesField(ResourceRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedNamesField");
    }

    JmixValuePicker<String> getLocalizedDescriptionsField(ResourceRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedDescriptionsField");
    }

    ResourceRoleEntity loadTranslatedRoleEntity() {
        return dataManager.load(ResourceRoleEntity.class)
                .query("e.code = :code")
                .parameter("code", TRANSLATED_ROLE_CODE)
                .one();
    }

    DataGrid<ResourceRoleModel> getChildRolesTable(ResourceRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "childRolesTable");
    }

    CollectionContainer<ResourceRoleModel> getChildRolesDc(ResourceRoleModelDetailView view) {
        return ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
    }
}
