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

package io.jmix.ui.widget.client.datefield;

import io.jmix.ui.widget.JmixDateField;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.datefield.PopupDateFieldConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixDateField.class)
public class JmixDateFieldConnector extends PopupDateFieldConnector {

    @Override
    public JmixDateFieldWidget getWidget() {
        return (JmixDateFieldWidget) super.getWidget();
    }

    @Override
    public JmixDateFieldState getState() {
        return (JmixDateFieldState) super.getState();
    }

    @Override
    public boolean delegateCaptionHandling() {
        return getState().captionManagedByLayout;
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        getWidget().getImpl().setMask(getState().dateMask);
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("tabIndex")) {
            getWidget().updateTabIndex(getState().tabIndex);
        }

        if (stateChangeEvent.hasPropertyChanged("autofill")) {
            getWidget().setAutofill(getState().autofill);
        }

        if (stateChangeEvent.hasPropertyChanged("rangeStart")) {
            getWidget().setDateRangeStart(getState().rangeStart);
        }

        if (stateChangeEvent.hasPropertyChanged("rangeEnd")) {
            getWidget().setDateRangeEnd(getState().rangeEnd);
        }
    }
}
