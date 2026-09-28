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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.security.model.BaseRoleModel;
import io.jmix.security.model.ResourceRoleModel;
import io.jmix.security.role.RoleLocalizationSupport;
import io.jmix.securityflowui.impl.role.LocalizedRoleColumnsSupport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import test_support.SecurityFlowuiTestConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {SecurityFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class LocalizedRoleColumnsSupportTest {

    @Autowired
    Metadata metadata;
    @Autowired
    UiComponents uiComponents;

    @Test
    void sortByName_resolvesNameOfEachModelOnce() {
        TestRoleLocalizationSupport roleLocalizationSupport = new TestRoleLocalizationSupport(
                Map.of("first", "D", "second", "b", "third", "C", "fourth", "a"));
        List<ResourceRoleModel> roleModels = List.of(createRoleModel("first", "Zeta"),
                createRoleModel("second", "Beta"), createRoleModel("third", "Alpha"),
                createRoleModel("fourth", "Gamma"));

        createSupport(roleLocalizationSupport).sortByName(roleModels);

        assertThat(roleLocalizationSupport.nameCalls).isEqualTo(roleModels.size());
        assertThat(roleLocalizationSupport.descriptionCalls).isZero();
    }

    @Test
    void sortByName_ordersByTranslationIgnoringCase() {
        TestRoleLocalizationSupport roleLocalizationSupport = new TestRoleLocalizationSupport(
                Map.of("first", "B", "second", "a", "third", "C"));
        List<ResourceRoleModel> roleModels = List.of(createRoleModel("first", "Zeta"),
                createRoleModel("second", "Beta"), createRoleModel("third", "Alpha"));

        List<ResourceRoleModel> sorted = createSupport(roleLocalizationSupport).sortByName(roleModels);

        assertThat(sorted)
                .extracting(BaseRoleModel::getCode)
                .containsExactly("second", "first", "third");
    }

    @Test
    void install_sortByNameColumn_resolvesEachNameOnceAndNoDescription() {
        TestRoleLocalizationSupport roleLocalizationSupport = new TestRoleLocalizationSupport(
                Map.of("first", "D", "second", "b", "third", "C", "fourth", "a"));
        List<ResourceRoleModel> roleModels = new ArrayList<>(List.of(createRoleModel("first", "Zeta"),
                createRoleModel("second", "Beta"), createRoleModel("third", "Alpha"),
                createRoleModel("fourth", "Gamma")));
        //noinspection unchecked
        DataGrid<ResourceRoleModel> grid = uiComponents.create(DataGrid.class);
        MetaPropertyPath namePath = metadata.getClass(ResourceRoleModel.class).getPropertyPath("name");
        grid.addColumn("name", Objects.requireNonNull(namePath));

        createSupport(roleLocalizationSupport).install(grid);
        DataGridColumn<ResourceRoleModel> nameColumn = Objects.requireNonNull(grid.getColumnByKey("name"));
        roleModels.sort(Objects.requireNonNull(nameColumn.getComparatorOrNull()));

        assertThat(roleModels)
                .extracting(BaseRoleModel::getCode)
                .containsExactly("fourth", "second", "third", "first");
        assertThat(roleLocalizationSupport.nameCalls).isEqualTo(roleModels.size());
        assertThat(roleLocalizationSupport.descriptionCalls).isZero();
    }

    /**
     * Returns the bean with the given resolver in place of the injected one.
     */
    LocalizedRoleColumnsSupport createSupport(RoleLocalizationSupport roleLocalizationSupport) {
        LocalizedRoleColumnsSupport support = new LocalizedRoleColumnsSupport();
        ReflectionTestUtils.setField(support, "roleLocalizationSupport", roleLocalizationSupport);
        return support;
    }

    ResourceRoleModel createRoleModel(String code, String name) {
        ResourceRoleModel roleModel = metadata.create(ResourceRoleModel.class);
        roleModel.setCode(code);
        roleModel.setName(name);
        return roleModel;
    }

    /**
     * Returns fixed translations by the code of a role and counts how often the texts of a role are resolved.
     */
    @NullMarked
    static class TestRoleLocalizationSupport extends RoleLocalizationSupport {

        final Map<String, String> namesByCode;
        int nameCalls;
        int descriptionCalls;

        TestRoleLocalizationSupport(Map<String, String> namesByCode) {
            this.namesByCode = namesByCode;
        }

        @Override
        public String getLocalizedName(BaseRoleModel roleModel) {
            nameCalls++;
            return namesByCode.get(roleModel.getCode());
        }

        @Override
        public @Nullable String getLocalizedDescription(BaseRoleModel roleModel) {
            descriptionCalls++;
            return null;
        }
    }
}
