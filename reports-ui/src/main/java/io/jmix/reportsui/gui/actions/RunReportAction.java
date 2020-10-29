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

package io.jmix.reportsui.gui.actions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.run.ReportRun;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

public class RunReportAction extends AbstractAction implements Action.HasBeforeActionPerformedHandler {

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected Messages messages;

    protected FrameOwner screen;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction()} instead
     * */
    @Deprecated
    public RunReportAction(FrameOwner screen) {
        this("runReport", screen);
    }

    /**
     * @deprecated Use {@link RunReportAction#RunReportAction(String)} instead
     * */
    @Deprecated
    public RunReportAction(String id, FrameOwner screen) {
        super(id);

        checkArgument(screen != null, "Can not create RunReportAction with null window");

        this.screen = screen;
        this.caption = messages.getMessage(getClass(), "actions.RunReport");
        this.icon = "icons/reports-print.png";
    }

    public RunReportAction() {
        this("runReport");
    }

    public RunReportAction(String id) {
        super(id);

        this.caption = messages.getMessage(getClass(), "actions.RunReport");
        this.icon = "icons/reports-print.png";
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null) {
            if (!beforeActionPerformedHandler.beforeActionPerformed())
                return;
        }
        if (screen != null) {
            openLookup(screen);
        } else if (component instanceof Component.BelongToFrame) {
            FrameOwner screen = ComponentsHelper.getWindowNN((Component.BelongToFrame) component).getFrameOwner();
            openLookup(screen);
        } else {
            throw new IllegalStateException("Please set window or specified component for 'RunReportAction' call");
        }
    }

    protected void openLookup(FrameOwner screen) {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        WindowManager wm = (WindowManager) screenContext.getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$Report.run");

        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        wm.openLookup(windowInfo, items -> {
            if (items != null && items.size() > 0) {
                Report report = (Report) items.iterator().next();

                if (screen instanceof LegacyFrame) {
                    DataSupplier dataSupplier = ((LegacyFrame) screen).getDsContext().getDataSupplier();
                    report = dataSupplier.reload(report, "report.edit");
                } else {
                    DataManager dataManager = AppBeans.get(DataManager.NAME);
                    report = dataManager.reload(report, "report.edit");
                }

                if (report.getInputParameters() != null && report.getInputParameters().size() > 0
                        || reportGuiManager.inputParametersRequiredByTemplates(report)) {
                    openReportParamsDialog(report, screen);
                } else {
                    reportGuiManager.printReport(report, Collections.emptyMap(), screen);
                }
            }
        }, OpenType.DIALOG, ParamsMap.of(ReportRun.SCREEN_PARAMETER, hostScreen.getId()));
    }

    protected void openReportParamsDialog(Report report, FrameOwner screen) {
        ScreenContext screenContext = UiControllerUtils.getScreenContext(screen);

        WindowManager wm = (WindowManager) screenContext.getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("report$inputParameters");

        wm.openWindow(windowInfo, OpenType.DIALOG, ParamsMap.of("report", report));
    }

    public void setScreen(FrameOwner screen) {
        this.screen = screen;
    }

    /**
     * @deprecated Use {@link #setScreen(FrameOwner)} instead.
     */
    @Deprecated
    public void setWindow(FrameOwner screen) {
        this.screen = screen;
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }
}