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
import io.jmix.securityui.screen.roleassignment.RoleAssignmentScreen;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.meta.StudioAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Shows the role assignments for the UserDetails instance"
)
@ActionType(ShowRoleAssignmentsAction.ID)
public class ShowRoleAssignmentsAction extends SecuredListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {
    public static final String ID = "showRoleAssignments";

    protected ScreenBuilders screenBuilders;

    public ShowRoleAssignmentsAction() {
        this(ID);
    }

    public ShowRoleAssignmentsAction(String id) {
        super(id);
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.showRoleAssignments");
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
            throw new IllegalStateException("Target items is null or does not implement EntityDataUnit");
        }

        if (!(target.getSingleSelected() instanceof UserDetails)) {
            throw new IllegalStateException("Target item does not implement UserDetails");
        }

        screenBuilders.screen(Objects.requireNonNull(target.getFrame()).getFrameOwner())
                .withScreenClass(RoleAssignmentScreen.class)
                .build()
                .setUser((UserDetails) target.getSingleSelected())
                .show();
    }
}
