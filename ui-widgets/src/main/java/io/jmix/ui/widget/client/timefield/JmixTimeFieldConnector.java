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

package io.jmix.ui.widget.client.timefield;

import io.jmix.ui.widget.JmixTimeField;
import io.jmix.ui.widget.client.textfield.JmixMaskedFieldConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

@Connect(JmixTimeField.class)
public class JmixTimeFieldConnector extends JmixMaskedFieldConnector {

    @Override
    public JmixTimeFieldState getState() {
        return (JmixTimeFieldState) super.getState();
    }

    @Override
    public JmixTimeFieldWidget getWidget() {
        return (JmixTimeFieldWidget) super.getWidget();
    }

    @Override
    public boolean delegateCaptionHandling() {
        return getState().captionManagedByLayout;
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        JmixTimeFieldWidget widget = getWidget();

        if (stateChangeEvent.hasPropertyChanged("resolution")) {
            // Remove old stylename that indicates current resolution
            setWidgetStyleName(widget.getStylePrimaryName() + "-"
                    + widget.resolutionAsString(), false);

            widget.setResolution(getState().resolution);

            // Add stylename that indicates current resolution
            setWidgetStyleName(widget.getStylePrimaryName() + "-"
                    + widget.resolutionAsString(), true);
        }

        if (stateChangeEvent.hasPropertyChanged("timeFormat")) {
            widget.setTimeFormat(getState().timeFormat);
        }
    }
}
