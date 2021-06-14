/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.app.scheduled;

import com.haulmont.cuba.core.app.SchedulingService;
import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.global.RunTaskOnceException;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.screen.Subscribe;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduledTaskBrowser extends AbstractWindow {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskBrowser.class);

    @Inject
    protected CollectionDatasource<ScheduledTask, UUID> tasksDs;
    @Inject
    protected Table<ScheduledTask> tasksTable;
    @Inject
    protected Button activateBtn;

    @Inject
    protected Screens screens;
    @Inject
    protected SchedulingService service;

    @Override
    public void init(Map<String, Object> params) {
        tasksTable.addAction(CreateAction.create(tasksTable));
        tasksTable.addAction(EditAction.create(tasksTable));
        tasksTable.addAction(RemoveAction.create(tasksTable));

        Action editAction = tasksTable.getActionNN(EditAction.ACTION_ID);
        editAction.setEnabled(false);

        Action removeAction = tasksTable.getActionNN(RemoveAction.ACTION_ID);
        removeAction.setEnabled(false);

        activateBtn.setAction(new BaseAction("activate")
                .withCaption(messages.getMainMessage("activate"))
                .withHandler(e -> {
                    Set<ScheduledTask> tasks = tasksTable.getSelected();
                    service.setActive(tasks, !BooleanUtils.isTrue(tasks.iterator().next().getActive()));
                    tasksDs.refresh();
                }));

        activateBtn.setEnabled(false);

        ShowExecutionsAction showExecutionsAction = new ShowExecutionsAction();
        tasksTable.addAction(showExecutionsAction);

        ExecuteOnceAction executeOnceAction = new ExecuteOnceAction();
        tasksTable.addAction(executeOnceAction);

        tasksDs.addItemChangeListener(e -> {
            ScheduledTask singleSelected = tasksTable.getSingleSelected();
            Set<ScheduledTask> selected = tasksTable.getSelected().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            boolean isSingleSelected = selected.size() == 1;
            boolean enableEdit = singleSelected != null && !BooleanUtils.isTrue(singleSelected.getActive());

            editAction.setEnabled(enableEdit);
            removeAction.setEnabled(checkAllTasksAreNotActive(selected));
            activateBtn.setEnabled(checkAllTasksHaveSameStatus(selected));

            if (singleSelected == null) {
                activateBtn.setCaption(messages.getMainMessage("activate"));
            } else {
                activateBtn.setCaption(BooleanUtils.isTrue(singleSelected.getActive()) ?
                        messages.getMainMessage("deactivate") : messages.getMainMessage("activate"));
            }

            showExecutionsAction.setEnabled(isSingleSelected);
            executeOnceAction.setEnabled(isSingleSelected && enableEdit);
        });
    }

    @Subscribe("showRunningTasksBtn")
    protected void onShowRunningTasksBtnClick(Button.ClickEvent event) {
        screens.create(ScheduledRunningTasksScreen.class)
                .show();
    }

    protected boolean checkAllTasksHaveSameStatus(Set<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            return false;
        }

        boolean activeState = BooleanUtils.toBoolean(tasks.iterator().next().getActive());
        for (ScheduledTask task : tasks) {
            if (BooleanUtils.toBoolean(task.getActive()) != activeState) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkAllTasksAreNotActive(Set<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            return false;
        }

        for (ScheduledTask task : tasks) {
            if (BooleanUtils.isTrue(task.getActive())) {
                return false;
            }
        }
        return true;
    }

    protected class ShowExecutionsAction extends ItemTrackingAction {
        public ShowExecutionsAction() {
            super("executions");

            setCaption(messages.getMainMessage("executions"));
        }

        @Override
        public void actionPerform(Component component) {
            ScheduledTask task = tasksTable.getSingleSelected();
            if (task != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("task", task);
                openWindow("sys$ScheduledExecution.browse", OpenType.THIS_TAB, params);
            }
        }

        @Override
        public boolean isApplicable() {
            return tasksTable.getSelected().size() == 1;
        }
    }

    protected class ExecuteOnceAction extends ItemTrackingAction {
        public ExecuteOnceAction() {
            super("executeOnce");

            setCaption(messages.getMainMessage("executeOnce"));
        }

        @Override
        public void actionPerform(Component component) {
            ScheduledTask task = tasksTable.getSingleSelected();
            try {
                if (task != null) {
                    service.runOnce(task);
                }
            } catch (RunTaskOnceException e) {
                log.error("Can't execute {}: not in permitted hosts or not a master.", e.getMessage());
                showNotification(messages.getMainMessage("errorNotification.caption"),
                        messages.getMainMessage("errorNotification.message"),
                        NotificationType.ERROR);
            }
        }

        @Override
        public boolean isApplicable() {
            ScheduledTask task = tasksTable.getSingleSelected();
            return task != null && !BooleanUtils.isTrue(task.getActive());
        }
    }
}