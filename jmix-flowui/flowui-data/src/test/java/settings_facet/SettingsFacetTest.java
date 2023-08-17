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

package settings_facet;

import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.facet.settings.component.DataGridSettings;
import io.jmix.flowui.facet.settings.component.JmixDetailsSettings;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import settings_facet.view.FacetAutoExcludedTestView;
import settings_facet.view.FacetAutoTestView;
import settings_facet.view.FacetDelegateTestView;
import settings_facet.view.FacetManualTestView;
import test_support.FlowuiDataTestConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = {"settings_facet.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class SettingsFacetTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    public void afterEach() {
        jdbc.update("delete from FLOWUI_UI_SETTING");
    }

    @Test
    @DisplayName("Facet with auto mode")
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public void facetAutoMode() {
        // Open View with SettingsFacet and close it to save settings
        viewNavigators.view(FacetAutoTestView.class)
                .navigate();

        FacetAutoTestView view = UiTestUtils.getCurrentView();

        view.closeWithDefaultAction();

        // Open View again, settings of DataGrid should be saved
        viewNavigators.view(FacetAutoTestView.class)
                .navigate();

        view = UiTestUtils.getCurrentView();

        Optional<DataGridSettings> dataGridSettings =
                view.facet.getSettings()
                        .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class);

        assertTrue(dataGridSettings.isPresent());
    }

    @Test
    @DisplayName("Facet with manual mode")
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public void facetManualMode() {
        // Open View with SettingsFacet and close it to save settings
        viewNavigators.view(FacetManualTestView.class)
                .navigate();

        FacetManualTestView view = UiTestUtils.getCurrentView();
        view.closeWithDefaultAction();

        // Open View again, settings of DataGrid must not be saved,
        // as it is not included to facet
        viewNavigators.view(FacetManualTestView.class)
                .navigate();

        view = UiTestUtils.getCurrentView();

        Optional<DataGridSettings> dataGridSettings =
                view.facet.getSettings()
                        .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class);

        assertFalse(dataGridSettings.isPresent());

        // Add DataGrid to facet
        view.facet.addComponentIds(view.projectsDataGrid.getId().get());

        // Close View to save settings
        view.closeWithDefaultAction();

        // Open View again, DataGrid's settings should be saved
        viewNavigators.view(FacetManualTestView.class)
                .navigate();

        dataGridSettings = view.facet.getSettings()
                .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class);

        assertTrue(dataGridSettings.isPresent());
    }

    @Test
    @DisplayName("Facet with auto mode and excluded component")
    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public void facetWithAutoModeAndExcludedComponent() {
        // Open View with SettingsFacet that excluded one component
        viewNavigators.view(FacetAutoExcludedTestView.class)
                .navigate();

        FacetAutoExcludedTestView view = UiTestUtils.getCurrentView();

        // Check that facet has excluded components
        assertTrue(view.facet.getExcludedComponentIds().size() > 0);
        assertTrue(view.facet.getExcludedComponentIds().contains("details"));

        // Reopen View
        view.closeWithDefaultAction();

        viewNavigators.view(FacetAutoExcludedTestView.class)
                .navigate();

        view = UiTestUtils.getCurrentView();

        // Settings for JmixDetails shouldn't be presented
        Optional<JmixDetailsSettings> detailsSettings = view.facet.getSettings()
                .getSettings(view.details.getId().get(), JmixDetailsSettings.class);
        assertFalse(detailsSettings.isPresent());

        // DataGrid's settings should be saved
        Optional<DataGridSettings> dataGridSettings = view.facet.getSettings()
                .getSettings(view.projectsDataGrid.getId().get(), DataGridSettings.class);
        assertTrue(dataGridSettings.isPresent());
    }

    @Test
    @DisplayName("Facet with delegated saving, applying and applyDataLoading")
    public void facetWithDelegatedSavingApplyingAndApplyDataLoading() {
        // Open View with SettingsFacet and check that delegates work
        viewNavigators.view(FacetDelegateTestView.class)
                .navigate();

        FacetDelegateTestView view = UiTestUtils.getCurrentView();

        // ApplyDataLoading and Apply delegates should be fired
        assertEquals(2, view.calls);

        // Close screen
        view.closeWithDefaultAction();

        // Save settings delegate should be fired
        assertEquals(3, view.calls);
    }
}
