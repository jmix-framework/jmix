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

package test_support;

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.entity.EntityValues;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.security.model.BaseRoleModel;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class TestRoleGrids {

    /**
     * The prefix of the codes of the test roles.
     */
    private static final String TEST_ROLE_CODE_PREFIX = "test-";

    private TestRoleGrids() {
    }

    /**
     * Returns the text that a column shows for an item: the text of the component its renderer creates, or else the
     * value of the property the column is bound to, which a property column shows by default.
     */
    public static <E> String getShownText(DataGrid<E> grid, String columnKey, E item) {
        DataGridColumn<E> column = Objects.requireNonNull(grid.getColumnByKey(columnKey));
        if (column.getRenderer() instanceof ComponentRenderer<?, E> renderer) {
            return renderer.createComponent(item).getElement().getText();
        }
        return Objects.toString(EntityValues.getValue(item, columnKey));
    }

    public static <E> void sort(DataGrid<E> grid, String columnKey, SortDirection direction) {
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(columnKey), direction)));
    }

    /**
     * Returns the role model with the code.
     */
    public static <M extends BaseRoleModel> M getRoleModel(Collection<M> roleModels, String code) {
        return roleModels.stream()
                .filter(roleModel -> code.equals(roleModel.getCode()))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Returns the codes of the test roles among the models, in the order of the models. The roles of the modules in
     * the context are left out.
     */
    public static List<String> getTestRoleCodes(Collection<? extends BaseRoleModel> roleModels) {
        return roleModels.stream()
                .map(BaseRoleModel::getCode)
                .filter(code -> code.startsWith(TEST_ROLE_CODE_PREFIX))
                .toList();
    }
}
