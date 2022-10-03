/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityflowui.view.resetpassword;

import com.vaadin.flow.component.textfield.TextArea;
import io.jmix.core.security.UserManager;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Set;

@ViewController("resetPasswordView")
@ViewDescriptor("reset-password-view.xml")
@DialogMode(width = "40em", height = "AUTO")
public class ResetPasswordView extends StandardView {

    @ViewComponent
    protected TextArea readyPassword;

    @Autowired
    private UserManager userManager;

    protected Set<? extends UserDetails> users;

    @SuppressWarnings("unchecked")
    @Subscribe("saveAction")
    public void onSaveActionPerformed(ActionPerformedEvent event) {
        Map<UserDetails, String> newPasswords = userManager.resetPasswords((Set<UserDetails>) users);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<UserDetails, String> entry : newPasswords.entrySet()) {
            builder.append(entry.getKey().getUsername())
                    .append("\t")
                    .append(entry.getValue())
                    .append("\n");
        }
        readyPassword.setValue(builder.toString());
        readyPassword.setVisible(true);
    }

    @Subscribe("closeAction")
    public void onCloseActionPerformed(ActionPerformedEvent event) {
        closeWithDefaultAction();
    }

    public void setUsers(Set<? extends UserDetails> users) {
        this.users = users;
    }
}
