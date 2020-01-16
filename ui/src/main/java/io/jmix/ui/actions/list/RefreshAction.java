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

package io.jmix.ui.actions.list;

import io.jmix.core.Messages;
import io.jmix.ui.actions.ActionType;
import io.jmix.ui.actions.ListAction;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.data.meta.ContainerDataUnit;
import io.jmix.ui.components.data.meta.EmptyDataUnit;
import io.jmix.ui.icons.CubaIcon;
import io.jmix.ui.icons.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Standard action for reloading a list of entities from the database.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@StudioAction(category = "List Actions", description = "Reloads a list of entities from the database")
@ActionType(RefreshAction.ID)
public class RefreshAction extends ListAction {

    public static final String ID = "refresh";

    protected Messages messages;

    private static final Logger log = LoggerFactory.getLogger(RefreshAction.class);

    public RefreshAction() {
        super(ID);
    }

    public RefreshAction(String id) {
        super(id);
    }

    @Inject
    protected void setIcons(Icons icons) {
        this.icon = icons.get(CubaIcon.REFRESH_ACTION);
    }

    @Inject
    protected void setMessages(Messages messages) {
        this.messages = messages;
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
            loader.load();
        } else {
            log.warn("RefreshAction '{}' target container has no loader, refresh is impossible", getId());
        }
    }
}