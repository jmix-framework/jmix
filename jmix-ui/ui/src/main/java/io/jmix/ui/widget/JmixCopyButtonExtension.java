/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import com.vaadin.event.SerializableEventListener;
import io.jmix.ui.widget.client.button.JmixCopyButtonExtensionServerRpc;
import io.jmix.ui.widget.client.button.JmixCopyButtonExtensionState;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Button;
import com.vaadin.util.ReflectTools;

import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.Objects;

public class JmixCopyButtonExtension extends AbstractExtension {

    protected Button component;

    protected JmixCopyButtonExtension(Button button) {
        component = button;
        extend(component);

        //noinspection Convert2Lambda
        registerRpc(new JmixCopyButtonExtensionServerRpc() {
            @Override
            public void copied(boolean success) {
                fireEvent(new CopyEvent(JmixCopyButtonExtension.this, success));
            }
        });
    }

    public static JmixCopyButtonExtension copyWith(Button button) {
        return new JmixCopyButtonExtension(button);
    }

    public static JmixCopyButtonExtension copyWith(Button button, String selector) {
        JmixCopyButtonExtension extension = new JmixCopyButtonExtension(button);
        extension.setCopyTargetSelector(selector);
        return extension;
    }

    @Override
    protected JmixCopyButtonExtensionState getState() {
        return (JmixCopyButtonExtensionState) super.getState();
    }

    @Override
    protected JmixCopyButtonExtensionState getState(boolean markAsDirty) {
        return (JmixCopyButtonExtensionState) super.getState(markAsDirty);
    }

    public String getCopyTargetSelector() {
        return getState(false).copyTargetSelector;
    }

    public void setCopyTargetSelector(String targetElementClass) {
        if (!Objects.equals(getState(false).copyTargetSelector, targetElementClass)) {
            getState().copyTargetSelector = targetElementClass;
        }
    }

    public static class CopyEvent extends EventObject {
        private final boolean success;

        public CopyEvent(JmixCopyButtonExtension source, boolean success) {
            super(source);
            this.success = success;
        }

        @Override
        public JmixCopyButtonExtension getSource() {
            return (JmixCopyButtonExtension) super.getSource();
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public interface CopyListener extends SerializableEventListener {
        void copied(CopyEvent event);
    }

    private static Method COPY_METHOD = ReflectTools.findMethod(CopyListener.class, "copied", CopyEvent.class);

    public Registration addCopyListener(CopyListener listener) {
        return addListener(CopyEvent.class, listener, COPY_METHOD);
    }

    public static boolean browserSupportsCopy() {
        WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
        return !webBrowser.isSafari() && !webBrowser.isIOS() && !webBrowser.isWindowsPhone();
    }
}
