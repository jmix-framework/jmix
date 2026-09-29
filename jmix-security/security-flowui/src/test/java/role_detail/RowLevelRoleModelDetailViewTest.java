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
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.security.impl.role.RoleLocalizedValuesUtils;
import io.jmix.security.model.RowLevelRoleModel;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import io.jmix.securityflowui.view.rolelocalization.RoleLocalizedValuesView;
import io.jmix.securityflowui.view.rowlevelrole.RowLevelRoleModelDetailView;
import org.jspecify.annotations.Nullable;
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
public class RowLevelRoleModelDetailViewTest {

    static final String PARENT_ROLE_CODE = "database-parent";
    static final String TRANSLATED_ROLE_CODE = "database-translated";
    // Stored in an order other than that of the available locales, with an entry of a locale that is not available.
    static final String LOCALIZED_NAMES = "fr=Archives\nen=Archive\nde=Archivrolle";
    static final String LOCALIZED_DESCRIPTIONS = "fr=Voit les archives\nen=Sees the archive\nde=Sieht das Archiv";

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

        RowLevelRoleEntity translatedRole = dataManager.create(RowLevelRoleEntity.class);
        translatedRole.setCode(TRANSLATED_ROLE_CODE);
        translatedRole.setName("Archiv");
        translatedRole.setLocalizedNames(LOCALIZED_NAMES);
        translatedRole.setLocalizedDescriptions(LOCALIZED_DESCRIPTIONS);

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

    @Test
    void localizedValueFields_databaseRole_showLocalesThatHaveValue() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        assertThat(getLocalizedNamesField(view).isVisible()).isTrue();
        assertThat(getCollapsedValue(getLocalizedNamesField(view))).isEqualTo("de, en");
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isTrue();
        assertThat(getCollapsedValue(getLocalizedDescriptionsField(view))).isEqualTo("de, en");
    }

    @Test
    void localizedValueFields_newRole_areShown() {
        RowLevelRoleModelDetailView view = openView(null);

        assertThat(getLocalizedNamesField(view).isVisible()).isTrue();
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isTrue();
    }

    @Test
    void localizedValueFields_designTimeRole_areHidden() {
        RowLevelRoleModelDetailView view = openView(TestBranchRowLevelRole.CODE);

        assertThat(getLocalizedNamesField(view).isVisible()).isFalse();
        assertThat(getLocalizedDescriptionsField(view).isVisible()).isFalse();
    }

    @Test
    void localizedValueFields_unreadableBundle_showNoLocales() {
        // A bundle with a malformed escape, as an import or an edit outside the views may store it.
        RowLevelRoleEntity brokenRole = dataManager.create(RowLevelRoleEntity.class);
        brokenRole.setCode("database-broken");
        brokenRole.setName("Kaputt");
        brokenRole.setLocalizedNames("en=Broken\nru=\\u00");
        dataManager.save(brokenRole);

        RowLevelRoleModelDetailView view = openView("database-broken");
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);

        assertThat(getCollapsedValue(localizedNamesField)).isEmpty();
        assertThat(getDialogFields(editLocalizedValues(localizedNamesField)))
                .extracting(TextFieldBase::getValue)
                .containsExactly("", "");
    }

    @Test
    void editLocalizedNames_opensDialogTitledWithFieldLabel() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);

        RoleLocalizedValuesView dialog = editLocalizedValues(localizedNamesField);

        assertThat(dialog.getPageTitle()).isEqualTo(localizedNamesField.getLabel());
        assertThat(getDialogFields(dialog))
                .allMatch(TypedTextField.class::isInstance)
                .extracting(TextFieldBase::getValue)
                .containsExactly("Archivrolle", "Archive");
    }

    @Test
    void editLocalizedNames_save_setsFieldValueAndViewStoresIt() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);
        RoleLocalizedValuesView dialog = editLocalizedValues(localizedNamesField);
        Map<String, String> editedEntries = Map.of("de", "Archivrolle", "en", "Records", "fr", "Archives");

        enterValue(getDialogFields(dialog).get(1), "Records");
        click(dialog, "saveAndCloseBtn");

        assertThat(RoleLocalizedValuesUtils.read(localizedNamesField.getValue())).isEqualTo(editedEntries);

        // Saved without closing: closing navigates to the parent layout, which the tests do not have.
        view.save();

        assertThat(RoleLocalizedValuesUtils.read(loadTranslatedRoleEntity().getLocalizedNames()))
                .isEqualTo(editedEntries);
    }

    @Test
    void editLocalizedNames_close_leavesValue() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);
        JmixValuePicker<String> localizedNamesField = getLocalizedNamesField(view);
        RoleLocalizedValuesView dialog = editLocalizedValues(localizedNamesField);

        enterValue(getDialogFields(dialog).get(1), "Records");
        click(dialog, "closeBtn");

        assertThat(localizedNamesField.getValue()).isEqualTo(LOCALIZED_NAMES);
    }

    @Test
    void clearLocalizedValues_save_viewStoresNone() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        clearValue(getLocalizedNamesField(view));
        clearValue(getLocalizedDescriptionsField(view));

        assertThat(getCollapsedValue(getLocalizedNamesField(view))).isEmpty();

        // Saved without closing: closing navigates to the parent layout, which the tests do not have.
        view.save();

        RowLevelRoleEntity role = loadTranslatedRoleEntity();
        assertThat(role.getLocalizedNames()).isNull();
        assertThat(role.getLocalizedDescriptions()).isNull();
    }

    @Test
    void editLocalizedDescriptions_opensMultilineDialog() {
        RowLevelRoleModelDetailView view = openView(TRANSLATED_ROLE_CODE);

        RoleLocalizedValuesView dialog = editLocalizedValues(getLocalizedDescriptionsField(view));

        assertThat(getDialogFields(dialog))
                .allMatch(JmixTextArea.class::isInstance)
                .extracting(TextFieldBase::getValue)
                .containsExactly("Sieht das Archiv", "Sees the archive");
    }

    /**
     * Opens the detail view of the role with the code, or of a new role for {@code null}.
     */
    RowLevelRoleModelDetailView openView(@Nullable String roleCode) {
        String serializedCode = roleCode == null
                ? StandardDetailView.NEW_ENTITY_ID
                : urlParamSerializer.serialize(roleCode);
        viewNavigationSupport.navigate(RowLevelRoleModelDetailView.class,
                new RouteParameters(RowLevelRoleModelDetailView.ROUTE_PARAM_NAME, serializedCode));
        return UiTestUtils.getCurrentView();
    }

    JmixValuePicker<String> getLocalizedNamesField(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedNamesField");
    }

    JmixValuePicker<String> getLocalizedDescriptionsField(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "localizedDescriptionsField");
    }

    RowLevelRoleEntity loadTranslatedRoleEntity() {
        return dataManager.load(RowLevelRoleEntity.class)
                .query("e.code = :code")
                .parameter("code", TRANSLATED_ROLE_CODE)
                .one();
    }

    DataGrid<RowLevelRoleModel> getChildRolesTable(RowLevelRoleModelDetailView view) {
        return UiTestUtils.getComponent(view, "childRolesTable");
    }

    CollectionContainer<RowLevelRoleModel> getChildRolesDc(RowLevelRoleModelDetailView view) {
        return ViewControllerUtils.getViewData(view).getContainer("childRolesDc");
    }
}
