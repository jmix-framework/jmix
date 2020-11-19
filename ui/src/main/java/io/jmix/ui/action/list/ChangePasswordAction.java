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

package io.jmix.ui.action.list;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiProperties;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ActionType(ChangePasswordAction.ID)
public class ChangePasswordAction extends SecuredListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "changePassword";

    private static final Logger LOG = LoggerFactory.getLogger(RefreshAction.class);

    public ChangePasswordAction() {
        super(ID);
    }

    public ChangePasswordAction(String id) {
        super(id);
    }

    protected ScreenBuilders screenBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    // Set default caption only once
    protected boolean captionInitialized = false;
    protected Messages messages;

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.EDIT_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.changePassword");
    }

    @Autowired
    protected void setUiProperties(UiProperties properties) {
        setShortcut(properties.getTableEditShortcut());
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    public void setCaption(@Nullable String caption) {
        super.setCaption(caption);

        this.captionInitialized = true;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isViewPermitted()) {
            return false;
        }

        return super.isPermitted();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        if (!(target.getItems() instanceof EntityDataUnit)) {
            return;
        }

        if (!captionInitialized) {
            MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            if (metaClass != null) {
                if (entityContext.isEditPermitted()) {
                    setCaption(messages.getMessage("actions.Edit"));
                } else {
                    setCaption(messages.getMessage("actions.View"));
                }
            }
        }
    }

    @Override
    public boolean isDisabledWhenScreenReadOnly() {
        if (!(target.getItems() instanceof EntityDataUnit)) {
            return true;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass != null) {
            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return entityContext.isEditPermitted();
        }

        return true;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
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
            throw new IllegalStateException("ChangePasswordAction target is not set");
        }
    }
}
