/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsflowui.action;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportRunView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;

/**
 * Standard action for running the reports associated with current screen or list component.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@ActionType(RunReportAction.ID)
public class RunReportAction<E> extends ListDataComponentAction<RunReportAction<E>, E> {

    public static final String ID = "report_runReport";

    public static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";
    public static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";

    protected DataManager dataManager;
    protected DialogWindows dialogWindows;
    protected UiReportRunner uiReportRunner;
    protected ReportsClientProperties reportsClientProperties;

    public RunReportAction() {
        this(ID);
    }

    public RunReportAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage(getClass(), "actions.RunReport");
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Autowired
    public void setUiReportRunner(UiReportRunner uiReportRunner) {
        this.uiReportRunner = uiReportRunner;
    }

    @Autowired
    public void setReportsClientProperties(ReportsClientProperties reportsClientProperties) {
        this.reportsClientProperties = reportsClientProperties;
    }

    @Override
    protected void initAction() {
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.PRINT);
    }

    @Override
    public void execute() {
        checkTarget();

        openLookup(UiComponentUtils.getView(((Component) target)));
    }

    protected void openLookup(View<?> parent) {
        DialogWindow<ReportRunView> reportRunDialogWindow = dialogWindows.lookup(parent, Report.class)
                .withViewClass(ReportRunView.class)
                .withSelectHandler(reports -> runReports(reports, parent))
                .build();

        ReportRunView reportRunView = reportRunDialogWindow.getView();
        reportRunView.setScreen(parent.getId().orElseThrow(() -> new NullPointerException("Parent view is null!")));
        reportRunDialogWindow.open();
    }

    protected void runReports(Collection<Report> reports, View<?> view) {
        if (CollectionUtils.isNotEmpty(reports)) {
            Report report = reports.iterator().next();

            report = dataManager.load(Id.of(report))
                    .fetchPlan("report.edit")
                    .one();

            if (report.getInputParameters() != null
                    && report.getInputParameters().size() > 0
                    || inputParametersRequiredByTemplates(report)) {
                openReportParamsDialog(report, view);
            } else {
                uiReportRunner.runAndShow(new UiReportRunContext(report)
                        .setInBackground(reportsClientProperties.getUseBackgroundReportProcessing())
                        .setOwner(view)
                        .setParametersDialogShowMode(ParametersDialogShowMode.NO)
                        .setParams(Collections.emptyMap()));
            }
        }
    }

    protected void openReportParamsDialog(Report report, View<?> view) {
        Map<String, Object> selectedItems = new HashMap<>();
        Set<E> items = target.getSelectedItems();
        if (!items.isEmpty()) {
            selectedItems = ImmutableMap.of(
                    DEFAULT_LIST_OF_ENTITIES_ALIAS, items,
                    DEFAULT_SINGLE_ENTITY_ALIAS, items.stream().findFirst().get());
        }

        DialogWindow<InputParametersDialog> inputParametersDialogWindow = dialogWindows.view(view, InputParametersDialog.class)
                .build();
        InputParametersDialog inputParametersDialog = inputParametersDialogWindow.getView();
        inputParametersDialog.setReport(report);
        inputParametersDialog.setParameters(selectedItems);
        inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());

        inputParametersDialogWindow.open();
    }
}
