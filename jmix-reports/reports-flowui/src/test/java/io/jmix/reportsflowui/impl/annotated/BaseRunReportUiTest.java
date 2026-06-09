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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.ReportExecutionPresentationIds;
import io.jmix.reportsflowui.runner.ReportPresentationRegistry;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportRunView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseRunReportUiTest extends BaseReportUiTest {

    @Autowired
    protected UiReportRunner uiReportRunner;
    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected ReportPresentationRegistry reportPresentationRegistry;

    protected void launchReportFromRunView(String reportCode) {
        launchPresentedReportFromRunView(reportCode, ReportExecutionPresentationIds.DEFAULT);
    }

    protected void launchSpreadsheetReportFromRunView(String reportCode) {
        launchPresentedReportFromRunView(reportCode, ReportExecutionPresentationIds.SPREADSHEET);
    }

    protected void launchTableReportFromRunView(String reportCode) {
        launchPresentedReportFromRunView(reportCode, ReportExecutionPresentationIds.TABLE);
    }

    protected void launchPresentedReportFromRunView(String reportCode, String presentationId) {
        viewNavigators.view(UiTestUtils.getCurrentView(), ReportRunView.class).navigate();
        Report report = reportRepository.loadForRunningByCode(reportCode);
        assertThat(report).isNotNull();

        uiReportRunner.runAndShow(reportPresentationRegistry.createRunContext(
                uiReportRunner.byReportEntity(report)
                        .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED)
                        .buildContext(),
                presentationId
        ));
    }

    @SuppressWarnings("unchecked")
    protected <T> T findInputParametersComponent(InputParametersDialog dialog, String fieldId) {
        return (T) findInputParametersComponentOptional(dialog, fieldId)
                .orElseThrow();
    }

    protected Optional<Component> findInputParametersComponentOptional(InputParametersDialog dialog, String fieldId) {
        Div div = findComponent(dialog, "inputParametersLayout");
        return findComponentRecursively(div, fieldId);
    }

    protected Optional<Component> findComponentRecursively(Component component, String fieldId) {
        if (fieldId.equals(component.getId().orElse(null))) {
            return Optional.of(component);
        }

        Optional<Component> nestedComponent = UiComponentUtils.findComponent(component, fieldId);
        if (nestedComponent.isPresent()) {
            return nestedComponent;
        }

        return component.getChildren()
                .map(child -> findComponentRecursively(child, fieldId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
