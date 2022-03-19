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

package io.jmix.datatoolsui.action;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.datatoolsui.accesscontext.UiShowEntityInfoContext;
import io.jmix.datatoolsui.screen.entityinfo.EntityInfoWindow;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;


@ActionType(EditorScreenShowEntityInfoAction.ID)
public class EditorScreenShowEntityInfoAction extends BaseAction implements Action.MainTabSheetAction {

    public static final String ID = "editorScreenShowEntityInfo";

    protected boolean visibleBySpecificUiPermission = true;

    public EditorScreenShowEntityInfoAction() {
        super(ID);
    }

    public EditorScreenShowEntityInfoAction(String id) {
        super(id);
    }

    @Autowired
    public void setMessages(Messages messages) {
        setCaption(messages.getMessage(ShowEntityInfoAction.class, "showEntityInfoAction.caption"));
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
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
        throw new UnsupportedOperationException("Use execute(Screen) instead");
    }

    @Override
    public boolean isApplicable(Screen screen) {
        return screen instanceof EditorScreen;
    }

    @Override
    public void execute(Screen screen) {
        if (!(screen instanceof EditorScreen)) {
            throw new IllegalArgumentException("Screen must be instance of EditorScreen");
        }

        Object entity = ((EditorScreen<?>) screen).getEditedEntity();
        Screens screens = UiControllerUtils.getScreenContext(screen).getScreens();

        EntityInfoWindow infoWindow = screens.create(EntityInfoWindow.class);
        infoWindow.setEntity(entity);

        infoWindow.show();
    }
}
