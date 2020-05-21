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

package io.jmix.ui.widget.client.browserframe;

import io.jmix.ui.widget.JmixBrowserFrame;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.browserframe.BrowserFrameConnector;
import com.vaadin.shared.ui.Connect;

import static io.jmix.ui.widget.client.browserframe.JmixBrowserFrameState.*;

@Connect(JmixBrowserFrame.class)
public class JmixBrowserFrameConnector extends BrowserFrameConnector {

    @Override
    public JmixBrowserFrameWidget getWidget() {
        return (JmixBrowserFrameWidget) super.getWidget();
    }

    @Override
    public JmixBrowserFrameState getState() {
        return (JmixBrowserFrameState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        JmixBrowserFrameState state = getState();

        if (stateChangeEvent.hasPropertyChanged(SRCDOC)) {
            getWidget().setSrcdoc(state.srcdoc, getConnectorId());
        }

        if (stateChangeEvent.hasPropertyChanged("resources")
                || stateChangeEvent.hasPropertyChanged(SRCDOC)) {
            getWidget().setAttribute(ALLOW, state.allow);
            getWidget().setAttribute(REFERRERPOLICY, state.referrerpolicy);
            getWidget().setAttribute(SANDBOX, state.sandbox);
        }

        if (stateChangeEvent.hasPropertyChanged(ALLOW)) {
            getWidget().setAttribute(ALLOW, state.allow);
        }

        if (stateChangeEvent.hasPropertyChanged(REFERRERPOLICY)) {
            getWidget().setAttribute(REFERRERPOLICY, state.referrerpolicy);
        }

        if (stateChangeEvent.hasPropertyChanged(SANDBOX)) {
            getWidget().setAttribute(SANDBOX, state.sandbox);
        }
    }
}
