/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.grid.editor;

import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;
import io.jmix.flowui.data.value.BufferedContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;

public class DataGridEditorValueSourceProvider<T> implements ValueSourceProvider {

    protected final DataGridEditor<T> editor;
    protected final InstanceContainer<T> container;

    public DataGridEditorValueSourceProvider(DataGridEditor<T> editor, InstanceContainer<T> container) {
        this.editor = editor;
        this.container = container;
    }

    public InstanceContainer<T> getContainer() {
        return container;
    }

    @Override
    public <V> ValueSource<V> getValueSource(String property) {
        return new BufferedContainerValueSource<>(container, property, editor.isBuffered());
    }
}
