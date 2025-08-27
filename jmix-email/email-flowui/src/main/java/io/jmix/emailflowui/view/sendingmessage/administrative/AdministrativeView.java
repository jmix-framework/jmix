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

//todo move up by 1 level
package io.jmix.emailflowui.view.sendingmessage.administrative;


import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.entity.RefreshToken;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.inputdialog.DialogActions;
import io.jmix.flowui.app.inputdialog.DialogOutcome;
import io.jmix.flowui.app.inputdialog.InputParameter;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "email-administrative-view", layout = DefaultMainViewParent.class)
@ViewController(id = "AdministrativeView")
@ViewDescriptor(path = "administrative-view.xml")
public class AdministrativeView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(AdministrativeView.class);

    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Notifications notifications;
    @Autowired
    protected EmailRefreshTokenManager emailRefreshTokenManager;
    @ViewComponent
    private InstanceLoader<RefreshToken> defaultRefreshTokenDl;
    @ViewComponent
    private JmixPasswordField defaultRefreshTokenValueField;

    @Subscribe
    public void onInit(final InitEvent event) {
        //defaultRefreshTokenDl.setEntityId(UUID.fromString("0198c7b9-4abc-77b6-9088-fb080c13200b"));
    }

    @Subscribe("updateDefaultRefreshTokenAction")
    public void onUpdateDefaultRefreshTokenAction(final ActionPerformedEvent event) {
        dialogs.createInputDialog(this)
                .withParameters(
                        InputParameter.stringParameter("tokenValue").withLabel("Token value")//todo localization, default value?
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                            if (closeEvent.closedWith(DialogOutcome.OK)) {
                                log.debug("[IVGA] Token save confirmed");
                                String tokenValue = closeEvent.getValue("tokenValue");
                                log.debug("[IVGA] Token value: {}", tokenValue);
                                RefreshToken refreshToken = emailRefreshTokenManager.storeDefaultRefreshTokenValue(tokenValue);
                                log.debug("[IVGA] Reload token");

                                defaultRefreshTokenDl.load();
                                notifications.create("Token value is updated").show();
                            } else {
                                log.debug("[IVGA] Token save outcome: {}", closeEvent.getCloseAction());
                            }
                        }
                )
                .withHeader("Update refresh token")
                .open();
    }

    @Install(to = "defaultRefreshTokenDl", target = Target.DATA_LOADER)
    private RefreshToken defaultRefreshTokenDlLoadDelegate(final LoadContext<RefreshToken> loadContext) {
        return emailRefreshTokenManager.loadDefaultRefreshToken();
    }
}