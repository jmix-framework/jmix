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

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import io.jmix.ui.widget.client.capslockindicator.JmixCapsLockIndicatorState;

import javax.annotation.Nullable;

public class JmixCapsLockIndicator extends Label {

    public JmixCapsLockIndicator() {
        initCapsLockIndicatorContent();
    }

    @Override
    protected JmixCapsLockIndicatorState getState() {
        return (JmixCapsLockIndicatorState) super.getState();
    }

    @Override
    protected JmixCapsLockIndicatorState getState(boolean markAsDirty) {
        return (JmixCapsLockIndicatorState) super.getState(markAsDirty);
    }

    protected void initCapsLockIndicatorContent() {
        getState().contentMode = ContentMode.HTML;
        getState().text = "<span></span>";
    }

    public void setCapsLockOnMessage(@Nullable String capsLockOnMessage) {
        getState().capsLockOnMessage = capsLockOnMessage;
    }

    @Nullable
    public String getCapsLockOnMessage() {
        return getState(false).capsLockOnMessage;
    }

    public void setCapsLockOffMessage(@Nullable String capsLockOffMessage) {
        getState().capsLockOffMessage = capsLockOffMessage;
    }

    @Nullable
    public String getCapsLockOffMessage() {
        return getState(false).capsLockOffMessage;
    }
}