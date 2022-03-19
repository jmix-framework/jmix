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

package io.jmix.ui.widget.client.calendar;

import io.jmix.ui.widget.JmixCalendar;
import com.vaadin.shared.ui.Connect;
import com.vaadin.v7.client.ui.calendar.CalendarConnector;

@Connect(value = JmixCalendar.class, loadStyle = Connect.LoadStyle.LAZY)
public class JmixCalendarConnector extends CalendarConnector {

    @Override
    public JmixCalendarWidget getWidget() {
        return (JmixCalendarWidget) super.getWidget();
    }

    @Override
    protected void registerListeners() {
        super.registerListeners();

        getWidget().setDayClickListener(date -> {
            if (!getWidget().isDisabled() && hasEventListener(JmixCalendarEventId.DAYCLICK)) {
                getRpcProxy(JmixCalendarServerRpc.class).dayClick(date);
            }
        });
    }
}
