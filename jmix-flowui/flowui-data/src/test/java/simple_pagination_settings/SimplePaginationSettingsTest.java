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

package simple_pagination_settings;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.component.pagination.PaginationSettingsUtils;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.component.SimplePaginationSettings;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import simple_pagination_settings.view.SimplePaginationSettingsTestView;
import test_support.AbstractSettingsTest;
import test_support.FlowuiDataTestConfiguration;
import test_support.entity.Project;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"simple_pagination_settings.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class SimplePaginationSettingsTest extends AbstractSettingsTest {

    @Autowired
    UserSettingsCache userSettingsCache;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    UnconstrainedDataManager dataManager;

    @BeforeEach
    public void beforeEach() {
        IntStream.range(0, 10).forEach(__ -> dataManager.save(dataManager.create(Project.class)));
    }

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from FLOWUI_USER_SETTINGS");
        jdbc.update("delete from TEST_PROJECT");
        userSettingsCache.clear();
    }

    @Test
    @DisplayName("Save settings")
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void saveSettingsTest() {
        // Open and close View with SimplePagination
        SimplePaginationSettingsTestView view = navigateTo(SimplePaginationSettingsTestView.class);
        view.closeWithDefaultAction();

        // Settings of SimplePagination should be saved
        ViewSettings viewSettings = loadSettings(view.getId().get());

        var paginationSettings = viewSettings.getSettings("pagination", SimplePaginationSettings.class)
                .orElse(null);
        assertNotNull(paginationSettings);
        assertEquals(5, paginationSettings.getItemsPerPageValue());

        // Open View again, change itemsPerPage value, close View
        view = navigateTo(SimplePaginationSettingsTestView.class);;

        var changedValue = 3;
        PaginationSettingsUtils.setItemsPerPageValue(view.pagination, changedValue);

        view.closeWithDefaultAction();

        // itemsPerPage of the SimplePagination should be saved
        viewSettings = loadSettings(view.getId().get());

        paginationSettings = viewSettings.getSettings("pagination", SimplePaginationSettings.class)
                .orElse(null);
        assertNotNull(paginationSettings);
        assertEquals(changedValue, paginationSettings.getItemsPerPageValue());
    }

    @Test
    @DisplayName("Apply settings")
    public void applySettingsTest() {
        // Open View and change SimplePagination itemsPerPage value
        SimplePaginationSettingsTestView view = navigateTo(SimplePaginationSettingsTestView.class);

        var changedValue = 3;
        PaginationSettingsUtils.setItemsPerPageValue(view.pagination, changedValue);

        view.closeWithDefaultAction();

        /*
         * Open View again. It should load amount of items according
         * to the saved value.
         */
        view = navigateTo(SimplePaginationSettingsTestView.class);

        assertEquals(changedValue, PaginationSettingsUtils.getItemsPerPageValue(view.pagination));
        assertEquals(changedValue, view.projectsDc.getItems().size());
    }

    @Test
    @DisplayName("Save selected null item")
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void saveSelectedNullItemTest() {
        /*
         * Open and close View. The default value will be saved.
         * Change itemsPerPage value to NULL and close the View.
         */
        SimplePaginationSettingsTestView view = navigateTo(SimplePaginationSettingsTestView.class);
        view.closeWithDefaultAction();

        viewNavigators.view(SimplePaginationSettingsTestView.class)
                .navigate();
        view = UiTestUtils.getCurrentView();

        PaginationSettingsUtils.setItemsPerPageValue(view.pagination, null);

        view.closeWithDefaultAction();

        /*
         * Null value should be saved. It means that max fetch size for the entity
         * will be saved, in this case - 10 000
         */
        var paginationSettings = loadSettings(view.getId().get())
                .getSettings("pagination", SimplePaginationSettings.class)
                .orElse(null);
        assertNotNull(paginationSettings);
        assertEquals(10000, paginationSettings.getItemsPerPageValue());
    }

    @Test
    @DisplayName("Apply saved null item")
    public void applySavedNullItem() {
        /*
         * Open and close View. The default value will be saved.
         * Change itemsPerPage value to NULL and close the View.
         */
        SimplePaginationSettingsTestView view = navigateTo(SimplePaginationSettingsTestView.class);
        view.closeWithDefaultAction();

        viewNavigators.view(SimplePaginationSettingsTestView.class)
                .navigate();
        view = UiTestUtils.getCurrentView();

        PaginationSettingsUtils.setItemsPerPageValue(view.pagination, null);

        view.closeWithDefaultAction();

        /*
         * Open View. Null option must be selected and loader should
         * load maximum amount of items (10 in this case).
         *
         * Null item means the max fetch size for the entity, in this case - 10 000
         */
        view = navigateTo(SimplePaginationSettingsTestView.class);

        assertEquals(10000, PaginationSettingsUtils.getItemsPerPageValue(view.pagination));
        assertEquals(10, view.projectsDc.getItems().size());
    }
}
