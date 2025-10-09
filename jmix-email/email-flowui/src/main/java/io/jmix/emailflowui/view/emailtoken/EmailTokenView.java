/*
 * Copyright 2025 Haulmont.
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

package io.jmix.emailflowui.view.emailtoken;


import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.entity.RefreshToken;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "email/token", layout = DefaultMainViewParent.class)
@ViewController(id = "email_tokenView")
@ViewDescriptor(path = "email-token-view.xml")
public class EmailTokenView extends StandardView {

    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected EmailRefreshTokenManager emailRefreshTokenManager;
    @Autowired
    protected Messages messages;

    @ViewComponent
    protected InstanceLoader<RefreshToken> refreshTokenDl;
    @ViewComponent
    protected JmixPasswordField refreshTokenValueField;

    @Subscribe("updateRefreshTokenAction")
    public void onUpdateRefreshTokenAction(final ActionPerformedEvent event) {
        dialogs.createInputDialog(this)
                .withParameters(buildTokenValueInputParameter())
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                            if (closeEvent.closedWith(DialogOutcome.OK)) {
                                String tokenValue = closeEvent.getValue("tokenValue");
                                emailRefreshTokenManager.storeRefreshTokenValue(tokenValue);

                                refreshTokenDl.load();
                                notifications.create("Token value is updated").show();
                            }
                        }
                )
                .withHeader("Update refresh token")
                .open();
    }

    protected InputParameter buildTokenValueInputParameter() {
        InputParameter parameter = InputParameter
                .stringParameter("tokenValue")
                .withLabel(messages.getMessage(getClass(), "onUpdateRefreshTokenAction.dialog.tokenValue.label"));
        RefreshToken refreshToken = emailRefreshTokenManager.loadRefreshToken();
        if (refreshToken != null) {
            parameter.withDefaultValue(refreshToken.getTokenValue());
        }
        return parameter;
    }

    @Install(to = "refreshTokenDl", target = Target.DATA_LOADER)
    private RefreshToken refreshTokenDlLoadDelegate(final LoadContext<RefreshToken> loadContext) {
        return emailRefreshTokenManager.loadRefreshToken();
    }
}