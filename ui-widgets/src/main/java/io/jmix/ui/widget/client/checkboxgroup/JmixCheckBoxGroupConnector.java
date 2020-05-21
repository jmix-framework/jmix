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

package io.jmix.ui.widget.client.checkboxgroup;

import io.jmix.ui.widget.JmixCheckBoxGroup;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.optiongroup.CheckBoxGroupConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Orientation;

@Connect(JmixCheckBoxGroup.class)
public class JmixCheckBoxGroupConnector extends CheckBoxGroupConnector {

    public static final String HORIZONTAL_ORIENTATION_STYLE = "horizontal";

    @Override
    public JmixCheckBoxGroupWidget getWidget() {
        return (JmixCheckBoxGroupWidget) super.getWidget();
    }

    @Override
    public JmixCheckBoxGroupState getState() {
        return (JmixCheckBoxGroupState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("orientation")) {
            if (getState().orientation == Orientation.VERTICAL) {
                getWidget().removeStyleDependentName(HORIZONTAL_ORIENTATION_STYLE);
            } else {
                getWidget().addStyleDependentName(HORIZONTAL_ORIENTATION_STYLE);
            }
        }
    }
}
