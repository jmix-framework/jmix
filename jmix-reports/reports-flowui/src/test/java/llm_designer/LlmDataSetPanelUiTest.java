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

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.Report;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import io.jmix.reportsflowui.view.report.model.LlmQueryColumn;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import llm_designer.test_support.LlmReportUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selecting the LLM data set type shows its own panel and nothing else, and the panel edits the data set.
 */
@UiTest
@SpringBootTest(classes = {LlmDesignerTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public class LlmDataSetPanelUiTest {

    protected static final int BANDS_TAB_INDEX = 1;
    protected static final String EDITED_JPQL =
            "select o.number as orderNumber from sales_Order o where o.date >= :dateFrom";

    @Autowired
    protected ViewNavigators viewNavigators;

    @Autowired
    protected LlmReportUtil llmReportUtil;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @AfterEach
    public void tearDown() {
        llmReportUtil.cleanupDatabaseReports();
    }

    @Test
    public void testLlmPanelIsShownAndOtherPanelsAreHidden() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isVisible()).isTrue();
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

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").getValue())
                .isEqualTo(LlmReportUtil.PROMPT);
        assertThat(this.<JmixCheckbox>findComponent(view, "llmRegenerateOnRunField").getValue()).isFalse();
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue())
                .contains("select o.number");
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testStoredQueryStartsLocked() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();
    }

    @Test
    public void testEditUnlocksTheQueryAndItsColumns() {
        View<?> view = openDesignerOnLlmDataSet();

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isTrue();
    }

    @Test
    public void testEditedQueryTextIsStoredAndSurvivesASave() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue(EDITED_JPQL);

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(EDITED_JPQL);
        assertThat(stored.getParameters()).extracting(LlmQueryParameter::getName).containsExactly("dateFrom");
        assertThat(stored.getExplanation()).isEqualTo("All order numbers");
    }

    @Test
    public void testEditIsInTheDataSetBeforeAnythingIsSavedOrConfirmed() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue(EDITED_JPQL);

        // Leaves the data set without saving the report and without pressing Done: coming back refills the
        // panel from the data set, so the edited query is there only if it was written through at once.
        selectBand(view, "Root");
        selectBand(view, LlmReportUtil.DATA_BAND_NAME);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue())
                .isEqualTo(EDITED_JPQL);
    }

    @Test
    public void testRenamedColumnIsStored() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        columnRows(view).get(1).setName("customer");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "customer");
    }

    @Test
    public void testColumnsAreNotWrittenBackWhileLocked() {
        View<?> view = openDesignerOnLlmDataSet();

        columnRows(view).get(1).setName("customer");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testColumnIsAddedAfterTheSelectedRow() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<DataGrid<LlmQueryColumn>>findComponent(view, "llmGeneratedColumnsDataGrid")
                .select(columnRows(view).get(0));

        this.<JmixButton>findComponent(view, "llmAddColumnBtn").click();
        columnRows(view).get(1).setName("amount");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "amount", "customerName");
    }

    @Test
    public void testAColumnLeftWithoutANameIsDroppedWhenEditingEnds() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        // Added and never filled in.
        this.<JmixButton>findComponent(view, "llmAddColumnBtn").click();
        assertThat(columnNames(view)).hasSize(3);

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testRemovedColumnDisappearsFromTheStoredQuery() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<DataGrid<LlmQueryColumn>>findComponent(view, "llmGeneratedColumnsDataGrid")
                .select(columnRows(view).get(1));

        this.<JmixButton>findComponent(view, "llmRemoveColumnBtn").click();

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber");
    }

    @Test
    public void testSelectingAnotherDataSetLocksTheQueryAgain() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        selectBand(view, "Root");
        selectBand(view, LlmReportUtil.DATA_BAND_NAME);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();
    }

    @Test
    public void testClearingTheQueryTextLeavesTheDataSetWithoutAStoredQuery() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();
        assertThat(llmReportUtil.loadStoredQuery()).isNull();
    }

    @Test
    public void testQueryWrittenByHandBecomesTheStoredQuery() {
        View<?> view = openDesigner(llmReportUtil.createAndSaveReportWithoutStoredQuery());
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor")
                .setValue("select o.number as orderNumber from sales_Order o");
        this.<JmixButton>findComponent(view, "llmAddColumnBtn").click();
        columnRows(view).get(0).setName("orderNumber");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo("select o.number as orderNumber from sales_Order o");
        assertThat(stored.getResultProperties()).containsExactly("orderNumber");
    }

    @Test
    public void testEditingThePromptMarksTheStoredQueryStale() {
        View<?> view = openDesignerOnLlmDataSet();
        Badge notice = findComponent(view, "llmStaleQueryNotice");

        assertThat(notice.isVisible()).isFalse();

        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("Something else entirely");

        assertThat(notice.isVisible()).isTrue();
    }

    @Test
    public void testBlankPromptPreventsSaving() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo(LlmReportUtil.PROMPT);
    }

    @Test
    public void testDataSetWithoutAStoredQuerySavesAnyway() {
        View<?> view = openDesigner(llmReportUtil.createAndSaveReportWithoutStoredQuery());
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("Orders of the last quarter");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo("Orders of the last quarter");
    }

    @Test
    public void testTheStoredQueryHasAHelpButton() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixButton>findComponent(view, "llmGeneratedQueryHelpBtn").isVisible()).isTrue();
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

        // The content of an unselected tab is disabled, so its buttons would refuse a click.
        this.<JmixTabSheet>findComponent(view, "mainTabSheet").setSelectedIndex(BANDS_TAB_INDEX);
        selectBand(view, LlmReportUtil.DATA_BAND_NAME);

        return view;
    }

    protected void selectBand(View<?> view, String bandName) {
        TreeDataGrid<BandDefinition> bandsGrid = findComponent(view, "bandsTreeDataGrid");
        BandDefinition band = Objects.requireNonNull(bandsGrid.getItems()).getItems().stream()
                .filter(item -> bandName.equals(item.getName()))
                .findFirst()
                .orElseThrow();
        bandsGrid.select(band);
    }

    protected List<LlmQueryColumn> columnRows(View<?> view) {
        DataGrid<LlmQueryColumn> columnsGrid = findComponent(view, "llmGeneratedColumnsDataGrid");
        return new ArrayList<>(Objects.requireNonNull(columnsGrid.getItems()).getItems());
    }

    protected List<String> columnNames(View<?> view) {
        return columnRows(view).stream()
                .map(LlmQueryColumn::getName)
                .toList();
    }

    /**
     * Saves the report and reads the stored query back from the database: an edit is written into the data set
     * as it is made, so a save must carry it away.
     */
    @Nullable
    protected LlmDataQuery saveAndLoadStoredQuery(View<?> view) {
        this.<JmixButton>findComponent(view, "saveBtn").click();
        return serializer.fromJson(llmReportUtil.loadStoredQuery());
    }

    @SuppressWarnings("unchecked")
    protected <T> T findComponent(View<?> view, String componentId) {
        return (T) UiComponentUtils.getComponent(view, componentId);
    }
}
