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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.support.LlmDataSetGenerationSupport;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import io.jmix.reportsflowui.view.report.model.LlmQueryColumn;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import llm_designer.test_support.TestLlmDataQueryService;
import llm_designer.test_support.TestLlmReportUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    protected TestLlmReportUtil llmReportUtil;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected Messages messages;

    @Autowired
    protected LlmDataSetGenerationSupport generationSupport;
    @Autowired
    protected LlmDataQueryService queryService;

    @AfterEach
    public void tearDown() {
        llmReportUtil.cleanupDatabaseReports();
        // The service is one bean for the whole class, so a test that made it reject queries must not leave it
        // rejecting them for the next one.
        ((TestLlmDataQueryService) queryService).setProblems(List.of());
    }

    @Test
    public void testLlmPanelIsShownAndOtherPanelsAreHidden() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isVisible()).isTrue();
        assertThat(this.<VerticalLayout>findComponent(view, "llmDataSetTypeBox").isVisible()).isTrue();
        assertThat(this.<VerticalLayout>findComponent(view, "dataSetScriptBox").isVisible()).isFalse();
        assertThat(this.<VerticalLayout>findComponent(view, "jsonDataSetTypeVBox").isVisible()).isFalse();
        assertThat(this.<VerticalLayout>findComponent(view, "commonEntityGrid").isVisible()).isFalse();
    }

    @Test
    public void testPanelShowsTheDataSetProperties() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").getValue())
                .isEqualTo(TestLlmReportUtil.PROMPT);
        assertThat(this.<JmixCheckbox>findComponent(view, "llmRegenerateOnRunField").getValue()).isFalse();
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue())
                .contains("select o.number");
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testStoredQueryStartsLockedAndEditUnlocksItWithItsColumns() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();

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
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

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
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

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
    public void testGenerationResultIsDiscardedWhenThePromptHasChanged() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        String storedBeforeGeneration = dataSet.getLlmGeneratedQuery();
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("A newer prompt");
        task.done(generatedQuery("staleResult"));

        assertThat(dataSet.getLlmGeneratedQuery()).isEqualTo(storedBeforeGeneration);
        assertThat(this.<Badge>findComponent(view, "llmStaleQueryNotice").isVisible()).isTrue();
    }

    @Test
    public void testGenerationResultDoesNotOverwriteANewerManualDraft() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        String storedBeforeGeneration = dataSet.getLlmGeneratedQuery();
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");
        task.done(generatedQuery("staleResult"));

        assertThat(dataSet.getLlmGeneratedQuery()).isEqualTo(storedBeforeGeneration);
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue()).isEmpty();
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
    }

    @Test
    public void testOlderGenerationCannotOverwriteANewerOne() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> olderTask = generationTask(view, dataSet);
        BackgroundTask<Integer, LlmDataQuery> newerTask = generationTask(view, dataSet);

        newerTask.done(generatedQuery("newerResult"));
        olderTask.done(generatedQuery("olderResult"));

        assertThat(Objects.requireNonNull(generationSupport.readStoredQuery(dataSet)).getJpql())
                .contains("newerResult");
        // The author is not told about a result replaced by the very generation they are waiting for.
        assertThat(UiTestUtils.getOpenedNotifications()).isEmpty();
    }

    @Test
    public void testStaleNoticeGoesAwayWithTheQueryItTalksAbout() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("A newer prompt");
        Badge notice = findComponent(view, "llmStaleQueryNotice");
        assertThat(notice.isVisible()).isTrue();

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        assertThat(selectedDataSet(view).getLlmGeneratedQuery()).isNull();
        assertThat(notice.isVisible()).isFalse();
    }

    @Test
    public void testCancelledGenerationIsForgotten() {
        // The token is a boxed long in an identity map, so forgetting it cannot rely on value equality.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        task.canceled();

        assertThat(latestGenerations(view)).doesNotContainKey(dataSet);
    }

    @Test
    public void testRemovingADataSetForgetsWhatIsRememberedAboutIt() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor")
                .setValue("select o.number as orderNumber from sales_Order o");

        assertThat(draftRevisions(view)).containsKey(dataSet);

        dataSetsContainer(view).getMutableItems().remove(dataSet);

        assertThat(draftRevisions(view)).doesNotContainKey(dataSet);
    }

    @Test
    public void testRemovingABandForgetsWhatIsRememberedAboutItsDataSets() {
        // A band takes its data sets with it, and they are never removed from the data set container.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor")
                .setValue("select o.number as orderNumber from sales_Order o");

        assertThat(draftRevisions(view)).containsKey(dataSet);

        BandDefinition band = dataSet.getBandDefinition();
        bandsContainer(view).getMutableItems().remove(band);

        assertThat(draftRevisions(view)).doesNotContainKey(dataSet);
    }

    @Test
    public void testDiscardedGenerationTellsTheAuthorThatNothingChanged() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("A newer prompt");
        task.done(generatedQuery("staleResult"));

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).isEqualTo(messages.getMessage("io.jmix.reportsflowui.view.report",
                "bandsTab.dataSetTypeLayout.llmGenerationDiscarded"));
    }

    @Test
    public void testAQueryThatWouldNotRunIsReportedAsSoonAsItIsGenerated() {
        // Nothing checks a stored query before a run, so an author who is not told here finds out by running
        // the report — after the model has already been paid for the query.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);
        ((TestLlmDataQueryService) queryService).setProblems(List.of("Unknown entity: sales_Ordr"));

        task.done(generatedQuery("orderNumber"));

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).contains("Unknown entity: sales_Ordr");
    }

    @Test
    public void testAQueryEditedIntoSomethingThatWouldNotRunIsReportedToo() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        ((TestLlmDataQueryService) queryService).setProblems(List.of("Unknown entity: sales_Ordr"));

        JmixButton editButton = findComponent(view, "llmEditQueryBtn");
        editButton.click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor")
                .setValue("select o.number as orderNumber from sales_Ordr o");
        editButton.click();

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).contains("Unknown entity: sales_Ordr");
    }

    @Test
    public void testBlankPromptPreventsSaving() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo(TestLlmReportUtil.PROMPT);
    }

    @Test
    public void testFailedValidationDoesNotFinishTheLlmQueryDraft() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<JmixButton>findComponent(view, "llmAddColumnBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");
        view.getEditedEntity().setDefaultTemplate(null);
        String storedDraftBeforeSave = dataSet.getLlmGeneratedQuery();

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue()).isEmpty();
        assertThat(columnRows(view)).hasSize(3);
        assertThat(dataSet.getLlmGeneratedQuery()).isEqualTo(storedDraftBeforeSave);
    }

    @Test
    public void testDataSetWithoutAStoredQuerySavesAnyway() {
        View<?> view = openDesigner(llmReportUtil.createAndSaveReportWithoutStoredQuery());
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("Orders of the last quarter");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo("Orders of the last quarter");
    }

    @Test
    public void testReadOnlyViewKeepsTheLlmPanelReadOnlyAfterItIsRefreshed() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        view.setReadOnly(true);

        selectBand(view, "Root");
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isReadOnly()).isTrue();
        assertThat(this.<JmixCheckbox>findComponent(view, "llmRegenerateOnRunField").isReadOnly()).isTrue();
        assertThat(this.<TypedTextField<Integer>>findComponent(view, "llmMaxResultsField").isReadOnly()).isTrue();
        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isEnabled()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmEditQueryBtn").isEnabled()).isFalse();
    }

    @Test
    public void testExistingLlmDataSetIsShownReadOnlyWhenGenerationIsUnavailable() {
        TestLlmDataQueryService service = (TestLlmDataQueryService) queryService;
        service.setGenerationAvailable(false);
        try {
            ReportDetailView view = openReportDesignerOnLlmDataSet();
            DataSet dataSet = selectedDataSet(view);
            JmixSelect<DataSetType> typeField = findComponent(view, "singleDataSetTypeField");

            // The offered types are fixed for the lifetime of the view, so a type that is no longer offered has
            // no option to display. Opening such a report must still leave the stored data set untouched.
            assertThat(typeField.getListDataView().getItems()).doesNotContain(DataSetType.LLM);
            assertThat(dataSet.getType()).isEqualTo(DataSetType.LLM);
            assertThat(dataSet.getLlmGeneratedQuery()).isNotBlank();

            assertThat(this.<VerticalLayout>findComponent(view, "llmDataSetTypeBox").isVisible()).isTrue();
            assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isReadOnly()).isTrue();
        } finally {
            service.setGenerationAvailable(true);
        }
    }

    @Test
    public void testChangingTheViewToReadOnlyLocksAnEditedLlmQuery() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        view.setReadOnly(true);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isEnabled()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmEditQueryBtn").isEnabled()).isFalse();
    }

    @Test
    public void testGenerationStartedBeforeReadOnlyDoesNotMutateTheDataSet() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        String storedBeforeGeneration = dataSet.getLlmGeneratedQuery();
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        view.setReadOnly(true);
        view.setReadOnly(false);
        task.done(generatedQuery("staleResult"));

        assertThat(dataSet.getLlmGeneratedQuery()).isEqualTo(storedBeforeGeneration);
    }

    @Test
    public void testReadOnlyCycleKeepsTheOpenDraftUntilItIsSaved() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");

        view.setReadOnly(true);
        view.setReadOnly(false);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isTrue();

        this.<JmixButton>findComponent(view, "saveBtn").click();
        assertThat(llmReportUtil.loadStoredQuery()).isNull();
    }

    @Test
    public void testReadOnlyAddColumnButtonIsDisabledAndInert() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        view.setReadOnly(true);

        JmixButton addButton = findComponent(view, "llmAddColumnBtn");
        assertThat(addButton.isEnabled()).isFalse();

        view.onLlmAddColumnBtnClick(new ClickEvent<>(addButton));
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testReadOnlyRemoveColumnButtonIsDisabledAndInert() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        DataGrid<LlmQueryColumn> columnsGrid = findComponent(view, "llmGeneratedColumnsDataGrid");
        columnsGrid.select(columnRows(view).get(0));
        view.setReadOnly(true);

        JmixButton removeButton = findComponent(view, "llmRemoveColumnBtn");
        assertThat(removeButton.isEnabled()).isFalse();

        view.onLlmRemoveColumnBtnClick(new ClickEvent<>(removeButton));
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    public void testStoredQueryWithoutNamedColumnsPreventsSaving() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        dataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number from sales_Order o", List.of("", "  "), List.of(), null, List.of(), null)));

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredQuery()).isEqualTo(TestLlmReportUtil.STORED_QUERY);
    }

    @Test
    public void testAColumnLeftWithoutANameIsDroppedWhenAnotherDataSetIsSelected() {
        // Editing ends whichever way it ends, so a row nobody named never reaches the stored document.
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<JmixButton>findComponent(view, "llmAddColumnBtn").click();

        selectBand(view, "Root");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).doesNotContain("");
    }

    @Test
    public void testChangingTheTypeOfAnotherRowFinalizesTheSelectedLlmDraft() {
        ReportDetailView view = (ReportDetailView) openDesigner(
                llmReportUtil.createAndSaveReportWithLlmAndJpqlDataSets());
        DataSet llmDataSet = selectedDataSet(view);
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");

        DataSet anotherDataSet = dataSetsContainer(view).getItems().get(1);

        JmixComboBox<DataSetType> anotherTypeField = Objects.requireNonNull(
                ReflectionTestUtils.invokeMethod(view, "dataSetTypeColumnValueProvider", anotherDataSet));
        anotherTypeField.setValue(DataSetType.SQL);

        assertThat(llmDataSet.getLlmGeneratedQuery()).isNull();
        assertThat(dataSetsContainer(view).getItem()).isSameAs(anotherDataSet);
    }

    @Test
    public void testClearingTheQueryTextIsUndoneWhileEditingContinues() {
        // The editor sends its value on blur, so a cut-and-paste passes through an empty text.
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        CodeEditor queryEditor = findComponent(view, "llmGeneratedQueryCodeEditor");

        queryEditor.setValue("");
        queryEditor.setValue(EDITED_JPQL);

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(EDITED_JPQL);
        assertThat(stored.getExplanation()).isNotNull();
    }

    @Test
    public void testChangingTheTypeAwayFromLlmDropsTheStoredQuery() {
        View<?> view = openDesignerOnLlmDataSet();

        this.<JmixSelect<DataSetType>>findComponent(view, "singleDataSetTypeField").setValue(DataSetType.JPQL);

        this.<JmixButton>findComponent(view, "saveBtn").click();
        assertThat(llmReportUtil.loadStoredQuery()).isNull();
    }

    protected View<?> openDesignerOnLlmDataSet() {
        return openDesigner(llmReportUtil.createAndSaveReportWithLlmDataSet());
    }

    protected ReportDetailView openReportDesignerOnLlmDataSet() {
        return (ReportDetailView) openDesignerOnLlmDataSet();
    }

    protected View<?> openDesigner(Report report) {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), Report.class)
                .withViewClass(ReportDetailView.class)
                .editEntity(report)
                .navigate();
        View<?> view = UiTestUtils.getCurrentView();

        // The content of an unselected tab is disabled, so its buttons would refuse a click.
        this.<JmixTabSheet>findComponent(view, "mainTabSheet").setSelectedIndex(BANDS_TAB_INDEX);
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

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

    protected DataSet selectedDataSet(ReportDetailView view) {
        return dataSetsContainer(view).getItem();
    }

    @SuppressWarnings("unchecked")
    protected CollectionContainer<BandDefinition> bandsContainer(ReportDetailView view) {
        return Objects.requireNonNull((CollectionContainer<BandDefinition>)
                ReflectionTestUtils.getField(view, "bandsDc"));
    }

    @SuppressWarnings("unchecked")
    protected Map<DataSet, Long> latestGenerations(ReportDetailView view) {
        return Objects.requireNonNull((Map<DataSet, Long>)
                ReflectionTestUtils.getField(view, "latestLlmGeneration"));
    }

    @SuppressWarnings("unchecked")
    protected Map<DataSet, Long> draftRevisions(ReportDetailView view) {
        return Objects.requireNonNull((Map<DataSet, Long>)
                ReflectionTestUtils.getField(view, "llmQueryDraftRevisions"));
    }

    @SuppressWarnings("unchecked")
    protected CollectionPropertyContainer<DataSet> dataSetsContainer(ReportDetailView view) {
        return Objects.requireNonNull((CollectionPropertyContainer<DataSet>)
                ReflectionTestUtils.getField(view, "dataSetsDc"));
    }

    protected BackgroundTask<Integer, LlmDataQuery> generationTask(ReportDetailView view, DataSet dataSet) {
        LlmQueryGenerationRequest request = generationSupport.createGenerationRequest(dataSet);
        return Objects.requireNonNull(ReflectionTestUtils.invokeMethod(view, "createLlmGenerationTask", request, dataSet));
    }

    protected LlmDataQuery generatedQuery(String alias) {
        return new LlmDataQuery("select o.number as " + alias + " from sales_Order o", List.of(alias),
                List.of(), null, List.of(), null);
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
