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

package io.jmix.ui.component.presentation.action;

import io.jmix.core.AppBeans;
import io.jmix.core.security.Security;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Table;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PresentationActionsBuilder {

    public enum Type {
        SAVE,
        SAVE_AS,
        EDIT,
        DELETE,
        RESET
    }

    protected Security security;

    protected Table table;

    protected Collection actionTypes;

    protected ComponentSettingsBinder settingsBinder;

    public PresentationActionsBuilder(Table component, ComponentSettingsBinder settingsBinder) {
        table = component;
        security = AppBeans.get(Security.NAME);
        this.settingsBinder = settingsBinder;
    }

    public Collection<AbstractAction> build() {
        Collection<AbstractAction> actions = new ArrayList<>();
        for (Object type : getActionTypes()) {
            AbstractAction action = buildAction(type);
            if (action != null)
                actions.add(action);
        }
        return actions;
    }

    @Nullable
    public AbstractAction buildAction(@Nullable Object type) {
        if (type == null)
            return null;
        return buildActionByType(type);
    }

    public Collection getActionTypes() {
        if (actionTypes == null)
            actionTypes = Arrays.asList(Type.values());
        return actionTypes;
    }

    @SuppressWarnings("unused")
    public void setActionTypes(Collection actionTypes) {
        this.actionTypes = actionTypes;
    }

    @Nullable
    protected AbstractAction buildActionByType(@Nonnull Object type) {
        if (type instanceof Type) {
            switch ((Type) type) {
                case SAVE: return buildSaveAction();
                case SAVE_AS: return buildSaveAsAction();
                case EDIT: return buildEditAction();
                case DELETE: return buildDeleteAction();
                case RESET: return buildResetAction();
            }
        }
        return buildCustomAction(type);
    }

    @SuppressWarnings("unused")
    protected AbstractAction buildCustomAction(Object type) {
        return null;
    }

    protected AbstractAction buildSaveAction() {
        if (isGlobalPresentation())
            return new SavePresentationAction(table, settingsBinder);
        return null;
    }

    protected AbstractAction buildSaveAsAction() {
        return new SaveAsPresentationAction(table, settingsBinder);
    }

    protected AbstractAction buildEditAction() {
        if (isGlobalPresentation())
            return new EditPresentationAction(table, settingsBinder);
        return null;
    }

    protected AbstractAction buildDeleteAction() {
        if (isGlobalPresentation())
            return new DeletePresentationAction(table);
        return null;
    }

    protected AbstractAction buildResetAction() {
        return new ResetPresentationAction(table);
    }

    protected boolean isGlobalPresentation() {
        TablePresentations presentations = table.getPresentations();
        TablePresentation presentation = presentations.getCurrent();
        return presentation != null && (!presentations.isGlobal(presentation) ||
                security.isSpecificPermitted("cuba.gui.presentations.global"));
    }
}
