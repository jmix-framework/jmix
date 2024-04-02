/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.component.multiselectcomboboxpicker;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.SupportsUserAction;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

@Tag("jmix-multi-select-combo-box-picker")
@JsModule("./src/multi-select-combo-box-picker/jmix-multi-select-combo-box-picker.js")
public class MultiSelectComboBoxPicker<V> extends MultiSelectComboBox<V>
        implements SupportsUserAction<Collection<V>>, HasActions, HasTitle {

    protected ValuePickerActionSupport actionsSupport;

    @Override
    public void setValueFromClient(@Nullable Collection<V> value) {
        setModelValue(value == null ? null : Set.copyOf(value), true);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id)
                .orElse(null);
    }

    protected ValuePickerActionSupport getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected ValuePickerActionSupport createActionsSupport() {
        return new ValuePickerActionSupport(this);
    }
}
