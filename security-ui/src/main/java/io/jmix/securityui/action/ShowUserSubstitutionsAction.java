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

package io.jmix.securityui.action;

import io.jmix.core.Messages;
import io.jmix.securityui.screen.usersubstitution.UserSubstitutionsScreen;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;


@ActionType(ShowUserSubstitutionsAction.ID)
public class ShowUserSubstitutionsAction extends SecuredListAction implements Action.ExecutableAction, Action.AdjustWhenScreenReadOnly {

    public static final String ID = "showUserSubstitutions";

    protected ScreenBuilders screenBuilders;

    @Autowired
    protected void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.showUserSubstitutions");
    }

    public ShowUserSubstitutionsAction() {
        this(ID);
    }

    public ShowUserSubstitutionsAction(String id) {
        super(id);
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
                .withScreenClass(UserSubstitutionsScreen.class)
                .build()
                .setUser((UserDetails) target.getSingleSelected())
                .show();
    }
}
