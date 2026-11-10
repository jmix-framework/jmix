/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reportsflowui.impl.annotated;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsflowui.test_support.report.MultiTemplateTableReport;
import io.jmix.reportsflowui.test_support.report.PublishersAndGamesReport;
import io.jmix.reportsflowui.test_support.report.SingleCompatibleTableReport;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportTableView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class TableOutputTest extends BaseRunReportUiTest {

    @Autowired
    DialogWindows dialogWindows;

    @Test
    public void testTableOutput() {
        // when
        ReportTableView tableOutputDialog = runPublishersAndGamesReport("2025-03-01", "2025-06-01");

        // then
        // band #1
        DataGrid<KeyValueEntity> publishersGrid = findComponent(tableOutputDialog, "PublishersTable");
        assertThat(
                publishersGrid.getColumns().stream().map(DataGrid.Column::getHeaderText).toList()
        ).contains("Name", "Game count");

        DataGridItems<KeyValueEntity> gridItems = publishersGrid.getItems();
        assertThat(gridItems).isNotNull();

        KeyValueEntity firstItem = gridItems.getItems().iterator().next();
        assertThat((String) firstItem.getValue("name")).isEqualTo("Activision");
        assertThat((Long) firstItem.getValue("gameCount")).isEqualTo(2);

        // band #2
        DataGrid<KeyValueEntity> gamesGrid = findComponent(tableOutputDialog, "GamesTable");
        assertThat(
                gamesGrid.getColumns().stream().map(DataGrid.Column::getHeaderText).toList()
        ).contains("Name", "Purchase count");

        gridItems = gamesGrid.getItems();
        assertThat(gridItems).isNotNull();

        firstItem = gridItems.getItems().iterator().next();
        assertThat((String) firstItem.getValue("name")).isEqualTo("Assassin's Creed");
        assertThat((Long) firstItem.getValue("purchaseCount")).isEqualTo(2);
    }

    @Test
    public void testTableOutputShowsBandWithoutRowsAsEmptyGrid() {
        // when: no games were purchased in 2024
        ReportTableView tableOutputDialog = runPublishersAndGamesReport("2024-01-01", "2024-12-31");

        // then
        DataGrid<KeyValueEntity> gamesGrid = findComponent(tableOutputDialog, "GamesTable");
        assertThat(gamesGrid.getColumns().stream().map(DataGrid.Column::getHeaderText).toList())
                .containsExactly("Name", "Purchase count");

        DataGridItems<KeyValueEntity> gridItems = gamesGrid.getItems();
        assertThat(gridItems).isNotNull();
        assertThat(gridItems.getItems()).isEmpty();
    }

    @Test
    public void testTableModeSkipsDialogForSingleCompatibleTemplateWithoutParameters() {
        launchTableReportFromRunView(SingleCompatibleTableReport.CODE);

        ReportTableView tableOutputDialog = (ReportTableView) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(tableOutputDialog).isNotNull();

        DataGrid<KeyValueEntity> tableGrid = findComponent(tableOutputDialog, "DataTable");
        assertThat(tableGrid.getColumns().stream().map(DataGrid.Column::getHeaderText).toList())
                .containsExactly("Name", "Count");
    }

    @Test
    public void testTableModeShowsTemplateSelectionForMultipleCompatibleTemplates() {
        launchTableReportFromRunView(MultiTemplateTableReport.CODE);

        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
        assertThat(parametersDialog).isNotNull();

        EntityComboBox<ReportTemplate> templateComboBox = findInputParametersComponent(parametersDialog, "templateComboBox");
        assertThat(templateComboBox.isVisible()).isTrue();
        assertThat(templateComboBox.getGenericDataView().getItems().toList())
                .extracting(ReportTemplate::getCode)
                .containsExactlyInAnyOrder(
                        MultiTemplateTableReport.FIRST_TABLE_TEMPLATE,
                        MultiTemplateTableReport.SECOND_TABLE_TEMPLATE
                );
    }

    private ReportTableView runPublishersAndGamesReport(String startDateStr, String endDateStr) {
        launchReportFromRunView(PublishersAndGamesReport.CODE);
        InputParametersDialog parametersDialog = (InputParametersDialog) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);

        TypedDatePicker startDateField = findInputParametersComponent(parametersDialog, "param_startDate");
        startDateField.setValue(parseDate(startDateStr));
        TypedDatePicker endDateField = findInputParametersComponent(parametersDialog, "param_endDate");
        endDateField.setValue(parseDate(endDateStr));

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        return (ReportTableView) dialogWindows.getOpenedDialogWindows()
                .getCurrentDialog().orElse(null);
    }
}
