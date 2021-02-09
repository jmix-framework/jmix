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
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@ActionType(ResetRememberMeTokenAction.ID)
public class ResetRememberMeTokenAction extends ListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "resetRememberMeToken";
    protected Messages messages;
    protected Notifications notifications;
    protected PersistentTokenRepository tokenRepository;

    public ResetRememberMeTokenAction() {
        super(ID);
    }

    public ResetRememberMeTokenAction(String id) {
        super(id);
    }

    @Autowired
    public void setTokenRepository(PersistentTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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
        Set<String> userNames = users.stream()
                .map(UserDetails::getUsername)
                .collect(Collectors.toSet());
        for (String userName : userNames) {
            tokenRepository.removeUserTokens(userName);
        }
        notifications.create()
                .withCaption(messages.getMessage("resetRememberMeResetDialog.resetRememberMeCompleted"))
                .withType(Notifications.NotificationType.HUMANIZED)
                .show();
    }


}
