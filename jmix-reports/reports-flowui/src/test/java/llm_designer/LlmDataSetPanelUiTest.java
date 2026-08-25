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
import io.jmix.core.Metadata;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.textarea.JmixTextArea;
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
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reportsflowui.support.LlmDataSetGenerationSupport;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
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
    protected Metadata metadata;

    @Autowired
    protected LlmDataSetGenerationSupport generationSupport;
    @Autowired
    protected LlmDataQueryService queryService;

    @AfterEach
    void tearDown() {
        llmReportUtil.cleanupDatabaseReports();
        // The service is one bean for the whole class, so a test that made it reject queries or report generation
        // as unavailable must not leave it that way for the next one.
        ((TestLlmDataQueryService) queryService).setProblems(List.of());
        ((TestLlmDataQueryService) queryService).setGenerationAvailable(true);
    }

    @Test
    void testLlmPanelIsShownAndOtherPanelsAreHidden() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isVisible()).isTrue();
        assertThat(this.<VerticalLayout>findComponent(view, "llmDataSetTypeBox").isVisible()).isTrue();
        assertThat(this.<VerticalLayout>findComponent(view, "dataSetScriptBox").isVisible()).isFalse();
        assertThat(this.<VerticalLayout>findComponent(view, "jsonDataSetTypeVBox").isVisible()).isFalse();
        assertThat(this.<VerticalLayout>findComponent(view, "commonEntityGrid").isVisible()).isFalse();
    }

    @Test
    void testPanelShowsTheDataSetProperties() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").getValue())
                .isEqualTo(TestLlmReportUtil.PROMPT);
        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").getValue())
                .contains("select o.number");
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    void testStoredQueryStartsLockedAndEditUnlocksItWithItsColumns() {
        View<?> view = openDesignerOnLlmDataSet();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();

        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isTrue();
    }

    @Test
    void testEditedQueryTextIsStoredAndSurvivesASave() {
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
    void testEditIsInTheDataSetBeforeAnythingIsSavedOrConfirmed() {
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
    void testRenamedColumnIsStored() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        columnRows(view).get(1).setName("customer");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "customer");
    }

    @Test
    void testColumnsAreNotWrittenBackWhileLocked() {
        View<?> view = openDesignerOnLlmDataSet();

        columnRows(view).get(1).setName("customer");

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getResultProperties()).containsExactly("orderNumber", "customerName");
    }

    @Test
    void testColumnIsAddedAfterTheSelectedRow() {
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
    void testAColumnLeftWithoutANameIsDroppedWhenEditingEnds() {
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
    void testRemovedColumnDisappearsFromTheStoredQuery() {
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
    void testSelectingAnotherDataSetLocksTheQueryAgain() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        selectBand(view, "Root");
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();
    }

    @Test
    void testTheRefusalToSaveNamesTheMissingQueryRatherThanSomethingElse() {
        // A data set has more than one way to be unusable, and the author has to be told which one this is.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        dataSet.setLlmGeneratedQuery(null);
        ValidationErrors errors = new ValidationErrors();

        ReflectionTestUtils.invokeMethod(view, "validateLlmDataSet", errors, dataSet);

        assertThat(errors.getAll()).extracting(ValidationErrors.Item::getDescription)
                .containsExactly(messages.formatMessage("io.jmix.reportsflowui.view.report",
                        "validation.error.llmDataSetStoredQueryNull", TestLlmReportUtil.DATA_BAND_NAME));
    }

    @Test
    void testClearingTheQueryTextPreventsSaving() {
        // Saving would finish the edit and remove the stored query, leaving a data set a run cannot execute.
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();
        assertThat(llmReportUtil.loadStoredQuery()).isEqualTo(TestLlmReportUtil.STORED_QUERY);
    }

    @Test
    void testQueryWrittenByHandBecomesTheStoredQuery() {
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
    void testEditingThePromptMarksTheStoredQueryStale() {
        View<?> view = openDesignerOnLlmDataSet();
        Badge notice = findComponent(view, "llmStaleQueryNotice");

        assertThat(notice.isVisible()).isFalse();

        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("Something else entirely");

        assertThat(notice.isVisible()).isTrue();
    }

    @Test
    void testEditingThePromptOfAnotherDataSetLeavesTheNoticeAlone() {
        // The container reports a property change of every data set of the band, while the notice describes the
        // stored query of the one the panel shows.
        ReportDetailView view = (ReportDetailView) openDesigner(
                llmReportUtil.createAndSaveReportWithLlmAndJpqlDataSets());
        Badge notice = findComponent(view, "llmStaleQueryNotice");
        // The other data set is made one the notice could speak about, or it would say nothing either way.
        DataSet another = dataSetsContainer(view).getItems().get(1);
        another.setType(DataSetType.LLM);
        another.setLlmGeneratedQuery(TestLlmReportUtil.STORED_QUERY);
        assertThat(another).isNotSameAs(selectedDataSet(view));
        assertThat(notice.isVisible()).isFalse();

        another.setText("A newer prompt");

        assertThat(notice.isVisible()).isFalse();
    }

    @Test
    void testGenerationResultIsDiscardedWhenThePromptHasChanged() {
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
    void testGenerationResultDoesNotOverwriteANewerManualDraft() {
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
    void testOlderGenerationCannotOverwriteANewerOne() {
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
    void testStaleNoticeGoesAwayWithTheQueryItTalksAbout() {
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
    void testCancelledGenerationIsForgotten() {
        // The token is a boxed long in an identity map, so forgetting it cannot rely on value equality.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        task.canceled();

        assertThat(latestGenerations(view)).doesNotContainKey(dataSet);
    }

    @Test
    void testRemovingADataSetForgetsWhatIsRememberedAboutIt() {
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
    void testRemovingABandForgetsWhatIsRememberedAboutItsDataSets() {
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
    void testDiscardedGenerationTellsTheAuthorThatNothingChanged() {
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
    void testUnguardedOptionalParameterIsReportedOnSave() {
        // The route no other check catches: the query is untouched and the report changes around it — a
        // parameter becomes optional, and the comparison written for a required one now matches nothing when the
        // parameter is left empty. Save is where every stored query of the report is read back.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);

        ReportInputParameter parameter = metadata.create(ReportInputParameter.class);
        parameter.setReport(view.getEditedEntity());
        parameter.setName("City");
        parameter.setAlias("city");
        parameter.setType(ParameterType.TEXT);
        parameter.setRequired(false);
        parameter.setPosition(0);
        view.getEditedEntity().setInputParameters(new ArrayList<>(List.of(parameter)));

        dataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.city = :city",
                List.of("orderNumber"), List.of(new LlmQueryParameter("city", "java.lang.String")),
                null, List.of())));

        this.<JmixButton>findComponent(view, "saveBtn").click();

        // Among all of them: the save itself reports success afterwards, and the warning is the one that stays
        // on screen until it is dismissed.
        assertThat(UiTestUtils.getOpenedNotifications())
                .extracting(NotificationInfo::getText)
                .contains(messages.formatMessage("io.jmix.reportsflowui.view.report",
                        "bandsTab.dataSetTypeLayout.llmUnguardedOptionalParametersInReport",
                        dataSet.getName() + " (city)"));
    }

    @Test
    void testAQueryThatWouldNotRunIsReportedAsSoonAsItIsGenerated() {
        // Nothing checks a stored query before a run, so an author who is not told here finds out by running
        // the report — after the model has already been paid for the query.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);
        ((TestLlmDataQueryService) queryService).setProblems(List.of("Unknown entity: sales_Ordr"));

        task.done(generatedQuery("orderNumber"));

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).isEqualTo(messages.formatMessage("io.jmix.reportsflowui.view.report",
                "bandsTab.dataSetTypeLayout.llmGeneratedQueryProblems", "Unknown entity: sales_Ordr"));
    }

    @Test
    void testFailedGenerationSaysWhatWentWrong() {
        // Nothing in the platform reports an unhandled failure of a background task, so without this the
        // author is left with a closed dialog and no query.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        boolean handled = task.handleException(new LlmDataQueryException(
                "Cannot generate a query for the data set prompt",
                new IllegalStateException("LLM returned an empty response")));

        assertThat(handled).isTrue();
        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getType()).isEqualTo(Notifications.Type.ERROR);
        assertThat(notification.getTitle()).isEqualTo(messages.getMessage("io.jmix.reportsflowui.view.report",
                "bandsTab.dataSetTypeLayout.llmGenerationFailed"));
        // The innermost cause is what says something: the layers above it repeat the notice itself.
        assertThat(notification.getMessage()).isEqualTo("LLM returned an empty response");
    }

    @Test
    void testFailureTooLongToShowIsCut() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        task.handleException(new IllegalStateException(
                "Cannot parse LLM response as JSON: " + "x".repeat(1000)));

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getMessage()).hasSizeLessThan(500).endsWith("...");
    }

    @Test
    void testGenerationThatRanOutOfTimeIsSaidToo() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        BackgroundTask<Integer, LlmDataQuery> task = generationTask(view, dataSet);

        boolean handled = task.handleTimeoutException();

        assertThat(handled).isTrue();
        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).isEqualTo(messages.getMessage("io.jmix.reportsflowui.view.report",
                "bandsTab.dataSetTypeLayout.llmGenerationTimedOut"));
    }

    @Test
    void testAQueryEditedIntoSomethingThatWouldNotRunIsReportedToo() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        ((TestLlmDataQueryService) queryService).setProblems(List.of("Unknown entity: sales_Ordr"));

        JmixButton editButton = findComponent(view, "llmEditQueryBtn");
        editButton.click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor")
                .setValue("select o.number as orderNumber from sales_Ordr o");
        editButton.click();

        NotificationInfo notification = UiTestUtils.getLastOpenedNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getText()).isEqualTo(messages.formatMessage("io.jmix.reportsflowui.view.report",
                "bandsTab.dataSetTypeLayout.llmQueryProblems", "Unknown entity: sales_Ordr"));
    }

    @Test
    void testBlankPromptPreventsSaving() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo(TestLlmReportUtil.PROMPT);
    }

    @Test
    void testFailedValidationDoesNotFinishTheLlmQueryDraft() {
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
    void testDataSetWithoutAStoredQueryPreventsSaving() {
        // A run executes the stored query and generates nothing, so a data set without one could never run —
        // the same reason the designer requires the script of a JPQL data set.
        View<?> view = openDesigner(llmReportUtil.createAndSaveReportWithoutStoredQuery());
        this.<JmixTextArea>findComponent(view, "llmPromptField").setValue("Orders of the last quarter");

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredPrompt()).isEqualTo(TestLlmReportUtil.PROMPT);
    }

    @Test
    void testReadOnlyViewKeepsTheLlmPanelReadOnlyAfterItIsRefreshed() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        view.setReadOnly(true);

        selectBand(view, "Root");
        selectBand(view, TestLlmReportUtil.DATA_BAND_NAME);

        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isReadOnly()).isTrue();
        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isEnabled()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmEditQueryBtn").isEnabled()).isFalse();
    }

    @Test
    void testChangingTheViewToReadOnlyLocksAnEditedLlmQuery() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();

        view.setReadOnly(true);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isTrue();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isEnabled()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmEditQueryBtn").isEnabled()).isFalse();
    }

    @Test
    void testGenerationStartedBeforeReadOnlyDoesNotMutateTheDataSet() {
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
    void testReadOnlyCycleKeepsTheOpenDraftUntilItIsSaved() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").setValue(EDITED_JPQL);

        view.setReadOnly(true);
        view.setReadOnly(false);

        assertThat(this.<CodeEditor>findComponent(view, "llmGeneratedQueryCodeEditor").isReadOnly()).isFalse();
        assertThat(this.<HorizontalLayout>findComponent(view, "llmColumnsButtonsLayout").isVisible()).isTrue();

        this.<JmixButton>findComponent(view, "saveBtn").click();
        LlmDataQuery stored = serializer.fromJson(llmReportUtil.loadStoredQuery());
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(EDITED_JPQL);
    }

    @Test
    void testReadOnlyAddColumnButtonIsDisabledAndInert() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        view.setReadOnly(true);

        JmixButton addButton = findComponent(view, "llmAddColumnBtn");
        assertThat(addButton.isEnabled()).isFalse();

        view.onLlmAddColumnBtnClick(new ClickEvent<>(addButton));
        assertThat(columnNames(view)).containsExactly("orderNumber", "customerName");
    }

    @Test
    void testReadOnlyRemoveColumnButtonIsDisabledAndInert() {
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
    void testStoredQueryWithoutNamedColumnsPreventsSaving() {
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        dataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number from sales_Order o", List.of("", "  "), List.of(), null, List.of())));

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredQuery()).isEqualTo(TestLlmReportUtil.STORED_QUERY);
    }

    @Test
    void testStoredQueryNamingTheSameColumnTwicePreventsSaving() {
        // A row of a band is keyed by these names, so a duplicate loses one of the selected values; a run
        // refuses such a query, and the designer must not hand one out.
        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);
        dataSet.setLlmGeneratedQuery(serializer.toJson(new LlmDataQuery(
                "select o.number as name, o.customer as name from sales_Order o",
                List.of("name", "name"), List.of(), null, List.of())));

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredQuery()).isEqualTo(TestLlmReportUtil.STORED_QUERY);
    }

    @Test
    void testEditedColumnsNamedTheSameTwicePreventSaving() {
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        List<LlmQueryColumn> columns = columnRows(view);
        columns.get(1).setName(columns.get(0).getName());

        this.<JmixButton>findComponent(view, "saveBtn").click();

        assertThat(llmReportUtil.loadStoredQuery()).isEqualTo(TestLlmReportUtil.STORED_QUERY);
    }

    @Test
    void testAColumnLeftWithoutANameIsDroppedWhenAnotherDataSetIsSelected() {
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
    void testChangingTheTypeOfAnotherRowFinalizesTheSelectedLlmDraft() {
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
    void testClearingTheQueryTextIsUndoneWhileEditingContinues() {
        // The editor sends its value on blur, so a cut-and-paste passes through an empty text.
        View<?> view = openDesignerOnLlmDataSet();
        this.<JmixButton>findComponent(view, "llmEditQueryBtn").click();
        CodeEditor queryEditor = findComponent(view, "llmGeneratedQueryCodeEditor");

        queryEditor.setValue("");
        queryEditor.setValue(EDITED_JPQL);

        LlmDataQuery stored = saveAndLoadStoredQuery(view);
        assertThat(stored).isNotNull();
        assertThat(stored.getJpql()).isEqualTo(EDITED_JPQL);
        // Carried over unchanged: had the blank text been taken for a cleared query, the explanation would have
        // gone with it, which "not null" would not have noticed.
        assertThat(stored.getExplanation()).isEqualTo("All order numbers");
    }

    @Test
    void testChangingTheTypeAwayFromLlmDropsTheStoredQuery() {
        View<?> view = openDesignerOnLlmDataSet();

        this.<JmixSelect<DataSetType>>findComponent(view, "singleDataSetTypeField").setValue(DataSetType.JPQL);

        this.<JmixButton>findComponent(view, "saveBtn").click();
        assertThat(llmReportUtil.loadStoredQuery()).isNull();
    }

    @Test
    void testExistingDataSetKeepsItsTypeAndItsEditorWhenGenerationIsUnavailable() {
        // An application whose model is not configured still has the add-on: such a report is opened, its stored
        // query edited by hand and the report run. Only generating a new query is out of reach.
        ((TestLlmDataQueryService) queryService).setGenerationAvailable(false);

        ReportDetailView view = openReportDesignerOnLlmDataSet();
        DataSet dataSet = selectedDataSet(view);

        JmixSelect<DataSetType> typeField = findComponent(view, "singleDataSetTypeField");
        assertThat(typeField.getListDataView().getItems().toList()).contains(DataSetType.LLM);
        assertThat(typeField.getValue()).isEqualTo(DataSetType.LLM);
        assertThat(dataSet.getType()).isEqualTo(DataSetType.LLM);
        assertThat(dataSet.getLlmGeneratedQuery()).isNotBlank();

        assertThat(this.<VerticalLayout>findComponent(view, "llmDataSetTypeBox").isVisible()).isTrue();
        assertThat(this.<JmixButton>findComponent(view, "llmGenerateBtn").isEnabled()).isFalse();
        assertThat(this.<JmixButton>findComponent(view, "llmEditQueryBtn").isEnabled()).isTrue();
        assertThat(this.<JmixTextArea>findComponent(view, "llmPromptField").isReadOnly()).isFalse();
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
        return Objects.requireNonNull(
                ReflectionTestUtils.invokeMethod(view, "createLlmGenerationTask", request, dataSet));
    }

    protected LlmDataQuery generatedQuery(String alias) {
        return new LlmDataQuery("select o.number as " + alias + " from sales_Order o", List.of(alias),
                List.of(), null, List.of());
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
