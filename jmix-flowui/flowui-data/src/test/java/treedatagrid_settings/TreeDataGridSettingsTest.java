/*
 * Copyright 2022 Haulmont.
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

package treedatagrid_settings;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
import io.jmix.flowui.facet.settings.component.TreeDataGridSettings;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.AbstractSettingsTest;
import test_support.FlowuiDataTestConfiguration;
import test_support.entity.Folder;
import treedatagrid_settings.view.TreeDataGridSettingsTestView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(viewBasePackages = {"treedatagrid_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class TreeDataGridSettingsTest extends AbstractSettingsTest {

    @Autowired
    UserSettingsCache userSettingsCache;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from UI_UI_SETTING");
        userSettingsCache.clear();
    }

    @Test
    @DisplayName("Save settings")
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public void saveSettingsTest() {
        // Open and close View
        TreeDataGridSettingsTestView view = navigateTo(TreeDataGridSettingsTestView.class);
        view.closeWithDefaultAction();

        // TreeDataGrid settings should be saved
        var dataGridSettings = loadSettings(view.getId().get())
                .getSettings(view.foldersDataGrid.getId().get(), TreeDataGridSettings.class)
                .orElse(null);
        assertNotNull(dataGridSettings);

        // Change settings of DataGrid
        view = navigateTo(TreeDataGridSettingsTestView.class);

        // Visibility is not saved in settings, it is needed to check that
        // columns size is not changed after saving settings.
        view.foldersDataGrid.getColumnByKey("parent").setVisible(false);
        view.foldersDataGrid.setColumnPosition(getColumn(view, "size"), 0);
        view.foldersDataGrid.sort(List.of(
                new GridSortOrder<>(getColumn(view, "name"), SortDirection.ASCENDING),
                new GridSortOrder<>(getColumn(view, "size"), SortDirection.DESCENDING)));

        view.closeWithDefaultAction();

        // TreeDataGrid settings should be saved
        dataGridSettings = loadSettings(view.getId().get())
                .getSettings(view.foldersDataGrid.getId().get(), TreeDataGridSettings.class)
                .orElse(null);

        assertNotNull(dataGridSettings);
        assertNotNull(dataGridSettings.getSortOrder());

        assertTrue(isSortAscending(dataGridSettings.getSortOrder(), "name"));
        assertFalse(isSortAscending(dataGridSettings.getSortOrder(), "size"));

        assertNotNull(dataGridSettings.getColumns());

        assertEquals("size", dataGridSettings.getColumns().get(0).getKey());

        /*
         * Hidden columns by 'visible' property are presented in Grid#getColumns(),
         * so settings also should contain them.
         */
        assertEquals(3, dataGridSettings.getColumns().size());
    }

    @Test
    @DisplayName("Apply settings")
    public void applySettingsTest() {
        // Change settings of TreeDataGrid
        TreeDataGridSettingsTestView view = navigateTo(TreeDataGridSettingsTestView.class);

        view.foldersDataGrid.setColumnPosition(getColumn(view, "size"), 0);
        view.foldersDataGrid.sort(List.of(
                new GridSortOrder<>(getColumn(view, "name"), SortDirection.ASCENDING),
                new GridSortOrder<>(getColumn(view, "size"), SortDirection.DESCENDING)));

        view.closeWithDefaultAction();

        // Reopen View
        view = navigateTo(TreeDataGridSettingsTestView.class);

        // TreeDataGrid settings should be applied
        var sortOrder = view.foldersDataGrid.getSortOrder();
        assertEquals(2, sortOrder.size());

        assertEquals("name", sortOrder.get(0).getSorted().getKey());
        assertEquals(SortDirection.ASCENDING, sortOrder.get(0).getDirection());

        assertEquals("size", sortOrder.get(1).getSorted().getKey());
        assertEquals(SortDirection.DESCENDING, sortOrder.get(1).getDirection());

        assertEquals("size", view.foldersDataGrid.getColumns().get(0).getKey());
    }

    protected boolean isSortAscending(List<DataGridSettings.SortOrder> sortOrder, String key) {
        DataGridSettings.SortOrder columnSo = sortOrder.stream().filter(so -> key.equals(so.getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no '" + key + "' column in SortOrder settings"));
        return SortDirection.ASCENDING.name().equals(columnSo.getSortDirection());
    }

    protected Grid.Column<Folder> getColumn(TreeDataGridSettingsTestView view, String key) {
        return view.foldersDataGrid.getColumnByKey(key);
    }
}
