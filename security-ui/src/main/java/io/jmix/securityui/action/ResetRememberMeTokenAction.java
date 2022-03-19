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

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Resets the remember me token for the UserDetails instance"
)
@ActionType(ResetRememberMeTokenAction.ID)
public class ResetRememberMeTokenAction extends ListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "resetRememberMeToken";
    protected Messages messages;
    protected Notifications notifications;
    protected UserManager userManager;
    protected AccessManager accessManager;

    public ResetRememberMeTokenAction() {
        super(ID);
    }

    public ResetRememberMeTokenAction(String id) {
        super(id);
    }

    @Autowired
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.resetRememberMeToken");
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
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

        if (!(UserDetails.class.isAssignableFrom(metaClass.getJavaClass()))) {
            throw new IllegalStateException("Target does not implement a UserDetails");
        }

        buildAndShowChangePasswordDialog();
    }

    private void buildAndShowChangePasswordDialog() {
        Dialogs.OptionDialogBuilder builder = UiControllerUtils.getScreenContext(target.getFrame().getFrameOwner())
                .getDialogs()
                .createOptionDialog();
        Set selected = target.getSelected();
        builder.withCaption(messages.getMessage("resetRememberMeResetDialog.resetRememberMeTitle"))
                .withMessage(messages.getMessage("resetRememberMeResetDialog.resetRememberMeQuestion"));
        BaseAction resetForAll = new BaseAction("resetRememberMeResetDialog.ResetAll")
                .withCaption(messages.getMessage("resetRememberMeResetDialog.ResetAll"))
                .withHandler(event -> resetRememberMeAll());
        DialogAction cancelAction = new DialogAction(DialogAction.Type.CANCEL, Status.PRIMARY);
        if (selected.isEmpty()) {
            builder.withActions(resetForAll, cancelAction);
        } else {
            builder.withActions(new BaseAction("resetRememberMeResetDialog.ResetOptionSelected")
                            .withCaption(messages.getMessage("resetRememberMeResetDialog.ResetOptionSelected"))
                            .withHandler(event -> resetRememberMe(selected)),
                    resetForAll, cancelAction);
        }
        builder.show();
    }

    public void resetRememberMeAll() {
        DataUnit items = target.getItems();
        if (items instanceof ContainerDataUnit) {
            resetRememberMe(((ContainerDataUnit<UserDetails>) items)
                    .getContainer()
                    .getItems());
        }
    }

    public void resetRememberMe(Collection<UserDetails> users) {
        userManager.resetRememberMe(users);
        notifications.create()
                .withCaption(messages.getMessage("resetRememberMeResetDialog.resetRememberMeCompleted"))
                .withType(Notifications.NotificationType.HUMANIZED)
                .show();
    }
}
