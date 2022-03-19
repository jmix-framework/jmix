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

package io.jmix.ui.widget.client.radiobuttongroup;

import io.jmix.ui.widget.JmixRadioButtonGroup;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.optiongroup.RadioButtonGroupConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Orientation;

@Connect(JmixRadioButtonGroup.class)
public class JmixRadioButtonGroupConnector extends RadioButtonGroupConnector {

    public static final String HORIZONTAL_ORIENTATION_STYLE = "horizontal";

    @Override
    public JmixRadioButtonGroupWidget getWidget() {
        return (JmixRadioButtonGroupWidget) super.getWidget();
    }

    @Override
    public JmixRadioButtonGroupState getState() {
        return (JmixRadioButtonGroupState) super.getState();
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

        if (stateChangeEvent.hasPropertyChanged("readOnly")) {
            getWidget().setReadonly(getState().readOnly);
        }
    }
}
