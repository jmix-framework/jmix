/*
 * Copyright (c) 2008-2019 Haulmont.
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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.screen.ReportGuiManager;
import io.jmix.reportsui.screen.report.run.InputParametersFragment;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.reportsui.screen.report.run.ReportRun;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    protected ReportGuiManager reportGuiManager;

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
    public void setReportGuiManager(ReportGuiManager reportGuiManager) {
        this.reportGuiManager = reportGuiManager;
    }

    @Override
    public void actionPerform(Component component) {
        if (target != null && target.getFrame() != null) {
            MetaClass metaClass = null;
            DataUnit items = target.getItems();
            if (items instanceof EntityDataUnit) {
                metaClass = ((EntityDataUnit) items).getEntityMetaClass();
            }

            openLookup(target.getFrame().getFrameOwner(), metaClass);
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen, null);
        } else {
            throw new IllegalStateException("No target screen or component found for 'RunReportAction'");
        }
    }

    protected void openLookup(FrameOwner screen, MetaClass metaClass) {
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
        reportRunScreen.setMetaClass(metaClass);
        reportRunScreen.setScreen(hostScreen.getId());
        reportRunScreen.show();
    }

    protected void runReports(Collection<Report> reports, FrameOwner screen) {
        if (reports != null && reports.size() > 0) {
            Report report = reports.iterator().next();

            report = dataManager.load(Id.of(report))
                    .fetchPlan("report.edit")
                    .one();

            if (report.getInputParameters() != null
                    && report.getInputParameters().size() > 0
                    || reportGuiManager.inputParametersRequiredByTemplates(report)) {
                openReportParamsDialog(report, screen);
            } else {
                reportGuiManager.printReport(report, Collections.emptyMap(), screen);
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

        screenBuilders.screen(screen)
                .withScreenClass(InputParametersDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        InputParametersDialog.REPORT_PARAMETER, report,
                        InputParametersFragment.PARAMETERS_PARAMETER, selectedItems)))
                .show();
    }
}
