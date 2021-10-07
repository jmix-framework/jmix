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

package io.jmix.securityui.screen.roleassignment;

import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Window;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

@UiController("sec_RoleAssignmentScreen")
@UiDescriptor("role-assignment-screen.xml")
public class RoleAssignmentScreen extends Screen {

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private MetadataTools metadataTools;

    @SuppressWarnings("JmixScreenControllerInjects")
    @Autowired
    private RoleAssignmentFragment roleAssignmentFragment;

    @Autowired
    private ScreenValidation screenValidation;

    private UserDetails user;
    @Autowired
    private DataContext dataContext;

    public RoleAssignmentScreen setUser(UserDetails user) {
        this.user = user;
        roleAssignmentFragment.setUser(user);
        return this;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        initScreenActions();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        getWindow().setCaption(messageBundle.formatMessage("RoleAssignmentScreen.caption",
                metadataTools.getInstanceName(user)));
    }

    protected void initScreenActions() {
        Window window = getWindow();

        Messages messages = getApplicationContext().getBean(Messages.class);
        Icons icons = getApplicationContext().getBean(Icons.class);

        String commitShortcut = getApplicationContext().getBean(UiScreenProperties.class).getCommitShortcut();

        Action commitAndCloseAction = new BaseAction(Window.COMMIT_ACTION_ID)
                .withCaption(messages.getMessage("actions.Ok"))
                .withIcon(icons.get(JmixIcon.EDITOR_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut)
                .withHandler(actionPerformedEvent -> {
                    //noinspection ConstantConditions
                    getScreenData().getDataContext().commit();
                    close(new StandardCloseAction(Window.COMMIT_ACTION_ID));
                });

        window.addAction(commitAndCloseAction);

        Action closeAction = new BaseAction(Window.CLOSE_ACTION_ID)
                .withIcon(icons.get(JmixIcon.EDITOR_CANCEL))
                .withCaption(messages.getMessage("actions.Cancel"))
                .withHandler(actionPerformedEvent -> {
                    if (dataContext.hasChanges()) {
                        screenValidation.showUnsavedChangesDialog(this, WINDOW_CLOSE_ACTION)
                                .onDiscard(() -> close(WINDOW_CLOSE_ACTION));
                    } else {
                        close(WINDOW_CLOSE_ACTION);
                    }
                });
        window.addAction(closeAction);
    }
}