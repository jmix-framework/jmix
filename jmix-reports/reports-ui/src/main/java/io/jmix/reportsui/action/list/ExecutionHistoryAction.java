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

import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.screen.report.history.ReportExecutionBrowser;
import io.jmix.reportsui.screen.report.history.ReportExecutionDialog;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Standard action for displaying the report execution history.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Shows the report execution history")
@ActionType(ExecutionHistoryAction.ID)
public class ExecutionHistoryAction extends ListAction {

    public static final String ID = "executionHistory";

    protected ScreenBuilders screenBuilders;

    public ExecutionHistoryAction() {
        this(ID);
    }

    public ExecutionHistoryAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.caption = messages.getMessage(getClass(), "actions.ExecutionHistory");
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
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
            throw new IllegalStateException("No target screen or component found for 'ExecutionHistoryAction'");
        }
    }

    protected void openLookup(FrameOwner screen, @Nullable MetaClass metaClass) {
        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }

        screenBuilders.lookup(Report.class, screen)
                .withScreenClass(ReportExecutionDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of()
                        .pair(ReportExecutionDialog.SCREEN_PARAMETER, hostScreen.getId())
                        .pair(ReportExecutionDialog.META_CLASS_PARAMETER, metaClass)
                        .create()))
                .withSelectHandler(reports -> openExecutionBrowser(reports, screen))
                .show();
    }

    protected void openExecutionBrowser(Collection<Report> reports, FrameOwner screen) {
        if (CollectionUtils.isNotEmpty(reports)) {
            screenBuilders.screen(screen)
                    .withScreenClass(ReportExecutionBrowser.class)
                    .withOptions(new MapScreenOptions(ParamsMap.of(
                            ReportExecutionBrowser.REPORTS_PARAMETER, new ArrayList<>(reports))))
                    .show();
        }
    }
}
