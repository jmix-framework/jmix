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

import com.vaadin.ui.Grid;
import io.jmix.ui.widget.grid.JmixEditorField;

import javax.annotation.Nullable;

/**
 * Factory that generates components for {@link JmixGrid} editor.
 */
public interface JmixGridEditorFieldFactory<T> {

    /**
     * Generates component for {@link JmixGrid} editor.
     *
     * @param bean   the editing item
     * @param column the column for which the field is being created
     * @return generated component
     */
    @Nullable
    JmixEditorField<?> createField(T bean, Grid.Column<T, ?> column);
}
