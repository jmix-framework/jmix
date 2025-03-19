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

package datagrid_settings;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import datagrid_settings.view.DataGridSettingsNestedContainerTestVIew;
import datagrid_settings.view.DataGridSettingsTestView;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
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
import test_support.entity.Project;
import test_support.entity.User;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"datagrid_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class DataGridSettingsTest extends AbstractSettingsTest {

    @Autowired
    UserSettingsCache userSettingsCache;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from FLOWUI_USER_SETTINGS");
        userSettingsCache.clear();
    }

    @Test
    @DisplayName("Save settings")
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public void saveSettingsTest() {
        // Open and close View
        DataGridSettingsTestView view = navigateTo(DataGridSettingsTestView.class);
        view.closeWithDefaultAction();

        // DataGrid settings should be saved
        var dataGridSettings = loadSettings(view.getId().get())
                .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class)
                .orElse(null);
        assertNotNull(dataGridSettings);

        // Change settings of DataGrid
        view = navigateTo(DataGridSettingsTestView.class);

        view.projectsDataGrid.getColumnByKey("active").setVisible(false);
        view.projectsDataGrid.setColumnPosition(getPdgColumn(view, "budget"), 0);
        view.projectsDataGrid.sort(List.of(
                new GridSortOrder<>(getPdgColumn(view, "name"), SortDirection.ASCENDING),
                new GridSortOrder<>(getPdgColumn(view, "description"), SortDirection.DESCENDING)));

        view.closeWithDefaultAction();

        // DataGrid settings should be saved
        dataGridSettings = loadSettings(view.getId().get())
                .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class)
                .orElse(null);

        assertNotNull(dataGridSettings);
        assertNotNull(dataGridSettings.getSortOrder());

        assertTrue(isSortAscending(dataGridSettings.getSortOrder(), "name"));
        assertFalse(isSortAscending(dataGridSettings.getSortOrder(), "description"));

        assertNotNull(dataGridSettings.getColumns());

        assertEquals(5, dataGridSettings.getColumns().size());
        assertEquals("budget", dataGridSettings.getColumns().get(0).getKey());
        assertFalse(dataGridSettings.getColumns().get(4).getVisible()); // "active" column
    }

    @Test
    @DisplayName("Apply settings")
    public void applySettingsTest() {
        // Change settings of DataGrid
        DataGridSettingsTestView view = navigateTo(DataGridSettingsTestView.class);

        view.projectsDataGrid.setColumnPosition(getPdgColumn(view, "budget"), 0);
        view.projectsDataGrid.sort(List.of(
                new GridSortOrder<>(getPdgColumn(view, "name"), SortDirection.ASCENDING),
                new GridSortOrder<>(getPdgColumn(view, "description"), SortDirection.DESCENDING)));

        view.closeWithDefaultAction();

        // Reopen View
        view = navigateTo(DataGridSettingsTestView.class);

        // DataGrid settings should be applied
        var sortOrder = view.projectsDataGrid.getSortOrder();
        assertEquals(2, sortOrder.size());

        assertEquals("name", sortOrder.get(0).getSorted().getKey());
        assertEquals(SortDirection.ASCENDING, sortOrder.get(0).getDirection());

        assertEquals("description", sortOrder.get(1).getSorted().getKey());
        assertEquals(SortDirection.DESCENDING, sortOrder.get(1).getDirection());

        assertEquals("budget", view.projectsDataGrid.getColumns().get(0).getKey());
    }

    @Test
    @DisplayName("Apply sorting to DataGrid with CollectionPropertyContainer")
    public void applySortingToDataGridWithCollectionPropertyContainer() {
        var view = navigateTo(DataGridSettingsNestedContainerTestVIew.class);

        /*
         * Change settings of DataGrid
         */
        view.nestedUsersDataGrid.sort(List.of(
                new GridSortOrder<>(getUdgColumn(view, "issuesCount"), SortDirection.ASCENDING)));

        view.closeWithDefaultAction();

        /*
         * Reopen view and check restored settings
         */
        view = navigateTo(DataGridSettingsNestedContainerTestVIew.class);

        assertEquals(1, view.nestedUsersDataGrid.getSortOrder().size());
        assertEquals(SortDirection.ASCENDING, view.nestedUsersDataGrid.getSortOrder().get(0).getDirection());

        // Check items order
        assertNotNull(view.nestedUsersDataGrid.getItems());

        Iterator<User> iterator = view.nestedUsersDataGrid.getItems().getItems().iterator();
        assertEquals(1, iterator.next().getIssuesCount());
        assertEquals(2, iterator.next().getIssuesCount());
    }

    protected boolean isSortAscending(List<DataGridSettings.SortOrder> sortOrder, String key) {
        DataGridSettings.SortOrder columnSo = sortOrder.stream().filter(so -> key.equals(so.getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no '" + key + "' column in SortOrder settings"));
        return SortDirection.ASCENDING.name().equals(columnSo.getSortDirection());
    }

    protected Grid.Column<Project> getPdgColumn(DataGridSettingsTestView view, String key) {
        return view.projectsDataGrid.getColumnByKey(key);
    }

    protected Grid.Column<User> getUdgColumn(DataGridSettingsNestedContainerTestVIew view, String key) {
        return view.nestedUsersDataGrid.getColumnByKey(key);
    }
}