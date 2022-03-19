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

import io.jmix.ui.widget.client.textarea.JmixTextAreaState;
import com.vaadin.ui.TextArea;

import java.util.Objects;

public class JmixTextArea extends TextArea {
    public JmixTextArea() {
    }

    @Override
    protected JmixTextAreaState getState() {
        return (JmixTextAreaState) super.getState();
    }

    @Override
    protected JmixTextAreaState getState(boolean markAsDirty) {
        return (JmixTextAreaState) super.getState(markAsDirty);
    }

    public CaseConversion getCaseConversion() {
        return CaseConversion.valueOf(getState(false).caseConversion);
    }

    public void setCaseConversion(CaseConversion caseConversion) {
        CaseConversion widgetCaseConversion = CaseConversion.valueOf(getState(false).caseConversion);
        if (!Objects.equals(caseConversion, widgetCaseConversion)) {
            getState(true).caseConversion = caseConversion.name();
        }
    }
}