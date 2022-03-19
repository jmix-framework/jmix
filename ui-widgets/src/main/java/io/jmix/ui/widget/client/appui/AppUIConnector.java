/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.widget.client.appui;

import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.UIDL;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.VNotification;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.ui.UIConstants;
import elemental.client.Browser;
import elemental.html.History;
import io.jmix.ui.AppUI;
import io.jmix.ui.widget.client.tooltip.JmixTooltip;
import io.jmix.ui.widget.client.ui.AppUIClientRpc;
import io.jmix.ui.widget.client.ui.AppUIConstants;

import java.util.Map;

@Connect(AppUI.class)
public class AppUIConnector extends UIConnector {

    public AppUIConnector() {
        VNotification.setRelativeZIndex(true);

        //noinspection Convert2Lambda
        registerRpc(AppUIClientRpc.class, new AppUIClientRpc() {
            @Override
            public void updateSystemMessagesLocale(Map<String, String> localeMap) {
                ApplicationConfiguration conf = getConnection().getConfiguration();
                ApplicationConfiguration.ErrorMessage communicationError = conf.getCommunicationError();
                communicationError.setCaption(localeMap.get(AppUIClientRpc.COMMUNICATION_ERROR_CAPTION_KEY));
                communicationError.setMessage(localeMap.get(AppUIClientRpc.COMMUNICATION_ERROR_MESSAGE_KEY));

                ApplicationConfiguration.ErrorMessage authError = conf.getAuthorizationError();
                authError.setCaption(localeMap.get(AppUIClientRpc.AUTHORIZATION_ERROR_CAPTION_KEY));
                authError.setMessage(localeMap.get(AppUIClientRpc.AUTHORIZATION_ERROR_MESSAGE_KEY));

                ApplicationConfiguration.ErrorMessage sessionExpiredError = conf.getSessionExpiredError();
                sessionExpiredError.setCaption(localeMap.get(AppUIClientRpc.SESSION_EXPIRED_ERROR_CAPTION_KEY));
                sessionExpiredError.setMessage(localeMap.get(AppUIClientRpc.SESSION_EXPIRED_ERROR_MESSAGE_KEY));
            }
        });
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.isInitialStateChange()) {
            // check mode of required indicator icon/hidden
            // performed on page open or full refresh
            JmixTooltip.checkRequiredIndicatorMode();
        }
    }

    @Override
    protected void updateBrowserHistory(UIDL uidl) {
        String lastHistoryOp = uidl.getStringAttribute(AppUIConstants.LAST_HISTORY_OP);

        History history = Browser.getWindow().getHistory();
        String pageTitle = getState().pageState.title;

        String replace = uidl.getStringAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE);
        String push = uidl.getStringAttribute(UIConstants.ATTRIBUTE_PUSH_STATE);

        if (AppUIConstants.HISTORY_PUSH_OP.equals(lastHistoryOp)) {
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE)) {
                history.replaceState(null, pageTitle, replace);
            }
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_PUSH_STATE)) {
                history.pushState(null, pageTitle, push);
            }
        } else {
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_PUSH_STATE)) {
                history.pushState(null, pageTitle, push);
            }
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE)) {
                history.replaceState(null, pageTitle, replace);
            }
        }
    }
}
