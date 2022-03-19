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

package com.haulmont.cuba.gui.components.presentation;

import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.presentation.action.EditPresentationAction;
import io.jmix.ui.component.presentation.action.PresentationActionsBuilder;
import io.jmix.ui.component.presentation.action.SaveAsPresentationAction;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;

public class CubaPresentationActionsBuilder extends PresentationActionsBuilder {

    public CubaPresentationActionsBuilder(Table component, ComponentSettingsBinder settingsBinder) {
        super(component, settingsBinder);
    }

    @Override
    protected AbstractAction buildSaveAction() {
        if (isGlobalPresentation())
            return new CubaSavePresentationAction(table, settingsBinder);
        return null;
    }

    @Override
    protected AbstractAction buildSaveAsAction() {
        SaveAsPresentationAction action = (SaveAsPresentationAction) super.buildSaveAsAction();
        action.setEditorClass(CubaPresentationEditor.class);
        return action;
    }

    @Override
    protected AbstractAction buildEditAction() {
        EditPresentationAction action = (EditPresentationAction) super.buildEditAction();
        if (action != null) {
            action.setEditorClass(CubaPresentationEditor.class);
        }
        return action;
    }
}
