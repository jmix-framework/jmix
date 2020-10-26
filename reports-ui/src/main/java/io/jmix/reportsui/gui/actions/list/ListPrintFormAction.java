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

package io.jmix.reportsui.gui.actions.list;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.ParameterPrototype;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.actions.AbstractPrintFormAction;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.ScreenContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * Standard action for printing reports for a list of entity instances associated with a list component.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action only selects reports having an external parameter of the {@code JmixEntity} or the List of entities type
 * and where the parameter entity type matches the entity type displayed by the list component.
 * If only one report is available as a result of selection, it is invoked immediately.
 * If several reports are available, their list is offered to the user for selection.
 */
@StudioAction(category = "Reports list actions", description = "Prints the reports for a list of entity instances associated with a list component")
@ActionType(ListPrintFormAction.ID)
public class ListPrintFormAction extends AbstractPrintFormAction implements Action.HasTarget {

    public static final String ID = "listPrintForm";

    protected Messages messages;

    protected ListComponent target;

    public ListPrintFormAction() {
        this(ID);
    }

    public ListPrintFormAction(String id) {
        super(id);
    }

    @Autowired
    public void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.PRINT);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage(ListPrintFormAction.class, "actions.ListPrintForm");
    }

    @Override
    public ListComponent getTarget() {
        return target;
    }

    @Override
    public void setTarget(ListComponent target) {
        this.target = target;
    }

    @Override
    public void actionPerform(Component component) {
        DialogAction cancelAction = new DialogAction(DialogAction.Type.CANCEL);

        ScreenContext screenContext = ComponentsHelper.getScreenContext(target);
        Preconditions.checkState(screenContext != null, "Component is not attached to window");

        if (beforeActionPerformedHandler != null
                && !beforeActionPerformedHandler.beforeActionPerformed()) {
            return;
        }

        Dialogs dialogs = screenContext.getDialogs();
        Set selected = target.getSelected();
        if (CollectionUtils.isNotEmpty(selected)) {
            if (selected.size() > 1) {
                Action printSelectedAction = new AbstractAction("actions.printSelected", Status.PRIMARY) {
                    @Override
                    public void actionPerform(Component component) {
                        printSelected(selected);
                    }
                };
                printSelectedAction.setIcon("icons/reports-print-row.png");

                Action printAllAction = new AbstractAction("actions.printAll") {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };
                printAllAction.setIcon("icons/reports-print-all.png");

                dialogs.createOptionDialog()
                        .withCaption(messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelectedheader"))
                        .withMessage(messages.getMessage(ReportGuiManager.class, "notifications.confirmPrintSelected"))
                        //TODO set type
//                        .withType(Dialogs.MessageType.CONFIRMATION)
                        .withActions(printAllAction, printSelectedAction, cancelAction)
                        .show();
            } else {
                printSelected(selected);
            }
        } else {
            if (isDataAvailable()) {
                Action yesAction = new DialogAction(DialogAction.Type.OK) {
                    @Override
                    public void actionPerform(Component component) {
                        printAll();
                    }
                };

                cancelAction.setPrimary(true);

                dialogs.createOptionDialog()
                        .withCaption(messages.getMessage(ListPrintFormAction.class, "notifications.confirmPrintAllheader"))
                        .withMessage(messages.getMessage(ListPrintFormAction.class, "notifications.confirmPrintAll"))
                        //TODO set type
//                        .withType(Dialogs.MessageType.CONFIRMATION)
                        .withActions(yesAction, cancelAction)
                        .show();
            } else {
                Notifications notifications = screenContext.getNotifications();
                notifications.create()
                        .withCaption(messages.getMessage(ReportGuiManager.class, "notifications.noSelectedEntity"))
                        .withType(Notifications.NotificationType.HUMANIZED)
                        .show();
            }

        }
    }

    protected boolean isDataAvailable() {
        if (target.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
            CollectionContainer container = unit.getContainer();
            return container instanceof HasLoader && unit.getState() == BindingState.ACTIVE && !container.getItems().isEmpty();
        } else {
            CollectionDatasource ds = ((com.haulmont.cuba.gui.components.ListComponent) target).getDatasource();
            if (ds != null)
                return ds.getState() == Datasource.State.VALID && ds.size() > 0;
        }
        return false;
    }

    protected void printSelected(Set selected) {
        MetaClass metaClass;
        if (target.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
            InstanceContainer container = unit.getContainer();
            metaClass = container.getEntityMetaClass();
        } else {
            CollectionDatasource ds = ((com.haulmont.cuba.gui.components.ListComponent) target).getDatasource();
            metaClass = ds.getMetaClass();
        }
        Window window = ComponentsHelper.getWindowNN(target);
        openRunReportScreen(window.getFrameOwner(), selected, metaClass);
    }

    protected void printAll() {
        MetaClass metaClass;
        LoadContext loadContext;

        if (target.getItems() instanceof ContainerDataUnit) {
            ContainerDataUnit unit = (ContainerDataUnit) target.getItems();
            CollectionContainer container = unit.getContainer();
            if (container instanceof CollectionPropertyContainer) {
                // as CollectionPropertyContainer does not have loader it always fetches all records,
                // so print these records as selected
                printSelected(new HashSet(container.getMutableItems()));
                return;
            }
            CollectionLoader loader = (CollectionLoader) ((HasLoader) unit.getContainer()).getLoader();
            metaClass = container.getEntityMetaClass();
            loadContext = (LoadContext) loader.createLoadContext();
        } else {
            CollectionDatasource ds = ((com.haulmont.cuba.gui.components.ListComponent) target).getDatasource();
            metaClass = ds.getMetaClass();
            loadContext = ds.getCompiledLoadContext();
        }

        ParameterPrototype parameterPrototype = new ParameterPrototype(metaClass.getName());
        parameterPrototype.setMetaClassName(metaClass.getName());
        LoadContext.Query query = loadContext.getQuery();
        parameterPrototype.setQueryString(query.getQueryString());
        parameterPrototype.setQueryParams(query.getParameters());
        parameterPrototype.setViewName(loadContext.getView().getName());
        parameterPrototype.setCondition(query.getCondition());
        parameterPrototype.setSort(query.getSort());

        Window window = ComponentsHelper.getWindowNN(target);
        openRunReportScreen(window.getFrameOwner(), parameterPrototype, metaClass);
    }
}
