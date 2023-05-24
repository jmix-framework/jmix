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

import io.jmix.reportsflowui.view.history.ReportExecutionDialog;
import io.jmix.reportsflowui.view.history.ReportExecutionListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Views;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
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
//todo
//@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Shows the report execution history")
@ActionType(ExecutionHistoryAction.ID)
public class ExecutionHistoryAction extends ListDataComponentAction {

    public static final String ID = "executionHistory";

    protected DialogWindows dialogWindows;

    public ExecutionHistoryAction() {
        this(ID);
    }

    public ExecutionHistoryAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage(getClass(), "actions.ExecutionHistory");
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    protected void initAction() {
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.CLOCK);
    }

    protected void openLookup(@Nullable MetaClass metaClass) {
        View<?> parent = findParent();
        DialogWindow<ReportExecutionDialog> reportExecutionDialogDialogWindow = dialogWindows.lookup(parent, Report.class)
                .withViewClass(ReportExecutionDialog.class)
                .withSelectHandler(reports -> openExecutionBrowser(reports, parent))
                .build();

        ReportExecutionDialog reportExecutionDialog = reportExecutionDialogDialogWindow.getView();
        reportExecutionDialog.setMetaClassParameter(metaClass);
        reportExecutionDialog.setScreenParameter(parent.getId().orElse(null));

        reportExecutionDialogDialogWindow.open();
    }

    protected void openExecutionBrowser(Collection<Report> reports, View screen) {
        if (CollectionUtils.isNotEmpty(reports)) {
            DialogWindow<ReportExecutionListView> reportExecutionDialogWindow = dialogWindows.view(screen, ReportExecutionListView.class)
                    .build();

            ReportExecutionListView reportExecutionListView = reportExecutionDialogWindow.getView();
            reportExecutionListView.setFilterByReports(new ArrayList<>(reports));
            reportExecutionDialogWindow.open();
        }
    }

    protected View<?> findParent() {
        View<?> view = UiComponentUtils.findView((Component) target);
        if (view == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    target.getClass().getSimpleName()));
        }

        return view;
    }

    @Override
    public void execute() {
        if (target != null) {
            MetaClass metaClass = null;
            DataUnit items = target.getItems();
            if (items instanceof EntityDataUnit) {
                metaClass = ((EntityDataUnit) items).getEntityMetaClass();
            }

            openLookup(metaClass);
        } else {
            throw new IllegalStateException("No target screen or component found for 'ExecutionHistoryAction'");
        }
    }
}
