/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.gui.actions.picker;

import com.haulmont.cuba.gui.components.PickerField;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.meta.StudioAction;

import javax.annotation.Nullable;

/**
 * Standard picker field action for clearing the field value.
 * <p>
 * Should be defined for {@code PickerField} or its subclass in a screen XML descriptor.
 */
@StudioAction(target = "com.haulmont.cuba.gui.components.PickerField", description = "Clears the picker field value")
@ActionType(ClearAction.ID)
public class ClearAction extends EntityClearAction implements PickerField.PickerFieldAction {

    public static final String ID = "picker_clear";

    public ClearAction() {
        super(ID);
    }

    public ClearAction(String id) {
        super(id);
    }

    @Override
    public void setPickerField(@Nullable PickerField pickerField) {
        super.setEntityPicker(pickerField);
    }

    @Override
    public void editableChanged(PickerField pickerField, boolean editable) {
        super.editableChanged(editable);
    }
}