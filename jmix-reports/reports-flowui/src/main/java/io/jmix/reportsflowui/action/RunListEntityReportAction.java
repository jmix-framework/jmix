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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.model.*;
import io.jmix.reports.app.ParameterPrototype;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard action for printing reports for a list of entity instances associated with a list component.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action only selects reports having an external parameter of the {@code Entity} or the List of entities type
 * and where the parameter entity type matches the entity type displayed by the list component.
 * If only one report is available as a result of selection, it is invoked immediately.
 * If several reports are available, their list is offered to the user for selection.
 */
@ActionType(RunListEntityReportAction.ID)
public class RunListEntityReportAction<E> extends ListDataComponentAction<RunListEntityReportAction<E>, E>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "report_runListEntityReport";

    protected Dialogs dialogs;
    protected Messages messages;
    protected Metadata metadata;
    protected Notifications notifications;
    protected ReportActionSupport reportActionSupport;

    protected String reportOutputName;

    public RunListEntityReportAction() {
        this(ID);
    }

    public RunListEntityReportAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        this.text = messages.getMessage(RunListEntityReportAction.class, "actions.ListPrintForm");
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setPrintReport(ReportActionSupport reportActionSupport) {
        this.reportActionSupport = reportActionSupport;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setReportOutputName(@Nullable String reportOutputName) {
        this.reportOutputName = reportOutputName;
    }

    @Override
    protected void initAction() {
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.PRINT);
    }

    protected boolean isDataAvailable() {
        ContainerDataUnit<E> unit = (ContainerDataUnit<E>) target.getItems();
        CollectionContainer<E> container = unit.getContainer();
        return container instanceof HasLoader && unit.getState() == BindingState.ACTIVE && !container.getItems().isEmpty();
    }

    protected void printSelected(Set<?> selected) {
        ContainerDataUnit<E> unit = (ContainerDataUnit<E>) target.getItems();
        InstanceContainer<E> container = unit.getContainer();
        MetaClass metaClass = container.getEntityMetaClass();

        reportActionSupport.openRunReportScreen(UiComponentUtils.getView(((Component) target)), selected, metaClass);
    }

    protected void printAll() {
        ContainerDataUnit<E> unit = (ContainerDataUnit<E>) target.getItems();
        CollectionContainer<E> container = unit.getContainer();
        if (container instanceof CollectionPropertyContainer) {
            // as CollectionPropertyContainer does not have loader it always fetches all records,
            // so print these records as selected
            printSelected(new HashSet<E>(container.getMutableItems()));
            return;
        }
        CollectionLoader<?> loader = (CollectionLoader<?>) ((HasLoader) unit.getContainer()).getLoader();
        MetaClass metaClass = container.getEntityMetaClass();
        LoadContext<?> loadContext = loader.createLoadContext();

        ParameterPrototype parameterPrototype = new ParameterPrototype(metaClass.getName());
        parameterPrototype.setMetaClassName(metaClass.getName());
        LoadContext.Query query = loadContext.getQuery();
        parameterPrototype.setQueryString(query.getQueryString());
        parameterPrototype.setQueryParams(query.getParameters());
        parameterPrototype.setCondition(query.getCondition());
        parameterPrototype.setSort(query.getSort());

        if (!Strings.isNullOrEmpty(loadContext.getFetchPlan().getName())) {
            parameterPrototype.setFetchPlanName(loadContext.getFetchPlan().getName());
        } else {
            parameterPrototype.setFetchPlan(loadContext.getFetchPlan());
        }

        reportActionSupport.openRunReportScreen(UiComponentUtils.getView(((Component) target)),
                parameterPrototype, metaClass);
    }

    @Override
    public void execute() {
        checkTarget();

        DialogAction cancelAction = new DialogAction(DialogAction.Type.CANCEL);

        Set<E> selected = target.getSelectedItems();
        if (selected.size() > 1) {
            Action printSelectedAction = new BaseAction("actions.printSelected")
                    .withVariant(ActionVariant.PRIMARY)
                    .withHandler(event -> printSelected(selected))
                    .withIcon(ComponentUtils.convertToIcon(VaadinIcon.LINES))
                    .withText(messages.getMessage(getClass(), "actions.printSelected"));

            Action printAllAction = new BaseAction("actions.printAll")
                    .withText(messages.getMessage(getClass(), "actions.printAll"))
                    .withIcon(ComponentUtils.convertToIcon(VaadinIcon.TABLE))
                    .withHandler(event -> printAll());

            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage(getClass(), "notifications.confirmPrintSelectedHeader"))
                    .withText(messages.getMessage(getClass(), "notifications.confirmPrintSelected"))
                    .withActions(printAllAction, printSelectedAction, cancelAction)
                    .open();
        } else if (selected.size() == 1) {
            printSelected(selected);
        } else if (isDataAvailable()) {
            Action yesAction = new DialogAction(DialogAction.Type.OK)
                    .withHandler(event -> printAll())
                    .withVariant(ActionVariant.PRIMARY);

            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage(getClass(), "notifications.confirmPrintAllheader"))
                    .withText(messages.getMessage(getClass(), "notifications.confirmPrintAll"))
                    .withActions(yesAction, cancelAction)
                    .open();
        } else {
            notifications.create(messages.getMessage(getClass(), "notifications.noSelectedEntity"))
                    .show();
        }
    }
}
