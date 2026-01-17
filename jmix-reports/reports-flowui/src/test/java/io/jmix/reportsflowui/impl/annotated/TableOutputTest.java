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
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.reportsflowui.test_support.OpenedDialogViewsTracker;
import io.jmix.reportsflowui.test_support.report.PublishersAndGamesReport;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportTableView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class TableOutputTest extends BaseRunReportUiTest {

    @Autowired
    OpenedDialogViewsTracker openedDialogViewsTracker;

    @Test
    public void testTableOutput() {
        // given
        String reportCode = PublishersAndGamesReport.CODE;
        String startDateStr = "2025-03-01";
        String endDateStr = "2025-06-01";

        // when
        launchReportFromRunView(reportCode);
        InputParametersDialog parametersDialog = (InputParametersDialog) openedDialogViewsTracker.getLastOpenedView();

        TypedDatePicker startDateField = findParameterField(parametersDialog, "param_startDate");
        startDateField.setValue(parseDate(startDateStr));
        TypedDatePicker endDateField = findParameterField(parametersDialog, "param_endDate");
        endDateField.setValue(parseDate(endDateStr));

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        // then
        ReportTableView tableOutputDialog = (ReportTableView) openedDialogViewsTracker.getLastOpenedView();

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
}
