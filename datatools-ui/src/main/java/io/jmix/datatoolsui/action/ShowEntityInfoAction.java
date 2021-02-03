/*
 * Copyright 2019 Haulmont.
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
package io.jmix.datatoolsui.action;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.datatoolsui.accesscontext.UiShowEntityInfoContext;
import io.jmix.datatoolsui.screen.entityinfo.EntityInfoWindow;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.meta.StudioAction;
import org.springframework.beans.factory.annotation.Autowired;

@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Displays a dialog window with detailed information about the selected entity")
@ActionType(ShowEntityInfoAction.ID)
public class ShowEntityInfoAction extends SecuredListAction implements Action.ExecutableAction {

    public static final String ID = "showEntityInfo";

    protected boolean visibleBySpecificUiPermission = true;

    public ShowEntityInfoAction() {
        this(ID);
    }

    public ShowEntityInfoAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        setCaption(messages.getMessage(ShowEntityInfoAction.class, "showEntityInfoAction.caption"));
    }

    @Autowired
    @Override
    protected void setAccessManager(AccessManager accessManager) {
        super.setAccessManager(accessManager);

        UiShowEntityInfoContext context = new UiShowEntityInfoContext();
        accessManager.applyRegisteredConstraints(context);

        visibleBySpecificUiPermission = context.isPermitted();
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleBySpecificUiPermission
                && super.isVisibleByUiPermissions();
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

        Object selectedItem = target.getSingleSelected();
        if (selectedItem != null) {
            showInfo(selectedItem, target);
        }
    }

    public void showInfo(Object entity, Component.BelongToFrame component) {
        Screens screens = ComponentsHelper.getScreenContext(component)
                .getScreens();

        EntityInfoWindow screen = screens.create(EntityInfoWindow.class);
        screen.setEntity(entity);

        screen.show();
    }
}
