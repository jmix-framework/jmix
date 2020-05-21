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

package io.jmix.ui.widget.client.image;

import io.jmix.ui.widget.JmixImage;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.image.ImageConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixImage.class)
public class JmixImageConnector extends ImageConnector {

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("scaleMode")) {
            getWidget().applyScaling(getState().scaleMode);
        }
    }

    @Override
    public JmixImageState getState() {
        return (JmixImageState) super.getState();
    }

    @Override
    public JmixImageWidget getWidget() {
        return (JmixImageWidget) super.getWidget();
    }
}
