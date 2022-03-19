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

package io.jmix.securityui.screen.resetpassword;

import io.jmix.core.Messages;
import io.jmix.core.security.UserManager;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.component.Window;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@UiController("ResetPasswordDialog")
@UiDescriptor("reset-password-dialog.xml")
public class ResetPasswordDialog extends Screen {

    private Set<UserDetails> users;

    @Autowired
    private UserManager userManager;
    @Autowired
    private Messages messages;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private Icons icons;
    @Autowired
    private UiScreenProperties uiProperties;
    @Autowired
    private TextArea<String> readyPassword;

    @Subscribe
    public void onInit(InitEvent event) {
        Map<String, Object> params = Collections.emptyMap();
        ScreenOptions options = event.getOptions();
        if (options instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }
        Object usersObject = params.get("users");
        if (usersObject instanceof Set) {
            users = (Set<UserDetails>) usersObject;
        }

        initScreenActions();
    }

    protected void initScreenActions() {
        Window window = getWindow();

        String commitShortcut = uiProperties.getCommitShortcut();

        Action commitAndCloseAction = new BaseAction(Window.COMMIT_ACTION_ID)
                .withCaption(messages.getMessage("io.jmix.securityui.screen.resetpassword/ResetPasswordDialog.generateSave"))
                .withIcon(icons.get(JmixIcon.EDITOR_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut)
                .withHandler(actionPerformedEvent -> {
                    Map<UserDetails, String> newPasswords = userManager.resetPasswords(users);
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<UserDetails, String> entry : newPasswords.entrySet()) {
                        builder.append(entry.getKey().getUsername()).append("\t").append(entry.getValue()).append("\n");
                    }
                    readyPassword.setValue(builder.toString());
                    readyPassword.setVisible(true);
                });

        window.addAction(commitAndCloseAction);

        Action closeAction = new BaseAction(Window.CLOSE_ACTION_ID)
                .withIcon(icons.get(JmixIcon.EDITOR_CANCEL))
                .withCaption(messages.getMessage("actions.Close"))
                .withHandler(actionPerformedEvent -> close(new StandardCloseAction(Window.CLOSE_ACTION_ID)));

        window.addAction(closeAction);
    }


}