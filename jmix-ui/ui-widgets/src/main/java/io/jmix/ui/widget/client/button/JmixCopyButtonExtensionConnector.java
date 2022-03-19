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

package io.jmix.ui.widget.client.button;

import io.jmix.ui.widget.JmixCopyButtonExtension;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VButton;
import com.vaadin.shared.ui.Connect;

@Connect(value = JmixCopyButtonExtension.class, loadStyle = Connect.LoadStyle.LAZY)
public class JmixCopyButtonExtensionConnector extends AbstractExtensionConnector {

    @Override
    public JmixCopyButtonExtensionState getState() {
        return (JmixCopyButtonExtensionState) super.getState();
    }

    @Override
    protected void extend(ServerConnector target) {
        VButton button = (VButton) ((ComponentConnector) target).getWidget();

        button.addClickHandler(event -> {
            if (getState().copyTargetSelector != null) {
                boolean success = copyToClipboard(getState().copyTargetSelector.startsWith(".")
                        ? getState().copyTargetSelector
                        : "." + getState().copyTargetSelector);
                getRpcProxy(JmixCopyButtonExtensionServerRpc.class).copied(success);
            }
        });
    }

    protected native boolean copyToClipboard(String selector) /*-{
        var copyTextArea = $doc.querySelector(selector);
        copyTextArea.select();
        try {
            return $doc.execCommand('copy');
        } catch (e) {
            console.log(e.message);
            return false;
        }
    }-*/;
}
