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

package io.jmix.ui.widget.client.twincolselect;

import io.jmix.ui.widget.JmixTwinColSelect;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.twincolselect.TwinColSelectConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixTwinColSelect.class)
public class JmixTwinColSelectConnector extends TwinColSelectConnector {

    @Override
    public JmixTwinColSelectWidget getWidget() {
        return (JmixTwinColSelectWidget) super.getWidget();
    }

    @Override
    public JmixTwinColSelectState getState() {
        return (JmixTwinColSelectState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("addAllBtnEnabled")) {
            getWidget().setAddAllBtnEnabled(getState().addAllBtnEnabled);
        }
        if (stateChangeEvent.hasPropertyChanged("reorderable")) {
            getWidget().setReorderable(getState().reorderable);
        }
    }
}
