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

import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.ReportsFlowuiTestConfiguration;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.test_support.OpenedDialogViewsTracker;
import io.jmix.reportsflowui.test_support.entity.TestDataInitializer;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.InputParametersFragment;
import io.jmix.reportsflowui.view.run.ReportRunView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {ReportsFlowuiTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public abstract class BaseRunReportUiTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @BeforeAll
    public static void setup(@Autowired TestDataInitializer testDataInitializer) {
        testDataInitializer.init();
    }

    protected void launchReportFromRunView(String reportCode) {
        viewNavigators.view(UiTestUtils.getCurrentView(), ReportRunView.class).navigate();
        ReportRunView reportRunView = UiTestUtils.getCurrentView();

        DataGrid<Report> dataGrid = findComponent(reportRunView, "reportDataGrid");

        DataGridItems<Report> gridItems = dataGrid.getItems();
        assertThat(gridItems).isNotNull();

        Report report = gridItems.getItems().stream()
                .filter(r -> r.getCode().equals(reportCode))
                .findFirst()
                .orElseThrow();

        dataGrid.select(report);

        JmixButton runBtn = findComponent(reportRunView, "runReport");
        assertThat(runBtn.isEnabled()).isTrue();
        runBtn.click();
    }

    @SuppressWarnings("unchecked")
    protected static <T> T findComponent(View<?> view, String componentId) {
        return (T) UiComponentUtils.getComponent(view, componentId);
    }

    protected LocalDate parseDate(String isoDateString) {
        return LocalDate.parse(isoDateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @SuppressWarnings("unchecked")
    protected  <T> T findParameterField(InputParametersDialog dialog, String fieldId) {
        Div div = findComponent(dialog, "inputParametersLayout");
        InputParametersFragment composite = (InputParametersFragment) div.getChildren().findFirst().orElseThrow();
        return (T) UiComponentUtils.findComponent(composite.getContent(), fieldId)
                .orElseThrow();
    }
}
