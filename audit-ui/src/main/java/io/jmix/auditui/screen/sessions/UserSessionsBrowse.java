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

package io.jmix.auditui.screen.sessions;

import io.jmix.audit.UserSessions;
import io.jmix.audit.entity.UserSession;
import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("userSessions.browse")
@UiDescriptor("user-sessions-browse.xml")
@LookupComponent("sessionsTable")
@Route("sessions")
public class UserSessionsBrowse extends StandardLookup<UserSession> {

    @Autowired
    protected Table<UserSession> sessionsTable;

    @Autowired
    protected CollectionContainer<UserSession> userSessionsDc;

    @Autowired
    protected CollectionLoader<UserSession> userSessionsDl;

    @Autowired
    protected UserSessions userSessions;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;
    @Autowired
    private TextField<String> userName;
    @Autowired
    private DateField<Date> lastRequestDateFrom;
    @Autowired
    private DateField<Date> lastRequestDateTo;


    @Subscribe
    protected void onInit(InitEvent event) {
        userSessionsDl.setLoadDelegate(loadContext -> userSessions.sessions().collect(Collectors.toList()));
        userSessionsDl.load();
    }

    @Subscribe("sessionsTable.expire")
    public void onSessionsTableExpire(Action.ActionPerformedEvent event) {
        UserSession session = sessionsTable.getSingleSelected();
        if (session != null) {
            userSessions.invalidate(session);
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.formatMessage(UserSessionsBrowse.class, "sessionInvalidated", session.getSessionId()))
                    .show();
            userSessionsDl.load();
        }
    }

    @Subscribe("clearButton")
    public void onClearButtonClick(Button.ClickEvent event) {
        userName.setValue(null);
        lastRequestDateFrom.setValue(null);
        lastRequestDateTo.setValue(null);
        refreshDlItems();
    }

    @Subscribe("sessionsTable.refresh")
    public void onSessionsTableRefresh(Action.ActionPerformedEvent event) {
        refreshDlItems();
    }

    private void refreshDlItems() {
        userSessionsDl.setLoadDelegate(loadContext -> {
            Stream<UserSession> sessions = userSessions.sessions();
            if (userName.getValue() != null) {
                sessions = sessions.filter(o -> o.getPrincipalName().toLowerCase().contains(userName.getValue()));
            }
            if (lastRequestDateFrom.getValue() != null) {
                sessions = sessions.filter(o -> o.getLastRequest().after(lastRequestDateFrom.getValue()));
            }
            if (lastRequestDateTo.getValue() != null) {
                sessions = sessions.filter(o -> o.getLastRequest().before(lastRequestDateTo.getValue()));
            }
            return sessions.collect(Collectors.toList());
        });
        userSessionsDl.load();
    }
}
