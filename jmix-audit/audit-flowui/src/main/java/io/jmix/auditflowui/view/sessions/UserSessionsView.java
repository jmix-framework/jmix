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

package io.jmix.auditflowui.view.sessions;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.audit.UserSessions;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.audit.entity.UserSession;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.timepicker.TypedTimePicker;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "audit/usersessions", layout = DefaultMainViewParent.class)
@ViewController("userSession.view")
@ViewDescriptor("user-sessions-view.xml")
@LookupComponent("sessionsTable")
@DialogMode(width = "50em")
public class UserSessionsView extends StandardListView<EntityLogItem> {

    @ViewComponent
    protected DataGrid<UserSession> sessionsTable;
    @ViewComponent
    protected TextField userName;
    @ViewComponent
    protected TypedDatePicker<LocalDate> lastRequestDateFrom;
    @ViewComponent
    protected TypedTimePicker<LocalTime> lastRequestTimeFrom;
    @ViewComponent
    protected TypedDatePicker<LocalDate> lastRequestDateTo;
    @ViewComponent
    protected TypedTimePicker<LocalTime> lastRequestTimeTo;
    @ViewComponent
    protected CollectionLoader<UserSession> userSessionsDl;

    @Autowired
    protected UserSessions userSessions;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;

    @Subscribe
    protected void onInit(InitEvent event) {
        userSessionsDl.setLoadDelegate(loadContext -> {
            Stream<UserSession> sessions = userSessions.sessions();
            if (userName.getValue() != null) {
                sessions = sessions.filter(o -> o.getPrincipalName().toLowerCase().contains(userName.getValue()));
            }
            if (lastRequestDateFrom.getTypedValue() != null || lastRequestTimeFrom.getTypedValue()!=null) {

                LocalDate afterDate = lastRequestDateFrom.getTypedValue()!=null ? lastRequestDateFrom.getTypedValue() :
                        LocalDate.now();
                LocalTime afterTime = lastRequestTimeFrom.getTypedValue()!=null ? lastRequestTimeFrom.getTypedValue() :
                    LocalTime.MIN;
                LocalDateTime afterDateTime = LocalDateTime.of(afterDate, afterTime);

                sessions = sessions.filter(o -> o.getLastRequest().after(Date
                        .from(afterDateTime.atZone(ZoneId.systemDefault()).toInstant())));
            }
            if (lastRequestDateTo.getValue() != null || lastRequestTimeTo.getTypedValue()!=null) {
                LocalDate beforeDate = lastRequestDateTo.getTypedValue()!=null ? lastRequestDateTo.getTypedValue() :
                        LocalDate.now();
                LocalTime beforeTime = lastRequestTimeTo.getTypedValue()!=null ? lastRequestTimeTo.getTypedValue() :
                        LocalTime.MAX;
                LocalDateTime beforeDateTime = LocalDateTime.of(beforeDate, beforeTime);
                sessions = sessions.filter(o -> o.getLastRequest().before(Date
                        .from(beforeDateTime.atZone(ZoneId.systemDefault()).toInstant())));
            }
            return sessions.collect(Collectors.toList());
        });
    }

    @Subscribe("sessionsTable.expire")
    protected void onSessionsTableExpire(ActionPerformedEvent event) {
        if (sessionsTable.getSelectedItems().size()==0){
            notifications.create(messages.getMessage(UserSessionsView.class, "needSelectSession"))
                    .withType(Notifications.Type.WARNING)
                    .show();
        } else {
            for (UserSession session : sessionsTable.getSelectedItems()) {
                userSessions.invalidate(session);
                notifications.create(messages.formatMessage(UserSessionsView.class, "sessionInvalidated", session.getSessionId()))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
                userSessionsDl.load();
            }
        }
    }

    @Subscribe("clearButton")
    protected void onClearButtonClick(ClickEvent<Button> event) {
        userName.clear();
        lastRequestDateFrom.clear();
        lastRequestTimeFrom.clear();
        lastRequestDateTo.clear();
        lastRequestTimeTo.clear();
        refreshDlItems();
    }

    @Subscribe("sessionsTable.refresh")
    protected void onSessionsTableRefresh(ActionPerformedEvent event) {
        refreshDlItems();
    }

    private void refreshDlItems() {

        userSessionsDl.load();
    }

}
