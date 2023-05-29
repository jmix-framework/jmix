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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasOrientation;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> value type: single type or {@code Collection<I>}
 * @param <I> item type
 * @deprecated Use either {@link io.jmix.ui.component.CheckBoxGroup}
 * or {@link io.jmix.ui.component.RadioButtonGroup}
 */
@Deprecated
public interface OptionsGroup<V, I> extends OptionsField<V, I>, LookupComponent, Component.Focusable, HasOrientation {
    String NAME = "optionsGroup";

    boolean isMultiSelect();
    void setMultiSelect(boolean multiselect);
}
