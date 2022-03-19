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

package io.jmix.ui.widget.client.popupview;

import io.jmix.ui.widget.JmixPopupView;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.popupview.PopupViewConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixPopupView.class)
public class JmixPopupViewConnector extends PopupViewConnector {

    @Override
    public JmixPopupViewWidget getWidget() {
        return (JmixPopupViewWidget) super.getWidget();
    }

    @Override
    public JmixPopupViewState getState() {
        return (JmixPopupViewState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        JmixPopupViewState state = getState();

        if (stateChangeEvent.hasPropertyChanged("popupPosition")) {
            getWidget().setupPopupPosition(state.popupPosition);
        }

        if (stateChangeEvent.hasPropertyChanged("popupPositionTop")) {
            getWidget().setupPopupPositionTop(state.popupPositionTop);
        }

        if (stateChangeEvent.hasPropertyChanged("popupPositionLeft")) {
            getWidget().setupPopupPositionLeft(state.popupPositionLeft);
        }
    }
}
