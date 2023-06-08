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
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.view.DetailCloseAction;
import io.jmix.flowui.action.view.OperationResultViewAction;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ActionType(RunSingleEntityReportAction.ID)
public class RunSingleEntityReportAction<E> extends OperationResultViewAction<DetailCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "reports_runSingleEntityReport";

    protected String reportOutputName;
    protected Metadata metadata;
    protected Messages messages;
    protected Notifications notifications;
    protected ReportsActionHelper reportsActionHelper;

    public RunSingleEntityReportAction() {
        this(ID);
    }

    public RunSingleEntityReportAction(String id) {
        super(id);

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PRINT);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage(getClass(), "actions.Report");
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setPrintReport(ReportsActionHelper reportsActionHelper) {
        this.reportsActionHelper = reportsActionHelper;
    }

    public void setReportOutputName(@Nullable String reportOutputName) {
        this.reportOutputName = reportOutputName;
    }

    @Override
    public void actionPerform(Component component) {
        Object entity = target.getEditedEntity();
        if (entity != null) {
            MetaClass metaClass = metadata.getClass(entity);
            reportsActionHelper.openRunReportScreen(target, entity, metaClass, reportOutputName);
        } else {
            notifications.create(messages.getMessage(getClass(), "notifications.noSelectedEntity"))
                    .show();
        }
    }
}