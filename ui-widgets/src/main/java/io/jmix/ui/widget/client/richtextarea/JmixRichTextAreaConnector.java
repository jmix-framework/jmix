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

package io.jmix.ui.widget.client.richtextarea;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.richtextarea.RichTextAreaConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.JmixRichTextArea;

@Connect(value = JmixRichTextArea.class, loadStyle = Connect.LoadStyle.LAZY)
public class JmixRichTextAreaConnector extends RichTextAreaConnector {

    @Override
    protected void init() {
        super.init();

        getWidget().setValueSupplier(() -> getState().value);
    }

    @Override
    public JmixRichTextAreaWidget getWidget() {
        return (JmixRichTextAreaWidget) super.getWidget();
    }

    @Override
    public JmixRichTextAreaState getState() {
        return (JmixRichTextAreaState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("localeMap")) {
            getWidget().setLocaleMap(getState().localeMap);
        }

        if (stateChangeEvent.hasPropertyChanged("tabIndex")) {
            getWidget().setTabIndex(getState().tabIndex);
        }
    }

    @Override
    public void sendValueChange() {
        String widgetValue = getWidget().getSanitizedValue();
        if (!hasStateChanged(widgetValue)) {
            return;
        }

        getRpcProxy(JmixRichTextAreaServerRpc.class)
                .setText(widgetValue, ((JmixRichTextToolbarWidget) getWidget().formatter).isLastUserActionSanitized());
        getState().value = widgetValue;
    }
}
