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

package io.jmix.securityui.action;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.securityui.screen.changepassword.ChangePasswordDialog;
import io.jmix.ui.Screens;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@ActionType(ChangePasswordAction.ID)
public class ChangePasswordAction extends SecuredListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "changePassword";

    // Set default caption only once
    protected boolean currentPasswordRequired = false;
    protected Screens screens;

    public ChangePasswordAction() {
        super(ID);
    }

    public ChangePasswordAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.changePassword");
    }

    @Autowired
    public void setScreens(Screens screens) {
        this.screens = screens;
    }

    public void setCurrentPasswordRequired(boolean currentPasswordRequired) {
        this.currentPasswordRequired = currentPasswordRequired;
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

        if (!entityContext.isEditPermitted()) {
            return false;
        }

        return super.isPermitted();
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
            throw new IllegalStateException("Target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("Target dataSource is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        Object editedEntity = target.getSingleSelected();
        if (editedEntity == null) {
            throw new IllegalStateException("There is not selected item in ChangePassword target");
        }

        UserDetails user = (UserDetails) editedEntity;

        buildAndShowChangePasswordDialog(user);
    }

    protected void buildAndShowChangePasswordDialog(UserDetails user) {
        screens.create(ChangePasswordDialog.class)
                .withUsername(user.getUsername())
                .withCurrentPasswordRequired(currentPasswordRequired)
                .show();
    }
}
