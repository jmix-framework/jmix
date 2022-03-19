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

package io.jmix.ui.widget.client.optiongroup;

import io.jmix.ui.widget.JmixOptionGroup;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;
import com.vaadin.v7.client.ui.optiongroup.OptionGroupConnector;

@Connect(JmixOptionGroup.class)
public class JmixOptionGroupConnector extends OptionGroupConnector {

    public static final String HORIZONTAL_ORIENTATION_STYLE = "horizontal";

    @Override
    public JmixOptionGroupWidget getWidget() {
        return (JmixOptionGroupWidget) super.getWidget();
    }

    @Override
    public JmixOptionGroupState getState() {
        return (JmixOptionGroupState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("orientation")) {
            if (getState().orientation == OptionGroupOrientation.VERTICAL)
                getWidget().removeStyleDependentName("horizontal");
            else
                getWidget().addStyleDependentName(HORIZONTAL_ORIENTATION_STYLE);
        }
    }
}
