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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.view.history.ReportExecutionDialog;
import io.jmix.reportsflowui.view.history.ReportExecutionListView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Standard action for displaying the report execution history.
 * <p>
 * Should be defined in the screen that is associated with {@link Report}. Should be defined for a {@code Button}
 * or a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@ActionType(ShowExecutionReportHistoryAction.ID)
public class ShowExecutionReportHistoryAction<E> extends ListDataComponentAction<ShowExecutionReportHistoryAction<E>, E> {

    public static final String ID = "report_showExecutionReportHistory";

    protected DialogWindows dialogWindows;
    protected Metadata metadata;

    public ShowExecutionReportHistoryAction() {
        this(ID);
    }

    public ShowExecutionReportHistoryAction(String id) {
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
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.CLOCK);
    }

    protected void openLookup(MetaClass metaClass) {
        View<?> parent = UiComponentUtils.getView(((Component) target));
        DialogWindow<ReportExecutionDialog> reportExecutionDialogDialogWindow = dialogWindows.lookup(parent, Report.class)
                .withViewClass(ReportExecutionDialog.class)
                .withSelectHandler(reports -> openExecutionBrowser(reports, parent))
                .build();

        ReportExecutionDialog reportExecutionDialog = reportExecutionDialogDialogWindow.getView();
        reportExecutionDialog.setMetaClassParameter(metaClass);
        reportExecutionDialog.setScreenParameter(parent.getId().orElseThrow(() -> new NullPointerException("Parent view is null!")));

        reportExecutionDialogDialogWindow.open();
    }

    protected void openExecutionBrowser(Collection<Report> reports, View<?> screen) {
        if (CollectionUtils.isNotEmpty(reports)) {
            DialogWindow<ReportExecutionListView> reportExecutionDialogWindow =
                    dialogWindows.view(screen, ReportExecutionListView.class)
                            .build();

            ReportExecutionListView reportExecutionListView = reportExecutionDialogWindow.getView();
            reportExecutionListView.setFilterByReports(new ArrayList<>(reports));
            reportExecutionDialogWindow.open();
        }
    }

    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        DataUnit items = target.getItems();
        MetaClass metaClass = ((EntityDataUnit) items).getEntityMetaClass();
        openLookup(metaClass);
    }
}
