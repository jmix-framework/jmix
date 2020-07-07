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

import io.jmix.ui.widget.client.passwordfield.JmixPasswordFieldState;
import com.vaadin.shared.Connector;
import com.vaadin.ui.PasswordField;

import javax.annotation.Nullable;
import java.util.Objects;

public class JmixPasswordField extends PasswordField {

    public JmixPasswordField() {
        setAutocomplete(false);
    }

    @Override
    protected JmixPasswordFieldState getState() {
        return (JmixPasswordFieldState) super.getState();
    }

    @Override
    protected JmixPasswordFieldState getState(boolean markAsDirty) {
        return (JmixPasswordFieldState) super.getState(markAsDirty);
    }

    public boolean isAutocomplete() {
        return getState(false).autocomplete;
    }

    public void setAutocomplete(boolean autocomplete) {
        if (isAutocomplete() != autocomplete) {
            getState().autocomplete = autocomplete;
        }
    }

    public void setCapsLockIndicator(@Nullable Connector capsLockIndicator) {
        getState().capsLockIndicator = capsLockIndicator;
    }

    public void setHtmlName(@Nullable String htmlName) {
        String oldHtmlName = getState(false).htmlName;
        if (!Objects.equals(htmlName, oldHtmlName)) {
            getState().htmlName = htmlName;
        }
    }

    @Nullable
    public String getHtmlName() {
        return getState(false).htmlName;
    }
}