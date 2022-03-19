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

package io.jmix.reportsui.action.list;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.runner.ParametersDialogShowMode;
import io.jmix.reportsui.runner.UiReportRunContext;
import io.jmix.reportsui.runner.UiReportRunner;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.reportsui.screen.report.run.InputParametersFragment;
import io.jmix.reportsui.screen.report.run.ReportRun;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.*;
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
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Runs the reports associated with current screen or list component")
@ActionType(RunReportAction.ID)
public class RunReportAction extends ListAction {

    public static final String ID = "runReport";
    public static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";
    public static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";

    protected DataManager dataManager;
    protected ScreenBuilders screenBuilders;
    protected UiReportRunner uiReportRunner;
    protected ReportsClientProperties reportsClientProperties;

    public RunReportAction() {
        this(ID);
    }

    public RunReportAction(String id) {
        super(id);
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.PRINT);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(getClass(), "actions.RunReport");
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
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
    public void actionPerform(Component component) {
        if (target != null && target.getFrame() != null) {
            openLookup(target.getFrame().getFrameOwner());
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen);
        } else {
            throw new IllegalStateException("No target screen or component found for 'RunReportAction'");
        }
    }

    protected void openLookup(FrameOwner screen) {
        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        ReportRun reportRunScreen = screenBuilders.lookup(Report.class, screen)
                .withScreenClass(ReportRun.class)
                .withOpenMode(OpenMode.DIALOG)
                .withSelectHandler(reports -> runReports(reports, screen))
                .build();
        reportRunScreen.setScreen(hostScreen.getId());
        reportRunScreen.show();
    }

    protected void runReports(Collection<Report> reports, FrameOwner screen) {
        if (CollectionUtils.isNotEmpty(reports)) {
            Report report = reports.iterator().next();

            report = dataManager.load(Id.of(report))
                    .fetchPlan("report.edit")
                    .one();

            if (report.getInputParameters() != null
                    && report.getInputParameters().size() > 0
                    || inputParametersRequiredByTemplates(report)) {
                openReportParamsDialog(report, screen);
            } else {
                uiReportRunner.runAndShow(new UiReportRunContext(report)
                        .setInBackground(reportsClientProperties.getUseBackgroundReportProcessing())
                        .setOriginFrameOwner(screen)
                        .setParametersDialogShowMode(ParametersDialogShowMode.NO)
                        .setParams(Collections.emptyMap()));
            }
        }
    }

    protected void openReportParamsDialog(Report report, FrameOwner screen) {
        Map<String, Object> selectedItems = null;
        if (target != null) {
            Set items = target.getSelected();
            if (!items.isEmpty()) {
                selectedItems = ImmutableMap.of(
                        DEFAULT_LIST_OF_ENTITIES_ALIAS, items,
                        DEFAULT_SINGLE_ENTITY_ALIAS, items.stream().findFirst().get());
            }
        }

        InputParametersDialog inputParametersDialog = screenBuilders.screen(screen)
                .withScreenClass(InputParametersDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of()
                        .pair(InputParametersDialog.REPORT_PARAMETER, report)
                        .pair(InputParametersFragment.PARAMETERS_PARAMETER, selectedItems)
                        .create()
                ))
                .build();
        inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());
        inputParametersDialog.show();
    }
}
