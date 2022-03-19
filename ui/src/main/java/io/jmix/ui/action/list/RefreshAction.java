/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.action.list;

import io.jmix.core.Messages;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EmptyDataUnit;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Standard action for reloading a list of entities from the database.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Reloads a list of entities from the database",
        availableInScreenWizard = true)
@ActionType(RefreshAction.ID)
public class RefreshAction extends ListAction implements Action.ExecutableAction {

    public static final String ID = "refresh";

    private static final Logger log = LoggerFactory.getLogger(RefreshAction.class);

    public RefreshAction() {
        super(ID);
    }

    public RefreshAction(String id) {
        super(id);
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.REFRESH_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Refresh");
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("RefreshAction target is not set");
        }

        if (target.getItems() instanceof EmptyDataUnit) {
            return;
        }

        if (!(target.getItems() instanceof ContainerDataUnit)) {
            throw new IllegalStateException("RefreshAction target is null or does not implement SupportsContainerBinding");
        }

        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        if (container == null) {
            throw new IllegalStateException("RefreshAction target is not bound to CollectionContainer");
        }

        DataLoader loader = null;
        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }
        if (loader != null) {
            DataContext dataContext = loader.getDataContext();
            if (dataContext != null) {
                for (Object entity : container.getItems()) {
                    dataContext.evict(entity);
                }
            }
            loader.load();
        } else {
            log.warn("RefreshAction '{}' target container has no loader, refresh is impossible", getId());
        }
    }
}
