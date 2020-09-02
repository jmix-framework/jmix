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

package io.jmix.reports.gui.actions.list;

import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import io.jmix.ui.action.ListAction;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.Report;
import io.jmix.reports.gui.ReportGuiManager;
import io.jmix.reports.gui.report.run.InputParametersFrame;
import io.jmix.reports.gui.report.run.InputParametersWindow;
import io.jmix.reports.gui.report.run.ReportRun;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
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
@StudioAction(category = "Reports list actions", description = "Runs the reports associated with current screen or list component")
@ActionType(RunReportAction.ID)
public class RunReportAction extends ListAction implements Action.HasBeforeActionPerformedHandler {

    public static final String ID = "runReport";
    public static final String DEFAULT_SINGLE_ENTITY_ALIAS = "entity";
    public static final String DEFAULT_LIST_OF_ENTITIES_ALIAS = "entities";

    protected BeanLocator beanLocator;
    protected ScreenBuilders screenBuilders;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

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
        this.caption = messages.getMessage(RunReportAction.class, "actions.RunReport");
    }

    @Autowired
    public void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null
                && !beforeActionPerformedHandler.beforeActionPerformed()) {
            return;
        }
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

        screenBuilders.lookup(Report.class, screen)
                .withScreenId("report$Report.run")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        ReportRun.SCREEN_PARAMETER, hostScreen.getId(),
                        ReportRun.META_CLASS_PARAMETER, metaClass)))
                .withSelectHandler(reports -> runReports(reports, screen))
                .show();
    }

    protected void runReports(Collection<Report> reports, FrameOwner screen) {
        if (reports != null && reports.size() > 0) {
            Report report = reports.iterator().next();

            DataManager dataManager = beanLocator.get(DataManager.NAME);
            report = dataManager.reload(report, "report.edit");

            ReportGuiManager reportGuiManager = beanLocator.get(ReportGuiManager.class);
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
                .withScreenId("report$inputParameters")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        InputParametersWindow.REPORT_PARAMETER, report,
                        InputParametersFrame.PARAMETERS_PARAMETER, selectedItems)))
                .show();
    }
}
