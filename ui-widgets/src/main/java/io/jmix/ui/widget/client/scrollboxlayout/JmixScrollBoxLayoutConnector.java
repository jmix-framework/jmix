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

package io.jmix.ui.widget.client.scrollboxlayout;

import io.jmix.ui.widget.JmixScrollBoxLayout;
import io.jmix.ui.widget.client.cssactionslayout.JmixCssActionsLayoutConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.shared.ui.Connect;

@Connect(JmixScrollBoxLayout.class)
public class JmixScrollBoxLayoutConnector extends JmixCssActionsLayoutConnector implements SimpleManagedLayout {

    @Override
    public JmixScrollBoxLayoutState getState() {
        return (JmixScrollBoxLayoutState) super.getState();
    }

    @Override
    public JmixScrollBoxLayoutWidget getWidget() {
        return (JmixScrollBoxLayoutWidget) super.getWidget();
    }

    @Override
    public void layout() {
        JmixScrollBoxLayoutWidget widget = getWidget();

        widget.setScrollTop(getState().scrollTop);
        widget.setScrollLeft(getState().scrollLeft);

        widget.onScrollHandler = (scrollTop, scrollLeft) -> {
            if (getState().scrollChangeMode.equals(JmixScrollBoxLayoutState.DEFERRED_MODE)) {
                getRpcProxy(JmixScrollBoxLayoutServerRpc.class).setDeferredScroll(scrollTop, scrollLeft);
            } else {
                getRpcProxy(JmixScrollBoxLayoutServerRpc.class).setDelayedScroll(scrollTop, scrollLeft);
            }
        };
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("scrollTop")) {
            getWidget().setScrollTop(getState().scrollTop);
        }

        if (stateChangeEvent.hasPropertyChanged("scrollLeft")) {
            getWidget().setScrollLeft(getState().scrollLeft);
        }

        if (stateChangeEvent.hasPropertyChanged("scrollChangeMode")) {
            getWidget().setScrollChangeMode(getState().scrollChangeMode);
        }
    }
}
