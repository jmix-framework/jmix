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

package llm_designer;

import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import llm_designer.test_support.LlmReportUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selecting the LLM data set type shows its own panel and nothing else, and the panel edits the data set.
 */
@UiTest
@SpringBootTest(classes = {LlmDesignerTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public class LlmDataSetPanelUiTest {

    @Autowired
    protected ViewNavigators viewNavigators;

    @Autowired
    protected LlmReportUtil llmReportUtil;

    @AfterEach
    public void tearDown() {
        llmReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testLlmPanelIsShownAndOtherPanelsAreHidden() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<CodeEditor>findComponent(view, "llmPromptCodeEditor").isVisible()).isTrue();
        assertThat(this.<com.vaadin.flow.component.orderedlayout.VerticalLayout>findComponent(
                view, "llmDataSetTypeBox").isVisible()).isTrue();
        assertThat(this.<com.vaadin.flow.component.orderedlayout.VerticalLayout>findComponent(
                view, "dataSetScriptBox").isVisible()).isFalse();
        assertThat(this.<com.vaadin.flow.component.orderedlayout.VerticalLayout>findComponent(
                view, "jsonDataSetTypeVBox").isVisible()).isFalse();
        assertThat(this.<com.vaadin.flow.component.orderedlayout.VerticalLayout>findComponent(
                view, "commonEntityGrid").isVisible()).isFalse();
    }

    @Test
    public void testPanelShowsTheDataSetProperties() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<CodeEditor>findComponent(view, "llmPromptCodeEditor").getValue())
                .isEqualTo(LlmReportUtil.PROMPT);
        assertThat(this.<JmixCheckbox>findComponent(view, "llmRegenerateOnRunField").getValue()).isFalse();
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue())
                .contains("select o.number");
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<Span>findComponent(view, "llmGeneratedColumnsSpan").getText()).contains("orderNumber");
    }

    @Test
    public void testEditingThePromptMarksTheStoredQueryStale() {
        View<?> view = openDesignerOnLlmDataSet();
        Span notice = findComponent(view, "llmStaleQueryNotice");

        assertThat(notice.isVisible()).isFalse();

        this.<CodeEditor>findComponent(view, "llmPromptCodeEditor").setValue("Something else entirely");

        assertThat(notice.isVisible()).isTrue();
    }

    @Test
    public void testBlankPromptPreventsSaving() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<CodeEditor>findComponent(view, "llmPromptCodeEditor").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo(LlmReportUtil.PROMPT);
    }

    @Test
    public void testDataSetWithoutAStoredQuerySavesAnyway() {
        View<?> view = openDesigner(llmReportUtil.createAndSaveReportWithoutStoredQuery());
        this.<CodeEditor>findComponent(view, "llmPromptCodeEditor").setValue("Orders of the last quarter");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo("Orders of the last quarter");
    }

    @Test
    public void testGenerateButtonIsAvailable() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isVisible()).isTrue();
    }

    protected View<?> openDesignerOnLlmDataSet() {
        return openDesigner(llmReportUtil.createAndSaveReportWithLlmDataSet());
    }

    protected View<?> openDesigner(Report report) {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), Report.class)
                .withViewClass(ReportDetailView.class)
                .editEntity(report)
                .navigate();
        View<?> view = UiTestUtils.getCurrentView();

        TreeDataGrid<BandDefinition> bandsGrid = findComponent(view, "bandsTreeDataGrid");
        BandDefinition dataBand = Objects.requireNonNull(bandsGrid.getItems()).getItems().stream()
                .filter(band -> LlmReportUtil.DATA_BAND_NAME.equals(band.getName()))
                .findFirst()
                .orElseThrow();
        bandsGrid.select(dataBand);

        return view;
    }

    @SuppressWarnings("unchecked")
    protected <T> T findComponent(View<?> view, String componentId) {
        return (T) UiComponentUtils.getComponent(view, componentId);
    }
}
