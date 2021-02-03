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
import io.jmix.core.Entity;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.meta.StudioAction;

/**
 * Standard action for setting an entity to the picker field using the entity lookup screen.
 * <p>
 * Should be defined for {@code PickerField} or its subclass in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> type of entity
 */
@StudioAction(
        target = "com.haulmont.cuba.gui.components.PickerField",
        description = "Sets an entity to the picker field using the entity lookup screen")
@ActionType(LookupAction.ID)
public class LookupAction<E extends Entity> extends EntityLookupAction<E>
        implements PickerField.PickerFieldAction {

    public static final String ID = "picker_lookup";

    public LookupAction() {
        super(LookupAction.ID);
    }

    public LookupAction(String id) {
        super(id);
    }

    @Override
    public void setPickerField(PickerField pickerField) {
        super.setEntityPicker(pickerField);
    }

    @Override
    public void editableChanged(PickerField pickerField, boolean editable) {
        super.editableChanged(editable);
    }
}