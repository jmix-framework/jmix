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

import io.jmix.ui.widget.client.textfield.JmixTextFieldState;
import com.vaadin.event.Action;
import com.vaadin.event.ActionManager;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.shared.Registration;
import com.vaadin.ui.LegacyComponent;
import com.vaadin.ui.TextField;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class JmixTextField extends TextField implements Action.Container, LegacyComponent {

    /**
     * Keeps track of the Actions added to this component, and manages the
     * painting and handling as well.
     */
    protected ActionManager shortcutsManager;

    public JmixTextField() {
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (shortcutsManager != null) {
            shortcutsManager.paintActions(null, target);
        }
    }

    @Override
    protected ActionManager getActionManager() {
        if (shortcutsManager == null) {
            shortcutsManager = new ActionManager(this);
        }
        return shortcutsManager;
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        // Actions
        if (shortcutsManager != null) {
            shortcutsManager.handleActions(variables, this);
        }
    }

    @Override
    public Registration addShortcutListener(ShortcutListener listener) {
        getActionManager().addAction(listener);
        return () -> getActionManager().removeAction(listener);
    }

    @Override
    public void removeShortcutListener(ShortcutListener listener) {
        getActionManager().removeAction(listener);
    }

    @Override
    public void addActionHandler(Action.Handler actionHandler) {
        getActionManager().addActionHandler(actionHandler);
    }

    @Override
    public void removeActionHandler(Action.Handler actionHandler) {
        getActionManager().removeActionHandler(actionHandler);
    }

    @Override
    protected JmixTextFieldState getState() {
        return (JmixTextFieldState) super.getState();
    }

    @Override
    protected JmixTextFieldState getState(boolean markAsDirty) {
        return (JmixTextFieldState) super.getState(markAsDirty);
    }

    /**
     * Sets whether a text field will be focusable in readOnly mode
     */
    public void setReadOnlyFocusable(boolean readOnlyFocusable) {
        getState(true).readOnlyFocusable = readOnlyFocusable;
    }

    public boolean isReadOnlyFocusable() {
        return getState(false).readOnlyFocusable;
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