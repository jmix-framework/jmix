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

import io.jmix.ui.widget.addon.popupbutton.PopupButton;
import io.jmix.ui.widget.client.popupbutton.JmixPopupButtonState;

public class JmixPopupButton extends PopupButton {

    @Override
    public JmixPopupButtonState getState() {
        return (JmixPopupButtonState) super.getState();
    }

    @Override
    protected JmixPopupButtonState getState(boolean markAsDirty) {
        return (JmixPopupButtonState) super.getState(markAsDirty);
    }

    public boolean isAutoClose() {
        return getState(false).autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        if (getState(false).autoClose != autoClose) {
            getState().autoClose = autoClose;
        }
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (!(getContent() instanceof JmixPopupButtonLayout) && !getState(false).customLayout) {
            getState().customLayout = true;
        }
    }
}
