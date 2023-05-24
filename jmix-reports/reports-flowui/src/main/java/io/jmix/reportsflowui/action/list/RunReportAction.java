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

package io.jmix.reportsflowui.action.list;

import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.run.ReportRunView;
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
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.jmix.reports.util.ReportTemplateUtils.inputParametersRequiredByTemplates;

/**
 * Standard action for running the reports associated with current screen or list component.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
//@StudioAction(
//        target = "io.jmix.ui.component.ListComponent",
//        description = "Runs the reports associated with current screen or list component")
@ActionType(RunReportAction.ID)
public class RunReportAction extends ListDataComponentAction {

    public static final String ID = "runReport";
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
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PRINT);
    }

    @Override
    public void execute() {
        if (target != null) {
            openLookup(findParent());
            //todo
//        } else if (component instanceof Component.BelongToFrame) {
//            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
//            openLookup(screen);
        } else {
            throw new IllegalStateException("No target screen or component found for 'RunReportAction'");
        }
    }

    protected void openLookup(View<?> view) {
        DialogWindow<ReportRunView> reportRunDialogWindow = dialogWindows.lookup(view, Report.class)
                .withViewClass(ReportRunView.class)
                .withSelectHandler(reports -> runReports(reports, view))
                .build();

        ReportRunView reportRunView = reportRunDialogWindow.getView();
        reportRunView.setScreen(view.getId().orElse(null));
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
                        .setOriginFrameOwner(view)
                        .setParametersDialogShowMode(ParametersDialogShowMode.NO)
                        .setParams(Collections.emptyMap()));
            }
        }
    }

    protected void openReportParamsDialog(Report report, View<?> view) {
        Map<String, Object> selectedItems = null;
        if (target != null) {
            Set items = target.getSelectedItems();
            if (!items.isEmpty()) {
                selectedItems = ImmutableMap.of(
                        DEFAULT_LIST_OF_ENTITIES_ALIAS, items,
                        DEFAULT_SINGLE_ENTITY_ALIAS, items.stream().findFirst().get());
            }
        }

        DialogWindow<InputParametersDialog> inputParametersDialogWindow = dialogWindows.view(view, InputParametersDialog.class)
                .build();
        InputParametersDialog inputParametersDialog = inputParametersDialogWindow.getView();
        inputParametersDialog.setReport(report);
        inputParametersDialog.setParameters(selectedItems);
        inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());

        inputParametersDialogWindow.open();
    }

    protected View<?> findParent() {
        View<?> view = UiComponentUtils.findView((Component) target);
        if (view == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()));
        }

        return view;
    }

}
