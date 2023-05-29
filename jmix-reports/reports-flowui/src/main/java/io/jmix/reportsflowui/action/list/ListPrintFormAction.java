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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
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
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.View;
import io.jmix.reports.app.ParameterPrototype;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
//todo
//@StudioAction(
//        target = "io.jmix.ui.component.ListComponent",
//        description = "Prints the reports for a list of entity instances associated with a list component")
@ActionType(ListPrintFormAction.ID)
public class ListPrintFormAction<E> extends ListDataComponentAction<ListPrintFormAction<E>, E>
        implements AdjustWhenViewReadOnly {

    //listViewPrint
    public static final String ID = "listPrintForm";

    protected Dialogs dialogs;
    protected Messages messages;
    protected Notifications notifications;
    protected PrintFormReport printFormReport;

    public ListPrintFormAction() {
        this(ID);
    }

    public ListPrintFormAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        this.text = messages.getMessage(ListPrintFormAction.class, "actions.ListPrintForm");
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
    public void setPrintReport(PrintFormReport printFormReport) {
        this.printFormReport = printFormReport;
    }

    @Override
    protected void initAction() {
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PRINT);
    }

    protected boolean isDataAvailable() {
        ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
        CollectionContainer container = unit.getContainer();
        return container instanceof HasLoader && unit.getState() == BindingState.ACTIVE && !container.getItems().isEmpty();
    }

    protected void printSelected(Set selected) {
        ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
        InstanceContainer container = unit.getContainer();
        MetaClass metaClass = container.getEntityMetaClass();

        printFormReport.openRunReportScreen(findParent(), selected, metaClass);
    }

    protected void printAll() {
        ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
        CollectionContainer container = unit.getContainer();
        if (container instanceof CollectionPropertyContainer) {
            // as CollectionPropertyContainer does not have loader it always fetches all records,
            // so print these records as selected
            printSelected(new HashSet(container.getMutableItems()));
            return;
        }
        CollectionLoader loader = (CollectionLoader) ((HasLoader) unit.getContainer()).getLoader();
        MetaClass metaClass = container.getEntityMetaClass();
        LoadContext loadContext = loader.createLoadContext();

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

        printFormReport.openRunReportScreen(findParent(), parameterPrototype, metaClass);
    }

    @Override
    public void execute() {
        DialogAction cancelAction = new DialogAction(DialogAction.Type.CANCEL);

        Set selected = target.getSelectedItems();
        if (CollectionUtils.isNotEmpty(selected)) {
            if (selected.size() > 1) {
                Action printSelectedAction = new BaseAction("actions.printSelected")
                        .withVariant(ActionVariant.PRIMARY)
                        .withHandler(event -> printSelected(selected))
                        .withIcon(FlowuiComponentUtils.convertToIcon(VaadinIcon.LINES))
                        .withText(messages.getMessage(getClass(), "actions.printSelected"));

                Action printAllAction = new BaseAction("actions.printAll")
                        .withText(messages.getMessage(getClass(), "actions.printAll"))
                        .withIcon(FlowuiComponentUtils.convertToIcon(VaadinIcon.TABLE))
                        .withHandler(event -> printAll());

                dialogs.createOptionDialog()
                        .withHeader(messages.getMessage(getClass(), "notifications.confirmPrintSelectedHeader"))
                        .withText(messages.getMessage(getClass(), "notifications.confirmPrintSelected"))
                        .withActions(printAllAction, printSelectedAction, cancelAction)
                        .open();
            } else {
                printSelected(selected);
            }
        } else {
            if (isDataAvailable()) {
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
                        .withType(Notifications.Type.DEFAULT)
                        .show();
            }

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
}
